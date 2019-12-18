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

package moe.kanon.epubby

import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.metainf.MetaInf
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.createTmpDirectory
import moe.kanon.kommons.io.paths.deleteIfExists
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.isDirectory
import moe.kanon.kommons.io.paths.isRegularFile
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.notExists
import moe.kanon.kommons.io.paths.readString
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * TODO
 *
 * @param [epubFile] the file to read and parse into a [Book] instance, this needs to be a EPUB file or this function
 * will throw a [MalformedBookException]
 *
 * @throws [IOException] if an i/o error occurred
 * @throws [EpubbyException] if something went wrong when parsing the [epubFile] file
 */
@JvmName("read")
@Throws(IOException::class, EpubbyException::class)
fun readBook(epubFile: Path): Book {
    logger.info { "Reading file '$epubFile' as an EPUB container.." }

    val fileSystem = try {
        FileSystems.newFileSystem(epubFile, null).also { validateContainer(epubFile, it.getPath("/")) }
    } catch (e: IOException) {
        // something went wrong when trying to create the new file-system, so we want to rethrow a the exception
        // wrapped in an epubby-exception, this is to notify the user that *we* know that this happened
        malformed(epubFile, "Could not create a file-system for '${epubFile.name}'", e)
    } catch (e: EpubbyException) {
        // the validation failed, so we want to just delete the copy we made and re-throw the exception
        throw e
    }
    val root = fileSystem.getPath("/")
    val metaInf = MetaInf.fromDirectory(epubFile, root.resolve("META-INF"), root)

    return Book(metaInf, epubFile, fileSystem, root)
}

/**
 * TODO
 *
 * This function copies the given [epubFile] to the given [copyDirectory], the returned [Book] instance then works on
 * the copied file and not the original.
 *
 * @param [epubFile] the file to read and parse into a [Book] instance, this needs to be a EPUB file or this function
 * will throw a [MalformedBookException]
 *
 * @throws [IOException] if an i/o error occurred
 * @throws [EpubbyException] if something went wrong when parsing the [epubFile] file
 */
@JvmOverloads
@JvmName("readCopy")
@Throws(IOException::class, EpubbyException::class)
fun readBookCopy(epubFile: Path, copyDirectory: Path = createTmpDirectory("epubby")): Book {
    logger.info { "Copying file '$epubFile' to directory '$copyDirectory'.." }
    val copy = epubFile.copyTo(copyDirectory, keepName = true)
    copy.toFile().deleteOnExit()
    logger.info { "Reading file '$copy' as an EPUB container.." }
    val fileSystem = try {
        FileSystems.newFileSystem(copy, null).also { validateContainer(epubFile, it.getPath("/")) }
    } catch (e: IOException) {
        // something went wrong when trying to create the new file-system, so we want to rethrow a the exception
        // wrapped in an epubby-exception, this is to notify the user that *we* know that this happened
        copy.deleteIfExists()
        malformed(epubFile, "Could not create a file-system for '${epubFile.name}'", e)
    } catch (e: EpubbyException) {
        // the validation failed, so we want to just delete the copy we made and re-throw the exception
        copy.deleteIfExists()
        throw e
    }
    val root = fileSystem.getPath("/")
    val metaInf = MetaInf.fromDirectory(copy, root.resolve("META-INF"), root)

    return Book(metaInf, copy, fileSystem, root)
}

// validates that the required parts of an EPUB is available in the epub file
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