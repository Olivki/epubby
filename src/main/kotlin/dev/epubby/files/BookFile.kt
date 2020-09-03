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

package dev.epubby.files

import dev.epubby.Book
import dev.epubby.utils.CloseableIterable
import moe.kanon.kommons.io.paths.createFrom
import org.apache.commons.io.file.PathUtils
import java.io.*
import java.nio.channels.ByteChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.StandardOpenOption.*
import java.nio.file.attribute.*

// TODO: implement checking of whether a file belongs to a resource (probably cache it in some manner for more efficient
//       accessing) and if it does, update the 'file' of a resource if it gets moved/renamed/changed in any manner.

sealed class BookFile {
    /**
     * The [Book] instance that this file belongs.
     */
    abstract val book: Book

    @get:JvmSynthetic
    internal abstract val delegate: Path

    // TODO: change the type of 'parent' and 'root' back to 'BookFile' if it comes to light that they can be of
    //       something that isn't a directory

    /**
     * The parent directory of this path, or `null` if it has none.
     */
    val parent: DirectoryFile? by lazy { delegate.parent?.toDirectoryFile() }

    /**
     * The root directory of this path, or `null` if it has none.
     */
    val root: DirectoryFile? by lazy { delegate.root?.toDirectoryFile() }

    val isAbsolute: Boolean
        get() = delegate.isAbsolute

    val fullPath: String by lazy { delegate.toString() }

    /**
     * The file name of this path.
     */
    val name: String
        get() = delegate.fileName.toString()

    /**
     * Returns `true` if this file exists, otherwise `false`.
     *
     * @see [Files.exists]
     */
    val exists: Boolean
        @JvmName("exists")
        get() = when (this) {
            is GhostFile -> false
            else -> Files.exists(delegate)
        }

    /**
     * Returns `true` if this file does *not* exist, otherwise `false`.
     *
     * @see [Files.notExists]
     */
    val notExists: Boolean
        @JvmName("notExists")
        get() = when (this) {
            is GhostFile -> true
            else -> Files.notExists(delegate)
        }

    /**
     * The last modified time of this file.
     *
     * @see [Files.getLastModifiedTime]
     * @see [Files.setLastModifiedTime]
     */
    var lastModified: FileTime
        get() = Files.getLastModifiedTime(delegate)
        set(value) {
            Files.setLastModifiedTime(delegate, value)
        }

    val attributes: BasicAttributes<Any> by lazy {
        object : BasicAttributes<Any> {
            override fun get(attribute: String): Any = Files.getAttribute(delegate, attribute)

            override fun set(attribute: String, value: Any) {
                Files.setAttribute(delegate, attribute, value)
            }
        }
    }

    /**
     * Returns a [FileAttributeView] of the given [type].
     *
     * @see [Files.getFileAttributeView]
     */
    fun <T : FileAttributeView> getAttributesView(type: Class<T>): T = Files.getFileAttributeView(delegate, type)

    /**
     * Returns a [FileAttributeView] of the given [type][T].
     *
     * @see [Files.getFileAttributeView]
     */
    @JvmSynthetic
    inline fun <reified T : FileAttributeView> getAttributesView(): T = getAttributesView(T::class.java)

    /**
     * Returns this file as a [RegularFile], or converts this file into a `RegularFile`, depending on the
     * implementation.
     *
     * This function may throw a [UnsupportedOperationException] if a conversion is impossible.
     *
     * As each implementation of this function behaves a bit differently, please see each implementations own
     * documentation for more information.
     *
     * @throws [UnsupportedOperationException] if a conversion is impossible
     *
     * @see [RegularFile.getOrCreateFile]
     * @see [DirectoryFile.getOrCreateFile]
     * @see [GhostFile.getOrCreateFile]
     */
    @Throws(BookFileIOException::class)
    abstract fun getOrCreateFile(vararg attributes: FileAttribute<*>): RegularFile

    /**
     * Returns this file as a [DirectoryFile], or converts this file into a `DirectoryFile`, depending on the
     * implementation.
     *
     * This function may throw a [UnsupportedOperationException] if a conversion is impossible.
     *
     * As each implementation of this function behaves a bit differently, please see each implementations own
     * documentation for more information.
     *
     * @throws [UnsupportedOperationException] if a conversion is impossible
     *
     * @see [RegularFile.getOrCreateDirectory]
     * @see [DirectoryFile.getOrCreateDirectory]
     * @see [GhostFile.getOrCreateDirectory]
     */
    @Throws(BookFileIOException::class)
    abstract fun getOrCreateDirectory(vararg attributes: FileAttribute<*>): DirectoryFile

