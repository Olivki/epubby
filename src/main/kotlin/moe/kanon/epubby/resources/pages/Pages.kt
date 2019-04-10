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

@file:Suppress("DataClassPrivateConstructor")

package moe.kanon.epubby.resources.pages

import arrow.core.Option
import moe.kanon.epubby.Book
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.utils.defaultOutputSettings
import moe.kanon.kommons.io.extension
import moe.kanon.kommons.io.not
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.nio.file.Path

/**
 * Represents a (X)HTML file inside of the epub.
 *
 * This class allows for easier editing of HTML contents and stylesheets appended onto the page.
 *
 * @property book Internal use.
 * @property document `[jSoup][Jsoup] document used for parsing the page and changing things within it.
 * @property title The title of the document.
 * @property resource The resource representation.
 */
data class Page private constructor(
    val book: Book,
    val document: Document,
    val title: String = document.title(),
    val resource: Option<PageResource>
) {
    
    /**
     * TODO: Explain this.
     */
    var isLinear: Boolean = false
    
    /**
     * Returns the location of this pages resource, or `NIL` if [resource] is null.
     */
    val href: String get() = resource?.href ?: "NIL"
    
    init {
        document.defaultOutputSettings()
    }
    
    @Throws(WritePageException::class)
    fun saveTo(directory: Path) {
        TODO("Implement saving feature.")
    }
    
    companion object {
        /**
         * Creates a [Page] from the given [htmlFile].
         *
         * This function will also add the `htmlFile` to the resource repository if it doesn't already contain a copy
         * of it.
         *
         * @throws ReadPageException
         *
         * - If the `extension` of the `htmlFile` doesn't end with "HTML". *(Case-insensitive)*
         * - If [jSoup][Jsoup] encountered an error when trying to parse the file into a [Document].
         */
        @JvmStatic
        @Throws(ReadPageException::class)
        fun from(book: Book, htmlFile: Path): Page {
            if (!htmlFile.extension.endsWith("HTML", true)) throw ReadPageException(
                "\"$htmlFile\" is not a (X)HTML file!"
            )
            
            // Try and fetch a resource with a matching file from the repository, if none is found, then it's safe
            // to assume that this is a new file, so we just add this file as a resource to the repository.
            val resource: PageResource = book.resources.getOrNone(htmlFile) ?: book.resources.load(htmlFile)
            
            val document: Document = try {
                Jsoup.parse(!htmlFile, "UTF-8")
            } catch (e: IOException) {
                throw ReadPageException("Could not read the page file: \"$htmlFile\".", e)
            }
            
            return Page(book, document, resource = resource)
        }
    }
}