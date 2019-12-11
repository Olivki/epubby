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

import kotlinx.collections.immutable.persistentHashSetOf
import moe.kanon.epubby.BookWriter.Options.DELETE_EMPTY_RESOURCE_DIRECTORIES
import moe.kanon.epubby.BookWriter.Options.FIX_FILE_HIERARCHY
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.malformed
import moe.kanon.kommons.checkThat
import moe.kanon.kommons.collections.enumSetOf
import moe.kanon.kommons.collections.isEmpty
import moe.kanon.kommons.io.paths.PathVisitor
import moe.kanon.kommons.io.paths.cleanDirectory
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.delete
import moe.kanon.kommons.io.paths.deleteIfExists
import moe.kanon.kommons.io.paths.entries
import moe.kanon.kommons.io.paths.isDirectory
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.touch
import moe.kanon.kommons.io.paths.walkFileTree
import java.io.IOException
import java.nio.file.ClosedFileSystemException
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.EnumSet

class BookWriter @JvmOverloads constructor(
    private val options: EnumSet<Options> = enumSetOf(
        DELETE_EMPTY_RESOURCE_DIRECTORIES,
        FIX_FILE_HIERARCHY
    )
) {
    // TODO: Add a function for writing a book to an output stream maybe?

    /**
     * Copies the [file][Book.file] of the given [book] to the given [file], overwriting any files that already exist
     * there, and then writes the contents of the `book` to the `file`.
     *
     * This will also transform all the pages using the currently registered page transformers of the `book`.
     *
     * **NOTE**: The `book` *NEEDS* to be closed before this function is invoked, otherwise a
     * [IllegalArgumentException] will be thrown. This is because if any actual modifications have been done to the
     * files of the `book` they will not be represented until *after* the [fileSystem][Book.fileSystem] of the `book`
     * has been closed. As such, if this function were to go ahead and copy the contents of the `book` without it first
     * being closed then the contents written to the given `file` would end being an inaccurate representation of the
     * `book`.
     *
     * @throws [IOException] if an i/o error occurs
     * @throws [EpubbyException] if an error with the serialization of the book occurs
     * @throws [IllegalStateException] if the [file-system][Book.fileSystem] of the given [book] is not closed *(the
     * file-system of a book will automatically be closed as long as the `book` is closed via the [close][Book.close]
     * function)*
     */
    @Throws(IOException::class, EpubbyException::class)
    fun writeToFile(book: Book, file: Path) {
        checkThat(isClosed(book)) { "file-system of 'book' should be closed before writing it to a new file" }
        logger.info { "Copying contents of book file '${book.file.name}' to file '$file'.." }
        book.file.copyTo(file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

        logger.debug { "Opening file-system for file '$file'.." }
        try {
            FileSystems.newFileSystem(file, null).use {
                logger.info { "Writing contents of book <$this> to file '$file'.." }
                book.metadata.updateLastModified()
                book.metaInf.writeToFiles(it)
                book.packageDocument.writeToFile(it)
                book.pages.writePagesToFile(it)
                for (setting in options) setting.modifyBook(book, it, it.getPath("/"))
            }
        } catch (e: IOException) {
            // something went wrong when trying to create the new file-system, so we want to rethrow a the exception
            // wrapped in an epubby-exception, this is to notify the user that *we* know that this happened
            malformed(file, "Could not create a file-system for '${file.name}'", e)
        }

        logger.debug { "Touching file '$file'.." }
        file.touch()
    }

    // ugly way of doing this, but there doesn't seem to be an actual function provided for checking if a file-system
    // is closed or not, so this is how it has to be done
    private fun isClosed(book: Book): Boolean = try {
        book.root.resolve("mimetype").touch()
        false
    } catch (e: ClosedFileSystemException) {
        true
    }

    override fun toString(): String = "BookWriter(settings=$options)"

    /**
     * Options that when enabled will change how the written EPUB will end up.
     *
     * Note that the operations these settings perform are invoked according to their ordinal position, meaning that
     * [MOVE_RESOURCES_TO_DESIRED_DIRECTORIES] will be invoked first, and [FIX_FILE_HIERARCHY] will be invoked
     * last.
     */
    enum class Options {
        /**
         * When the book gets written, any empty directories inside of the root resource directory *(generally speaking
         * this would be the `'/OEBPS/'` directory located at the root of the book)* will be deleted.
         */
        DELETE_EMPTY_RESOURCE_DIRECTORIES {
            @JvmSynthetic
            override fun modifyBook(book: Book, fileSystem: FileSystem, root: Path) {
                fileSystem.getPath(book.packageRoot.toString()).walkFileTree(visitor = object : PathVisitor {
                    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                        if (dir.entries.isEmpty) {
                            logger.trace { "Directory <$dir> is empty, deleting..." }
                            dir.delete()
                        }
                        return FileVisitResult.CONTINUE
                    }

                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =
                        FileVisitResult.CONTINUE

                    override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult =
                        FileVisitResult.CONTINUE

                    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
                        FileVisitResult.CONTINUE
                })
            }
        },
        /**
         * When the book gets written any files/directories located at the root of the book that does not belong there
         * will be promptly deleted.
         *
         * The files/directories that "belong" in the root of a book are the following
         * - The resource directory. *(This is the directory where all the resources should be located, i.e;
         * `'/OEBPS/'`)*
         * - The `META-INF` directory, which contains all the meta-inf files.
         * - The `mimetype` file, which should contain a single ASCII encoded string stating `"application/epub+zip"`.
         *
         * Any other files that end up in the root of a book is the sign of a badly formatted EPUB, and is not actually
         * "valid", it is therefore recommended to have this setting enabled to make sure that one is producing valid
         * epubs.
         */
        FIX_FILE_HIERARCHY {
            // we don't have an entry for the resource directory here, as that may be a name we don't know of
            private val KNOWN_ROOT_FILES = persistentHashSetOf("META-INF", "META-INF/", "mimetype")

            @JvmSynthetic
            override fun modifyBook(book: Book, fileSystem: FileSystem, root: Path) {
                for (file in root.entries) {
                    val name =
                        if (file.isDirectory) file.name.let { if (!it.endsWith('/')) "$it/" else it } else file.name
                    val packageRootName = book.packageRoot.name.let { if (!it.endsWith('/')) "$it/" else it }

                    if (name !in KNOWN_ROOT_FILES && name != packageRootName) {
                        if (file.isDirectory) {
                            logger.debug { "Encountered unknown directory '$name' in root level of book <$book>, removing all files.." }
                            // TODO: Clean directory
                        } else {
                            logger.debug { "Encountered unknown file '$name' in root level of book <$book>, removing.." }
                            file.deleteIfExists()
                        }
                    }
                }
            }
        };

        @JvmSynthetic
        internal abstract fun modifyBook(book: Book, fileSystem: FileSystem, root: Path)
    }
}