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

package dev.epubby.packages

import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.page.Page
import dev.epubby.resources.LocalResource
import dev.epubby.resources.PageResource
import dev.epubby.resources.ResourceDocumentReference
import dev.epubby.utils.PageProgressionDirection
import dev.epubby.utils.attributes
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.asUnmodifiableList
import moe.kanon.kommons.io.paths.name
import org.jsoup.nodes.Attribute

// TODO: make sure to redirect the user to the PageRepository for modifying pages.
class PackageSpine @JvmOverloads constructor(
    override val book: Book,
    var identifier: String? = null,
    var pageProgressionDirection: PageProgressionDirection? = null,
    private var tableOfContentsIdentifier: String? = null
) : BookElement, Iterable<Page> {
    override val elementName: String
        get() = "PackageSpine"

    var tableOfContents: PageResource?
        get() = TODO()
        set(value) {
            TODO()
        }

    private val _pages: MutableList<Page> = mutableListOf()

    val page: List<Page>
        get() = _pages.asUnmodifiableList()

    fun prepend(page: Page) {
        _pages.add(0, page)
    }

    fun append(page: Page) {
        _pages.add(page)
    }

    fun insert(index: Int, page: Page) {
        _pages.add(index, page)
    }

    operator fun set(index: Int, page: Page) {
        _pages[index] = page
    }

    operator fun get(index: Int): Page = _pages[index]

    fun getOrNull(index: Int): Page? = _pages.getOrNull(index)

    fun remove(page: Page): Boolean = _pages.remove(page)

    fun removeAt(index: Int): Page = _pages.removeAt(index)

    /*
     * Adds the underlying `resource` of the given `page` to the resources of the book if it is not already in it.
     *
     * This is because when we add a new page to this "repository" we also need to add it the spine of the book, and
     * spine entries require a manifest-item to actually reference, meaning that we need to make sure that the `page`
     * is properly registered as a resource as that is what handles the creation of manifest-items.
     */
    private fun addToResources(page: Page) {
        if (page.resource !in book.manifest) {
            book.manifest.addLocalResource(page.resource)
        }
    }

    /**
     * Returns a list of HTML entities that are referencing the given [resource] in some manner.
     *
     * The returned list will become stale the moment any change is done to the [file][LocalResource.file] of the
     * `resource`, or if the document the reference heralds from gets changed in any manner, therefore it is not
     * recommended to cache the returned list, instead one should retrieve a new one whenever needed.
     */
    fun getDocumentReferencesOf(resource: LocalResource): PersistentList<ResourceDocumentReference> {
        // fix for epubs where things like the images are stored in the same directory as the xhtml files
        // meaning that the src attribute can be completely relative
        // TODO: Implement a better system for handling this
        fun predicate(attr: Attribute): Boolean = when {
            '/' in attr.value -> resource.href in attr.value
            // TODO: should we really be ignoring case here?
            else -> attr.value.equals(resource.file.name, true)
        }

        return _pages
            .asSequence()
            .flatMap { it.document.allElements }
            .filter { it.attributes.any(::predicate) }
            .map { ResourceDocumentReference(it, it.attributes.first(::predicate)) }
            .asIterable()
            .toPersistentList()
    }

    override fun iterator(): Iterator<Page> = _pages.iterator().asUnmodifiable()

    @JvmSynthetic
    operator fun plusAssign(page: Page) {
        append(page)
    }

    @JvmSynthetic
    operator fun minusAssign(page: Page) {
        remove(page)
    }
}