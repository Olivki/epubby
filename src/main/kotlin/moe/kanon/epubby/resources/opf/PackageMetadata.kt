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
import org.jdom2.Attribute

/**
 * Implementation of the [metadata](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.2) specification.
 *
 * @property [container] The [OPF document][PackageDocument] that `this` metadata belongs to.
 * @property [book] The [Book] instance that `this` metadata is bound to.
 */
class PackageMetadata internal constructor(val container: PackageDocument, val book: Book = container.book) :
    Iterable<PackageMetadata.Element> {
    private val elements: MutableList<Element> = ArrayList()
    
    override fun iterator(): Iterator<Element> = elements.iterator()
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageMetadata -> false
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
    
    override fun toString(): String = "PackageMetadata(container=$container, book=$book, elements=$elements)"
    
    sealed class Element : Iterable<Attribute> {
        abstract val value: String
        abstract val id: Option<String>
        protected abstract val attributes: List<Attribute>
        
        override fun iterator(): Iterator<Attribute> = attributes.iterator()
        
        /**
         * TODO
         *
         * [dublin core](http://www.dublincore.org/specifications/dublin-core/dces/)
         *
         * @property value
         * @property attributes
         */
        data class DublinCore internal constructor(
            override val value: String,
            override val id: Option<String>,
            override val attributes: List<Attribute>
        ) : Element()
        
        data class Meta internal constructor(
            override val value: String,
            override val id: Option<String>,
            override val attributes: List<Attribute>
        ) : Element()
    }
}