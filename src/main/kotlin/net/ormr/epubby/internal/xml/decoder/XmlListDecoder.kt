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

package net.ormr.epubby.internal.xml.decoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.ormr.epubby.internal.xml.XmlTag
import org.jdom2.Element

@OptIn(ExperimentalSerializationApi::class)
internal class XmlListDecoder(
    private val children: List<Element>,
    override val serializersModule: SerializersModule,
) : XmlDecoder() {
    private var currentIndex = 0

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> {
            // TODO: verify names or some shit
            val element = children[currentIndex - 1]
            XmlElementDecoder(element, serializersModule)
        }
        StructureKind.LIST -> throw SerializationException("Nested lists aren't supported.")
        StructureKind.MAP -> TODO("map")
        else -> throw SerializationException("Unsupported kind: ${descriptor.kind}")
    }

    override fun decodeTaggedValue(tag: XmlTag): Any =
        throw SerializationException("Primitives are not allowed in lists (tag: $tag)")

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (currentIndex == children.size) return CompositeDecoder.DECODE_DONE
        return currentIndex++
    }

    fun getElementName(index: Int): String = children[index].name
}