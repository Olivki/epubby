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

import moe.kanon.epubby.Book
import moe.kanon.epubby.logger
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.styles.StyleSheet
import moe.kanon.epubby.utils.applyDefaultOutputSettings
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.paths.writeString
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Represents an [EPUB Content Document](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-epub-content-document).
 */
class Page private constructor(val book: Book, val document: Document, val resource: PageResource) {
    companion object {
        /**
         * Returns a new [Page] instance based on the given [resource].
         */
        @Throws(IOException::class)
        @JvmStatic fun fromResource(resource: PageResource): Page {
            val file = resource.file
            val document = file.newInputStream().use {
                Jsoup.parse(it, "UTF-8", file.toAbsolutePath().toString())
            }
            document.applyDefaultOutputSettings()
            return Page(resource.book, document, resource)
        }

        @Throws(IOException::class)
        @JvmStatic fun fromFile(file: Path, book: Book): Page {
            val document = file.newInputStream().use {
                Jsoup.parse(it, "UTF-8", file.toAbsolutePath().toString())
            }
            // TODO: Make it add the newly created 'Page' instance as a resource to the book
            TODO()
        }
    }

    /**
     * Returns the [file][Resource.file] of the underlying [resource] of this page.
     */
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
     * Returns the [body][Document.body] of the [document] of this page.
     */
    val body: Element get() = document.body()

    // -- STYLE-SHEETS -- \\
    fun addStyleSheet(styleSheet: StyleSheet) {
        TODO()
    }

    // -- MISC -- \\
    /**
     * Attempts to save this page back into it's `XHTML` format.
     *
     * @throws [IOException] if an i/o error occurs
     */
    @Throws(IOException::class)
    fun savePage() {
        logger.debug { "Saving page <$this> to file <$file>" }
        val text = document.outerHtml()
        file.writeString(
            text,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    // -- OVERRIDES -- \\
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Page -> false
        book != other.book -> false
        document != other.document -> false
        resource != other.resource -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + document.hashCode()
        result = 31 * result + resource.hashCode()
        return result
    }

    override fun toString(): String = "Page(title='$title', resource=$resource)"
}