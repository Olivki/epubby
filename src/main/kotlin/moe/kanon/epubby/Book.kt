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

import moe.kanon.epubby.Book.Format
import moe.kanon.epubby.metainf.MetaInf
import moe.kanon.epubby.resources.PageRepository
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.ResourceRepository
import moe.kanon.epubby.resources.StyleSheetRepository
import moe.kanon.epubby.resources.root.PackageDocument
import moe.kanon.epubby.utils.SemVer
import moe.kanon.epubby.utils.compareTo
import moe.kanon.epubby.utils.inside
import moe.kanon.epubby.utils.stringify
import moe.kanon.kommons.TMP_DIR
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.io.paths.cleanDirectory
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.createTmpDirectory
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.isDirectory
import moe.kanon.kommons.io.paths.notExists
import moe.kanon.kommons.io.paths.readString
import java.io.Closeable
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * Represents the container holding together an [EPUB](...).
 *
 * @property [version] The version of `this` book.
 *
 * For more thorough information regarding versions, check the [format] property, and the [Format] enum.
 * @property [file] The epub file that `this` book is wrapped around.
 *
 * Any changes made to `this` book will be reflected in the actual file.
 *
 * Note that if `this` book was made from parsing an already existing epub, then this will be pointing towards the
 * *copy* of that file. If you want to access the original file, use [originFile].
 * @property [fileSystem] A zip [file-system][FileSystem] built around the given [file].
 *
 * This `file-system` is used throughout the system to ensure that we're only working with files that actually exist
 * inside of the epub file, and not outside.
 * @property [originFile] The original file that `this` book is wrapped around.
 *
 * This will point to different files depending on how `this` instance was created;
 * - If `this` was created from parsing *([Book.parse])* an already existing epub file, then this will point to that
 * file, while [file] will be pointing towards the *copy* that was made of that file.
 * - If `this` was created from scratch, *([Book.create])* then this will be pointing towards the same file as [file],
 * as no copy is made when creating a new epub from scratch.
 * @property [settings] The [BookSettings] used for various operations throughout the system.
 */
