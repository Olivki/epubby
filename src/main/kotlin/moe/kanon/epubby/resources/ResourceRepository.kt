/*
 * Copyright 2019 Oliver Berg
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

package moe.kanon.epubby.resources

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableHashMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.logger
import moe.kanon.epubby.root.ManifestItem
import moe.kanon.epubby.root.PackageManifest
import moe.kanon.kommons.collections.asUnmodifiable

class ResourceRepository(val book: Book) : Iterable<Resource> {
    private val _resources: MutableMap<String, Resource> = HashMap()

    /**
     * Returns a [ImmutableMap] containing all the resources of the [book], mapped in the following fashion;
     * `identifier:resource`.
     */
    val resources: ImmutableMap<String, Resource> get() = _resources.toImmutableHashMap()

    /**
     * Populates this repository with the entries from the [manifest][PackageManifest] of the [book].
     */
    @JvmSynthetic internal fun populateFromManifest() {
        logger.info { "Populating resource repository..." }
        val localResources = book.packageManifest.items.values
            .asSequence()
            .filterIsInstance<ManifestItem.Local>()
        val knownResources = localResources
            .filter { it.mediaType.isPresent }
            .map {
                val href = it.href
                val id = it.identifier
                return@map when (it.mediaType.value) {
                    in TableOfContentsResource.MEDIA_TYPES -> TableOfContentsResource(book, href, id)
                    in PageResource.MEDIA_TYPES -> PageResource(book, href, id)
                    in StyleSheetResource.MEDIA_TYPES -> StyleSheetResource(book, href, id)
                    in ImageResource.MEDIA_TYPES -> ImageResource(book, href, id)
                    in FontResource.MEDIA_TYPES -> FontResource(book, href, id)
                    in AudioResource.MEDIA_TYPES -> AudioResource(book, href, id)
                    in ScriptResource.MEDIA_TYPES -> ScriptResource(book, href, id)
                    in VideoResource.MEDIA_TYPES -> VideoResource(book, href, id)
                    else -> {
                        // TODO: change back to 'debug' level
                        logger.error { "Item <$it> has been marked as a 'MiscResource'" }
                        MiscResource(book, href, id)
                    }
                }
            }
        val unknownResources = localResources
            .filter { it.mediaType.isEmpty }
            .onEach { logger.warn { "Item <$it> does not have a 'mediaType', will be marked as a 'MiscResource'" } }
            .map { MiscResource(book, it.href, it.identifier) }
        val allResources = (knownResources + unknownResources).associateByTo(HashMap()) { it.identifier }
        _resources.putAll(allResources)
        logger.info { "Resource repository successfully populated!" }
    }

    override fun iterator(): Iterator<Resource> = _resources.values.iterator().asUnmodifiable()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ResourceRepository -> false
        book != other.book -> false
        _resources != other._resources -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + _resources.hashCode()
        return result
    }

    override fun toString(): String = "ResourceRepository(book=$book, resources=$_resources)"
}