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

import com.google.common.io.MoreFiles
import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.packages.PackageManifest
import dev.epubby.resources.LocalResource
import dev.epubby.utils.CloseableIterable
import kotlinx.collections.immutable.persistentHashSetOf
import moe.kanon.kommons.io.paths.createFrom
import org.apache.commons.io.file.PathUtils
import java.io.*
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.StandardOpenOption.*
import java.nio.file.attribute.*

// TODO: construct a blacklist of directories where files should never be moved/created? (i.e: the root and maybe
//       META-INF, META-INF may only allow files that have a name that matches the known XML documents that should
//       reside inside of the META-INF directory)
// TODO: Remove the checks for 'RegularFile' and 'DirectoryFile' factory functions on whether or not they exist and if
//       they do exist it is checked if they're a directory, becuase I'm unsure if a directory and a file can exist
//       with the same name, they probably can so double check this

sealed class BookFile {
    /**
     * The [Epub] instance that this file belongs.
     */
    abstract val epub: Epub

    @get:JvmSynthetic
    internal abstract val delegate: Path

    // TODO: change the type of 'parent' and 'root' back to 'BookFile' if it comes to light that they can be of
    //       something that isn't a directory

    /**
     * The parent directory of this path, or `null` if it has none.
     */
    // TODO: should this be 'lazy' ?
    val parent: DirectoryFile? by lazy { delegate.parent?.toDirectoryFile() }

    /**
     * The root directory of this path, or `null` if it has none.
     */
    val root: DirectoryFile? by lazy { delegate.root?.toDirectoryFile() }

    val isAbsolute: Boolean
        get() = delegate.isAbsolute

    val path: String by lazy { delegate.toString() }

    /**
     * The absolute path of this file.
     *
     * If this file is *not* [absolute][isAbsolute] this will still return the absolute path.
     */
    val fullPath: String by lazy {
        when {
            isAbsolute -> path
            else -> delegate.toAbsolutePath().toString()
        }
    }

    /**
     * Returns the [MediaType] of this file, or `null` if the `MediaType` could not be determined.
     *
     * @see [Files.probeContentType]
     * @see [MediaType.parse]
     */
    val mediaType: MediaType?
        get() = Files.probeContentType(delegate)?.let(MediaType::parse)

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
     * Returns the size of this file in bytes.
     *
     * @see [Files.size]
     */
    val size: Long
        get() = Files.size(delegate)

    /**
     * Returns `true` if this file belongs to a [LocalResource], otherwise `false`.
     */
    val isResourceFile: Boolean
        get() = fullPath in epub.manifest.fileToLocalResource

    /**
     * Returns the [LocalResource] that this file belongs to, or `null` if this file doesn't belong a `LocalResource`.
     */
    val resource: LocalResource?
        get() = epub.manifest.fileToLocalResource[fullPath]

    val isMimeType: Boolean
        get() = this == epub.mimeType

    val isMetaInf: Boolean
        get() = this == epub.metaInf.directory

    val isOpfFile: Boolean
        get() = this == epub.opfFile

    val isOpfDirectory: Boolean
        get() = this == epub.opfDirectory

    val isMetaInfFile: Boolean
        get() = parent == epub.metaInf.directory && name in META_INF_WHITELIST && this !is DirectoryFile

    /**
     * Returns `true` if this file can be modified in any manner, otherwise `false`.
     */
    val canBeModified: Boolean
        @JvmName("canBeModified")
        get() = !isMimeType && !isMetaInfFile && !isOpfFile && !isOpfDirectory

    /**
     * Returns `true` if this file can be deleted by [deleteIfExists], otherwise `false`.
     *
     * Whether a file can be deleted or not varies, but the most generally case of where a file *can't* be deleted is
     * if it belongs to a part of the system that needs to handle the deletion in a safe manner *(i.e; [isResourceFile])*
     * or it's an integral part of the structure of the EPUB file.
     *
     * As a general rule, if any of the following is *true* then this file can not be deleted:
     * - This is a [resource file][isResourceFile]
     * - This is the [mime-type file][isMimeType]
     * - This is a [meta-inf file][isMetaInfFile]
     * - This is the [meta-inf directory][isMetaInf]
     *
     * If a file can't [be modified][canBeDeleted] then it can't be deleted either.
     */
    val canBeDeleted: Boolean
        @JvmName("canBeDeleted")
        get() = !isResourceFile && canBeModified && !isOpfFile && !isOpfDirectory