    /**
     * Creates any directories that are missing leading up to this files [parent], and then its `parent` too.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.createDirectories]
     */
    @Throws(BookFileIOException::class)
    fun createParents() {
        parent?.getOrCreateDirectory()
    }

    // TODO: how does 'moveTo' and 'renameTo' behave when it is a 'GhostFile'?

    /**
     * Moves this file to the given [target] file.
     *
     * @throws [BookFileAlreadyExistsException] if there already exists a file at the given [target]
     */
    @Throws(BookFileIOException::class)
    fun moveTo(target: BookFile): BookFile {
        checkExistence(target.delegate)
        return wrapIO { Files.move(delegate, target.delegate, COPY_ATTRIBUTES).toBookFile() }
    }

    @Throws(BookFileIOException::class)
    fun renameTo(name: String): BookFile {
        val target = delegate.resolveSibling(name)
        checkExistence(target)
        return wrapIO { Files.move(delegate, target, COPY_ATTRIBUTES).toBookFile() }
    }

    fun resolve(other: BookFile): BookFile = delegate.resolve(other.delegate).toBookFile()

    fun resolve(name: String): BookFile = delegate.resolve(name).toBookFile()

    fun resolveSibling(other: BookFile): BookFile = delegate.resolveSibling(other.delegate).toBookFile()

    fun resolveSibling(name: String): BookFile = delegate.resolveSibling(name).toBookFile()

    // TODO: find a better name?
    protected inline fun <T> withValidName(name: String, block: () -> T): T {
        require('/' !in name) { "Illegal character '/' in: $name" }
        require('\\' !in name) { "Illegal character '\\' in: $name" }
        return block()
    }

    protected fun Path.toBookFile(): BookFile = newInstance(this, book)

    protected fun Path.toRegularFile(): RegularFile = RegularFile.newInstance(this, book)

    protected fun Path.toDirectoryFile(): DirectoryFile = DirectoryFile.newInstance(this, book)

    protected fun checkExistence(path: Path = delegate) {
        if (Files.exists(path)) {
            throw newBookFileAlreadyExistsException(path)
        }
    }

    final override fun toString(): String = fullPath

    final override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is BookFile -> false
        book != other.book -> false
        delegate != other.delegate -> false
        else -> true
    }

    final override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + delegate.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        @Throws(BookFileIOException::class)
        fun fromFile(parent: DirectoryFile, file: File): BookFile = fromPath(parent, file.toPath())

        @JvmStatic
        @Throws(BookFileIOException::class)
        fun fromPath(parent: DirectoryFile, path: Path): BookFile = when {
            Files.isRegularFile(path) -> RegularFile.fromPath(parent, path)
            Files.isDirectory(path) -> DirectoryFile.fromPath(parent, path)
            Files.notExists(path) -> GhostFile.newInstance(path, parent.book)
            else -> throw IllegalArgumentException("File at '$path' exists, but is neither a regular file nor a directory.")
        }

        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [path] exists[Files.exists], but is neither a
         * [regular file][Files.isRegularFile] nor a [directory][Files.isDirectory]
         */
        @JvmStatic
        fun newInstance(path: Path, book: Book): BookFile = when {
            Files.isRegularFile(path) -> RegularFile.newInstance(path, book)
            Files.isDirectory(path) -> DirectoryFile.newInstance(path, book)
            Files.notExists(path) -> GhostFile.newInstance(path, book)
            else -> throw IllegalArgumentException("File at '$path' exists, but is neither a regular file nor a directory.")
        }
    }
}

