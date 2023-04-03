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
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder
import net.ormr.epubby.internal.xml.XmlTag
import net.ormr.epubby.internal.xml.getXmlTag

@OptIn(InternalSerializationApi::class)
internal sealed class XmlDecoder : TaggedDecoder<XmlTag>() {
    private var currentIndex = 0

    override fun SerialDescriptor.getTag(index: Int): XmlTag = getXmlTag(this, index)

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (currentIndex == descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
        return currentIndex++
    }

    fun findTag(): XmlTag = currentTag
}