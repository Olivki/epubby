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

import moe.kanon.epubby.metainf.MetaInf
import moe.kanon.epubby.packages.Manifest
import moe.kanon.epubby.packages.Metadata
import moe.kanon.epubby.packages.PackageDocument
import moe.kanon.epubby.packages.Spine
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.Resources
import moe.kanon.epubby.resources.pages.Pages
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.Version
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.touch
import java.io.Closeable
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.spi.FileSystemProvider
import java.util.Locale

/**
 * Represents the container that makes up an [EPUB](...).
 *
 * @property [version] The version of `this` book.
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
 */
class Book internal constructor(
    val metaInf: MetaInf,
    val file: Path,
    val fileSystem: FileSystem,
    val originFile: Path,
    val root: Path,
    val version: Version
) : Closeable {
    // TODO: Name? packageDocument is kind of a mouth-full..
    val packageDocument: PackageDocument = PackageDocument.fromBook(this)

    val packageFile: Path get() = packageDocument.file

    // TODO: Name?
    val packageRoot: Path = packageFile.parent

    // TODO: Documentation
    val manifest: Manifest get() = packageDocument.manifest

    val metadata: Metadata get() = packageDocument.metadata

    val spine: Spine get() = packageDocument.spine

    /**
     * Returns the primary title of `this` book.
     */
    var title: String
        get() = metadata.title.value
        set(value) {
            metadata.title.value = value
        }

    /**
     * Returns the primary language of `this` book.
     */
    var language: Locale
        get() = metadata.language.value
        set(value) {
            metadata.language.value = value
        }

    val resources: Resources = Resources(this)

    val pages: Pages = Pages(this)

    init {
        resources.populateFromManifest()
        pages.populateFromSpine()
    }

    fun getResource(identifier: Identifier): Resource = resources.getResource(identifier)

    fun getResourceOrNull(identifier: Identifier): Resource? = resources.getResourceOrNull(identifier)

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

    fun `save all this shit to the place yo lol`() {
        logger.info { "Saving book <$this> to file '${file.name}'" }
        pages.transformers.transformAllPages()
        packageDocument.writeToFile()
        pages.writeAllPages()
        file.touch()
        logger.info { "The book has been successfully saved." }
    }

    /**
     * Closes the [fileSystem] of `this` book, and any other streams that are currently in use.
     *
     * After this function has been invoked no more operations should be done on `this` book instance, as it can no
     * longer be modified once its `fileSystem` has been closed.
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
    }

    override fun toString(): String = "Book(version='$version', title='$title', language='$language', file='$file')"
}