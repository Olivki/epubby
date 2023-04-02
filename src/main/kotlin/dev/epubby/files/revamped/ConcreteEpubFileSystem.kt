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
import dev.epubby.internal.utils.safeCast
import java.io.Closeable
import java.nio.file.FileSystem

internal class ConcreteEpubFileSystem(override val epub: Epub, val delegate: FileSystem) : EpubFileSystem, Closeable {
    override val root: Directory = ConcreteDirectory(createPath(delegate.getPath("./")))

    override fun getPath(first: String, vararg more: String): Path =
        createPath(delegate.getPath(first, *more))

    override fun importFile(file: JPath, target: ModifiableDirectory): Either<FileError, ExistingResource> {
        TODO("Not yet implemented")
    }

    override fun close() {
        delegate.close()
    }

    private fun createPath(path: JPath): Path = ConcretePath(path, this)
}

internal fun EpubFileSystem.asConcrete(): ConcreteEpubFileSystem = this.safeCast()