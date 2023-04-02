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
import dev.epubby.errors.FileError
import java.math.BigInteger
import java.nio.file.CopyOption
import java.nio.file.attribute.FileTime

sealed interface Directory : ExistingResource {
    val parent: Directory?

    /**
     * Returns the sum of the sizes of all the entries of `this` directory.
     */
    fun calculateDirectorySize(): Either<FileError, ULong>

    /**
     * Returns the sum of the sizes of all the entries of `this` directory.
     */
    fun calculateLargeDirectorySize(): Either<FileError, BigInteger>

    fun listEntries(glob: String = "*"): Either<FileError, List<ExistingResource>>

    fun copyEntriesTo(target: ModifiableDirectory, vararg options: CopyOption): Either<FileError, ModifiableDirectory>
}

sealed interface DeletableDirectory : Directory, Deletable {
    fun deleteRecursively(): Either<FileError, Nil>
}

sealed interface ModifiableDirectory : Directory, Modifiable, `ModifiableDirectory | Nil` {
    override fun setLastModifiedTime(time: FileTime): Either<FileError, ModifiableDirectory>

    override fun renameTo(name: String, overwrite: Boolean): Either<FileError, Directory>

    fun moveTo(target: ModifiableDirectory, vararg options: CopyOption): Either<FileError, Directory>

    fun moveRecursivelyTo(target: ModifiableDirectory, vararg options: CopyOption): Either<FileError, Directory>
}

sealed interface UnprotectedDirectory : Unprotected, Directory, DeletableDirectory, ModifiableDirectory {
    override fun setLastModifiedTime(time: FileTime): Either<FileError, UnprotectedDirectory>
}