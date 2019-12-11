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
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.StyleSheetResource
import moe.kanon.epubby.utils.applyDefaultOutputSettings
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.paths.writeString
import moe.kanon.kommons.requireThat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path

class Page private constructor(val book: Book, val document: Document, val resource: PageResource) {
    val file: Path get() = resource.file

    /**
     * The [title][Document.title] of the [document].
     */
    var title: String
        get() = document.title()
        set(value) {
            document.title(value)
        }

    /**
     * Returns the [head][Document.head] element of this page.
     */
    val head: Element get() = document.head()

    /**
     * Returns the [body][Document.body] element of this page.
     */
    val body: Element get() = document.body()

    /**
     * Returns a list containing all the known [StyleSheet] instances used by this page.
     *
     * Note that not *all* stylesheets referenced in the actual [document] may be represented in here, as some may
     * be external stylesheets, which are not supported, or they may be pointing towards a non-existent file.
     */
    val styleSheets: ImmutableList<StyleSheetResource>
        get() = head.select("link[rel=stylesheet]")
            .asSequence()
            .map { element -> book.resources.firstOrNull { it.isHrefEqual(element.attr("href")) } }
            .filterNotNull()
            .filterIsInstance<StyleSheetResource>()
            .asIterable()
            .toImmutableList()

    /**
     * Adds the given [styleSheet] to this page, at the given [index].
     *
     * If this page has no stylesheets, then the `index` will be ignored, if the `index` is greater than the amount of
     * stylesheets available, then `styleSheet` will be appended as the *last* element.
     */
    fun addStyleSheet(styleSheet: StyleSheetResource, index: Int) {
        requireThat(index >= 0, "index >= 0")
        val html = """"<link rel="stylesheet" type="text/css" href="${styleSheet.relativeHref}">"""
        val styleSheets = head.select("link[rel=stylesheet]")
        if (styleSheets.isNotEmpty()) {
            when {
                index == 0 -> styleSheets.first().before(html)
                index > (styleSheets.size - 1) -> styleSheets.last().after(html)
                else -> styleSheets[index].before(html)
            }
        } else {
            head.append(html)
        }
    }

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
        head.select("link[rel=stylesheet]")
            .asSequence()
            .filter { element -> styleSheet.isHrefEqual(element.attr("href")) }
            .forEach { it.remove() }
    }

    fun hasStyleSheet(styleSheet: StyleSheetResource): Boolean = styleSheet in styleSheets

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        logger.trace { "Writing contents of page <$this> to file <$file>.." }
        fileSystem.getPath(file.toString()).writeString(document.outerHtml())
    }

    @JvmSynthetic
    operator fun component1(): Document = document

    @JvmSynthetic
    operator fun component2(): Element = head

    @JvmSynthetic
    operator fun component3(): Element = body

    companion object {
        /**
         * Returns a new [Page] instance wrapped around the given [resource].
         */
        @JvmStatic
        @Throws(IOException::class)
        fun fromResource(resource: PageResource): Page {
            val file = resource.file
            val document = file.newInputStream().use { Jsoup.parse(it, "UTF-8", file.toAbsolutePath().toString()) }
            document.applyDefaultOutputSettings()
            return Page(resource.book, document, resource)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun fromString(contents: String): Page = TODO()

        @JvmStatic
        @Throws(IOException::class)
        fun fromHtml(contents: String): Page = TODO()
    }
}