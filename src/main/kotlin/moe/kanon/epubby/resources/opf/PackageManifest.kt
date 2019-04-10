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

package moe.kanon.epubby.resources.opf

import arrow.core.Option
import moe.kanon.epubby.Book
import moe.kanon.epubby.resources.HREF

/**
 * Implementation of the [manifest](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.3) specification.
 *
 * Note that the layout of the manifest will change depending on the [format][Book.format] used by the specified `book`.
 *
 * @property [container] The [OPF document][PackageDocument] that `this` manifest belongs to.
 * @property [book] The [Book] instance that `this` manifest is bound to.
 */
class PackageManifest internal constructor(val container: PackageDocument, val book: Book = container.book) {
    private val elements: MutableMap<String, Item> = LinkedHashMap()
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageManifest -> false
        container != other.container -> false
        book != other.book -> false
        elements != other.elements -> false
        else -> true
    }
    
    override fun hashCode(): Int {
        var result = container.hashCode()
        result = 31 * result + book.hashCode()
        result = 31 * result + elements.hashCode()
        return result
    }
    
    override fun toString(): String = "Manifest{${elements.values.joinToString()}}"
    
    data class Item internal constructor(
        val parent: PackageManifest,
        val id: String,
        val href: HREF,
        val mediaType: String,
        val fallback: Option<String> = Option.empty(),
        val properties: Option<String> = Option.empty(),
        val mediaOverlay: Option<String> = Option.empty()
    ) {
        override fun toString(): String = "Item[id=\"$id\", href=\"${href.get()}\", media-type=\"$mediaType\"]"
    }
}