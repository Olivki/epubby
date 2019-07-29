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
import moe.kanon.epubby.settings.BookSettings
import moe.kanon.epubby.utils.SemVer
import moe.kanon.epubby.utils.SemVerType
import moe.kanon.epubby.utils.combineWith
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.isDirectory
import moe.kanon.kommons.io.paths.notExists
import moe.kanon.kommons.io.paths.readString
import moe.kanon.kommons.io.paths.simpleName
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardCopyOption

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
@Throws(EpubbyException::class, IOException::class)
@JvmOverloads fun readBook(
    origin: Path,
    outputDirectory: Path,
    outputName: String = origin.simpleName,
    settings: BookSettings = BookSettings.default
): Book {
    logger.info { "Attempting to read file <$origin> as an EPUB container..." }

    // make sure that we're working with a valid EPUB file before we copy it
    // we're not storing this file system as the 'Book' instance will be using the file-system from the backed
    // up file that we are creating if this check passes.
    FileSystems.newFileSystem(origin, null).use { validate(origin, it.getPath("/")) }

    // we make a defensive copy of the given 'origin' file so that we are not modifying the original file at
    // any point
    val copy = origin.copyTo(outputDirectory.resolve("$outputName.epub"), StandardCopyOption.REPLACE_EXISTING)
    val fs = FileSystems.newFileSystem(copy, null)
    val root = fs.getPath("/")
    val metaInf = MetaInf.parse(copy, root.resolve("META-INF"))
    val packageDocument = metaInf.container.packageDocument.fullPath

    logger.debug { "Package document located at <${origin.combineWith(packageDocument)}>" }

    val version = parseXmlFile(packageDocument) {
        SemVer(
            getAttributeValue("version") ?: raiseMalformedError(
                origin,
                packageDocument,
                "'package' element is missing 'version' attribute"
            ),
            SemVerType.LOOSE
        )
    }

    Book.logger.debug { "EPUB Format is $version" }

    return Book(version, copy, fs, origin, settings, metaInf)
}

/**
 * Checks the basic validity of the given [container] as an EPUB file.
 *
 * This function is here to provide better information regarding what is wrong with an EPUB file right at the
 * start, rather than the system failing later on in with a non-descriptive error.
 */
@Throws(EpubbyException::class)
private fun validate(container: Path, root: Path) {
    val metaInf = root.resolve("META-INF")
    val mimetype = root.resolve("mimetype")
    when {
        // none of the epub formats have changed the behaviour of requiring the '!META-INF' directory, so it's
        // safe *(for now)* to assume that to be a valid epub file, no matter the format version, a '!META-INF'
        // directory containing a 'container.xml' is REQUIRED
        metaInf.notExists -> {
            throw MalformedBookException(root, "Missing 'META-INF' directory in file <$container>")
        }
        !(metaInf.isDirectory) -> {
            throw MalformedBookException(root, "'META-INF' is not a directory in file <$container>")
        }
        // much like how 'META-INF' is REQUIRED for a file to be a valid epub, 'container.xml' is also REQUIRED.
        // 'container.xml' contains the information of *where* the manifest (.opf) file is located, so we want
        // to parse that for it. (granted we could just manually search through all the files for the first
        // .opf file, but because the epub spec allows one to have *more* than one manifest file, it's safer
        // to use the one that's defined as the MAIN manifest file in the 'container.xml')
        metaInf.resolve("container.xml").notExists -> {
            throw MalformedBookException(
                root,
                "'container.xml' file missing from 'META-INF' directory in file <$container>"
            )
        }
        // 'mimetype' also needs to exist in the root of the epub file, and it NEEDS to contain the ASCII
        // string "application/epub+zip"
        mimetype.notExists -> {
            throw MalformedBookException(root, "'mimetype' file missing from root of file <$container>")
        }
        mimetype.isDirectory -> {
            throw MalformedBookException(
                root,
                "'mimetype' in file <$container> is a directory, needs to be a file"
            )
        }
        mimetype.exists -> {
            val contents =
                mimetype.readString(StandardCharsets.US_ASCII) // because it's supposed to be in ascii
            if (contents != "application/epub+zip") {
                throw MalformedBookException(
                    root,
                    "'mimetype' does not contain \"application/epub+zip\", instead contains \"$contents\""
                )
            }
        }
    }
}

/**
 * TODO
 *
 * @param [destination] where the newly created epub should be saved to
 */
@Throws(EpubbyException::class, IOException::class)
@JvmOverloads fun createBook(destination: Path, settings: BookSettings = BookSettings.default): Book {
    TODO()
}