/*
 * Copyright 2020-2022 Oliver Berg
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

import dev.epubby.Epub
import dev.epubby.utils.CloseableIterable
import org.apache.commons.io.file.PathUtils
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute

class DirectoryFile private constructor(
    override val epub: Epub,
    @get:JvmSynthetic
    override val delegate: Path,
) : EpubFile() {
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
    override fun moveTo(target: EpubFile): DirectoryFile {
        checkCreationPermissions(target)
        require(target !is RegularFile) { "Target '$target' is a regular file." }
        return Files.move(delegate, target.delegate, StandardCopyOption.COPY_ATTRIBUTES).toDirectoryFile()
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

    fun newEntriesIterable(): CloseableIterable<EpubFile> =
        EntriesIterable(Files.newDirectoryStream(delegate))

    fun newEntriesIterable(glob: String): CloseableIterable<EpubFile> =
        EntriesIterable(Files.newDirectoryStream(delegate, glob))

    fun newEntriesIterable(filter: (EpubFile) -> Boolean): CloseableIterable<EpubFile> =
        EntriesIterable(Files.newDirectoryStream(delegate) { filter(it.toBookFile()) })

    /**
     * Walks the file tree of this directory with the given [visitor].
     *
     * @see [Files.walkFileTree]
     */
    @JvmOverloads
    fun walkFileTree(
        visitor: EpubFileVisitor,
        options: Set<FileVisitOption> = emptySet(),
        maxDepth: Int = Int.MAX_VALUE,
    ): DirectoryFile = apply {
        Files.walkFileTree(delegate, options, maxDepth, FileVisitorConverter(visitor))
    }

    override fun toAbsoluteFile(): DirectoryFile = delegate.toAbsolutePath().toDirectoryFile()

    private object DeletingFileVisitor : AbstractEpubFileVisitor() {
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

    private inner class FileVisitorConverter(val wrapper: EpubFileVisitor) : FileVisitor<Path> {
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
    private inner class EntriesIterable(private val stream: DirectoryStream<Path>) : CloseableIterable<EpubFile> {
        override fun iterator(): Iterator<EpubFile> = object : Iterator<EpubFile> {
            val delegate = stream.iterator()

            override fun hasNext(): Boolean = delegate.hasNext()

            override fun next(): EpubFile = delegate.next().toBookFile()
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

                    PathUtils.copyDirectory(path, target, StandardCopyOption.COPY_ATTRIBUTES)

                    DirectoryFile(parent.epub, target)
                }
            }

            else -> throw IllegalArgumentException("File at '$path' is not a directory file.")
        }

        @JvmSynthetic
        internal operator fun invoke(path: Path, epub: Epub): DirectoryFile = DirectoryFile(epub, path)
    }
}