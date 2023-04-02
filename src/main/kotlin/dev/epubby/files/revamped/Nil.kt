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

/**
 * Represents a non-existing file or directory in a [EpubFileSystem].
 */
// TODO: name?
sealed interface Nil : Resource, ModifiableTarget, `ModifiableFile | Nil`, `ModifiableDirectory | Nil` {
    fun createFile(vararg attributes: FileAttribute<*>, createParents: Boolean = false): Either<FileError, File>

    fun createDirectory(vararg attributes: FileAttribute<*>, createParents: Boolean = false): Either<FileError, Directory>
}