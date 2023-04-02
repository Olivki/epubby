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

import dev.epubby.internal.utils.safeCast
import kotlin.io.path.*

internal class ConcretePath(internal val delegate: JPath, override val fileSystem: EpubFileSystem) : Path {
    override val parent: Path?
        get() = delegate.parent?.let(this::createPath)

    override val name: String
        get() = delegate.name

    override val pathName: Path
        get() = createPath(delegate.fileName)

    override val size: Int
        get() = delegate.nameCount

    override val isAbsolute: Boolean
        get() = delegate.isAbsolute

    // the two exceptions below are truly exceptional, as symbolic links should generally not show up in zip files
    // (the java zip file system doesn't even support handling symbolic links in zip files), and a file being neither
    // of the branches should generally never happen. so if any of the unhappy branches match, a crash of the program
    // is probably the best solution rather than trying to fix it, as something is very wrong if the unhappy branches
    // match.
    override val resource: Resource
        get() = when {
            delegate.notExists() -> ConcreteNil(this)
            delegate.isRegularFile() -> File(this)
            delegate.isDirectory() -> Directory(this)
            // TODO: replace 'error' with some sort of 'FatallyMalformedEpubException'
            delegate.isSymbolicLink() -> error("Symbolic link encountered at '$delegate', symbolic links are not supported in EPUB files, EPUB file may be corrupt.")
            else -> error("File at '$delegate' definitely exists but is neither a file, directory or symbolic link.")
        }

    override val exists: Boolean
        get() = delegate.exists()

    override val notExists: Boolean
        get() = delegate.notExists()

    override fun get(index: Int): Path = createPath(delegate.getName(index))

    override fun resolve(other: Path): Path = createPath(delegate.resolve(other.delegate))

    override fun resolveSibling(other: Path): Path = createPath(delegate.resolveSibling(other.delegate))

    override fun relativize(other: Path): Path = createPath(delegate.relativize(other.delegate))

    override fun startsWith(other: Path): Boolean = delegate.startsWith(other.delegate)

    override fun endsWith(other: Path): Boolean = delegate.endsWith(other.delegate)

    override fun absolute(): Path = if (isAbsolute) this else createPath(delegate.toAbsolutePath())

    override fun normalize(): Path = createPath(delegate.normalize())

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ConcretePath -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = delegate.toString()

    override fun iterator(): Iterator<Path> {
        val iterator = delegate.iterator()
        return object : Iterator<Path> {
            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): Path = createPath(iterator.next())
        }
    }

    override fun compareTo(other: Path): Int = delegate.compareTo(other.delegate)

    private fun createPath(path: JPath): Path = ConcretePath(path, fileSystem)
}

internal val Path.delegate: JPath get() = asConcrete().delegate

internal fun Path.asConcrete(): ConcretePath = this.safeCast()

@PublishedApi
internal fun Path(delegate: JPath, fileSystem: EpubFileSystem): Path = ConcretePath(delegate, fileSystem)

@Suppress("FunctionName")
internal fun EpubPath(delegate: JPath, fileSystem: EpubFileSystem): Path = Path(delegate, fileSystem)