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

import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.properties.Properties
import dev.epubby.resources.PageResource
import moe.kanon.kommons.io.paths.newInputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import java.io.IOException

class Page private constructor(
    val document: Document,
    val resource: PageResource,
    // TODO: update things that reference this 'identifier' ?
    var identifier: String? = null,
    var isLinear: Boolean = true,
    // TODO: validate that this is empty if version is lower than 3.0 at some point ?
    val properties: Properties = Properties.empty()
) : BookElement {
    override val book: Book
        get() = resource.book

    override val elementName: String
        get() = "PackageSpine.Page"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Page -> false
        document != other.document -> false
        resource != other.resource -> false
        identifier != other.identifier -> false
        isLinear != other.isLinear -> false
        properties != other.properties -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = document.hashCode()
        result = 31 * result + resource.hashCode()
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + isLinear.hashCode()
        result = 31 * result + properties.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun fromResource(
            resource: PageResource,
            isLinear: Boolean = true,
            identifier: String? = null,
            properties: Properties = Properties.empty()
        ): Page {
            check(resource in resource.book.resources)
            val file = resource.file
            val document = file.newInputStream().use { Jsoup.parse(it, "UTF-8", file.toUri().toString()) }
            setupOutputSettings(document)
            return Page(document, resource, identifier, isLinear, properties)
        }

        private fun setupOutputSettings(document: Document) {
            document.outputSettings().apply {
                prettyPrint(true)
                syntax(Document.OutputSettings.Syntax.xml)
                escapeMode(Entities.EscapeMode.xhtml)
                indentAmount(2)
            }
        }
    }
}