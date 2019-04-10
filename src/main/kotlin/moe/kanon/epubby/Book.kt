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
@file:Suppress("MemberVisibilityCanBePrivate")

package moe.kanon.epubby

import arrow.core.Option
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.ResourceRepository
import moe.kanon.epubby.utils.SemVer
import moe.kanon.epubby.utils.createFileSystem
import moe.kanon.kommons.io.copyTo
import moe.kanon.kommons.io.createTempFile
import moe.kanon.kommons.io.createTemporaryDirectory
import moe.kanon.kommons.io.createTemporaryFile
import moe.kanon.kommons.io.deleteOnShutdown
import moe.kanon.kommons.io.requireDirectory
import moe.kanon.kommons.io.requireExistence
import mu.KLogger
import mu.KotlinLogging
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.StandardCopyOption

// TODO: Add DSL builder for book settings
// TODO: Add DSL builder for creating new epubs from nothing, builder will be for initial settings like name and author
// TODO: Add service-loader support for altering the epub via "cleaners" and functions to add them manually
// TODO: Add documentation regarding thrown exceptions

/**
 * The class containing all the information regarding the underlying epub [file].
 *
 * @param [file] the [Path] instance to use for creating the new book class.
 *
 * @property [fileSystem] The underlying [FileSystem] of `this` book.
 *
 * This property should be used for creating relative paths and accessing any files inside of the actual epub file.
 * @property [file] The underlying epub file of `this` book.
 *
 * Depending on how `this` [Book] instance was created, this [Path] might point to an already existing file, or a newly
 * created file.
 */
class Book private constructor(val fileSystem: FileSystem, val file: Path) : Closeable {
    /**
     * The [logger][KLogger] instance used for any and all logging done by epubby.
     */
    val logger: KLogger = KotlinLogging.logger("epubby")
    
    /**
     * The version of the epub format used by `this` book.
     *
     * ([UNKNOWN][BookVersion.UNKNOWN] by default)
     */
    var version: BookVersion = BookVersion.UNKNOWN
        @JvmSynthetic internal set(value) = when (value.format) {
            EpubFormat.UNKNOWN, EpubFormat.NOT_SUPPORTED ->
                throw BookDeserializationException.create(
                    this,
                    "epubby does not support epubs of version <${value.semVer}>."
                )
            else -> field = value
        }
    
    /**
     * Returns the [format][EpubFormat] that `this` book uses.
     */
    val format: EpubFormat get() = version.format
    
    /**
     * The repository containing all the resources currently in use by `this` book.
     */
    val resources: ResourceRepository = ResourceRepository(this)
    
    /**
     * Attempts to save `this` book into a `.epub` file in the specified [dir], using the specified [name].
     *
     * The resulting file will overwrite any files that already exist in the specified `dir` with the specified `name`.
     *
     * The specified [name] is only used as a prefix, the resulting file will *always* end with the `.epub` extension.
     *
     * Note that invoking this function will *not* change the underlying [file] property of `this` book to the file
     * created by this function. The underlying `file` will always stay the same during the life-span of `this` book.
     *
     * @param [dir] the directory to save the epub file to
     * @param [name] the name of the `.epub` file
     *
     * @return the newly created `.epub` file
     */
    @Throws(IOException::class, BookSerializationException::class)
    fun saveTo(dir: Path, name: String): Path {
        dir.requireDirectory()
        
        // TODO: Other things before copying all the bytes from 'file' to the 'epubFile'
        
        return file.copyTo(
            dir.resolve("$name.epub"),
            false,
            StandardCopyOption.COPY_ATTRIBUTES,
            StandardCopyOption.REPLACE_EXISTING
        )
    }
    
    /**
     * Signals to the system that `this` book will not be worked on any longer.
     *
     * This will close all the streams that are currently in use by `this` book. And as such, this function should only
     * be invoked when all operations on `this` book are finished. Any calls to `this` book after this function has
     * been invoked will most likely result in several exceptions being thrown.
     *
     * To ensure that `this` book gets closed at a logical time, `try-with-resources` can be used;
     *
     * **Kotlin**
     * ```kotlin
     *  val book: Book = ...
     *  book.use {
     *      ...
     *  }
     * ```
     *
     * **Java**
     * ```java
     *  try (final Book book = ...) {
     *      ...
     *  }
     * ```
     */
    override fun close() {
        fileSystem.close()
        // make this delete the temp file?
    }
    
    // wrapper functions for convenience sake
    // - for retrieving resources
    /**
     * Returns the first [Resource] that has a [href][Resource.href] that matches the specified [href], or it will
     * throw a [NoSuchElementException] if none is found.
     *
     * @throws [NoSuchElementException] if no `resource` could be found under the specified [href]
     */
    @JvmName("getResource")
    operator fun <R : Resource> get(href: String): R = resources[href]
    
    /**
     * Returns the first [Resource] that has a [href][Resource.href] that matches the specified [href], or it will
     * throw a [NoSuchElementException] if none is found.
     *
     * ```kotlin
     *  val book: Book = ...
     *  // this function enables the use of a syntax which
     *  // is very close to that of the 'get' operator syntax,
     *  // while still allowing one to pass a generic type
     *  val page = book<PageResource>("...")
     * ```
     *
     * @throws [NoSuchElementException] if no `resource` could be found under the specified [href]
     */
    @JvmSynthetic
    operator fun <R : Resource> invoke(href: String): R = resources[href]
    
