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

import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.DeprecatedFeature
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.NonEmptyList
import moe.kanon.epubby.structs.toNonEmptyList
import moe.kanon.epubby.utils.attr
import moe.kanon.kommons.collections.asUnmodifiable
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path

/**
 * Represents the [tours](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.5) element.
 *
 * The `tours` element was deprecated in [EPUB v2.0.1][BookVersion.EPUB_2_0].
 */
// As this element was already deprecated in v2.0.1 of the EPUB specification, the chance of coming across a book
// containing this is *very* slim.
@DeprecatedFeature(since = BookVersion.EPUB_2_0)
class PackageTours private constructor(val book: Book, val entries: MutableMap<Identifier, Tour>) {
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("tours", namespace).apply {
        for ((_, tour) in entries) {
            addContent(tour.toElement(namespace))
        }
    }

    class Tour internal constructor(val identifier: Identifier, var title: String, val sites: NonEmptyList<Site>) :
        Iterable<Tour.Site> {

        override fun iterator(): Iterator<Site> = sites.iterator().asUnmodifiable()

        override fun toString(): String = "Tour(identifier=$identifier, title='$title', sites=$sites)"

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Tour -> false
            identifier != other.identifier -> false
            title != other.title -> false
            sites != other.sites -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = identifier.hashCode()
            result = 31 * result + title.hashCode()
            result = 31 * result + sites.hashCode()
            return result
        }

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("tour", namespace).apply {
            setAttribute(identifier.toAttribute(namespace = namespace))
            setAttribute("title", title)
            sites.forEach { addContent(it.toElement(namespace)) }
        }

        // TODO: Change 'href' to path?
        class Site internal constructor(var href: String, var title: String) {
            override fun toString(): String = "Site(href='$href', title='$title')"

            override fun equals(other: Any?): Boolean = when {
                this === other -> true
                other !is Site -> false
                href != other.href -> false
                title != other.title -> false
                else -> true
            }

            override fun hashCode(): Int {
                var result = href.hashCode()
                result = 31 * result + title.hashCode()
                return result
            }

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
        internal fun fromElement(book: Book, element: Element, file: Path): PackageTours = with(element) {
            val tourElements = getChildren("tour", namespace)
                .asSequence()
                .map { createTour(element, book.file, file) }
                .associateByTo(LinkedHashMap()) { it.identifier }
            return PackageTours(book, tourElements)
        }

        private fun createTour(element: Element, container: Path, current: Path): Tour = with(element) {
            val identifier = Identifier.fromElement(element, container, current)
            val title = attr("title", container, current)
            val sites = getChildren("site", namespace)
                .asSequence()
                .map { createTourSite(it, container, current) }
                .ifEmpty { malformed(container, current, "'tour' elements need to contain at least one 'site' element") }
                .toNonEmptyList()
            return Tour(identifier, title, sites)
        }

        private fun createTourSite(element: Element, container: Path, current: Path): Tour.Site =
            with(element) {
                val href = attr("href", container, current)
                val title = attr("title", container, current)
                return Tour.Site(href, title)
            }
    }
}