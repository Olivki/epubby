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

package dev.epubby.files.revamped

import arrow.core.Either
import arrow.core.continuations.either
import dev.epubby.errors.FileError
import java.io.IOException
import java.math.BigInteger
import java.nio.file.CopyOption
import java.nio.file.FileVisitResult
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.io.path.*

internal data class ConcreteDirectory(override val path: Path) : Directory {
    override val parent: Directory?
        get() = path.parent?.let(::Directory)

    override fun getLastModifiedTime(): Either<FileError, FileTime> =
        wrapIOException { delegatePath.getLastModifiedTime() }

    override fun calculateDirectorySize(): Either<FileError, ULong> = either.eager {
        useEntries { entries ->
            entries.filterIsInstance<File>()
                .fold<_, ULong>(0u) { acc, file -> acc + file.fileSize.bind().toULong() }
        }.bind()
    }

    override fun calculateLargeDirectorySize(): Either<FileError, BigInteger> = either.eager {
        useEntries { entries ->
            entries.filterIsInstance<File>()
                .fold(BigInteger.ZERO) { acc, file -> acc + file.fileSize.bind().toBigInteger() }
        }.bind()
    }

    override fun listEntries(glob: String): Either<FileError, List<ExistingResource>> = useEntries(glob) { it.toList() }

    override fun copyEntriesTo(
        target: ModifiableDirectory,
        vararg options: CopyOption,
    ): Either<FileError, ModifiableDirectory> = either.eager {
        val copier = EntriesCopier(this@ConcreteDirectory, target, options)
        walkFileTree(copier).bind()
        target
    }

    override fun isEmpty(): Either<FileError, Boolean> = useEntries { it.none() }

    override fun isSameAs(other: Resource): Either<FileError, Boolean> =
        wrapIOException { delegatePath.isSameFileAs(other.delegatePath) }

    override fun toString(): String = path.toString()

    private class EntriesCopier(
        val source: Directory,
        val target: ModifiableDirectory,
        val options: Array<out CopyOption>,
    ) : ExistingResourceVisitor {
        override fun preVisitDirectory(
            directory: Directory,
            attributes: BasicFileAttributes,
        ): Either<FileError, FileVisitResult> = either.eager {
            val resource = target.resolve(source.path.relativize(directory))
            ensure(resource !is File) { FileError.NotDirectory(resource.path.toString()) }

            if (resource is Nil) {
                resource.createDirectory().bind()
            }

            FileVisitResult.CONTINUE
        }

        override fun visitFile(
            file: File,
            attributes: BasicFileAttributes,
        ): Either<FileError, FileVisitResult> = either.eager {
            val resource = target.resolve(source.path.relativize(file))
            ensure(resource !is Directory) { FileError.NotFile(resource.path.toString()) }

            file.copyTo(target, *options)

            FileVisitResult.CONTINUE
        }
    }
}

internal data class ConcreteUnprotectedDirectory(private val delegate: ConcreteDirectory) : Directory by delegate,
    UnprotectedDirectory {
    override fun setLastModifiedTime(time: FileTime): Either<FileError, UnprotectedDirectory> = wrapIOException {
        delegatePath.setLastModifiedTime(time)
        this
    }

    override fun delete(): Either<FileError, Nil> =
        wrapIOException(delegatePath::deleteExisting) { createNil() }

    override fun deleteRecursively(): Either<FileError, Nil> = walkFileTree(ResourceDeleter).map { createNil() }

    override fun renameTo(
        name: String,
        overwrite: Boolean,
    ): Either<FileError, Directory> = either.eager {
        when (val target = resolveSibling(name)) {
            is Directory -> when (target) {
                is ModifiableDirectory -> (if (overwrite) moveTo(target) else moveTo(target, REPLACE_EXISTING)).bind()
                else -> shift(FileError.NotModifiable(target.toString()))
            }

            else -> shift(FileError.NotDirectory(target.toString()))
        }
    }

    override fun moveTo(
        target: ModifiableDirectory,
        vararg options: CopyOption,
    ): Either<FileError, Directory> =
        wrapIOException { delegatePath.moveTo(target.delegatePath, *options).toEpubDirectory() }

    override fun moveRecursivelyTo(
        target: ModifiableDirectory,
        vararg options: CopyOption,
    ): Either<FileError, Directory> = either.eager {
        walkFileTree(ResourceMover(target, options)).bind()
        target
    }

    private fun JPath.toEpubDirectory(): Directory =
        Directory(ConcretePath(this, this@ConcreteUnprotectedDirectory.fileSystem))

    override fun toString(): String = path.toString()

    private class ResourceMover(
        private val target: ModifiableDirectory,
        private val options: Array<out CopyOption>,
    ) : ExistingResourceVisitor {
        override fun preVisitDirectory(
            directory: Directory,
            attributes: BasicFileAttributes
        ): Either<FileError, FileVisitResult> = either.eager {
            TODO("move resources")
            if (directory is ModifiableDirectory) {

            }
        }

        override fun visitFile(
            file: File,
            attributes: BasicFileAttributes,
        ): Either<FileError, FileVisitResult> = either.eager {
            if (file is ModifiableFile) {
                file.moveTo(target, *options).bind()
            }

            FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(
            directory: Directory,
            exception: IOException?,
        ): Either<FileError, FileVisitResult> = either.eager {
            if (directory is ModifiableDirectory) {
                directory.moveRecursivelyTo()
            }

            FileVisitResult.CONTINUE
        }
    }

    private object ResourceDeleter : ExistingResourceVisitor {
        override fun visitFile(
            file: File,
            attributes: BasicFileAttributes,
        ): Either<FileError, FileVisitResult> = either.eager {
            if (file is DeletableFile) {
                file.delete().bind()
            } else {
                shift(FileError.NotDeletable(file.toString()))
            }

            FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(
            directory: Directory,
            exception: IOException?,
        ): Either<FileError, FileVisitResult> = either.eager {
            if (directory is DeletableDirectory) {
                directory.delete().bind()
            } else {
                shift(FileError.NotDeletable(directory.toString()))
            }

            FileVisitResult.CONTINUE
        }
    }
}

internal data class ConcreteDeletableDirectory(private val delegate: ConcreteUnprotectedDirectory) :
    DeletableDirectory by delegate

internal data class ConcreteModifiableDirectory(private val delegate: ConcreteUnprotectedDirectory) :
    ModifiableDirectory by delegate

internal fun Directory(path: Path): Directory {
    val directory = ConcreteDirectory(path)
    return when {
        isOpfDirectory(path) || isMetaInfDirectoryPath(path) -> directory
        else -> ConcreteUnprotectedDirectory(directory)
    }
}