    /**
     * Returns `true` if this file is empty, otherwise `false`.
     *
     * @see [PathUtils.isEmpty]
     */
    fun isEmpty(): Boolean = PathUtils.isEmpty(delegate)

    fun isNotEmpty(): Boolean = !(isEmpty())

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
    abstract fun getOrCreateDirectory(vararg attributes: FileAttribute<*>): DirectoryFile

    /**
     * Creates any directories that are missing leading up to this files [parent], and then its `parent` too.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.createDirectories]
     */
    fun createParents() {
        parent?.getOrCreateDirectory()
    }

    // TODO: documentation
    // TODO: document that modification operations can throw [UnsupportedOperationException]

    /**
     * Moves this file to the given [target] file.
     *
     * @throws [FileAlreadyExistsException] if there already exists a file at the given [target]
     */
    abstract fun moveTo(target: BookFile): BookFile

    abstract fun moveTo(target: DirectoryFile): BookFile

    abstract fun renameTo(name: String): BookFile

    /**
     * Returns a relative path between this file and the given [other] file.
     *
     * @see [Path.relativize]
     */
    fun relativize(other: BookFile): BookFile = delegate.relativize(other.delegate).toBookFile()

    fun relativizeFile(other: BookFile): RegularFile = delegate.relativize(other.delegate).toRegularFile()

    fun relativizeDirectory(other: BookFile): DirectoryFile = delegate.relativize(other.delegate).toDirectoryFile()

    /**
     * Returns a normalized version of this file.
     *
     * @see [Path.normalize]
     */
    abstract fun normalize(): BookFile

    /**
     * Deletes this file if it [exists], and it [can be deleted][canBeDeleted].
     *
     * If this file can not be deleted, then a [UnsupportedOperationException] will be thrown upon invocation.
     *
     * @throws [IOException] if an I/O error occurs
     * @throws [UnsupportedOperationException] if this file belongs to a [LocalResource]
     *
     * @return `true` if this file was deleted by this function, otherwise `false`
     *
     * @see [canBeDeleted]
     * @see [PackageManifest.removeLocalResource]
     * @see [Files.deleteIfExists]
     */
    abstract fun deleteIfExists(): Boolean

    @JvmSynthetic
    internal fun getResource(): LocalResource? = epub.manifest.fileToLocalResource[fullPath]

    /**
     * Returns `true` if this file is the same file as the given [other] file, otherwise `false`.
     *
     * If the [epub] of this file does *not* equal the epub of `other` then this will always return `false`, even if
     * everything else about the `other` file would be the exact same.
     *
     * @see [Files.isSameFile]
     * @see [MoreFiles.equal]
     */
    fun isSameAs(other: BookFile): Boolean = when {
        epub != other.epub -> false
        else -> Files.isSameFile(delegate, other.delegate)
    }

    fun resolve(other: BookFile): BookFile = delegate.resolve(other.delegate).toBookFile()

    fun resolve(other: RegularFile): RegularFile = delegate.resolve(other.delegate).toRegularFile()

    fun resolve(other: DirectoryFile): DirectoryFile = delegate.resolve(other.delegate).toDirectoryFile()

    fun resolve(name: String): BookFile = delegate.resolve(name).toBookFile()

    fun resolveFile(name: String): RegularFile = delegate.resolve(name).toRegularFile()

    fun resolveDirectory(name: String): DirectoryFile = delegate.resolve(name).toDirectoryFile()

    /**
     * Resolves the given [other] file against this files [parent].
     *
     * @see [Path.resolveSibling]
     */
    fun resolveSibling(other: BookFile): BookFile = delegate.resolveSibling(other.delegate).toBookFile()

    fun resolveSibling(other: RegularFile): RegularFile = delegate.resolveSibling(other.delegate).toRegularFile()

