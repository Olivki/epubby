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

import moe.kanon.epubby.structs.Version
import java.io.Closeable
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path

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
    val version: Version,
    val file: Path,
    val fileSystem: FileSystem,
    val originFile: Path
) : Closeable {
    /**
     * Returns the root directory of the book.
     */
    val root: Path = getPath("/")

    val title: String = TODO()

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
     * Signals to the system that `this` book will not be worked on any longer.
     *
     * This will close all the streams that are currently in use by the book. And as such, this function should only
     * be invoked when all operations on the book are finished. Any calls to the book after this function has
     * been invoked will most likely result in several exceptions being thrown.
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
        fileSystem.close()
    }
}