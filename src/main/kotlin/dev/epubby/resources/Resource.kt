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

package dev.epubby.resources

import com.google.common.net.MediaType
import dev.epubby.Book
import dev.epubby.packages.PackageDocument
import dev.epubby.properties.Properties
import dev.epubby.utils.verifyFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentSet
import moe.kanon.kommons.io.paths.contentType
import moe.kanon.kommons.io.paths.isSameAs
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.requireFileExistence
import moe.kanon.kommons.reflection.KServiceLoader
import moe.kanon.kommons.reflection.loadServices
import java.io.IOException
import java.nio.file.Path
import kotlin.properties.Delegates

abstract class Resource(val book: Book, identifier: String, file: Path) {
    companion object {
        private val locators: KServiceLoader<ResourceLocator> = loadServices()

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun fromFile(
            file: Path,
            book: Book,
            identifier: String = "x_${file.name}"
        ): Resource {
            require(identifier !in book.resources) { "Identifier '$identifier' is not unique" }
            verifyFile(book, file)
            val mediaType =
                requireNotNull(file.contentType?.let(MediaType::parse)) { "Media type of file '$file' is null" }
            return locators.asSequence()
                .map { it.findFactory(mediaType) }
                .filterNotNull()
                .firstOrNull()
                ?.invoke(book, identifier, file, mediaType) ?: MiscResource(book, identifier, file, mediaType)
        }
    }

    abstract val mediaType: MediaType

    var file: Path = file
        @Throws(IOException::class)
        set(value) {
            val oldFile = field
            verifyFile(book, file)
            require(!(value.parent isSameAs book.root)) { "must not be root of book" }
            // TODO: updateReferencesTo(value)
            field = value
            onFileChanged(oldFile, value)
        }

    protected open fun onFileChanged(oldFile: Path, newFile: Path) {}

    /**
     * Returns a path that's relative to the [OPF file][PackageDocument.file] of the [book].
     */
    val relativeFile: Path
        get() = book.packageDocument.file.relativize(file)

    val href: String
        get() = book.packageDocument.directory.relativize(file).toString()

    val relativeHref: String
        get() = relativeFile.toString()

    // TODO: narrow the type down to a more general set, like EnumSet<ManifestVocabulary> ?
    open val properties: Properties = Properties.empty()

    /**
     * The resource that a reading system should use instead if it can't properly display `this` resource.
     */
    var fallback: Resource? = null

    var identifier: String by Delegates.observable(identifier) { _, old, new ->
        book.resources.updateIdentifier(this, old, new)
    }

    val references: ImmutableList<ResourceReference>
        get() = book.spine.getReferencesOf(this)

    abstract fun <R : Any> accept(visitor: ResourceVisitor<R>): R

    final override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Resource -> false
        mediaType != other.mediaType -> false
        file != other.file -> false
        properties != other.properties -> false
        fallback != other.fallback -> false
        else -> true
    }

    final override fun hashCode(): Int {
        var result = mediaType.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + (fallback?.hashCode() ?: 0)
        return result
    }
}