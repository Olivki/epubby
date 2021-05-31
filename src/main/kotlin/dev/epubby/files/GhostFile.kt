/*
 * Copyright 2020-2021 Oliver Berg
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
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute

/**
 * A [file][EpubFile] which does not yet [exist][EpubFile.exists].
 *
 * To turn a `GhostFile` into a proper `BookFile` implementation, use either the
 * [getOrCreateFile][GhostFile.getOrCreateFile] function or the [getOrCreateDirectory][GhostFile.getOrCreateDirectory]
 * function and store that result.
 */
class GhostFile private constructor(
    override val epub: Epub,
    @get:JvmSynthetic
    override val delegate: Path,
) : EpubFile() {
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

    override fun moveTo(target: EpubFile): GhostFile {
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