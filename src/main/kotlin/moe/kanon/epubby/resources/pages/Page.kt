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
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.utils.applyDefaultOutputSettings
import moe.kanon.kommons.io.paths.newInputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
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

    // TODO: Some more stuff yo

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
    }
}