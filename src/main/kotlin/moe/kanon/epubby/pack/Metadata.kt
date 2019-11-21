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

package moe.kanon.epubby.pack

import com.google.common.net.MediaType
import moe.kanon.epubby.Book
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.structs.props.Relationship
import moe.kanon.epubby.utils.Namespaces
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path

class Metadata private constructor(val book: Book) {

    // TODO: Check if this namespace is correct
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element {
        TODO()
    }

    data class Meta(val something: String)

    /**
     * Represents the [link](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-opf-link)
     * element.
     *
     * Linked resources are not [Publication Resources](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-publication-resource)
     * and *MUST NOT* be listed in the [manifest][Manifest]. A linked resource *MAY* be embedded in a
     * `Publication Resource` that is listed in the `manifest`, however, in which case it *MUST* be a
     * [Core Media Type Resource](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-core-media-types)
     * *(e.g., an EPUB Content Document could contain a metadata record serialized as
     * [RDFA-CORE](https://www.w3.org/TR/rdfa-core/) or [JSON-LD](https://www.w3.org/TR/json-ld/)).*
     *
     * @property [href] TODO
     * @property [mediaType] TODO
     * @property [relation] TODO
     * @property [identifier] TODO
     * @property [properties] TODO
     * @property [refines] TODO
     */
    data class Link @JvmOverloads constructor(
        var href: String,
        var relation: Relationship,
        var mediaType: MediaType? = null,
        var identifier: Identifier? = null,
        var properties: Properties? = null,
        var refines: String? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("link", namespace).apply {
            setAttribute("href", href)
            setAttribute(relation.toAttribute("rel", namespace))
            mediaType?.also { setAttribute("media-type", it.toString()) }
            identifier?.also { setAttribute(it.toAttribute(namespace)) }
            properties?.also { setAttribute(it.toAttribute(namespace = namespace)) }
            refines?.also { setAttribute("refines", it) }
        }

        companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element, onMalformed: (String) -> Nothing): Link = element.run {
                Link(
                    getAttributeValue("href") ?: onMalformed("'link' element is missing 'href' attribute"),
                    getAttributeValue("rel")?.let { Properties.parse(Link::class, it) }
                        ?: onMalformed("'link' element is missing 'rel' attribute"),
                    getAttributeValue("media-type")?.let(MediaType::parse),
                    getAttributeValue("id")?.let(::Identifier),
                    getAttributeValue("properties")?.let { Properties.parse(Link::class, it) },
                    getAttributeValue("refines")
                )
            }
        }
    }

    companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, file: Path): Metadata = TODO()
    }
}