class RegularFile private constructor(
    override val book: Book,
    @get:JvmSynthetic
    override val delegate: Path,
) : BookFile() {
    val simpleName: String
        get() = name.substringBeforeLast('.')

    val extension: String?
        get() = when {
            '.' in name -> name.substringAfterLast('.')
            else -> null
        }

    /**
     * Throws an [UnsupportedOperationException] as converting a regular file into a directory is not supported.
     */
    @Deprecated(
        message = "Can't convert a regular file to a directory.",
        replaceWith = ReplaceWith("createFile(*attributes)"),
        level = DeprecationLevel.HIDDEN
    )
    @Throws(UnsupportedOperationException::class)
    override fun getOrCreateDirectory(vararg attributes: FileAttribute<*>): DirectoryFile =
        throw UnsupportedOperationException("Can't convert a regular file to a directory.")

    /**
     * Returns this regular file, and [creates a new file][Files.createFile] if one doesn't exist.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.createFile]
     */
    @Throws(BookFileIOException::class)
    override fun getOrCreateFile(vararg attributes: FileAttribute<*>): RegularFile = apply {
        if (exists) {
            return this
        }

        wrapIO { Files.createFile(delegate, *attributes) }
    }

    /**
     * TODO
     *
     * @see [Files.newInputStream]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun newInputStream(vararg options: OpenOption = arrayOf(READ)): InputStream =
        wrapIO { Files.newInputStream(delegate, *options) }

    /**
     * TODO
     *
     * @see [Files.newOutputStream]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun newOutputStream(vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE)): OutputStream =
        wrapIO { Files.newOutputStream(delegate, *options) }

    /**
     * TODO
     *
     * @see [Files.newBufferedReader]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun newBufferedReader(charset: Charset = StandardCharsets.UTF_8): BufferedReader =
        wrapIO { Files.newBufferedReader(delegate, charset) }

    /**
     * TODO
     *
     * @see [Files.newBufferedWriter]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun newBufferedWriter(vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE)): BufferedWriter =
        wrapIO { Files.newBufferedWriter(delegate, *options) }

    /**
     * TODO
     *
     * @see [Files.newBufferedWriter]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun newBufferedWriter(
        charset: Charset,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): BufferedWriter = wrapIO { Files.newBufferedWriter(delegate, charset, *options) }

    /**
     * TODO
     *
     * @see [Files.newByteChannel]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun newByteChannel(vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE)): ByteChannel =
        wrapIO { Files.newByteChannel(delegate, *options) }

    /**
     * Writes the given [bytes] to this file.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.write]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun writeBytes(
        bytes: ByteArray,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): RegularFile = wrapIO { Files.write(delegate, bytes, *options).toRegularFile() }

    /**
     * Returns a byte array containing the contents of this file.
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.readAllBytes]
     */
    @Throws(BookFileIOException::class)
    fun readBytes(): ByteArray = wrapIO { Files.readAllBytes(delegate) }

    /**
     * Returns a list containing all the lines of this file, encoded in the given [charset].
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.readAllLines]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun readLines(charset: Charset = StandardCharsets.UTF_8): List<String> =
        wrapIO { Files.readAllLines(delegate, charset) }

    /**
     * Writes the given [lines] to this file.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.write]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun writeBytes(
        lines: Iterable<CharSequence>,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): RegularFile = wrapIO { Files.write(delegate, lines, *options).toRegularFile() }

    /**
     * Returns a string containing all the lines of this file, encoded in the given [charset].
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [readBytes]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun readString(charset: Charset = StandardCharsets.UTF_8): String =
        wrapIO { String(Files.readAllBytes(delegate), charset) }

    /**
     * Writes the given [string] to this file encoded using the given [charset].
     *
     * @throws [BookFileIOException] if an I/O error occurs
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun writeString(
        string: String,
        charset: Charset,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): RegularFile = wrapIO { Files.write(delegate, string.toByteArray(charset), *options).toRegularFile() }

    /**
     * Writes the given [string] to this file encoded using the [UTF_8][StandardCharsets.UTF_8] charset.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun writeString(
        string: String,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): RegularFile =
        wrapIO { Files.write(delegate, string.toByteArray(StandardCharsets.UTF_8), *options).toRegularFile() }

    /**
     * Returns a relative path between this file and the given [other] file.
     *
     * @see [Path.relativize]
     */
    fun relativize(other: BookFile): RegularFile = delegate.relativize(other.delegate).toRegularFile()

    companion object {
        /**
         * Returns a new [RegularFile] located inside the given [parent] with the given [name].
         *
         * The returned `RegularFile` may, or may not, [exist][RegularFile.exists], if it does not, invoke
         * [getOrCreateFile][RegularFile.getOrCreateFile] to create it.
         */
        @JvmStatic
        fun of(parent: DirectoryFile, name: String): RegularFile {
            val target = parent.delegate.resolve(name)
            return RegularFile(parent.book, target)
        }

        /**
         * TODO
         *
         * @throws [BookFileAlreadyExistsException] if a file already exists in the given [parent] with the given
         * [fileName]
         * @throws [BookFileIOException] if an I/O error occurs
         */
        @JvmStatic
        @Throws(BookFileIOException::class)
        fun fromInputStream(input: InputStream, parent: DirectoryFile, fileName: String): RegularFile = wrapIO {
            val target = parent.delegate.resolve(fileName)

            if (Files.exists(target)) {
                throw newBookFileAlreadyExistsException(parent)
            }

            target.createFrom(input)

            return@wrapIO RegularFile(parent.book, target)
        }

        /**
         * TODO
         *
         * @throws [BookFileAlreadyExistsException] if a file already exists in the given [parent] with the given
         * [fileName]
         * @throws [BookFileIOException] if an I/O error occurs
         */
        @JvmStatic
        @Throws(BookFileIOException::class)
        fun fromBytes(
            bytes: ByteArray,
            parent: DirectoryFile,
            name: String,
        ): RegularFile = fromInputStream(ByteArrayInputStream(bytes), parent, name)

        /**
         * TODO
         *
         * @throws [BookFileAlreadyExistsException] if a file already exists in the given [parent] with the same
         * [fileName][Path.getFileName] as the given [path]
         * @throws [BookFileIOException] if an I/O error occurs
         * @throws [IllegalArgumentException] if the given [path] is not a [regular file][Files.isRegularFile]
         */
        @JvmStatic
        @Throws(BookFileIOException::class)
        fun fromPath(parent: DirectoryFile, path: Path): RegularFile = when {
            Files.isRegularFile(path) -> when (path.fileSystem) {
                parent.book.fileSystem -> RegularFile(parent.book, path)
                else -> {
                    val target = parent.delegate.resolve(path.fileName.toString())

                    if (Files.exists(target)) {
                        throw newBookFileAlreadyExistsException(target)
                    }

                    RegularFile(parent.book, wrapIO { Files.copy(path, target, COPY_ATTRIBUTES) })
                }
            }
            else -> throw IllegalArgumentException("File at '$path' is not a regular file.")
        }

        @JvmSynthetic
        internal fun newInstance(path: Path, book: Book): RegularFile = RegularFile(book, path)
    }
}