    fun resolveSibling(other: DirectoryFile): DirectoryFile = delegate.resolveSibling(other.delegate).toDirectoryFile()

    /**
     * Resolves the given [name] as a file against this files [parent].
     *
     * @see [Path.resolveSibling]
     */
    fun resolveSibling(name: String): BookFile = delegate.resolveSibling(name).toBookFile()

    fun resolveSiblingFile(name: String): RegularFile = delegate.resolveSibling(name).toRegularFile()

    fun resolveSiblingDirectory(name: String): DirectoryFile = delegate.resolveSibling(name).toDirectoryFile()

    /**
     * Returns an `URI` that represents the path to this file.
     *
     * @see [Path.toUri]
     */
    fun toUri(): URI = delegate.toUri()

    /**
     * Returns a new [BookFile] instance representing the absolute path of this file.
     *
     * If this file is already [absolute][isAbsolute] then it simply returns itself.
     *
     * @see [Path.toAbsolutePath]
     */
    abstract fun toAbsoluteFile(): BookFile

    // TODO: find a better name?
    protected inline fun <T> withValidName(name: String, block: () -> T): T {
        require('/' !in name) { "Illegal character '/' in: $name" }
        require('\\' !in name) { "Illegal character '\\' in: $name" }
        return block()
    }

    protected fun Path.toBookFile(): BookFile = BookFile(this, epub)

    protected fun Path.toRegularFile(): RegularFile = RegularFile(this, epub)

    protected fun Path.toDirectoryFile(): DirectoryFile = DirectoryFile(this, epub)

    protected fun Path.toGhostFile(): GhostFile = GhostFile(this, epub)

    protected fun checkExistence(path: Path = delegate) {
        if (Files.exists(path)) {
            fileAlreadyExists(path)
        }
    }

    protected fun checkModificationPermissions() {
        if (!canBeModified) {
            throw UnsupportedOperationException("The file at '$fullPath' can not be modified.")
        }
    }

    protected fun checkCreationPermissions(target: BookFile = this) {
        if (target.parent == epub.root) {
            if (target !is DirectoryFile) {
                throw UnsupportedOperationException("Only directories are allowed to be created in the root of an EPUB.")
            }
        }

        if (target.parent == epub.metaInf.directory) {
            if (target.name !in META_INF_WHITELIST) {
                throw UnsupportedOperationException("File '$target' is not allowed inside of the /META-INF/ directory.")
            }

            if (target is DirectoryFile) {
                throw UnsupportedOperationException("Directory files are not allowed inside of the /META-INF/ directory.")
            }
        }
    }

    final override fun toString(): String = delegate.toString()

    final override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is BookFile -> false
        epub != other.epub -> false
        delegate != other.delegate -> false
        else -> true
    }

    final override fun hashCode(): Int {
        var result = epub.hashCode()
        result = 31 * result + delegate.hashCode()
        return result
    }

    companion object {
        private val META_INF_WHITELIST: Set<String> = persistentHashSetOf(
            "container.xml",
            "encryption.xml",
            "manifest.xml",
            "metadata.xml",
            "rights.xml",
            "signatures.xml",
        )

        @JvmStatic
        fun fromFile(parent: DirectoryFile, file: File): BookFile = fromPath(parent, file.toPath())

        @JvmStatic
        fun fromPath(parent: DirectoryFile, path: Path): BookFile = when {
            Files.isRegularFile(path) -> RegularFile.fromPath(parent, path)
            Files.isDirectory(path) -> DirectoryFile.fromPath(parent, path)
            Files.notExists(path) -> GhostFile(path, parent.epub)
            else -> throw IllegalArgumentException("File at '$path' exists, but is neither a regular file nor a directory.")
        }

        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [path] exists[Files.exists], but is neither a
         * [regular file][Files.isRegularFile] nor a [directory][Files.isDirectory]
         */
        @JvmSynthetic
        internal operator fun invoke(path: Path, epub: Epub): BookFile = when {
            Files.isRegularFile(path) -> RegularFile(path, epub)
            Files.isDirectory(path) -> DirectoryFile(path, epub)
            Files.notExists(path) -> GhostFile(path, epub)
            else -> throw IllegalArgumentException("File at '$path' exists, but is neither a regular file nor a directory.")
        }
    }
}

