/*
 * Copyright 2019-2020 Oliver Berg
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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.metainf.MetaInf
import moe.kanon.epubby.packages.Manifest
import moe.kanon.epubby.packages.Metadata
import moe.kanon.epubby.packages.PackageDocument
import moe.kanon.epubby.packages.Spine
import moe.kanon.epubby.resources.Resources
import moe.kanon.epubby.resources.pages.Pages
import moe.kanon.epubby.resources.toc.TableOfContents
import moe.kanon.epubby.resources.transformers.Transformers
import java.io.Closeable
import java.io.IOException
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.spi.FileSystemProvider
import java.util.Locale

// TODO: Make it so that all inner classes of package classes have an instance of the 'Book' they're tied to?
// TODO: Change most of the current 'models' from being 'data' classes as the 'copy' function is not needed for the
//       majority of them, as a lot of them have mutable parts
// TODO: Port over the 'MediaType' class from Guava? Or use more parts of guava, because right now it's a somewhat
//       heavy dependency just to use the 'MediaType' class. Problem right now is that 'MediaType' is somewhat deeply
//       coupled with other guava classes.
// TODO: Add alternate functions that accept a 'String' instead of a 'Identifier' to make it less verbose to work with
//       the api/framework?
// TODO: Add DTD (or whatever they're called) for validating the EPUB XML files?
// TODO: Add 'OrNone' functions along with the 'OrNull' functions
// TODO: Remove the kotlin CSS DSL from this project, create a new small dependency containing only that and some
//       util functions for making the CSS framework nicer to work with from the kotlin side, so that we're not
//       cluttering up this library with util functions and the like.

/**
 * Represents the container that makes up an [EPUB](...).
 *
 * @property [version] The version of `this` book.
 * @property [file] The epub file that `this` book is wrapped around.
 *
 * @property [fileSystem] A zip [file-system][FileSystem] built around the given [file].
 *
 * When `this` book is [closed][close] so is this underlying file-system.
 *
 * Note that for any changes made to any files in this file-system to be reflected, this file-system needs to be
 * closed. It is *NOT* recommended to manually close the file-system, as that could cause issues, but instead the
 * [close] function in `this` book should be invoked.
 * @property [root] The root of the [fileSystem] of the EPUB file.
 */
class Book internal constructor(
    val metaInf: MetaInf,
    val version: BookVersion,
    val file: Path,
    val fileSystem: FileSystem,
    val root: Path
) : Closeable {
    lateinit var packageDocument: PackageDocument
        @JvmSynthetic internal set

    lateinit var tableOfContents: TableOfContents
        @JvmSynthetic internal set

    val resources: Resources = Resources(this)

    val pages: Pages = Pages(this)

    val packageFile: Path get() = packageDocument.file

    // TODO: Name?
    val packageRoot: Path get() = packageFile.parent

    // TODO: Documentation
    val manifest: Manifest get() = packageDocument.manifest

    val metadata: Metadata get() = packageDocument.metadata

    val spine: Spine get() = packageDocument.spine

    val transformers: Transformers = Transformers(this)

    /**
     * Returns the primary title of `this` book.
     */
    var title: String
        get() = metadata.title.content
        set(value) {
            metadata.title.content = value
        }

    /**
     * Returns a list of all the titles defined for `this` book.
     */
    val titles: ImmutableList<String>
        get() = metadata.titles.map { it.content }.toImmutableList()

    /**
     * Returns the primary language of `this` book.
     */
    var language: Locale
        get() = metadata.language.content
        set(value) {
            metadata.language.content = value
        }

    /**
     * Returns a list of all the languages defined for `this` book.
     */
    val languages: ImmutableList<Locale>
        get() = metadata.languages.map { it.content }.toImmutableList()

    /*fun getResource(identifier: Identifier): Resource = resources.getResource(identifier)

    fun getResourceOrNull(identifier: Identifier): Resource? = resources.getResourceOrNull(identifier)*/

    // TODO: Remove these? (The 'getPath' functions)
    /**
     * Constructs and returns a new `path` based on the given [path], the returned `path` is tied to the [fileSystem]
     * of `this` book.
     *
     * See [FileSystemProvider.getPath] for more information regarding how the `Path` instance is created.
     *
     * @param [path] the path uri
     *
     * @see FileSystemProvider.getPath
     */
    fun getPath(path: URI): Path = fileSystem.provider().getPath(path)

    /**
     * Returns a new [Path] instance tied to the underlying [fileSystem] of `this` book.
     *
     * See [FileSystem.getPath] for more information regarding how the `Path` instance is created.
     *
     * @param [path] the path string
     *
     * @see FileSystem.getPath
     */
    fun getPath(path: String): Path = fileSystem.getPath(path)

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
    fun getPath(first: String, vararg more: String): Path = fileSystem.getPath(first, *more)

    /**
     * Closes the [fileSystem] of `this` book, and any other streams that are currently in use.
     *
     * After this function has been invoked no more operations should be done on `this` book instance, as there is no
     * guarantee that any operations will work/will work as intended.
     *
     * If [BookSettings.deleteFileOnClose] is set to `true` then the [file] will also be deleted once this function is
     * invoked.
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
     *
     * @throws [IOException] if an i/o error occurs
     */
    @Throws(IOException::class)
    override fun close() {
        logger.info { "Closing the file-system of book <$this>.." }
        fileSystem.close()
    }

    override fun toString(): String = "Book(title='$title', language='${language.displayLanguage}', version='$version')"
}