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
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.internal.TaggedEncoder
import net.ormr.epubby.internal.xml.XmlTag
import net.ormr.epubby.internal.xml.getXmlTag

@OptIn(InternalSerializationApi::class)
internal sealed class XmlEncoder : TaggedEncoder<XmlTag>() {
    override fun SerialDescriptor.getTag(index: Int): XmlTag = getXmlTag(this, index)

    override fun <T : Any?> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        if (encodeElement(descriptor, index))
            encodeSerializableValue(serializer, value)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?,
    ) {
        if (encodeElement(descriptor, index))
            encodeNullableSerializableValue(serializer, value)
    }

    private fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val tag = descriptor.getTag(index)
        pushTag(tag)
        val shouldEncode = shouldEncodeElement(descriptor, index)
        // TODO: is this sound?
        if (!shouldEncode) {
            popTag()
        }
        return shouldEncode
    }

    protected open fun shouldEncodeElement(descriptor: SerialDescriptor, index: Int): Boolean = true

    fun findTag(): XmlTag = currentTag
}