class RegularFile private constructor(
    override val epub: Epub,
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
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.createFile]
     */
    override fun getOrCreateFile(vararg attributes: FileAttribute<*>): RegularFile = apply {
        if (exists) {
            return this
        }

        checkCreationPermissions()

        Files.createFile(delegate, *attributes)
    }

    override fun moveTo(target: BookFile): RegularFile {
        checkModificationPermissions()
        checkCreationPermissions(target)
        checkExistence(target.delegate)
        val result = Files.move(delegate, target.delegate, COPY_ATTRIBUTES).toRegularFile()

        if (isResourceFile) {
            epub.manifest.getLocalResourceFromFile(this)?.file = result
        }

        return result
    }

    override fun moveTo(target: DirectoryFile): RegularFile = moveTo(target.resolve(name))

    override fun renameTo(name: String): RegularFile = moveTo(resolveSibling(name))

    override fun deleteIfExists(): Boolean = when {
        canBeDeleted -> Files.deleteIfExists(delegate)
        else -> throw UnsupportedOperationException("File at '$fullPath' can not be deleted.")
    }

    override fun normalize(): RegularFile = delegate.normalize().toRegularFile()

    /**
     * TODO
     *
     * @see [Files.newInputStream]
     */
    // TODO: throw exception if 'options' contains a modifying operation when modification permission is not allowed
    @JvmOverloads
    fun newInputStream(vararg options: OpenOption = arrayOf(READ)): InputStream =
        Files.newInputStream(delegate, *options)

    /**
     * TODO
     *
     * @see [Files.newOutputStream]
     */
    @JvmOverloads
    fun newOutputStream(vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE)): OutputStream {
        checkModificationPermissions()
        return Files.newOutputStream(delegate, *options)
    }

    /**
     * TODO
     *
     * @see [Files.newBufferedReader]
     */
    @JvmOverloads
    fun newBufferedReader(charset: Charset = StandardCharsets.UTF_8): BufferedReader =
        Files.newBufferedReader(delegate, charset)

    /**
     * TODO
     *
     * @see [Files.newBufferedWriter]
     */
    @JvmOverloads
    fun newBufferedWriter(vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE)): BufferedWriter {
        checkModificationPermissions()
        return Files.newBufferedWriter(delegate, *options)
    }

    /**
     * TODO
     *
     * @see [Files.newBufferedWriter]
     */
    @JvmOverloads
    fun newBufferedWriter(
        charset: Charset,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): BufferedWriter {
        checkModificationPermissions()
        return Files.newBufferedWriter(delegate, charset, *options)
    }

    /**
     * TODO
     *
     * @see [Files.newByteChannel]
     */
    @JvmOverloads
    fun newByteChannel(vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE)): SeekableByteChannel {
        val channel = Files.newByteChannel(delegate, *options)
        return when {
            canBeModified -> channel
            else -> NonWritableByteChannel(channel)
        }
    }

    /**
     * Writes the given [bytes] to this file.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.write]
     */
    @JvmOverloads
    fun writeBytes(
        bytes: ByteArray,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): RegularFile {
        checkModificationPermissions()
        return Files.write(delegate, bytes, *options).toRegularFile()
    }

    /**
     * Returns a byte array containing the contents of this file.
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.readAllBytes]
     */
    fun readBytes(): ByteArray = Files.readAllBytes(delegate)

    /**
     * Returns a list containing all the lines of this file, encoded in the given [charset].
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.readAllLines]
     */
    @JvmOverloads
    fun readLines(charset: Charset = StandardCharsets.UTF_8): List<String> =
        Files.readAllLines(delegate, charset)

    /**
     * Writes the given [lines] to this file.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.write]
     */
    @JvmOverloads
    fun writeLines(
        lines: Iterable<CharSequence>,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): RegularFile {
        checkModificationPermissions()
        return Files.write(delegate, lines, *options).toRegularFile()
    }

    /**
     * Returns a string containing all the lines of this file, encoded in the given [charset].
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [readBytes]
     */
    @JvmOverloads
    fun readString(charset: Charset = StandardCharsets.UTF_8): String = String(Files.readAllBytes(delegate), charset)

    /**
     * Writes the given [string] to this file encoded using the given [charset].
     *
     * @throws [IOException] if an I/O error occurs
     */
    @JvmOverloads
    fun writeString(
        string: String,
        charset: Charset,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): RegularFile {
        checkModificationPermissions()
        return Files.write(delegate, string.toByteArray(charset), *options).toRegularFile()
    }

    /**
     * Writes the given [string] to this file encoded using the [UTF_8][StandardCharsets.UTF_8] charset.
     *
     * @throws [IOException] if an I/O error occurs
     */
    @JvmOverloads
    fun writeString(
        string: String,
        vararg options: OpenOption = arrayOf(CREATE, TRUNCATE_EXISTING, WRITE),
    ): RegularFile {
        checkModificationPermissions()
        return Files.write(delegate, string.toByteArray(StandardCharsets.UTF_8), *options).toRegularFile()
    }

    override fun toAbsoluteFile(): RegularFile = delegate.toAbsolutePath().toRegularFile()

    private class NonWritableByteChannel(private val delegate: SeekableByteChannel) : SeekableByteChannel by delegate {
        override fun write(src: ByteBuffer): Int =
            throw UnsupportedOperationException("Can not write to this byte-channel as it belongs to a non-modifiable file.")

        override fun truncate(size: Long): SeekableByteChannel =
            throw UnsupportedOperationException("Can not truncate this byte-channel as it belongs to a non-modifiable file.")
    }

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
            return RegularFile(parent.epub, target)
        }

        /**
         * TODO
         *
         * @throws [FileAlreadyExistsException] if a file already exists in the given [parent] with the given
         * [fileName]
         * @throws [IOException] if an I/O error occurs
         */
        @JvmStatic
        fun fromInputStream(input: InputStream, parent: DirectoryFile, fileName: String): RegularFile {
            val target = parent.delegate.resolve(fileName)

            if (Files.exists(target)) {
                fileAlreadyExists(parent.delegate)
            }

            target.createFrom(input)

            return RegularFile(parent.epub, target)
        }

        /**
         * TODO
         *
         * @throws [FileAlreadyExistsException] if a file already exists in the given [parent] with the given
         * [fileName]
         * @throws [IOException] if an I/O error occurs
         */
        @JvmStatic
        fun fromBytes(
            bytes: ByteArray,
            parent: DirectoryFile,
            name: String,
        ): RegularFile = fromInputStream(ByteArrayInputStream(bytes), parent, name)

        /**
         * TODO
         *
         * @throws [FileAlreadyExistsException] if a file already exists in the given [parent] with the same
         * [fileName][Path.getFileName] as the given [path]
         * @throws [IOException] if an I/O error occurs
         * @throws [IllegalArgumentException] if the given [path] is not a [regular file][Files.isRegularFile]
         */
        @JvmStatic
        fun fromPath(parent: DirectoryFile, path: Path): RegularFile = when {
            Files.isRegularFile(path) -> when (path.fileSystem) {
                parent.epub.fileSystem -> RegularFile(parent.epub, path)
                else -> {
                    val target = parent.delegate.resolve(path.fileName.toString())

                    if (Files.exists(target)) {
                        fileAlreadyExists(target)
                    }

                    RegularFile(parent.epub, Files.copy(path, target, COPY_ATTRIBUTES))
                }
            }
            else -> throw IllegalArgumentException("File at '$path' is not a regular file.")
        }

        @JvmSynthetic
        internal operator fun invoke(path: Path, epub: Epub): RegularFile = RegularFile(epub, path)
    }
}

