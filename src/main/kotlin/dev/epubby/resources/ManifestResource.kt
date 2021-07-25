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
import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.internal.IntroducedIn
import dev.epubby.packages.metadata.Opf2Meta
import dev.epubby.properties.ManifestVocabulary
import dev.epubby.properties.Properties
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

// TODO: documentation

sealed class ManifestResource : EpubElement {
    abstract override val epub: Epub

    /**
     * The identifier of this resource.
     *
     * The identifier of a resource is what it's generally used throughout the system to refer to a specific resource,
     * however, some parts of the system may use the [href] of a resource to refer to it.
     */
    abstract val identifier: String

    /**
     * A URI pointing to the location of the file that this resource is wrapping around.
     *
     * For [LocalResource] instances this will be pointing towards a local file, while a [ExternalResource] will be
     * pointing towards an URI.
     */
    abstract val href: String

    /**
     * The [MediaType] of this resource.
     */
    abstract val mediaType: MediaType

    /**
     * The resource that a reading system should use instead if it can't properly display this resource.
     */
    abstract var fallback: ManifestResource?

    abstract val mediaOverlay: String?

    /**
     * The properties belonging to this resource.
     *
     * See [ManifestVocabulary] for a list of properties that are supported by default in an EPUB.
     */
    @IntroducedIn(version = EPUB_3_0)
    abstract val properties: Properties

    /**
     * Returns a list of [Opf2Meta.Name] instances that provide additional metadata for this resource.
     *
     * The returned list will become stale the moment any `meta` element is added or removed from the epub, or when the
     * [content][Opf2Meta.Name.content] property of the `meta` element is changed, therefore it is not recommended to
     * cache the returned list, instead one should retrieve a new one when needed.
     */
    val additionalMetadata: PersistentList<Opf2Meta.Name>
        get() = epub.metadata
            .opf2MetaEntries
            .asSequence()
            .filterIsInstance<Opf2Meta.Name>()
            .filter { it.isFor(this) }
            .asIterable()
            .toPersistentList()

    /**
     * Returns the result of invoking the appropriate `visitXXX` function of the given [visitor].
     */
    abstract fun <R> accept(visitor: ResourceVisitor<R>): R
}