class DirectoryFile private constructor(
    override val book: Book,
    @get:JvmSynthetic
    override val delegate: Path,
) : BookFile() {
    /**
     * Throws an [UnsupportedOperationException] as converting a directory into a regular file is not supported.
     */
    @Deprecated(
        message = "Can't convert a directory to a regular file.",
        replaceWith = ReplaceWith("createDirectory(*attributes)"),
        level = DeprecationLevel.HIDDEN
    )
    @Throws(UnsupportedOperationException::class)
    override fun getOrCreateFile(vararg attributes: FileAttribute<*>): RegularFile =
        throw UnsupportedOperationException("Can't convert a directory to a regular file.")

    /**
     * Returns this directory file, and [creates a new directory][Files.createDirectory] if one doesn't exist.
     *
     * This function will also create any and all missing parent directories if needed.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.createDirectories]
     */
    @Throws(BookFileIOException::class)
    override fun getOrCreateDirectory(vararg attributes: FileAttribute<*>): DirectoryFile = apply {
        if (exists) {
            return this
        }

        wrapIO { Files.createDirectories(delegate, *attributes) }
    }

    @Throws(BookFileIOException::class)
    fun newEntriesIterable(): CloseableIterable<BookFile> = wrapIO { EntriesStream(Files.newDirectoryStream(delegate)) }

    @Throws(BookFileIOException::class)
    fun newEntriesIterable(glob: String): CloseableIterable<BookFile> =
        wrapIO { EntriesStream(Files.newDirectoryStream(delegate, glob)) }

    @Throws(BookFileIOException::class)
    fun newEntriesIterable(filter: (BookFile) -> Boolean): CloseableIterable<BookFile> = wrapIO {
        val filterImpl = DirectoryStream.Filter<Path> { filter(it.toBookFile()) }
        EntriesStream(Files.newDirectoryStream(delegate, filterImpl))
    }

    /**
     * Walks the file tree of this directory with the given [visitor].
     *
     * @see [Files.walkFileTree]
     */
    @JvmOverloads
    @Throws(BookFileIOException::class)
    fun walkFileTree(
        options: Set<FileVisitOption> = emptySet(),
        maxDepth: Int = Int.MAX_VALUE,
        visitor: BookFileVisitor,
    ): DirectoryFile = apply {
        Files.walkFileTree(delegate, options, maxDepth, FileVisitorConverter(visitor))
    }

    /**
     * Returns a relative path between this file and the given [other] file.
     *
     * @see [Path.relativize]
     */
    fun relativize(other: BookFile): DirectoryFile = delegate.relativize(other.delegate).toDirectoryFile()

    private inner class FileVisitorConverter(val wrapper: BookFileVisitor) : FileVisitor<Path> {
        override fun preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes,
        ): FileVisitResult = wrapIO { wrapper.preVisitDirectory(dir.toDirectoryFile(), attrs) }

        override fun visitFile(
            file: Path,
            attrs: BasicFileAttributes,
        ): FileVisitResult = wrapIO { wrapper.visitFile(file.toRegularFile(), attrs) }

        override fun visitFileFailed(
            file: Path,
            exc: IOException,
        ): FileVisitResult = wrapIO { wrapper.visitFileFailed(file.toRegularFile(), exc) }

        override fun postVisitDirectory(
            dir: Path,
            exc: IOException?,
        ): FileVisitResult = wrapIO { wrapper.postVisitDirectory(dir.toDirectoryFile(), exc) }
    }

    // wrapper for 'DirectoryStream<Path>' to map objects from 'Path' to 'BookPath'
    private inner class EntriesStream(private val stream: DirectoryStream<Path>) : CloseableIterable<BookFile> {
        override fun iterator(): Iterator<BookFile> = object : Iterator<BookFile> {
            val delegate = stream.iterator()

            override fun hasNext(): Boolean = delegate.hasNext()

            override fun next(): BookFile = delegate.next().toBookFile()
        }

        override fun close() {
            stream.close()
        }
    }

    companion object {
        @JvmStatic
        fun of(parent: DirectoryFile, name: String): DirectoryFile = TODO()

        // TODO: if adding a 'fromPath' factory function here, make sure to actually copy all the files that may be
        //       contained inside the given 'path' parameter

        /**
         * TODO
         * TODO: explain that *all* the contents of the directory will be copied over
         *
         * @throws [BookFileAlreadyExistsException] if a file already exists in the given [parent] with the same
         * [fileName][Path.getFileName] as the given [path]
         * @throws [BookFileIOException] if an I/O error occurs
         * @throws [IllegalArgumentException] if the given [path] is not a [directory][Files.isDirectory]
         *
         * @see [PathUtils.copyDirectory]
         */
        @JvmStatic
        @Throws(BookFileIOException::class)
        fun fromPath(parent: DirectoryFile, path: Path): DirectoryFile = when {
            Files.isDirectory(path) -> when (path.fileSystem) {
                parent.book.fileSystem -> DirectoryFile(parent.book, path)
                else -> {
                    val target = parent.delegate.resolve(path.fileName.toString())

                    if (Files.exists(target)) {
                        throw newBookFileAlreadyExistsException(target)
                    }

                    wrapIO { PathUtils.copyDirectory(path, target, COPY_ATTRIBUTES) }

                    DirectoryFile(parent.book, target)
                }
            }
            else -> throw IllegalArgumentException("File at '$path' is not a directory file.")
        }

        @JvmSynthetic
        internal fun newInstance(path: Path, book: Book): DirectoryFile = DirectoryFile(book, path)
    }
}