class DirectoryFile private constructor(
    override val epub: Epub,
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
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.createDirectories]
     */
    override fun getOrCreateDirectory(vararg attributes: FileAttribute<*>): DirectoryFile = apply {
        if (exists) {
            return this
        }

        checkCreationPermissions()

        Files.createDirectories(delegate, *attributes)
    }

    // TODO: the 'moveTo' functions may throw an exception because the directories may contain entries
    // TODO: implement a custom move function for this using a file visitor or something so that we can update any
    //       resource files that may belong to this directory
    override fun moveTo(target: BookFile): DirectoryFile {
        checkCreationPermissions(target)
        require(target !is RegularFile) { "Target '$target' is a regular file." }
        return Files.move(delegate, target.delegate, COPY_ATTRIBUTES).toDirectoryFile()
    }

    override fun moveTo(target: DirectoryFile): DirectoryFile = moveTo(target.resolve(name))

    override fun renameTo(name: String): DirectoryFile = moveTo(resolveSibling(name))

    /**
     * Deletes this directory and all it's sub-directories if it [exists], and it [can be deleted][canBeDeleted].
     *
     * If this file can not be deleted, then a [UnsupportedOperationException] will be thrown upon invocation.
     *
     * @throws [IOException] if an I/O error occurs
     * @throws [UnsupportedOperationException] if this file belongs to a [LocalResource]
     *
     * @return `true` if this file was deleted by this function, otherwise `false`
     *
     * @see [canBeDeleted]
     * @see [PackageManifest.removeLocalResource]
     * @see [Files.deleteIfExists]
     * @see [PathUtils.deleteDirectory]
     */
    override fun deleteIfExists(): Boolean = when {
        canBeDeleted -> when {
            exists -> {
                deleteEntriesIfExists()

                if (isNotEmpty()) {
                    throw IOException("Directory '$this' could not be fully deleted.")
                } else {
                    Files.deleteIfExists(delegate)
                }

                true
            }
            else -> false
        }
        else -> throw UnsupportedOperationException("File at '$fullPath' can not be deleted.")
    }

    // TODO: documentation
    fun deleteEntriesIfExists(): Boolean = when {
        canBeDeleted -> when {
            exists -> {
                walkFileTree(DeletingFileVisitor)
                true
            }
            else -> false
        }
        else -> false
    }

    override fun normalize(): DirectoryFile = delegate.normalize().toDirectoryFile()

    fun newEntriesIterable(): CloseableIterable<BookFile> =
        EntriesIterable(Files.newDirectoryStream(delegate))

    fun newEntriesIterable(glob: String): CloseableIterable<BookFile> =
        EntriesIterable(Files.newDirectoryStream(delegate, glob))

    fun newEntriesIterable(filter: (BookFile) -> Boolean): CloseableIterable<BookFile> {
        val filterImpl = DirectoryStream.Filter<Path> { filter(it.toBookFile()) }
        return EntriesIterable(Files.newDirectoryStream(delegate, filterImpl))
    }

    /**
     * Walks the file tree of this directory with the given [visitor].
     *
     * @see [Files.walkFileTree]
     */
    @JvmOverloads
    fun walkFileTree(
        visitor: BookFileVisitor,
        options: Set<FileVisitOption> = emptySet(),
        maxDepth: Int = Int.MAX_VALUE,
    ): DirectoryFile = apply {
        Files.walkFileTree(delegate, options, maxDepth, FileVisitorConverter(visitor))
    }

    override fun toAbsoluteFile(): DirectoryFile = delegate.toAbsolutePath().toDirectoryFile()

    private object DeletingFileVisitor : AbstractBookFileVisitor() {
        override fun preVisitDirectory(directory: DirectoryFile, attributes: BasicFileAttributes): FileVisitResult {
            if (directory.isEmpty()) {
                directory.deleteIfExists()
            }

            return super.preVisitDirectory(directory, attributes)
        }

        override fun visitFile(file: RegularFile, attributes: BasicFileAttributes): FileVisitResult {
            if (file.exists) {
                file.deleteIfExists()
            }

            return FileVisitResult.CONTINUE
        }
    }

    private inner class FileVisitorConverter(val wrapper: BookFileVisitor) : FileVisitor<Path> {
        override fun preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes,
        ): FileVisitResult = wrapper.preVisitDirectory(dir.toDirectoryFile(), attrs)

        override fun visitFile(
            file: Path,
            attrs: BasicFileAttributes,
        ): FileVisitResult = wrapper.visitFile(file.toRegularFile(), attrs)

        override fun visitFileFailed(
            file: Path,
            exc: IOException,
        ): FileVisitResult = wrapper.visitFileFailed(file.toRegularFile(), exc)

        override fun postVisitDirectory(
            dir: Path,
            exc: IOException?,
        ): FileVisitResult = wrapper.postVisitDirectory(dir.toDirectoryFile(), exc)
    }

    // wrapper for 'DirectoryStream<Path>' to map objects from 'Path' to 'BookPath'
    private inner class EntriesIterable(private val stream: DirectoryStream<Path>) : CloseableIterable<BookFile> {
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
        fun of(parent: DirectoryFile, name: String): DirectoryFile =
            DirectoryFile(parent.epub, parent.delegate.resolve(name))

        // TODO: if adding a 'fromPath' factory function here, make sure to actually copy all the files that may be
        //       contained inside the given 'path' parameter

        /**
         * TODO
         * TODO: explain that *all* the contents of the directory will be copied over
         *
         * @throws [FileAlreadyExistsException] if a file already exists in the given [parent] with the same
         * [fileName][Path.getFileName] as the given [path]
         * @throws [IOException] if an I/O error occurs
         * @throws [IllegalArgumentException] if the given [path] is not a [directory][Files.isDirectory]
         *
         * @see [PathUtils.copyDirectory]
         */
        @JvmStatic
        fun fromPath(parent: DirectoryFile, path: Path): DirectoryFile = when {
            Files.isDirectory(path) -> when (path.fileSystem) {
                parent.epub.fileSystem -> DirectoryFile(parent.epub, path)
                else -> {
                    val target = parent.delegate.resolve(path.fileName.toString())

                    if (Files.exists(target)) {
                        fileAlreadyExists(target)
                    }

                    PathUtils.copyDirectory(path, target, COPY_ATTRIBUTES)

                    DirectoryFile(parent.epub, target)
                }
            }
            else -> throw IllegalArgumentException("File at '$path' is not a directory file.")
        }

        @JvmSynthetic
        internal operator fun invoke(path: Path, epub: Epub): DirectoryFile = DirectoryFile(epub, path)
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
    override val epub: Epub,
    @get:JvmSynthetic
    override val delegate: Path,
) : BookFile() {
    /**
     * Returns a [RegularFile] pointing towards the file that this file points towards, if the file doesn't yet exist,
     * one will be [created][Files.createFile].
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.createFile]
     */
    override fun getOrCreateFile(vararg attributes: FileAttribute<*>): RegularFile {
        checkCreationPermissions()

        return when {
            Files.exists(delegate) -> when {
                Files.isRegularFile(delegate) -> delegate.toRegularFile()
                else -> throw IOException("File at '$delegate' exists, but it is not a regular file.")
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
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.createDirectories]
     */
    override fun getOrCreateDirectory(vararg attributes: FileAttribute<*>): DirectoryFile {
        checkCreationPermissions()

        return when {
            Files.exists(delegate) -> when {
                Files.isDirectory(delegate) -> delegate.toDirectoryFile()
                else -> throw IOException("File at '$delegate' exists, but it is not a directory.")
            }
            else -> Files.createFile(delegate, *attributes).toDirectoryFile()
        }
    }

    override fun moveTo(target: BookFile): GhostFile {
        checkExistence(target.delegate)

        return target.delegate.toGhostFile()
    }

    override fun moveTo(target: DirectoryFile): GhostFile {
        checkExistence(target.delegate)

        return target.delegate.resolve(name).toGhostFile()
    }

    override fun renameTo(name: String): GhostFile {
        val target = parent?.delegate?.resolve(name) ?: epub.root.delegate.resolve(name)

        checkExistence(target)

        return target.toGhostFile()
    }

    override fun deleteIfExists(): Boolean = false

    override fun normalize(): GhostFile = delegate.normalize().toGhostFile()

    override fun toAbsoluteFile(): GhostFile = delegate.toAbsolutePath().toGhostFile()

    internal companion object {
        @JvmSynthetic
        internal operator fun invoke(path: Path, epub: Epub): GhostFile = GhostFile(epub, path)
    }
}