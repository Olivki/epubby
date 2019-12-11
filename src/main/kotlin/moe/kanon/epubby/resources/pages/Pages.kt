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
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.packages.Spine
import moe.kanon.epubby.packages.contains
import moe.kanon.epubby.packages.get
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.ResourceReference
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.isEmpty
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Element
import java.nio.file.FileSystem
import java.nio.file.Path

class Pages internal constructor(val book: Book) : Iterable<Page> {
    private val pages: MutableList<Page> = mutableListOf()

    val transformers: PageTransformers = PageTransformers(book)

    /**
     * Returns a linked-map containing all the [pages][Page] used by the [book].
     */
    val entries: ImmutableList<Page> get() = pages.toImmutableList()

    /**
     * Sorts all the pages registered here by their index in the book [spine][Spine].
     *
     * Note that if the [book] has been created via parsing, then all registered pages in here will have already been
     * sorted by their index in the `spine` upon the creation of the `book`.
     */
    fun sortPagesBySpine() {
        logger.debug { "Sorting pages by their position in the book spine.." }
        synchronized(pages) {
            pages.sortBy { page ->
                val ref = book.spine.getReferenceOfOrNull(page.resource)
                ref?.let { book.spine.references.indexOf(it) }
            }
        }
        logger.debug { "Page sorting is done." }
    }

    // TODO: Add functions for adding pages from strings and the kotlinx.html library, and make sure that those things
    //       also create a new page resource

    // TODO: Make sure to document that adding a page to this "repo" will also add that page to the book spine if it is
    //       not in it, and add it to the resources of the book if it also is not in it

    fun addPage(page: Page): Page {
        pages += page
        // we want to add the underlying 'resource' of the given 'page' to the resources of the book, because when a
        // page gets added here that means it should also create an entry in the book spine, and the book spine
        // requires a manifest-item, and a manifest-item will be created and added to the manifest when a resource
        // gets added to the book resources
        if (page.resource !in book.resources) book.resources.addResource(page.resource)
        if (page.resource !in book.spine) book.spine.addReferenceFor(page)
        logger.trace { "Added page <$page> to the spine" }
        return page
    }

    fun addPage(index: Int, page: Page): Page {
        pages.add(index, page)
        // we want to add the underlying 'resource' of the given 'page' to the resources of the book, because when a
        // page gets added here that means it should also create an entry in the book spine, and the book spine
        // requires a manifest-item, and a manifest-item will be created and added to the manifest when a resource
        // gets added to the book resources
        if (page.resource !in book.resources) book.resources.addResource(page.resource)
        if (page.resource !in book.spine) book.spine.addReferenceFor(page)
        logger.trace { "Added page <$page> to the spine, at index $index" }
        return page
    }

    fun removePage(page: Page) {
        if (page in pages) {
            pages -= page

            if (page.resource in book.spine) {
                book.spine._references -= book.spine[page.resource]
                logger.trace { "Removed <$page> from pages & book spine" }
            } else {
                logger.error { "Page <$page> had an entry in 'Pages' but no entry in the book spine, this should NEVER be the case" }
            }
        }
    }

    // this will throw a index-out-of-bounds exception if index is out of bounds
    fun removePageAt(index: Int) {
        pages.removeAt(index)
        // TODO: Is it safe to assume that these will always be at the same index? Might want to do some sanity checks, just in case
        book.spine._references.removeAt(index)
        logger.trace { "Removed page at index <$index> from pages and book spine" }
    }

    /**
     * Returns the `index` of the given [page], compliant with the [spine][Spine] of the [book], or `-1` if the given
     * `page` does not have an entry in the `spine` of the `book`.
     */
    fun indexOf(page: Page): Int = pages.indexOf(page)

    fun getPage(index: Int): Page = pages[index]

    fun getPageOrNull(index: Int): Page? = pages.getOrNull(index)

    /**
     * Returns the first page that has a [resource][Page.resource] that matches the given [resource], or throws a
     * [NoSuchElementException] if none is found.
     */
    // TODO: Rename to 'getPageFor'?
    fun getPage(resource: PageResource): Page = getPageOrNull(resource)
        ?: throw NoSuchElementException("No page found associated with the resource '$resource'")

    /**
     * Returns the first page that has a [resource][Page.resource] that matches the given [resource], `null` if none is
     * found.
     */
    fun getPageOrNull(resource: PageResource): Page? = pages.firstOrNull { it.resource == resource }

    /**
     * Returns the first page that has a [resource][Page.resource] with an [identifier][Resource.identifier] that
     * matches the given [identifier], or throws a [NoSuchElementException] if none is found.
     */
    fun getPage(identifier: Identifier): Page = getPageOrNull(identifier)
        ?: throw NoSuchElementException("No page found associated with the identifier '$identifier'")

    /**
     * Returns the first page that has a [resource][Page.resource] with an [identifier][Resource.identifier] that
     * matches the given [identifier], or `null` if none is found.
     */
    fun getPageOrNull(identifier: Identifier): Page? = pages.firstOrNull { it.resource.identifier == identifier }

    fun hasPage(resource: PageResource): Boolean = pages.any { it.resource == resource }

    // -- UTILITY FUNCTIONS -- \\
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
            .asSequence()
            .map { it.document.allElements }
            .flatten()
            .filterNot { it.attributes().isEmpty }
            .filter { it.attributes().any(::predicate) }
            .map { ResourceReference(it, it.attributes().first(::predicate)) }
            .asIterable()
            .toImmutableList()
    }

    override fun iterator(): Iterator<Page> = pages.iterator().asUnmodifiable()

    @JvmSynthetic
    internal fun writePagesToFile(fileSystem: FileSystem) {
        transformers.transformPages()
        logger.debug { "Starting the writing process of all pages to their respective files.." }
        for (page in pages) {
            page.writeToFile(fileSystem)
        }
    }

    // TODO: Rework this one because we're changing how pages are being added to the system.
    @JvmSynthetic
    internal fun populateFromSpine(spine: Spine) {
        logger.debug { "Populating and sorting page instances for the book from the spine.." }
        val pageResources = book
            .resources
            .asSequence()
            .filterIsInstance<PageResource>()
            .sortedBy { resource ->
                // there is no guarantee that every 'PageResource' will be referenced in the spine
                val ref = spine.getReferenceOfOrNull(resource)
                return@sortedBy ref?.let { spine.references.indexOf(it) }
            }
            .filterNotNull()
            .map { Page.fromResource(it) }
        pages.addAll(pageResources)
    }
}