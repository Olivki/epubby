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

import java.nio.file.Files
import java.nio.file.Path as JPath

typealias EpubPath = Path

sealed interface Path : Iterable<Path>, Comparable<Path> {
    /**
     * The parent of `this` path, or `null` if `this` path has no parent.
     */
    val parent: Path?

    val name: String

    val pathName: Path

    /**
     * The number of elements `this` path represents.
     *
     * @see [JPath.getNameCount]
     */
    val size: Int

    /**
     * Whether `this` path is definitely absolute.
     *
     * @see [JPath.isAbsolute]
     */
    val isAbsolute: Boolean

    val fileSystem: EpubFileSystem

    val resource: Resource

    /**
     * Returns `true` if the file located by `this` path definitely exists, otherwise `false`.
     *
     * @see [Files.exists]
     */
    val exists: Boolean

    /**
     * Returns `true` if the file located by `this` path definitely does not exist, otherwise `false`.
     *
     * @see [Files.notExists]
     */
    val notExists: Boolean

    /**
     * Returns the [pathName] of the element at [index].
     *
     * @see [JPath.getName]
     */
    operator fun get(index: Int): Path

    /**
     * Resolves [other] against `this` path.
     *
     * @see [JPath.resolve]
     */
    fun resolve(other: Path): Path

    /**
     * Resolves [other] against `this` path.
     *
     * @see [JPath.resolve]
     */
    fun resolve(other: String): Path = resolve(fileSystem.getPath(other))

    /**
     * Resolves [other] against the [parent] of `this` path. If `parent` is `null` then `other` is returned.
     *
     * @see [JPath.resolveSibling]
     */
    fun resolveSibling(other: Path): Path = parent?.resolve(other) ?: other

    /**
     * Resolves [other] against the [parent] of `this` path.
     *
     * @see [JPath.resolveSibling]
     */
    fun resolveSibling(other: String): Path = resolveSibling(fileSystem.getPath(other))

    /**
     * Returns a relative path between `this` and [other].
     *
     * @see [JPath.relativize]
     */
    fun relativize(other: Path): Path

    /**
     * Returns `true` if `this` starts with [other], otherwise `false`.
     *
     * @see [JPath.startsWith]
     */
    fun startsWith(other: Path): Boolean

    /**
     * Returns `true` if `this` starts with [other], otherwise `false`.
     *
     * @see [JPath.startsWith]
     */
    fun startsWith(other: String): Boolean = startsWith(fileSystem.getPath(other))

    /**
     * Returns `true` if `this` ends with [other], otherwise `false`.
     *
     * @see [JPath.endsWith]
     */
    fun endsWith(other: Path): Boolean

    /**
     * Returns `true` if `this` ends with [other], otherwise `false`.
     *
     * @see [JPath.endsWith]
     */
    fun endsWith(other: String): Boolean = endsWith(fileSystem.getPath(other))

    /**
     * Returns a new [Path] that represents the absolute path of `this` path.
     *
     * @see [JPath.toAbsolutePath]
     */
    fun absolute(): Path

    /**
     * Returns a new [Path] that represents `this` path with redundant name elements removed.
     *
     * @see [JPath.normalize]
     */
    fun normalize(): Path
}