/**
 * A [file][BookFile] which does not yet [exist][BookFile.exists].
 *
 * To turn a `GhostFile` into a proper `BookFile` implementation, use either the
 * [getOrCreateFile][GhostFile.getOrCreateFile] function or the [getOrCreateDirectory][GhostFile.getOrCreateDirectory]
 * function and store that result.
 */
class GhostFile private constructor(
    override val book: Book,
    @get:JvmSynthetic
    override val delegate: Path,
) : BookFile() {
    /**
     * Returns a [RegularFile] pointing towards the file that this file points towards, if the file doesn't yet exist,
     * one will be [created][Files.createFile].
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.createFile]
     */
    @Throws(BookFileIOException::class)
    override fun getOrCreateFile(vararg attributes: FileAttribute<*>): RegularFile = wrapIO {
        when {
            Files.exists(delegate) -> when {
                Files.isRegularFile(delegate) -> delegate.toRegularFile()
                else -> throw BookFileIOException("File at '$delegate' exists, but it is not a regular file.")
            }
            else -> Files.createFile(delegate, *attributes).toRegularFile()
        }
    }

    /**
     * Returns a [DirectoryFile] pointing towards the file that this file points towards, if the file doesn't yet exist,
     * one will be [created][Files.createDirectories].
     *
     * This function will also create any and all missing parent directories if needed.
     *
     * @throws [BookFileIOException] if an I/O error occurs
     *
     * @see [Files.createDirectories]
     */
    @Throws(BookFileIOException::class)
    override fun getOrCreateDirectory(vararg attributes: FileAttribute<*>): DirectoryFile = wrapIO {
        when {
            Files.exists(delegate) -> when {
                Files.isDirectory(delegate) -> delegate.toDirectoryFile()
                else -> throw BookFileIOException("File at '$delegate' exists, but it is not a directory.")
            }
            else -> Files.createFile(delegate, *attributes).toDirectoryFile()
        }
    }

    internal companion object {
        @JvmSynthetic
        fun newInstance(path: Path, book: Book): GhostFile = GhostFile(book, path)
    }
}