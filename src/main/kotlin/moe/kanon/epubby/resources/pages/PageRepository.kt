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
import moe.kanon.epubby.logger
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.ResourceReference
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.isEmpty
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.getValueOrNone
import org.jsoup.nodes.Attribute
import java.io.IOException

class PageRepository(val book: Book) : Iterable<Page> {
    private val _pages: MutableMap<String, Page> = LinkedHashMap()

    val transformers: PageTransformerRepository = PageTransformerRepository(book)

    /**
     * Returns a linked-map containing all the [pages][Page] used by the [book].
     */
    val pages: ImmutableMap<String, Page> get() = _pages.toImmutableMap()

    /**
     * Fills the entries of this repository with all the known [PageResource]s, and sorts them after their order in the
     * [spine][PackageSpine] of the [book].
     */
    @JvmSynthetic internal fun populateRepository() {
        logger.info { "Populating page repository..." }
        val pageResources = book.resources
            .asSequence()
            .filterIsInstance<PageResource>()
            .sortedBy {
                val ref = book.spine.getReference(it.identifier)
                return@sortedBy book.spine.references.indexOf(ref)
            }
            .map { Page.fromResource(it) }
            .associateBy { it.resource.identifier }
        _pages.putAll(pageResources)
        logger.info { "Page repository successfully populated!" }
    }

    // -- PAGES -- \\
    /**
     * Returns the [Page] stored under the given [id], or throws a [NoSuchElementException] if none is found.
     */
    fun getPage(id: String): Page = _pages[id] ?: throw NoSuchElementException("No page found under key <$id>")

    /**
     * Returns the [Page] stored under the given [id], or [None] if none is found.
     */
    fun getPageOrNone(id: String): Option<Page> = _pages.getValueOrNone(id)

    /**
     * Attempts to save all the pages of this repository to their respective files.
     *
     * @throws [IOException] if an i/o error occurs
     */
    @Throws(IOException::class)
    @JvmSynthetic internal fun savePages() {
        logger.debug { "Saving page files.." }
        for ((_, page) in _pages) page.savePage()
    }

    // -- OTHER -- \\
    /**
     * Returns a list containing all [Element]s that have an `href`/`src` reference to the given [resource], or an
     * empty list if no such elements are found.
     */
    fun getReferencesOf(resource: Resource): ImmutableList<ResourceReference> {
        fun attributePredicate(attr: Attribute): Boolean = resource.href in attr.value
        val pages = _pages.values.asSequence()
        val elements = pages.map { it.document.allElements }.flatten().distinct()
        return elements
            .filterNot { it.attributes().isEmpty }
            .filter { it.attributes().any(::attributePredicate) }
            .map { ResourceReference(it, it.attributes().first(::attributePredicate)) }
            .toList()
            .toImmutableList()
    }

    // -- OVERRIDES -- \\
    override fun iterator(): Iterator<Page> = _pages.values.iterator().asUnmodifiable()

    override fun toString(): String =
        "PageRepository[${_pages.entries.joinToString { (key, page) -> "\"$key\" -> $page" }}]"
}

/**
 * Returns the [Page] stored under the given [key], or throws a [NoSuchElementException] if none is found.
 */
operator fun PageRepository.get(key: String): Page = this.getPage(key)