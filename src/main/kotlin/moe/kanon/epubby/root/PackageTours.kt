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

package moe.kanon.epubby.root

import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.EpubDeprecated
import moe.kanon.epubby.EpubRemoved
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.kommons.collections.asUnmodifiable
import org.jdom2.Element
import java.nio.file.Path

/**
 * Represents the [tours](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.5) element.
 *
 * As this element was already deprecated in v2.0.1 of the EPUB specification, the chance of coming across a book
 * containing this is *very* slim. It is solely here for the sake of consistency.
 */
@EpubRemoved(Book.Format.EPUB_3_0)
@EpubDeprecated(Book.Format.EPUB_2_0)
class PackageTours private constructor(val book: Book, private val tours: MutableMap<String, Tour>) : ElementSerializer,
    Iterable<PackageTours.Tour> {
    companion object {
        internal fun parse(book: Book, packageDocument: Path, tours: Element) = with(tours) {
            fun malformed(reason: String, cause: Throwable? = null): Nothing =
                raiseMalformedError(book.originFile, packageDocument, reason, cause)

            val tourElements = getChildren("tour", namespace)
                .asSequence()
                .map { tour ->
                    Tour(
                        tour.getAttributeValue("id") ?: malformed("'tour' element is missing required 'id' attribute"),
                        tour.getAttributeValue("title")
                            ?: malformed("'tour' element is missing required 'title' attribute"),
                        tour.getChildren("site", tour.namespace)
                            .map { site ->
                                Tour.Site(
                                    site.getAttributeValue("href")
                                        ?: malformed("'site' element is missing required 'href' attribute"),
                                    site.getAttributeValue("title")
                                        ?: malformed("'site' element is missing required 'title' attribute")
                                )
                            }
                            .ifEmpty { malformed("'tour' elements need to contain at least one 'site' element") }
                            .toMutableList()
                    )
                }
                .associateBy { it.identifier }
                .toMutableMap()

            return@with PackageTours(book, tourElements)
        }
    }

    // TODO: Add functions for modifying tours and tour (lol)

    override fun toElement(): Element = Element("tours", PackageDocument.NAMESPACE).apply {
        for (tour in tours) addContent(tour.value.toElement())
    }

    override fun iterator(): Iterator<Tour> = tours.values.iterator().asUnmodifiable()

    /**
     * Represents the `tour` element.
     *
     * > Much as a tour-guide might assemble points of interest into a set of sightseers' tours, a content provider
     * could assemble selected parts of a publication into a set of tours to enable convenient navigation
     */
    data class Tour(
        @SerializedName("id") val identifier: String, val title: String,
        private val sites: MutableList<Site>
    ) :
        ElementSerializer, Iterable<Tour.Site> {
        override fun toElement(): Element = Element("tour", PackageDocument.NAMESPACE).apply {
            setAttribute("id", identifier)
            setAttribute("title", this@Tour.title)
            for (site in sites) addContent(site.toElement())
        }

        override fun iterator(): Iterator<Site> = sites.iterator().asUnmodifiable()

        // nesting, hell yeah
        /**
         * Represents the `site` element.
         */
        data class Site(val href: String, val title: String) : ElementSerializer {
            override fun toElement(): Element = Element("site", PackageDocument.NAMESPACE).apply {
                setAttribute("href", href)
                setAttribute("title", title)
            }
        }
    }
}