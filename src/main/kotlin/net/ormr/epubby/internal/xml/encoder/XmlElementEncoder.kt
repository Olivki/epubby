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

package net.ormr.epubby.internal.xml.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.ormr.epubby.internal.util.addAdditionalNamespaces
import net.ormr.epubby.internal.xml.QName
import net.ormr.epubby.internal.xml.XmlElementSerializer
import net.ormr.epubby.internal.xml.XmlTag
import org.jdom2.Element
import org.jdom2.Namespace

@OptIn(ExperimentalSerializationApi::class)
internal class XmlElementEncoder(
    private val element: Element,
    override val serializersModule: SerializersModule,
) : XmlEncoder() {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = when (val kind = descriptor.kind) {
        is StructureKind -> {
            val tag = currentTag
            val namespace = tag.namespace ?: element.namespace
            val additionalNamespaces = tag.additionalNamespaces
            when (kind) {
                StructureKind.CLASS, StructureKind.OBJECT -> {
                    val child = createChild(currentTag.name, namespace)
                    child.addAdditionalNamespaces(additionalNamespaces)
                    XmlElementEncoder(child, serializersModule)
                }
                StructureKind.LIST -> {
                    val elementsName = tag.elementsName
                    when (val wrapperName = tag.listWrapperElementName) {
                        null -> XmlListEncoder(elementsName, element, serializersModule)
                        else -> {
                            val parent = createChild(wrapperName, namespace)
                            parent.addAdditionalNamespaces(additionalNamespaces)
                            XmlListEncoder(elementsName, parent, serializersModule)
                        }
                    }
                }
                StructureKind.MAP -> TODO("map")
            }
        }
        else -> throw SerializationException("Unsupported kind: ${descriptor.kind}")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        when (val tag = currentTagOrNull) {
            null -> encodeSerializable(serializer, value)
            else -> when {
                tag.isAttributeOverflowTarget -> {
                    val attributes = value as Map<QName, String>
                    for ((qualifiedName, text) in attributes) {
                        element.setAttribute(qualifiedName.name, text, qualifiedName.namespace)
                    }
                }
                else -> encodeSerializable(serializer, value)
            }
        }
    }

    private fun <T> encodeSerializable(serializer: SerializationStrategy<T>, value: T) {
        when (serializer) {
            is XmlElementSerializer<T> -> serializer.serializeElement(this, element, value)
            else -> serializer.serialize(this, value)
        }
    }

    // don't encode elements with default values
    override fun shouldEncodeElement(descriptor: SerialDescriptor, index: Int): Boolean =
        !descriptor.isElementOptional(index)

    override fun encodeNull() {
        // null values are represented as missing attributes
    }

    override fun encodeTaggedValue(tag: XmlTag, value: Any) {
        val textValue = tag.textValue
        if (textValue != null) {
            // TODO: apply normalization here?
            element.text = value.toString()
        } else {
            element.setAttribute(tag.name, value.toString(), tag.namespace ?: element.namespace)
        }
    }

    override fun encodeTaggedEnum(tag: XmlTag, enumDescriptor: SerialDescriptor, ordinal: Int) {
        element.setAttribute(tag.name, enumDescriptor.getElementName(ordinal), tag.namespace ?: element.namespace)
    }

    private fun createChild(name: String, namespace: Namespace): Element {
        val element = Element(name, namespace)
        this.element.addContent(element)
        return element
    }
}