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

@file:OptIn(ExperimentalSerializationApi::class)

package net.ormr.epubby.internal.xml.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.ormr.epubby.internal.xml.XmlTag
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.Text
import kotlin.reflect.typeOf

internal class XmlElementDecoder(
    private val element: Element,
    override val serializersModule: SerializersModule,
) : XmlDecoder() {
    private var currentIndex = 0
    private var forceNull = false

    // TODO: support contextual for normal element decode
    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (currentIndex < descriptor.elementsCount) {
            val tag = descriptor.getTag(currentIndex++)
            val index = currentIndex - 1
            val name = tag.name
            val namespace = tag.namespace ?: element.namespace
            val isRequired = !descriptor.isElementOptional(index)
            forceNull = false
            when (val kind = tag.descriptor.kind) {
                is PrimitiveKind, SerialKind.ENUM -> when (tag.textValue) {
                    null -> if (element.anyAttribute(name, namespace) || absenceIsNull(descriptor, index)) {
                        return index
                    } else if (isRequired) {
                        // TODO: include namespace prefix if available
                        throw SerializationException("Missing attribute '$name' on element '${element.name}'")
                    }
                    else -> if (getOwnText(element) != null || absenceIsNull(descriptor, index)) {
                        return index
                    } else if (isRequired) {
                        throw SerializationException("Missing required text content on element '${element.name}'")
                    }
                }
                is StructureKind -> when (kind) {
                    StructureKind.LIST -> {
                        val wrapperName = tag.listWrapperElementName
                        if (wrapperName != null) {
                            if (element.anyChildWithName(wrapperName, namespace) || absenceIsNull(descriptor, index)) {
                                return index
                            } else if (isRequired) {
                                throw SerializationException("Missing wrapper element '$wrapperName' on element '${element.name}'")
                            }
                        }
                        // this is an incredibly brittle and hackish way of doing this because there's no guarantee
                        // every LIST kind always has an element at 0 like this, but for our use cases it should
                        // generally speaking always be the case
                        // TODO: duplicate this logic in 'beginStructure'
                        // TODO: only allow 1 free standing list per structure
                        val elementDescriptor = tag.descriptor.getElementDescriptor(0)
                        when (val elementKind = elementDescriptor.kind) {
                            SerialKind.CONTEXTUAL -> {
                                // 'elementsName' is not applied for contextual
                                if (elementDescriptor.elementDescriptors.any {
                                        element.anyChildWithName(
                                            it.serialName,
                                            namespace
                                        )
                                    } || absenceIsNull(
                                        descriptor,
                                        index
                                    )) {
                                    return index
                                } else if (isRequired) {
                                    val names = elementDescriptor.elementDescriptors.map { it.serialName }
                                    throw SerializationException("None of $names found on element '${element.name}'")
                                }
                            }
                            is StructureKind -> {
                                val elementName = tag.elementsName ?: elementDescriptor.serialName
                                if (element.anyChildWithName(elementName, namespace) || absenceIsNull(
                                        descriptor,
                                        index
                                    )
                                ) {
                                    return index
                                } else if (isRequired) {
                                    throw SerializationException("Missing element '$elementName' on element '${element.name}'")
                                }
                            }
                            else -> throw SerializationException("Can't decode list of kind $elementKind")
                        }
                    }
                    else -> if (element.anyChildWithName(name, namespace) || absenceIsNull(descriptor, index)) {
                        return index
                    } else if (isRequired) {
                        // TODO: include namespace prefix if available
                        throw SerializationException("Missing element '$name' on element '${element.name}'")
                    }
                }
                else -> throw SerializationException("Can't decode kind $kind from element")
            }
        }
        return CompositeDecoder.DECODE_DONE
    }

    private fun absenceIsNull(descriptor: SerialDescriptor, index: Int): Boolean {
        forceNull = !descriptor.isElementOptional(index) && descriptor.getElementDescriptor(index).isNullable
        return forceNull
    }

    // TODO: support contextual for normal element decode
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> {
            val tag = currentTag
            val namespace = tag.namespace ?: element.namespace
            val child = element.getSingleChild(tag.name, namespace)
            XmlElementDecoder(child, serializersModule)
        }
        StructureKind.LIST -> {
            val tag = currentTag
            val namespace = tag.namespace ?: element.namespace
            // TODO: use elementsName
            when (val wrapperName = tag.listWrapperElementName) {
                null -> {
                    val elementDescriptor = tag.descriptor.getElementDescriptor(0)
                    when (val elementKind = elementDescriptor.kind) {
                        SerialKind.CONTEXTUAL -> {
                            val availableNames =
                                elementDescriptor.elementDescriptors.mapTo(hashSetOf()) { it.serialName }
                            // TODO: check namespace
                            // 'elementsName' is not applied for contextual
                            val children = element
                                .children
                                .filter { it.name in availableNames && it.namespace == namespace }
                            XmlListDecoder(children, serializersModule)
                        }
                        is StructureKind -> {
                            val elementName = tag.elementsName ?: elementDescriptor.serialName
                            // TODO: check namespace
                            val children = element
                                .children
                                .filter { it.name == elementName && it.namespace == namespace }
                            XmlListDecoder(children, serializersModule)
                        }
                        else -> throw SerializationException("Can't decode list of kind $elementKind")
                    }
                }
                else -> {
                    val child = element.getSingleChild(wrapperName, namespace)
                    XmlListDecoder(child.children, serializersModule)
                }
            }
        }
        StructureKind.MAP -> TODO("map")
        else -> throw SerializationException("Unsupported kind: ${descriptor.kind}")
    }

    private fun Element.anyAttribute(name: String, namespace: Namespace): Boolean =
        getAttribute(name, namespace) != null

    // TODO: use 'getChild' with namespace or some shit
    private fun Element.anyChildWithName(name: String, namespace: Namespace): Boolean =
        getChild(name, namespace) != null

    // TODO: use 'getChild' instead? but that requires the use of namespace
    private fun Element.getSingleChild(name: String, namespace: Namespace): Element {
        val matchedElements = children.filter { it.name == name && it.namespace == namespace }
        if (matchedElements.isEmpty()) {
            throw SerializationException("Missing element '${name}'")
        }
        if (matchedElements.size > 1) {
            throw SerializationException("More than one '${name}' element found")
        }
        return matchedElements.first()
    }

    override fun decodeTaggedNotNullMark(tag: XmlTag): Boolean = !forceNull && when (val kind = tag.descriptor.kind) {
        is PrimitiveKind, SerialKind.ENUM -> when (tag.textValue) {
            null -> element.getAttribute(tag.name, tag.namespace ?: element.namespace) != null
            else -> getOwnText(element) != null
        }
        is StructureKind -> TODO("Handle nullable structures")
        else -> throw SerializationException("Can't decode not null mark for kind $kind")
    }

    // numbers
    override fun decodeTaggedByte(tag: XmlTag): Byte = parseNumber(tag, String::toByte)

    override fun decodeTaggedShort(tag: XmlTag): Short = parseNumber(tag, String::toShort)

    override fun decodeTaggedInt(tag: XmlTag): Int = parseNumber(tag, String::toInt)

    override fun decodeTaggedLong(tag: XmlTag): Long = parseNumber(tag, String::toLong)

    override fun decodeTaggedDouble(tag: XmlTag): Double = parseNumber(tag, String::toDouble)

    override fun decodeTaggedFloat(tag: XmlTag): Float = parseNumber(tag, String::toFloat)

    // others
    override fun decodeTaggedEnum(tag: XmlTag, enumDescriptor: SerialDescriptor): Int =
        enumDescriptor.getElementIndex(getValue(tag))

    override fun decodeTaggedString(tag: XmlTag): String = getValue(tag)

    override fun decodeTaggedBoolean(tag: XmlTag): Boolean = when (val value = getValue(tag)) {
        "false" -> false
        "true" -> true
        else -> throw SerializationException("Expected 'false' or 'true' got '$value'")
    }

    override fun decodeTaggedChar(tag: XmlTag): Char = TODO("support char decoding")

    private inline fun <reified T> parseNumber(tag: XmlTag, converter: String.() -> T): T {
        val value = getValue(tag)
        return try {
            converter(value)
        } catch (e: NumberFormatException) {
            throw SerializationException("Could not convert '$value' to ${typeOf<T>()}", e)
        }
    }

    private fun getValue(tag: XmlTag): String = when (val textValue = tag.textValue) {
        null -> element.getAttributeValue(tag.name, tag.namespace ?: element.namespace)
            ?: throw SerializationException("Missing attribute '${tag.name}' on element '${element.name}'")
        else -> getOwnText(element, textValue.normalize)
            ?: throw SerializationException("No text content found on element '${element.name}' for field '${tag.name}'")
    }

    // because jdom returns empty string even if no text exists and some other behavior we don't really want :-^)
    private fun getOwnText(element: Element, normalize: Boolean = false): String? = when (element.contentSize) {
        0 -> null
        1 -> {
            val child = element.content.first() as? Text
            if (normalize) child?.textNormalize else child?.text
        }
        else -> null
    }
}