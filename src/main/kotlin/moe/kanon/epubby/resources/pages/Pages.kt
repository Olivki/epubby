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

package moe.kanon.epubby.resources.pages

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.ResourceReference
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.getValueOrThrow
import moe.kanon.kommons.collections.isEmpty
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Element

class Pages internal constructor(val book: Book) : Iterable<Page> {
    private val pages: MutableMap<Identifier, Page> = LinkedHashMap()

    val transformers: PageTransformers = PageTransformers(book)

    /**
     * Returns a linked-map containing all the [pages][Page] used by the [book].
     */
    val entries: ImmutableMap<Identifier, Page> get() = pages.toImmutableMap()

    @JvmSynthetic
    internal fun populateFromSpine() {
        logger.debug { "Populating book pages with entries from the book spine.." }
        val pageResources = book
            .resources
            .asSequence()
            .filterIsInstance<PageResource>()
            .sortedBy { resource ->
                // there is no guarantee that every 'PageResource' will be referenced in the spine
                val ref = book.spine.getReferenceOfOrNull(resource)
                return@sortedBy ref?.let { book.spine.references.indexOf(it) }
            }
            .filterNotNull()
            .map(Page.Companion::fromResource)
            .associateBy { it.resource.identifier }
        pages.putAll(pageResources)
        logger.debug { "Book pages have been successfully populated." }
    }

    // TODO: Add functions for adding pages from strings and the kotlinx.html library, and make sure that those things
    //       also create a new page resource

    /**
     * Returns the [Page] stored under the given [identifier], or throws a [NoSuchElementException] if none is found.
     */
    fun getPage(identifier: Identifier): Page =
        pages.getValueOrThrow(identifier) { "No page found associated with the identifier '$identifier'" }

    /**
     * Returns the [Page] stored under the given [identifier], or `null` if none is found.
     */
    fun getPageOrNull(identifier: Identifier): Page? = pages[identifier]

    /**
     * Returns a list containing all [Element]s that have an `href`/`src` reference to the given [resource], or an
     * empty list if no such elements are found.
     */
    fun getReferencesOf(resource: Resource): ImmutableList<ResourceReference> {
        // fix for epubs where things like the images are stored in the same directory as the xhtml files
        // meaning that the src attribute can be completely relative
        // TODO: Implement a better system for handling this?
        fun predicate(attr: Attribute): Boolean = if ('/' in attr.value) {
            resource.href in attr.value
        } else attr.value.equals(resource.file.fileName.toString(), true) // should we really be ignoring case here?

        return pages
            .values
            .asSequence()
            .map { it.document.allElements }
            .flatten()
            .filterNot { it.attributes().isEmpty }
            .filter { it.attributes().any(::predicate) }
            .map { ResourceReference(it, it.attributes().first(::predicate)) }
            .asIterable()
            .toImmutableList()
    }

    @JvmSynthetic
    internal fun writeAllPages() {
        logger.debug { "Saving all page files.." }
        for ((_, page) in pages) page.writeToFile()
        logger.debug { "Finished saving all page files." }
    }

    override fun iterator(): Iterator<Page> = pages.values.iterator().asUnmodifiable()
}