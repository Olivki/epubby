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
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.internal.IntroducedIn
import dev.epubby.properties.Properties
import dev.epubby.resources.PageResource
import dev.epubby.page.Page
import dev.epubby.resources.Resource
import dev.epubby.resources.ResourceReference
import dev.epubby.utils.PageProgressionDirection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.asUnmodifiableList
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
        if (page.resource !in book.resources) {
            book.resources.add(page.resource)
        }
    }

    fun getReferencesOf(resource: Resource): ImmutableList<ResourceReference> {
        // fix for epubs where things like the images are stored in the same directory as the xhtml files
        // meaning that the src attribute can be completely relative
        // TODO: Implement a better system for handling this?
        fun predicate(attr: Attribute): Boolean = if ('/' in attr.value) {
            resource.href in attr.value
        } else attr.value.equals(resource.file.fileName.toString(), true) // should we really be ignoring case here?

        return _pages
            .asSequence()
            .map { it.document.allElements }
            .flatten()
            .filter { it.attributes().any() }
            .filter { it.attributes().any(::predicate) }
            .map { ResourceReference(it, it.attributes().first(::predicate)) }
            .asIterable()
            .toImmutableList()
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