class Book private constructor(
    val version: SemVer,
    val file: Path,
    val fileSystem: FileSystem,
    val originFile: Path,
    val settings: BookSettings,
    val metaInf: MetaInf
) : Closeable {
    /**
     * The [ResourceRepository] bound to `this` book.
     */
    val resources: ResourceRepository = ResourceRepository(this, file) // TODO: this was 'rootDocument' before

    val pages: PageRepository = PageRepository(this)

    val styleSheets: StyleSheetRepository = StyleSheetRepository(this)

    /**
     * The [Format] that `this` book uses.
     */
    val format: Format = Format.from(version)

    /**
     * Returns the root directory of `this` book.
     */
    val root: Path by lazy { fileSystem.getPath("") }

    companion object {
        private val randomDirectory: Path get() = createTmpDirectory("epubby")

        /**
         * Returns a new [Book] instance based on the given [origin].
         *
         * This function attempts to parse the `origin` file, and *will* fail if the given file is *not* a valid EPUB.
         *
         * Note that the `Book` instance will *not* be working on the given `origin` file, but rather on a defensive
         * copy that has been made of it. To access the `origin` file from the book instance, use the
         * [originFile][Book.originFile] property.
         *
         * @throws [EpubbyException] if something went wrong when parsing the [origin] file
         * @throws [IOException] if an i/o error occurred
         */
        @Throws(EpubbyException::class, IOException::class)
        @JvmOverloads @JvmStatic fun parse(origin: Path, settings: BookSettings = BookSettings.default): Book {
            // make sure that we're working with a valid EPUB file before we copy it
            // we're not storing this file system as the 'Book' instance will be using the file-system from the backed
            // up file that we are creating if this check passes.
            FileSystems.newFileSystem(origin, null).use { validate(origin, it.getPath("/")) }

            // we make a defensive copy of the given 'origin' file so that we are not modifying the original file at
            // any point
            val copy = origin.copyTo(randomDirectory, keepName = true)
            val fs = FileSystems.newFileSystem(copy, null)
            val root = fs.getPath("/")
            val metaInf = MetaInf.parse(copy, root.resolve("META-INF"))

            println(metaInf.container.toDocument().stringify())

            val packageDocument = PackageDocument.parse(origin, metaInf.container.packageDocument.fullPath)

            /*
            // we're retrieving the path to the first 'rootfile' element in the 'container.xml' because for our system
            // we have no real use for any of the different renditions available.
            val rootDocument = root.resolve("META-INF", "container.xml").newInputStream().use { input ->
                with(SAXBuilder(XMLReaders.NONVALIDATING).build(input).rootElement) {
                    val file = origin.resolve("META-INF", "container.xml")
                    val rootFiles = children.find { it.name == "rootfiles" } ?: throw MalformedBookException(
                        origin,
                        "Missing 'rootfiles' element in file <$file>"
                    )
                    val rootFile = rootFiles.children.find { it.name == "rootfile" } ?: throw MalformedBookException(
                        origin,
                        "Missing 'rootfile' element in file <$file>"
                    )
                    val path = rootFile.getAttributeValue("full-path") ?: throw MalformedBookException(
                        origin,
                        "Missing 'full-path' attribute on element 'rootfile' in file <$file>"
                    )
                    return@with root.resolve(path)
                }
            }

            println(rootDocument)*/
            TODO()
        }

        /**
         * Checks the basic validity of the given [container] as an EPUB file.
         *
         * This function is here to provide better information regarding what is wrong with an EPUB file right at the
         * start, rather than the system failing later on in with a non-descriptive error.
         */
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
         * @param [destionation] where the newly created epub should be saved to
         */
        @Throws(EpubbyException::class, IOException::class)
        @JvmOverloads @JvmStatic fun create(destionation: Path, settings: BookSettings = BookSettings.default): Book {
            TODO()
        }
    }

    /**
     * TODO
     *
     * @see [saveTo]
     */
    fun save(): Path = saveTo(file)

    fun saveTo(dest: Path): Path {
        TODO()
    }

    /**
     * Signals to the system that `this` book will not be worked on any longer.
     *
     * This will close all the streams that are currently in use by `this` book. And as such, this function should only
     * be invoked when all operations on `this` book are finished. Any calls to `this` book after this function has
     * been invoked will most likely result in several exceptions being thrown.
     *
     * Note that if [file] is stored in the temp directory of the OS, then this function will also delete [file] and
     * its [parent][Path.getParent] directory, so make sure to call [saveTo] before this is called, otherwise all
     * progress will be lost.
     *
     * To ensure that `this` book gets closed at a logical time, `try-with-resources` can be used;
     *
     * ### Kotlin
     * ```kotlin
     *  val book: Book = ...
     *  book.use {
     *      ...
     *  }
     * ```
     *
     * ### Java
     * ```java
     *  try (final Book book = ...) {
     *      ...
     *  }
     * ```
     */
    override fun close() {
        fileSystem.close()
        // checking if 'file' is stored inside the temp dir of the OS
        // TODO: Make sure this works
        if (file.parent.parent.toString() == TMP_DIR) file.parent.cleanDirectory()
    }

    // -- UTILITY FUNCTIONS -- \\
    fun getResource(href: String): Resource = TODO()

    fun getResourceOrNone(href: String): Option<Resource> = TODO()

    /**
     * Returns a new [Path] instance tied to the underlying [fileSystem] of `this` book.
     *
     * See [FileSystem.getPath] for more information regarding how the `Path` instance is created.
     *
     * @param [path] the path string
     *
     * @see FileSystem.getPath
     */
    @JvmName("getPath")
    fun pathOf(path: String): Path = fileSystem.getPath(path)

    /**
     * Returns a new [Path] instance tied to the underlying [fileSystem] of `this` book.
     *
     * See [FileSystem.getPath] for more information regarding how the `Path` instance is created.
     *
     * @param [first] the path string or initial part of the path string
     * @param [more] additional strings to be joined to form the path string
     *
     * @see FileSystem.getPath
     */
    @JvmName("getPath")
    fun pathOf(first: String, vararg more: String): Path = fileSystem.getPath(first, *more)

    /**
     * Represents the **major** version formats available for epubs as of creation. (2019-07-22).
     *
     * This enum is used to sort the epub into a certain "version category".
     *
     * This is important to know because there are generally pretty large differences between the formats between
     * major versions, and trying to parse, say, an epub in the v3 format with v2 format rules will end up with
     * a slightly corrupted file.
     *
     * So it's safer to just loudly fail and alert the user that this version is not supported rather than greedily
     * trying to still parse a document that most likely has features this library wasn't built to process.
     *
     * @property [version] The lowest supported version number for this version format.
     *
     * ie. If a book is found to be using `v3.0.1` of the format, it will be assigned the [EPUB_3][Format.EPUB_3_0]
     * version, but if it is using `v4.x` it will be assigned the [NOT_SUPPORTED][Format.NOT_SUPPORTED] version.
     */
    enum class Format(val version: SemVer) {
        /**
         * Represents an unknown epub format version.
         *
         * During the initial deserialization process [Book.version] is set to this value.
         */
        UNKNOWN(SemVer("0.0.0")),
        /**
         * Represents the [EPUB 2.x](http://www.idpf.org/epub/dir/#epub201) format.
         *
         * Any version where `n >= 2 && n < 3` will be sorted into this category.
         *
         * Specifications for EPUB 2.0 format can be found [here](http://www.idpf.org/epub/20/spec/OPS_2.0.1_draft.htm).
         */
        EPUB_2_0(SemVer("2.0.0")),
        /**
         * Represents the [EPUB 3.0](http://www.idpf.org/epub/dir/#epub301) format.
         *
         * Any version where `n >= 3 && n < 3.0.x` will be sorted into this category.
         *
         * Specifications for EPUB 3.0 format can be found [here](http://www.idpf.org/epub/301/spec/epub-publications.html).
         */
        EPUB_3_0(SemVer("3.0.0")),
        /**
         * Represents the [EPUB 3.1](http://www.idpf.org/epub/dir/#epub31) format.
         *
         * Any version where `n >= 3.1.x && n < 3.1.x` will be sorted into this category.
         *
         * The EPUB 3.1 format is [officially discouraged](http://www.idpf.org/epub/dir/#epub31) from use, and as such the
         * format is explicitly ***not*** supported by epubby, and it should never be used.
         */
        EPUB_3_1(SemVer("3.1.0")),
        /**
         * Represents the [EPUB 3.2](http://www.idpf.org/epub/dir/#epub32) format.
         *
         * Any version where `n >= 3.2.x && n < 4` will be sorted into this category.
         */
        EPUB_3_2(SemVer("3.2.0")),
        /**
         * Represents an unsupported EPUB format.
         *
         * This should always be set to the highest **major** version number that's not supported by epubby.
         * Generally this will be an unreleased version of the EPUB format, unless support for this library has been
         * dropped.
         *
         * Currently this is set to react on any `>= v4.x.x` versions.
         */
        NOT_SUPPORTED(SemVer("4.0.0"));

        // we can't implement custom 'compareTo' for 'EpubFormat' because 'compareTo(other: EpubFormat)' is generated by
        // default in java, and it's set to be 'final', so we can't override it.
        /**
         * Compares the [version] of `this` format to the specified [semVer].
         */
        operator fun compareTo(semVer: SemVer): Int = version.compareTo(semVer)

        /**
         * Returns a [ClosedRange] with the [start][ClosedRange.start] value set to `this` formats [version] and the
         * [end][ClosedRange.endInclusive] value set to the `version` of the specified [other] format.
         *
         * @param [other] the format to create a range to
         */
        operator fun rangeTo(other: Format): ClosedRange<SemVer> = version..other.version

        override fun toString(): String = version.toString()

        companion object {
            /**
             * Returns the [Format] with the closest matching version to the specified [version], or [UNKNOWN] if none
             * is found.
             */
            @JvmStatic fun from(version: SemVer): Format = when {
                version inside EPUB_2_0..EPUB_3_0 -> EPUB_2_0
                version inside EPUB_3_0..EPUB_3_1 -> EPUB_3_0
                version inside EPUB_3_1..EPUB_3_2 -> throw UnsupportedOperationException(
                    "The use of the epub 3.1.x format is officially discouraged and should never be used."
                )
                version inside EPUB_3_2..NOT_SUPPORTED -> EPUB_3_2
                version >= NOT_SUPPORTED -> NOT_SUPPORTED
                else -> UNKNOWN
            }
        }

    }
}

operator fun Book.get(href: String): Resource = this.getResource(href)