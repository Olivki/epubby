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

package dev.epubby.internal.models.toc

import dev.epubby.internal.models.SerializedName
import dev.epubby.utils.*
import kotlinx.collections.immutable.PersistentList
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.nio.file.Path
import kotlin.io.path.writeText

internal class NavigationDocumentModel internal constructor(
    private val file: Path,
    internal val tocNav: Navigation,
    internal val pageListNav: Navigation?,
    internal val landmarksNav: Navigation?,
    internal val customNavs: PersistentList<Navigation>
) {
    internal fun writeToFile() {
        val document = toDocument(file.toUri().toString())
        file.writeText(document.toOuterHtml())
    }

    private fun toDocument(baseUri: String): Document = documentShell(baseUri, true).also {
        it.title = "Table of Contents"
        it.body.also { body ->
            body.appendChild(tocNav.toElement())

            if (pageListNav != null) body.appendChild(pageListNav.toElement())
            if (landmarksNav != null) body.appendChild(landmarksNav.toElement())

            for (customNav in customNavs) {
                body.appendChild(customNav.toElement())
            }
        }
    }

    @SerializedName("nav")
    internal data class Navigation internal constructor(
        internal val header: Element? = null,
        @SerializedName("ol")
        internal val orderedList: OrderedList,
        @SerializedName("epub:type")
        internal val type: String,
        @SerializedName("hidden")
        internal val isHidden: Boolean,
        internal val attributes: Attributes
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = Element("nav").also {
            it.attributes.addAll(attributes)
            it.setAttribute("epub:type", type)
            if (isHidden) it.setAttribute("hidden", "hidden")

            if (header != null) it.appendChild(header)

            it.appendChild(orderedList.toElement())
        }
    }

    // represents the 'ol' element that can be used in the 'Navigation' class
    // TODO: apparently this can have an 'epub:type' attribute too, 'li' might also be able to have them? check later
    @SerializedName("ol")
    internal data class OrderedList internal constructor(
        // needs to have at least ONE element
        internal val entries: PersistentList<ListItem>,
        internal val attributes: Attributes
    ) {
        internal fun toElement(): Element = Element("ol").also {
            it.attributes.addAll(attributes)

            for (entry in entries) {
                it.appendChild(entry.toElement())
            }
        }
    }

    // represents the 'li' element that can be used in the 'OrderedList' class
    // ordered-list is "conditionally required"
    @SerializedName("li")
    internal data class ListItem internal constructor(
        internal val content: Content,
        @SerializedName("ol")
        internal val orderedList: OrderedList?,
        internal val attributes: Attributes
    ) {
        internal fun toElement(): Element = Element("li").also {
            it.attributes.addAll(attributes)
            it.appendChild(content.toElement())

            if (orderedList != null) it.appendChild(orderedList.toElement())
        }
    }

    // represents the 'span' and 'a' elements that can be used in a 'EntryList' class
    internal sealed class Content {
        internal abstract val phrasingContent: Element
        internal abstract val attributes: Attributes

        internal abstract fun toElement(): Element

        // TODO: Change 'href' to a hard-link to a resource so that we can properly re-serialize data later on
        @SerializedName("a")
        internal data class Link internal constructor(
            internal val href: String,
            override val phrasingContent: Element,
            override val attributes: Attributes
        ) : Content() {
            override fun toElement(): Element = Element("a").also {
                // TODO: Change to hard-link to a resource? Remember that this can contain fragment-identifiers
                it.attributes.addAll(attributes)
                it.setAttribute("href", href)
                it.appendChild(phrasingContent.unwrap() ?: TextNode(""))
                //insertChildren(0, phrasingContent.children()) // TODO: does this work properly?
            }
        }

        @SerializedName("span")
        internal data class Span internal constructor(
            override var phrasingContent: Element,
            override val attributes: Attributes
        ) : Content() {
            override fun toElement(): Element = Element("span").also {
                it.attributes.addAll(attributes)
                it.appendChild(phrasingContent.unwrap() ?: TextNode(""))
                //insertChildren(0, phrasingContent.children()) // TODO: does this work properly?
            }
        }
    }

    internal companion object
}