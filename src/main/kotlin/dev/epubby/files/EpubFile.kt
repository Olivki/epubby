/*
 * Copyright 2019-2022 Oliver Berg
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
import kotlinx.collections.immutable.persistentHashSetOf
import org.apache.commons.io.file.PathUtils
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.attribute.FileTime

// TODO: construct a blacklist of directories where files should never be moved/created? (i.e: the root and maybe
//       META-INF, META-INF may only allow files that have a name that matches the known XML documents that should
//       reside inside of the META-INF directory)
// TODO: Remove the checks for 'RegularFile' and 'DirectoryFile' factory functions on whether or not they exist and if
//       they do exist it is checked if they're a directory, because I'm unsure if a directory and a file can exist
//       with the same name, they probably can so double check this

/**
 * A file belonging to a specific [epub][EpubFile.epub] instance.
 */
sealed class EpubFile {
    /**
     * The EPUB that this file belongs.
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
    // TODO: rename to something like 'isProtected'? Of course in that case the semantics would be the opposite of this
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
    abstract fun moveTo(target: EpubFile): EpubFile

    abstract fun moveTo(target: DirectoryFile): EpubFile

    abstract fun renameTo(name: String): EpubFile

    /**
     * Returns a relative path between this file and the given [other] file.
     *
     * @see [Path.relativize]
     */
    fun relativize(other: EpubFile): EpubFile = delegate.relativize(other.delegate).toBookFile()

    fun relativizeFile(other: EpubFile): RegularFile = delegate.relativize(other.delegate).toRegularFile()

    fun relativizeDirectory(other: EpubFile): DirectoryFile = delegate.relativize(other.delegate).toDirectoryFile()

    /**
     * Returns a normalized version of this file.
     *
     * @see [Path.normalize]
     */
    abstract fun normalize(): EpubFile

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
    fun isSameAs(other: EpubFile): Boolean = when {
        epub != other.epub -> false
        else -> Files.isSameFile(delegate, other.delegate)
    }

    fun resolve(other: EpubFile): EpubFile = delegate.resolve(other.delegate).toBookFile()

    fun resolve(other: RegularFile): RegularFile = delegate.resolve(other.delegate).toRegularFile()

    fun resolve(other: DirectoryFile): DirectoryFile = delegate.resolve(other.delegate).toDirectoryFile()

    fun resolve(name: String): EpubFile = delegate.resolve(name).toBookFile()

    fun resolveFile(name: String): RegularFile = delegate.resolve(name).toRegularFile()

    fun resolveDirectory(name: String): DirectoryFile = delegate.resolve(name).toDirectoryFile()

    /**
     * Resolves the given [other] file against this files [parent].
     *
     * @see [Path.resolveSibling]
     */
    fun resolveSibling(other: EpubFile): EpubFile = delegate.resolveSibling(other.delegate).toBookFile()

    fun resolveSibling(other: RegularFile): RegularFile = delegate.resolveSibling(other.delegate).toRegularFile()

    fun resolveSibling(other: DirectoryFile): DirectoryFile = delegate.resolveSibling(other.delegate).toDirectoryFile()

    /**
     * Resolves the given [name] as a file against this files [parent].
     *
     * @see [Path.resolveSibling]
     */
    fun resolveSibling(name: String): EpubFile = delegate.resolveSibling(name).toBookFile()

    fun resolveSiblingFile(name: String): RegularFile = delegate.resolveSibling(name).toRegularFile()

    fun resolveSiblingDirectory(name: String): DirectoryFile = delegate.resolveSibling(name).toDirectoryFile()

    /**
     * Returns an `URI` that represents the path to this file.
     *
     * @see [Path.toUri]
     */
    fun toUri(): URI = delegate.toUri()

    /**
     * Returns a new [EpubFile] instance representing the absolute path of this file.
     *
     * If this file is already [absolute][isAbsolute] then it simply returns itself.
     *
     * @see [Path.toAbsolutePath]
     */
    abstract fun toAbsoluteFile(): EpubFile

    // TODO: find a better name?
    protected inline fun <T> withValidName(name: String, block: () -> T): T {
        require('/' !in name) { "Illegal character '/' in: $name" }
        require('\\' !in name) { "Illegal character '\\' in: $name" }
        return block()
    }

    protected fun Path.toBookFile(): EpubFile = EpubFile(this, epub)

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

    protected fun checkCreationPermissions(target: EpubFile = this) {
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
        other !is EpubFile -> false
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
        fun fromFile(parent: DirectoryFile, file: File): EpubFile = fromPath(parent, file.toPath())

        @JvmStatic
        fun fromPath(parent: DirectoryFile, path: Path): EpubFile = when {
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
        internal operator fun invoke(path: Path, epub: Epub): EpubFile = when {
            Files.isRegularFile(path) -> RegularFile(path, epub)
            Files.isDirectory(path) -> DirectoryFile(path, epub)
            Files.notExists(path) -> GhostFile(path, epub)
            else -> throw IllegalArgumentException("File at '$path' exists, but is neither a regular file nor a directory.")
        }
    }
}