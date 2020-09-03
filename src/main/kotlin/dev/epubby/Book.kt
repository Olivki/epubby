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

package dev.epubby

import dev.epubby.files.BookFile
import dev.epubby.files.DirectoryFile
import dev.epubby.metainf.MetaInf
import dev.epubby.packages.PackageDocument
import dev.epubby.packages.PackageManifest
import dev.epubby.packages.PackageSpine
import dev.epubby.packages.metadata.PackageMetadata
import java.io.Closeable
import java.nio.file.FileSystem
import java.nio.file.Files

/**
 * Represents an EPUB file.
 *
 * TODO: more documentation
 *
 * @property [version] The version of the EPUB specification that this book adheres to.
 * @property [fileSystem] TODO
 */
class Book internal constructor(
    val version: BookVersion,
    @get:JvmSynthetic
    internal val fileSystem: FileSystem,
) : Closeable {
    @set:JvmSynthetic
    lateinit var metaInf: MetaInf
        internal set

    @set:JvmSynthetic
    lateinit var packageDocument: PackageDocument
        internal set

    val metadata: PackageMetadata
        get() = packageDocument.metadata

    val manifest: PackageManifest
        get() = packageDocument.manifest

    val spine: PackageSpine
        get() = packageDocument.spine

    /**
     * The root directory of the book.
     *
     * Normally only the `mimetype` file and the `META-INF` and `OEBPS` *(may not always be named `OEBPS`)* directories
     * should be located at the root of a book. Any *direct* changes *(i.e; [Files.delete], [Files.move])* to any of
     * these files is ***highly discouraged***, as that can, and most likely will, cause severe issues for the system.
     */
    // TODO: will this work correctly?
    val root: DirectoryFile = newPath("/").getOrCreateDirectory()

    /**
     * The primary title of the book.
     *
     * @see [PackageMetadata.primaryTitle]
     */
    var title: String
        get() = metadata.primaryTitle.content
        set(value) {
            metadata.primaryTitle.content = value
        }

    /**
     * The primary author of the book, or `null` if no primary author is defined.
     *
     * @see [PackageMetadata.primaryAuthor]
     */
    var author: String?
        get() = metadata.primaryAuthor?.content
        set(value) {
            if (value != null) {
                metadata.primaryAuthor?.content = value
            } else {
                metadata.primaryAuthor = null
            }
        }

    /**
     * The primary language of the book.
     *
     * @see [PackageMetadata.primaryLanguage]
     */
    var language: String
        get() = metadata.primaryLanguage.content
        set(value) {
            metadata.primaryLanguage.content = value
        }

    /**
     * Returns a new [BookFile] that belongs to this book.
     *
     * @see [FileSystem.getPath]
     */
    // TODO: rename to something better?
    fun newPath(first: String, vararg more: String): BookFile =
        BookFile.newInstance(fileSystem.getPath(first, *more), this)

    /**
     * Closes the [fileSystem] belonging to this book, signaling the end of any and all modification to the book.
     */
    override fun close() {
        fileSystem.close()
    }
}