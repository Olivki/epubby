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
import moe.kanon.epubby.root.PackageDocument
import moe.kanon.epubby.root.PackageManifest
import moe.kanon.epubby.root.PackageMetadata
import moe.kanon.epubby.root.PackageSpine
import moe.kanon.epubby.settings.BookSettings
import moe.kanon.epubby.utils.SemVer
import moe.kanon.epubby.utils.SemVerType
import moe.kanon.epubby.utils.compareTo
import moe.kanon.epubby.utils.inside
import moe.kanon.kommons.func.Option
import mu.KLogger
import mu.KotlinLogging
import java.io.Closeable
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.*

/**
 * Represents the container holding together an [EPUB](...).
 *
 * To create an instance of this class, use either the [readBook] or [createBook] function.
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
 * - If `this` was created from parsing *([Book.read])* an already existing epub file, then this will point to that
 * file, while [file] will be pointing towards the *copy* that was made of that file.
 * - If `this` was created from scratch, *([Book.create])* then this will be pointing towards the same file as [file],
 * as no copy is made when creating a new epub from scratch.
 * @property [settings] The [BookSettings] used for various operations throughout the system.
 * @property [metaInf] TODO
 */
class Book internal constructor(
    val version: SemVer,
    val file: Path,
    val fileSystem: FileSystem,
    val originFile: Path,
    val settings: BookSettings,
    val metaInf: MetaInf
) : Closeable {
    companion object {
        @JvmStatic val logger: KLogger = KotlinLogging.logger("epubby")
    }

    // -- PACKAGE DOCUMENT -- \\
    /**
     * Returns the [PackageDocument] of `this` book.
     */
    val packageDocument: PackageDocument = PackageDocument.parse(this, metaInf.container.packageDocument.fullPath)

    // TODO: Documentation

    val packageManifest: PackageManifest get() = packageDocument.manifest

    val packageMetadata: PackageMetadata get() = packageDocument.metadata

    val packageSpine: PackageSpine get() = packageDocument.spine

    // -- METADATA -- \\
    /**
     * Returns the title of `this` book.
     */
    var title: String
        get() = packageMetadata.title.value
        set(value) {
            packageMetadata.title = packageMetadata.title.copy(value = value)
        }

    /**
     * Returns the language of `this` book.
     */
    var language: Locale
        get() = packageMetadata.language.value
        set(value) {
            packageMetadata.language = packageMetadata.language.copy(value = value)
        }

    // -- REPOSITORIES -- \\
    /**
     * The [ResourceRepository] bound to `this` book.
     */
    val resources: ResourceRepository = ResourceRepository(this)

    val pages: PageRepository = PageRepository(this)

    val styleSheets: StyleSheetRepository = StyleSheetRepository(this)

    /**
     * The [Format] that `this` book uses.
     */
    val format: Format = Format.of(version)

    /**
     * Returns the root directory of `this` book.
     */
    val root: Path = fileSystem.getPath("/")

    init {
        logger.info { "Book format: $format" }
        logger.info { "Book titles: [${packageMetadata.titles.joinToString { it.value }}]" }
        logger.info { "Book languages: [${packageMetadata.languages.joinToString { it.value.toString() }}]" }
        resources.populateFromManifest()
    }

    fun saveAll() {
        logger.info { "Attempting to save book to destination <$file>..." }
        packageDocument.save()
        logger.info { "Book has been successfully saved to <$file>!" }
    }

    /**
     * Signals to the system that `this` book will not be worked on any longer.
     *
     * This will close all the streams that are currently in use by `this` book. And as such, this function should only
     * be invoked when all operations on `this` book are finished. Any calls to `this` book after this function has
     * been invoked will most likely result in several exceptions being thrown.
     *
     * Note that if [file] is stored in the temp directory of the OS, then this function will also delete [file] and
     * its [parent][Path.getParent] directory, so make sure to call [saveAll] before this is called, otherwise all
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
    @Throws(IOException::class)
    override fun close() {
        logger.info { "Closing file-system of container <$file>" }
        fileSystem.close()
    }

    // -- UTILITY FUNCTIONS -- \\
    fun getResource(id: String): Resource = TODO()

    fun getResourceOrNone(id: String): Option<Resource> = TODO()

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

    override fun toString(): String = "Book(title='$title', language='$language', version='$version')"

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
        UNKNOWN(SemVer("0.0", SemVerType.LOOSE)),
        /**
         * Represents the [EPUB 2.x](http://www.idpf.org/epub/dir/#epub201) format.
         *
         * Any version where `n >= 2 && n < 3` will be sorted into this category.
         *
         * Specifications for EPUB 2.0 format can be found [here](http://www.idpf.org/epub/20/spec/OPS_2.0.1_draft.htm).
         */
        EPUB_2_0(SemVer("2.0", SemVerType.LOOSE)),
        /**
         * Represents the [EPUB 3.0](http://www.idpf.org/epub/dir/#epub301) format.
         *
         * Any version where `n >= 3 && n < 3.0.x` will be sorted into this category.
         *
         * Specifications for EPUB 3.0 format can be found [here](http://www.idpf.org/epub/301/spec/epub-publications.html).
         */
        EPUB_3_0(SemVer("3.0", SemVerType.LOOSE)),
        /**
         * Represents the [EPUB 3.1](http://www.idpf.org/epub/dir/#epub31) format.
         *
         * Any version where `n >= 3.1.x && n < 3.1.x` will be sorted into this category.
         *
         * The EPUB 3.1 format is [officially discouraged](http://www.idpf.org/epub/dir/#epub31) from use, and as such the
         * format is explicitly ***not*** supported by epubby, and it should never be used.
         */
        EPUB_3_1(SemVer("3.1", SemVerType.LOOSE)),
        /**
         * Represents the [EPUB 3.2](http://www.idpf.org/epub/dir/#epub32) format.
         *
         * Any version where `n >= 3.2.x && n < 4` will be sorted into this category.
         */
        EPUB_3_2(SemVer("3.2", SemVerType.LOOSE)),
        /**
         * Represents an unsupported EPUB format.
         *
         * This should always be set to the highest **major** version number that's not supported by epubby.
         * Generally this will be an unreleased version of the EPUB format, unless support for this library has been
         * dropped.
         *
         * Currently this is set to react on any `>= v4.x.x` versions.
         */
        NOT_SUPPORTED(SemVer("4.0", SemVerType.LOOSE));

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
            @JvmStatic fun of(version: SemVer): Format = when {
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

@PublishedApi internal inline val logger: KLogger get() = Book.logger