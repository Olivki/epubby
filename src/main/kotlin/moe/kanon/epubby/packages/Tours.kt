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

package moe.kanon.epubby.packages

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.internal.Namespaces
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.malformed
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.getValueOrThrow
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path

/**
 * Represents the [tours](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.5) element.
 *
 * The `tours` element was deprecated in EPUB [v2.0.1][].
 */
// As this element was already deprecated in v2.0.1 of the EPUB specification, the chance of coming across a book
// containing this is *very* slim.
class Tours private constructor(val book: Book, private val tours: MutableMap<Identifier, Tour>) :
    Iterable<Tours.Tour> {
    val entries: ImmutableMap<Identifier, Tour> get() = tours.toImmutableMap()

    // TODO: Should a tour already existing with the same identifier be considered exceptional behaviour?

    fun addTour(tour: Tour) {
        val identifier = tour.identifier
        if (identifier !in tours) {
            tours[identifier] = tour
            logger.debug { "Added tour <$tour> to book <$book>" }
        } else {
            logger.error { "There already exists a tour with the same identifier as the given one. (existing=${tours[identifier]}, given=$tour)" }
        }
    }

    fun removeTour(identifier: Identifier) {
        if (identifier in tours) {
            val tour = tours[identifier]
            tours -= identifier
            logger.debug { "Removed tour <$tour> from book <$book>" }
        } else {
            logger.error { "There exists no tour with the given identifier <$identifier>" }
        }
    }

    fun getTour(identifier: Identifier): Tour =
        tours.getValueOrThrow(identifier) { "No 'tour' found with the identifier '$identifier'" }

    fun getTourOrNull(identifier: Identifier): Tour? = tours[identifier]

    fun hasTour(identifier: Identifier): Boolean = identifier in tours

    fun hasTour(tour: Tour): Boolean = tours.containsValue(tour)

    override fun iterator(): Iterator<Tour> = tours.values.iterator().asUnmodifiable()

    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("tours", namespace).apply {
        for (tour in tours) {
            addContent(tour.value.toElement(namespace))
        }
    }

    data class Tour(val identifier: Identifier, val title: String, private val sites: MutableList<Site>) :
        Iterable<Tour.Site> {

        fun addSite(site: Site) {
            sites += site
            logger.debug { "Added site <$site> to tour <$this>" }
        }

        fun removeSite(site: Site) {
            if (site in sites) {
                sites -= site
                logger.debug { "Removed site <$site> from tour <$this>" }
            } else {
                logger.error { "The given site <$site> does not belong to this tour <$this>" }
            }
        }

        fun getSiteAt(index: Int): Site = sites[index]

        fun getSiteAtOrNull(index: Int): Site? = sites.getOrNull(index)

        fun hasSite(site: Site): Boolean = site in sites

        override fun iterator(): Iterator<Site> = sites.iterator().asUnmodifiable()

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("tour", namespace).apply {
            setAttribute(identifier.toAttribute(namespace = namespace))
            setAttribute("title", title)
            sites.forEach { addContent(it.toElement(namespace)) }
        }

        // TODO: Change 'href' to path?
        data class Site(val href: String, val title: String) {
            @JvmSynthetic
            internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("site", namespace).apply {
                setAttribute("href", href)
                setAttribute("title", title)
            }
        }
    }

    companion object {
        // -- PARSING -- \\
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, documentFile: Path): Tours = with(element) {
            val tourElements = getChildren("tour", namespace)
                .asSequence()
                .map { createTour(element, book.file, documentFile) }
                .associateByTo(LinkedHashMap()) { it.identifier }
            return Tours(book, tourElements)
        }

        private fun createTour(element: Element, container: Path, current: Path): Tour = with(element) {
            val identifier = Identifier.fromElement(element, container, current)
            val title = attr("title", container, current)
            val sites = getChildren("site", namespace).mapTo(ArrayList()) { createTourSite(it, container, current) }
            if (sites.isEmpty()) {
                malformed(container, current, "'tour' elements need to contain at least one 'site' element")
            }
            return Tour(identifier, title, sites)
        }

        private fun createTourSite(element: Element, container: Path, current: Path): Tour.Site = with(element) {
            val href = attr("href", container, current)
            val title = attr("title", container, current)
            return Tour.Site(href, title)
        }
    }
}