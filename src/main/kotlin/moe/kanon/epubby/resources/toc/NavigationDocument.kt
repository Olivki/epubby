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

package moe.kanon.epubby.resources.toc

import moe.kanon.epubby.Book
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.structs.NonEmptyList
import moe.kanon.epubby.structs.toNonEmptyList
import moe.kanon.epubby.utils.matches
import moe.kanon.epubby.utils.parseHtmlFile
import moe.kanon.kommons.io.paths.writeString
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Path

/**
 * Represents the [navigation document](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-package-nav)
 * introduced in [EPUB 3.0][_BookVersion.EPUB_3_0].
 */
class NavigationDocument private constructor(
    var file: Path,
    val document: Document,
    tocNav: Navigation,
    var pageListNav: Navigation?,
    var landmarksNav: Navigation?,
    val customNavs: MutableList<Navigation>
) {
    var tocNav: Navigation = tocNav
        private set

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        updateDocument()
        val target = fileSystem.getPath(file.toString())
        logger.debug { "Writing navigation-document to file '$target'.." }
        target.writeString(document.outerHtml())
    }

    private fun updateDocument() {
        document.body().apply {
            // remove all the previously set 'nav' elements
            select("nav").remove()
            // populate the document 'body' with our own implementations
            appendChild(tocNav.toElement())
            pageListNav?.also { appendChild(it.toElement()) }
            landmarksNav?.also { appendChild(it.toElement()) }
            customNavs.forEach { appendChild(it.toElement()) }
        }
    }

    data class Navigation(
        var header: Element?,
        val orderedList: OrderedList,
        val type: String, // TODO: make this not user-specifiable? But there can be custom types so idk
        var isHidden: Boolean,
        val attributes: Attributes
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = Element("nav").apply {
            attr("epub:type", type)
            if (isHidden) attr("hidden", "hidden")
            attributes().addAll(attributes)
            header?.also { appendChild(it) }
            appendChild(orderedList.toElement())
        }
    }

    // represents the 'ol' element that can be used in the 'Navigation' class
    // TODO: apparently this can have an 'epub:type' attribute too, 'li' might also be able to have them? check later
    data class OrderedList internal constructor(val entries: NonEmptyList<ListItem>, val attributes: Attributes) {
        @JvmSynthetic
        internal fun toElement(): Element = Element("ol").apply {
            attributes().addAll(attributes)
            entries.forEach { appendChild(it.toElement()) }
        }
    }

    // represents the 'li' element that can be used in the 'OrderedList' class
    // ordered-list is "conditionally required"
    data class ListItem internal constructor(
        val content: Content,
        val orderedList: OrderedList?,
        val attributes: Attributes
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = Element("li").apply {
            attributes().addAll(attributes)
            appendChild(content.toElement())
            orderedList?.also { appendChild(it.toElement()) }
        }
    }

    // represents the 'span' and 'a' elements that can be used in a 'EntryList' class
    sealed class Content {
        abstract var phrasingContent: Element
        abstract val attributes: Attributes

        @JvmSynthetic
        internal abstract fun toElement(): Element

        // TODO: Change 'href' to a hard-link to a resource so that we can properly re-serialize data later on
        data class Link internal constructor(
            var href: URI,
            override var phrasingContent: Element,
            override val attributes: Attributes
        ) : Content() {
            //fun getPath(book: Book): Path = book.packageRoot.resolve(href.path)

            //fun toResource(book: Book): Resource = book.resources.getResourceByFile(getPath(book))

            fun toResource(book: Book): Resource = book.resources.getResourceByHref(href.path)

            @JvmSynthetic
            override fun toElement(): Element = Element("a").apply {
                // TODO: Change to hard-link to a resource? Remember that this can contain fragment-identifiers
                attr("href", href.toString())
                attributes().addAll(attributes)
                appendChild(phrasingContent.unwrap() ?: TextNode(""))
                //insertChildren(0, phrasingContent.children()) // TODO: does this work properly?
            }
        }

        data class Span internal constructor(
            override var phrasingContent: Element,
            override val attributes: Attributes
        ) : Content() {
            @JvmSynthetic
            override fun toElement(): Element = Element("span").apply {
                attributes().addAll(attributes)
                appendChild(phrasingContent.unwrap() ?: TextNode(""))
                //insertChildren(0, phrasingContent.children()) // TODO: does this work properly?
            }
        }
    }

    internal companion object {
        @JvmSynthetic
        fun fromFile(epub: Path, file: Path): NavigationDocument = parseHtmlFile(file) { doc ->
            with(doc.body()) {
                val tocNav = selectFirst("nav[epub:type=toc]")
                    ?.let { createNavigation(it, epub, file) }
                    ?: malformed(epub, file, "missing required 'toc' nav type")
                val pageListNav = selectFirst("nav[epub:type=page-list]")?.let { createNavigation(it, epub, file) }
                val landmarksNav = selectFirst("nav[epub:type=landmarks]")?.let { createNavigation(it, epub, file) }
                val customNavs = select("nav[epub:type]")
                    .map { createNavigation(it, epub, file) }
                    .filterNotTo(mutableListOf()) { it.type == "toc" || it.type == "page-list" || it.type == "landmarks" }
                return NavigationDocument(file, doc, tocNav, pageListNav, landmarksNav, customNavs)
            }
        }

        private fun createNavigation(element: Element, epub: Path, file: Path): Navigation {
            val header = element.selectFirst("h1, h2, h3, h4, h5, h6")
            val orderedList = element.selectFirst("ol")
                ?.let { createOrderedList(element, epub, file) }
                ?: malformed(epub, file, "missing required 'ol' element; '$element'")
            val type = element.attr("epub:type") ?: malformed(epub, file, "missing required 'epub:type' attribute")
            val isHidden = element.hasAttr("hidden")
            val attributes = element.attributes().clone().apply { remove("epub:type") }
            return Navigation(header, orderedList, type, isHidden, attributes)
        }

        private fun createOrderedList(element: Element, epub: Path, file: Path): OrderedList {
            val entries = element
                .select("li")
                .mapNotNull { createListItem(it, epub, file) }
                .ifEmpty { malformed(epub, file, "missing required 'li' elements") }
                .toNonEmptyList()
            return OrderedList(entries, element.attributes())
        }

        private fun createListItem(element: Element, epub: Path, file: Path): ListItem? {
            val content: Content? = try {
                val firstChild = element.children()[0]
                when {
                    firstChild matches "a" -> createContentLink(firstChild, epub, file)
                    firstChild matches "span" -> createContentSpan(firstChild)
                    else -> {
                        logger.error { "First child of 'li' element in 'nav' document '$element' is not 'a' or 'span'" }
                        null
                    }
                }
            } catch (e: IndexOutOfBoundsException) {
                logger.error { "'li' element in 'nav' document '$element' has no children" }
                null
            }
            val orderedList = element.selectFirst("ol")?.let { createOrderedList(it, epub, file) }
            val attributes = element.attributes()
            return content?.let { ListItem(content, orderedList, attributes) }
        }

        private fun createContentLink(element: Element, epub: Path, file: Path): Content.Link {
            val rawHref = element.attr("href") ?: malformed(epub, file, "nav document 'a' element is missing required 'href' attribute")
            val href = URI(rawHref)
            // TODO: Do some error handling in case 'element' does not have any children
            val attributes = element.attributes()
            return Content.Link(href, element.clone(), attributes)
        }

        private fun createContentSpan(element: Element): Content.Span {
            // TODO: Do some error handling in case 'element' does not have any children
            val attributes = element.attributes()
            return Content.Span(element.clone(), attributes)
        }
    }
}