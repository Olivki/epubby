/*
 * Copyright 2019 Oliver Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("BookFactory")

package moe.kanon.epubby

import moe.kanon.epubby.metainf.MetaInf
import moe.kanon.epubby.structs.Version
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.malformed
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.deleteIfExists
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.isDirectory
import moe.kanon.kommons.io.paths.isRegularFile
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.notExists
import moe.kanon.kommons.io.paths.readString
import moe.kanon.kommons.io.paths.simpleName
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Path

// TODO: See if I can make it so that the Book instance/some BookWriter thing can handle the actual writing of the book
//       because the way it's done right now is quite ugly and not the best.
/**
 * Returns a new [Book] instance based on the given [origin].
 *
 * This function attempts to parse the `origin` file, and *will* fail if the given file is *not* a valid EPUB.
 *
 * Note that the `Book` instance will *not* be working on the given `origin` file, but rather on a file created using
 * the given [outputName] located in the given [outputDirectory]. To access the `origin` file from the book instance,
 * use the [originFile][Book.originFile] property.
 *
 * @param [origin] TODO
 * @param [outputDirectory] the directory where the parsed book should be saved when [Book.save] is invoked
 * @param [outputName] the [simple name][Path.simpleName] of the resulting book.
 * @param [settings] TODO
 *
 * @throws [EpubbyException] if something went wrong when parsing the [origin] file
 * @throws [IOException] if an i/o error occurred
 */
@JvmOverloads
@JvmName("fromFile")
@Throws(EpubbyException::class, IOException::class)
fun readBook(origin: Path, outputDirectory: Path, outputName: String = origin.simpleName): Book {
    logger.info { "Reading file '${origin.name}' as an EPUB container" }

    val copy = origin.copyTo(outputDirectory.resolve("$outputName.epub"))
    val fileSystem = try {
        FileSystems.newFileSystem(copy, null).also { validateContainer(origin, it.getPath("/")) }
    } catch (e: IOException) {
        // something went wrong when trying to create the new file-system, so we want to delete the copy we made
        // and inform about it
        copy.deleteIfExists()
        malformed(origin, "Could not create a file-system for '${origin.name}'", e)
    } catch (e: EpubbyException) {
        // the validation failed, so we want to just delete the copy we made and re-throw the exception
        copy.deleteIfExists()
        throw e
    }
    val root = fileSystem.getPath("/")
    val metaInf = MetaInf.fromDirectory(copy, root.resolve("META-INF"), root)
    val opfFile = metaInf.container.packageDocument.path
    val version = parseXmlFile(opfFile) { _, it -> it.attr("version", copy, opfFile).let { Version.fromString(it) } }

    return Book(metaInf, copy, fileSystem, origin, root, version)
}

private fun validateContainer(epub: Path, root: Path) {
    val metaInf = root.resolve("META-INF")
    val mimeType = root.resolve("mimetype")
    when {
        // none of the epub formats have changed the behaviour of requiring the '!META-INF' directory, so it's
        // safe *(for now)* to assume that to be a valid epub file, no matter the format version, a '!META-INF'
        // directory containing a 'container.xml' is REQUIRED
        metaInf.notExists -> {
            root.fileSystem.close()
            malformed(epub, root, "'META-INF' directory is missing from root of epub")
        }
        metaInf.isRegularFile -> {
            root.fileSystem.close()
            malformed(epub, metaInf, "'META-INF' should be a directory, was a file")
            // 'mimetype' also needs to exist in the root of the epub file, and it NEEDS to contain the ASCII
        }
        // string "application/epub+zip"
        mimeType.notExists -> {
            root.fileSystem.close()
            malformed(epub, root, "'mimetype' file is missing from root of epub")
        }
        mimeType.isDirectory -> {
            root.fileSystem.close()
            malformed(epub, mimeType, "'mimetype' should be a file, was a directory")
        }
        mimeType.exists -> {
            // the mimeType is supposed to be encoded in ASCII format, so we make sure to read it that way
            val contents = mimeType.readString(StandardCharsets.US_ASCII)
            if (contents != "application/epub+zip") {
                root.fileSystem.close()
                malformed(epub, mimeType, "contents of 'mimetype' should be 'application/epub+zip', was '$contents'")
            }
        }
    }
}