    /**
     * Returns the first [Resource] that has a `href` that matches the specified [href] wrapped as a [Option].
     */
    @JvmName("getResourceOr")
    fun <R : Resource> getOr(href: String): Option<R> = resources.getOrNone(href)
    
    // - for creating local paths on the book 'fileSystem'
    /**
     * Creates and returns a [Path] instance tied to the underlying [fileSystem] of `this` book.
     *
     * See [FileSystem.getPath] for more information regarding how the `Path` instance is created.
     *
     * @param [path] the path string
     *
     * @see FileSystem.getPath
     */
    fun pathOf(path: String): Path = fileSystem.getPath(path)
    
    /**
     * Creates and returns a [Path] instance tied to the underlying [fileSystem] of `this` book.
     *
     * See [FileSystem.getPath] for more information regarding how the `Path` instance is created.
     *
     * @param [first] the path string or initial part of the path string
     * @param [more] additional strings to be joined to form the path string
     *
     * @see FileSystem.getPath
     */
    fun pathOf(first: String, vararg more: String): Path = fileSystem.getPath(first, *more)
    
    companion object {
        // because there is no equivalent to package private in Kotlin, so we just gotta make a pseudo package private
        // entry point. this is also to ensure that all the logic required for the book has been properly created and
        // initialized. (hefty logic should be avoided in the constructor block, so we're just delegating it to the
        // factory functions)
        @JvmSynthetic
        internal fun newInstance(fileSystem: FileSystem, epubFile: Path): Book = Book(fileSystem, epubFile)
    }
}

/**
 * A class representing basic information about the version of the currently loaded book.
 *
 * @constructor Creates a new [BookVersion] from the specified [semVer] instance.
 *
 * @param [semVer] the [SemVer] instance to use for creating the new [BookVersion].
 *
 * @property [semVer] The underlying [SemVer] used in `this` book-version.
 */
inline class BookVersion(val semVer: SemVer) : Comparable<BookVersion> {
    /**
     * Returns the closest matching [version format][EpubFormat].
     */
    val format: EpubFormat get() = EpubFormat.from(semVer)
    
    /**
     * Compares this [version][BookVersion] to the specified [other] version and returns which one is the bigger.
     */
    override fun compareTo(other: BookVersion): Int = semVer.compareTo(other.semVer)
    
    companion object {
        /**
         * Returns a [BookVersion] that represents an unknown version of the epub format.
         */
        @JvmStatic
        @get:JvmName("getUnknown")
        val UNKNOWN: BookVersion = BookVersion(EpubFormat.UNKNOWN.version)
    }
}

// TODO: Better name
interface BookListener {
    /**
     * This is ran during the initialization of the [Book] instance.
     */
    fun onBookInitialization()
}

/**
 * Creates and returns a [Book] instance based on the contents of the specified epub [epubFile].
 *
 * Note that epubby does *not* edit the original file in any way, instead it creates a backup copy of the file stored
 * inside of the temporary file directory of the current system, which is the file that will actually be altered. The
 * backup file is however marked for deletion upon exit, and as such, to preserve any and all changes done to the epub,
 * the [Book.saveTo] function will need to be invoked before exit.
 *
 * @param [epubFile] the epub file to read the book from
 */
@JvmName("read")
@Throws(IOException::class, BookDeserializationException::class)
fun bookFrom(epubFile: Path): Book {
    epubFile.requireExistence()
    
    // create a file in the temporary directory of the system and copy the specified 'epubFile' onto it
    // this is to ensure that we don't actually modify the base epub file
    val newFile = createTemporaryDirectory("epubby").createTempFile(suffix = ".epub")
    epubFile.copyTo(newFile, false, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
    newFile.deleteOnShutdown()
    val fileSystem = newFile.createFileSystem()
    
    // newFile.createFileSystem().use {
    //
    // }
    
    // TODO: Make sure that the folder structure of the epub is actually sound
    
    return Book.newInstance(fileSystem, newFile)
}

/**
 * Creates and returns a [Book] instance from the specified [inputStream].
 *
 * This function will [create a temporary file][createTemporaryFile] using the bytes of the `inputStream`, replacing
 * any files that already exist.
 *
 * Note that epubby does *not* edit the original file in any way, instead it creates a backup copy of the file stored
 * inside of the temporary file directory of the current system, which is the file that will actually be altered. The
 * backup file is however marked for deletion upon exit, and as such, to preserve any and all changes done to the epub,
 * the [Book.saveTo] function will need to be invoked before exit.
 *
 * @param [inputStream] the [InputStream] to copy the contents of
 */
@JvmName("read")
@Throws(IOException::class, BookDeserializationException::class)
fun bookFrom(inputStream: InputStream): Book {
    val tempFile = createTemporaryFile()
    // copy the bytes from the 'inputStream' to the newly created temp file
    inputStream.use { it.copyTo(tempFile, StandardCopyOption.REPLACE_EXISTING) }
    tempFile.deleteOnShutdown()
    return bookFrom(tempFile)
}

/**
 * Creates and returns a empty [Book] instance, TODO
 */
// TODO: Make it create a zip file somewhere (https://stackoverflow.com/a/14733863)
// TODO: Make some sort of DSL to ease the process of creating wholly new epub files
@JvmName("empty")
@Throws(IOException::class, BookDeserializationException::class)
fun emptyBook(): Book {
    TODO("Implement factory method")
}