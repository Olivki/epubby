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
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.isSameFileAs

internal data class ConcreteNil(override val path: Path) : Nil {
    override fun createFile(vararg attributes: FileAttribute<*>, createParents: Boolean): Either<FileError, File> =
        wrapIOException {
            if (createParents) delegatePath.parent?.createDirectories()
            val resource = Path(delegatePath.createFile(), fileSystem).resource
            check(resource is File) { "File '$resource' should be a file, but it was not. (${resource::class})" }
            resource
        }

    override fun createDirectory(
        vararg attributes: FileAttribute<*>,
        createParents: Boolean,
    ): Either<FileError, Directory> = wrapIOException {
        if (createParents) delegatePath.parent?.createDirectories()
        val resource = Path(delegatePath.createFile(), fileSystem).resource
        check(resource is Directory) { "File '$resource' should be a directory, but it was not. (${resource::class})" }
        resource
    }

    override fun isSameAs(other: Resource): Either<FileError, Boolean> =
        wrapIOException { delegatePath.isSameFileAs(other.delegatePath) }
}