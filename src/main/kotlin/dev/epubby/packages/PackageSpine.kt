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

package dev.epubby.packages

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.page.Page
import dev.epubby.resources.LocalResource
import dev.epubby.resources.NcxResource
import dev.epubby.resources.ResourceDocumentReference
import dev.epubby.utils.PageProgressionDirection
import dev.epubby.utils.attributes
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import krautils.collections.asUnmodifiable
import krautils.collections.asUnmodifiableList

class PackageSpine internal constructor(
    override val epub: Epub,
    private val _pages: MutableList<Page> = mutableListOf(),
    var identifier: String? = null,
    var pageProgressionDirection: PageProgressionDirection? = null,
    val tableOfContents: NcxResource,
) : EpubElement, Iterable<Page> {
    override val elementName: String
        get() = "PackageSpine"

    val pages: List<Page>
        get() = _pages.asUnmodifiableList()

    fun prependPage(page: Page) {
        _pages.add(0, page)
    }

    fun appendPage(page: Page) {
        _pages.add(page)
    }

    fun insertPageAt(index: Int, page: Page) {
        _pages.add(index, page)
    }

    fun setPageAt(index: Int, page: Page) {
        _pages[index] = page
    }

    fun getPage(index: Int): Page = _pages[index]

    fun getPageOrNull(index: Int): Page? = _pages.getOrNull(index)

    fun removePage(page: Page): Boolean = _pages.remove(page)

    fun removeAt(index: Int): Page = _pages.removeAt(index)

    /*
     * Adds the underlying `resource` of the given `page` to the resources of the epub if it is not already in it.
     *
     * This is because when we add a new page to this "repository" we also need to add it the spine of the epub, and
     * spine entries require a manifest-item to actually reference, meaning that we need to make sure that the `page`
     * is properly registered as a resource as that is what handles the creation of manifest-items.
     */
    private fun addToResources(page: Page) {
        if (page.reference !in epub.manifest) {
            epub.manifest.addLocalResource(page.reference)
        }
    }

    /**
     * Returns a list of HTML entities that are referencing the given [resource] in some manner.
     *
     * The returned list will become stale the moment any change is done to the [file][LocalResource.file] of the
     * `resource`, or if the document the reference heralds from gets changed in any manner, therefore it is not
     * recommended to cache the returned list, instead one should retrieve a new one whenever needed.
     */
    fun getDocumentReferencesOf(resource: LocalResource): PersistentList<ResourceDocumentReference> =
        _pages.asSequence()
            .flatMap { it.document.allElements }
            .filter { it.attributes.any { (_, value) -> value.endsWith(resource.file.name) } }
            .map { element ->
                val attributes = element.attributes
                    .asList()
                    .filter { (_, value) -> value.endsWith(resource.file.name) }
                    .toPersistentList()

                ResourceDocumentReference(element, attributes)
            }
            .asIterable()
            .toPersistentList()

    override fun iterator(): Iterator<Page> = _pages.iterator().asUnmodifiable()

    @JvmSynthetic
    operator fun plusAssign(page: Page) {
        appendPage(page)
    }

    @JvmSynthetic
    operator fun minusAssign(page: Page) {
        removePage(page)
    }

    @JvmSynthetic
    operator fun set(index: Int, page: Page) {
        setPageAt(index, page)
    }

    @JvmSynthetic
    operator fun get(index: Int): Page = getPage(index)

    @JvmSynthetic
    internal fun writePagesToFile() {
        for (page in pages) {
            page.writeToFile()
        }
    }
}