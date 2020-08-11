/*
 * Copyright 2019-2020 Oliver Berg
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

package dev.epubby

import dev.epubby.metainf.MetaInf
import dev.epubby.packages.PackageDocument
import dev.epubby.packages.PackageManifest
import dev.epubby.packages.PackageMetadata
import dev.epubby.packages.PackageSpine
import dev.epubby.resources.ResourceRepository
import java.io.Closeable
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path

/**
 * Represents an EPUB file.
 *
 * @property [version] The version of the EPUB specification that this book adheres to.
 * @property [fileSystem] TODO
 */
class Book internal constructor(
    val version: BookVersion,
    val fileSystem: FileSystem
) : Closeable {
    val resources: ResourceRepository = ResourceRepository(this)

    @set:JvmSynthetic
    lateinit var metaInf: MetaInf
        internal set

    @set:JvmSynthetic
    lateinit var packageDocument: PackageDocument
        internal set

    val metadata: PackageMetadata
        get() = packageDocument.metadata

    val manifest: PackageManifest
        get() = packageDocument.manifest

    val spine: PackageSpine
        get() = packageDocument.spine

    /**
     * The root directory of the book.
     *
     * Normally only the `mimetype` file and the `META-INF` and `OEBPS` *(may not always be named `OEBPS`)* directories
     * should be located at the root of a book. Any *direct* changes *(i.e; [Files.delete], [Files.move])* to any of
     * these files is ***highly discouraged***, as that can, and most likely will, cause severe issues for the system.
     */
    val root: Path
        get() = fileSystem.getPath("/")

    /**
     * The primary title of this book.
     */
    var title: String
        get() = TODO()
        set(value) {
            TODO()
        }

    /**
     * The primary author of this book, or `null` if no primary author is defined.
     */
    var author: String?
        get() = TODO()
        set(value) {
            TODO()
        }

    /**
     * The primary language of this book.
     */
    var language: String
        get() = TODO()
        set(value) {
            TODO()
        }

    /**
     * Closes the [fileSystem] belonging to this book, signaling the end of any and all modification to the book.
     */
    override fun close() {
        fileSystem.close()
    }
}