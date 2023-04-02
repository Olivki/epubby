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
import dev.epubby.Epub
import dev.epubby.errors.FileError
import java.io.InputStream

// this file system model is very heavily inspired by the way Ceylon modeled its file system

sealed interface EpubFileSystem {
    val epub: Epub

    val root: Directory

    fun getPath(first: String, vararg more: String): Path

    fun importFile(file: JPath, target: ModifiableDirectory): Either<FileError, ExistingResource>

    fun importStream(stream: InputStream, target: ModifiableDirectory): Either<FileError, ExistingResource>
}