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

package dev.epubby.page

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.EpubVersion
import dev.epubby.internal.IntroducedIn
import dev.epubby.packages.PackageSpine
import dev.epubby.properties.Properties
import dev.epubby.properties.vocabularies.SpineVocabulary
import dev.epubby.resources.PageResource
import dev.epubby.resources.StyleSheetResource
import dev.epubby.utils.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import moe.kanon.kommons.require
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * A [PackageSpine] entry that allows modification of it's contents.
 *
 * @property [reference] TODO
 * @property [identifier] TODO
 * @property [isLinear] TODO
 * @property [properties] TODO
 *
 * See [SpineVocabulary] for a list of properties that are available by default.
 */
class Page private constructor(
    val reference: PageResource,
    // TODO: update things that reference this 'identifier' ?
    var identifier: String? = null,
    var isLinear: Boolean = true,
    // TODO: validate that this is empty if version is lower than 3.0 at some point ?
    @IntroducedIn(version = EpubVersion.EPUB_3_0)
    val properties: Properties = Properties.empty()
) : EpubElement {
    override val epub: Epub
        get() = reference.epub

    override val elementName: String
        get() = "PackageSpine.Page"

    private var isDocumentLoaded: Boolean = false

    /**
     * The document belonging to this page.
     *
     * Any changes done to this document will be reflected when the [epub] this page belongs to is saved.
     */
    val document: Document by lazy {
        val file = reference.file
        val document = documentFrom(file.delegate)
        isDocumentLoaded = true
        setupSettings(document)
        document
    }

    /**
     * The [head][Document.head] of the [document] of this page.
     */
    val head: Element
        get() = document.head

    /**
     * The [body][Document.body] of the [document] of this page.
     */
    val body: Element
        get() = document.body

    /**
     * Returns a list of all the [StyleSheetResource]s that are used by this page.
     *
     * The returned list will become stale the moment any new stylesheet is added, modified, or changed on this page,
     * therefore it is not recommended to cache the returned list, instead one should retrieve a new one whenever
     * needed.
     *
     * Note that not *all* stylesheets that are referenced in this page may be represented in the returned list, as
     * some may be external stylesheets, or they may be pointing towards a file that does not exist.
     */
    val styleSheets: PersistentList<StyleSheetResource>
        get() = document.head
            // TODO: is this sound?
            .filter("link[rel=stylesheet], [href]")
            .asSequence()
            //.filter { it.hasAttribute("href") }
            .map { element -> epub.manifest.localResources.values.firstOrNull { it.isHrefEqual(element.getAttribute("href")!!) } }
            .filterNotNull()
            .filterIsInstance<StyleSheetResource>()
            .asIterable()
            .toPersistentList()

    /**
     * Adds the given [styleSheet] to this page, at the given [index].
     *
     * If this page has no stylesheets, then the `index` will be ignored, if the `index` is greater than the amount of
     * stylesheets available, then `styleSheet` will be appended as the *last* element.
     */
    fun addStyleSheet(styleSheet: StyleSheetResource, index: Int) {
        require(index >= 0, "index >= 0")
        val html = """"<link rel="stylesheet" type="text/css" href="${styleSheet.relativeHref}">"""
        val styleSheets = head.filter("link[rel=stylesheet]")

        if (styleSheets.isNotEmpty()) {
            when {
                index == 0 -> styleSheets.first().before(html)
                index > styleSheets.lastIndex -> styleSheets.last().after(html)
                else -> styleSheets[index].before(html)
            }
        } else {
            head.append(html)
        }
    }

    /**
     * Adds the given [styleSheet] to this page.
     */
    fun addStyleSheet(styleSheet: StyleSheetResource) {
        val html = """<link rel="stylesheet" type="text/css" href="${styleSheet.relativeHref}">"""
        val styleSheets = head.select("link[rel=stylesheet]")

        if (styleSheets.isNotEmpty()) {
            styleSheets.last().after(html)
        } else {
            head.append(html)
        }
    }

    /**
     * Removes all instances of the given [styleSheet] from this page.
     */
    fun removeStyleSheet(styleSheet: StyleSheetResource) {
        head.filter("link[rel=stylesheet]")
            .filter { it.hasAttribute("href") }
            .filter { styleSheet.isHrefEqual(it.getAttribute("href")!!) }
            .forEach { it.remove() }
    }

    // TODO: is this sound?
    fun hasStyleSheet(styleSheet: StyleSheetResource): Boolean = head.filter("link[rel=stylesheet], [href]")
        .any { styleSheet.isHrefEqual(it.getAttribute("href")!!) }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Page -> false
        document != other.document -> false
        reference != other.reference -> false
        identifier != other.identifier -> false
        isLinear != other.isLinear -> false
        properties != other.properties -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = document.hashCode()
        result = 31 * result + reference.hashCode()
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + isLinear.hashCode()
        result = 31 * result + properties.hashCode()
        return result
    }

    @JvmSynthetic
    internal fun writeToFile() {
        if (isDocumentLoaded) {
            reference.file.writeString(document.toOuterHtml())
        }
    }

    companion object {
        private val LOGGER: InlineLogger = InlineLogger(Page::class)

        @JvmSynthetic
        internal operator fun invoke(
            reference: PageResource,
            isLinear: Boolean,
            identifier: String?,
            properties: Properties
        ): Page = Page(reference, identifier, isLinear, properties)

        @JvmStatic
        @JvmOverloads
        fun fromResource(
            reference: PageResource,
            isLinear: Boolean = true,
            identifier: String? = null,
            properties: Properties = Properties.empty()
        ): Page {
            check(reference in reference.epub.manifest)
            return Page(reference, identifier, isLinear, properties)
        }
    }
}