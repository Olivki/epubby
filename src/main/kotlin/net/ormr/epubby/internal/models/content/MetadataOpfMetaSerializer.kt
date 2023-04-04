/*
 * Copyright 2019-2023 Oliver Berg
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

package net.ormr.epubby.internal.models.content

import dev.epubby.ReadingDirection
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import net.ormr.epubby.internal.Namespaces.XML_PREFIX
import net.ormr.epubby.internal.Namespaces.XML_URI
import net.ormr.epubby.internal.models.content.MetadataModel.Op3MetaModel
import net.ormr.epubby.internal.models.content.MetadataModel.Opf2MetaModel
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.getOwnText
import net.ormr.epubby.internal.util.set
import net.ormr.epubby.internal.xml.QName
import net.ormr.epubby.internal.xml.XmlElementSerializer
import net.ormr.epubby.internal.xml.XmlNamespace
import net.ormr.epubby.internal.xml.XmlTextValue
import net.ormr.epubby.internal.xml.decoder.XmlDecoder
import net.ormr.epubby.internal.xml.encoder.XmlEncoder
import org.jdom2.Element
import org.jdom2.Namespace.XML_NAMESPACE

// TODO: create a separate serializer for opf2 when the version is epub 2
internal object MetadataOpfMetaSerializer : XmlElementSerializer<MetadataModel.OpfMeta>() {
    private val opf2Attributes: Set<String> = hashSetOf("charset", "content", "http-equiv", "name", "scheme")

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("net.ormr.epubby.internal.models.content.MetadataModel.OpfMeta") {
            // opf2
            element<String?>("charset")
            element<String?>("content")
            element<String?>("http-equiv")
            element<String?>("name")
            element<String?>("scheme")

            // opf3
            element<String?>("value", listOf(XmlTextValue())) // not null
            element<String?>("property") // not null
            element<String?>("id")
            element<ReadingDirection?>("dir")
            element<String?>("refines")
            //element<String?>("scheme")
            element<String?>("lang", listOf(XmlNamespace(XML_PREFIX, XML_URI)))
        }

    override fun deserializeElement(decoder: XmlDecoder, element: Element): MetadataModel.OpfMeta = when {
        element.isOpf3() -> {
            val value = element.getOwnText(normalize = true) ?: throw SerializationException("Empty 'meta' text")
            val property = element.getAttributeValue("property")
                ?: throw SerializationException("Missing attribute 'property' on element '${element.name}'")
            val identifier = element.getAttributeValue("id")
            val direction = element.getAttributeValue("dir")?.let {
                ReadingDirection.fromValueOrNull(it) ?: throw SerializationException("Unknown 'dir' value: $it")
            }
            val refines = element.getAttributeValue("refines")
            val scheme = element.getAttributeValue("scheme")
            val language = element.getAttributeValue("lang", XML_NAMESPACE)
            Op3MetaModel(value, property, identifier, direction, refines, scheme, language)
        }
        else -> {
            val charset = element.getAttributeValue("charset")
            val content = element.getAttributeValue("content")
            val httpEquiv = element.getAttributeValue("http-equiv")
            val name = element.getAttributeValue("name")
            val scheme = element.getAttributeValue("scheme")
            val attributes = element.attributes
                .asSequence()
                .filter { it.name !in opf2Attributes }
                .associateBy({ QName(it.namespace, it.name) }, { it.value })
            Opf2MetaModel(charset, content, httpEquiv, name, scheme, attributes)
        }
    }

    override fun serializeElement(encoder: XmlEncoder, parent: Element, value: MetadataModel.OpfMeta) {
        val element = when (value) {
            is Opf2MetaModel -> buildElement("meta", parent.namespace) {
                this["charset"] = value.charset
                this["content"] = value.content
                this["http-equiv"] = value.httpEquiv
                this["name"] = value.name
                this["scheme"] = value.scheme

                for ((qualifiedName, text) in value.attributes) {
                    this[qualifiedName.name, qualifiedName.namespace] = text
                }
            }
            is Op3MetaModel -> buildElement("meta", parent.namespace) {
                this["property"] = value.property
                this["id"] = value.identifier
                this["dir"] = value.direction?.value
                this["refines"] = value.refines
                this["scheme"] = value.scheme
                this["lang", XML_NAMESPACE] = value.language
                text = value.value
            }
        }
        parent.addContent(element)
    }

    // because the geniuses decided to make the new meta element the same name as the old one, while STILL
    // supporting the use of the old meta element, we have to try and do our best to guess if a meta element is
    // actually an opf3 variant or not, how fun
    private fun Element.isOpf3(): Boolean = (getAttribute("property") != null) && (getOwnText() != null)
}