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

package net.ormr.epubby.internal.models.dublincore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import net.ormr.epubby.internal.xml.decoder.XmlListDecoder
import net.ormr.epubby.internal.xml.encoder.XmlListEncoder
import kotlin.reflect.full.findAnnotation

// there be dragons here
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal object DublinCoreModelSerializer : KSerializer<DublinCoreModel> {
    override val descriptor: SerialDescriptor by lazy {
        buildSerialDescriptor("net.ormr.epubby.internal.models.dublincore.DublinCoreModel", SerialKind.CONTEXTUAL) {
            serialNameToSerializer.forEach { (name, serializer) ->
                element(name, serializer.descriptor)
            }
        }
    }
    private val classToSerializer by lazy {
        val dcClasses = DublinCoreModel::class.sealedSubclasses.filterNot { it.java.isInterface }
        val ldcClasses = LocalizedDublinCoreModel::class.sealedSubclasses
        val classes = dcClasses + ldcClasses
        classes.associateWith { it.serializer() }
    }
    private val serialNameToSerializer by lazy {
        classToSerializer.mapKeys { (clz, _) ->
            clz.findAnnotation<SerialName>()?.value ?: error("Missing @SerialName annotation on $clz")
        }
    }

    override fun deserialize(decoder: Decoder): DublinCoreModel {
        require(decoder is XmlListDecoder) { decoder.javaClass }
        val tag = decoder.findTag()
        val name = decoder.getElementName(tag.index)
        val serializer = serialNameToSerializer.getValue(name)
        return decoder.decodeSerializableValue(serializer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: DublinCoreModel) {
        require(encoder is XmlListEncoder) { encoder.javaClass }
        val serializer = classToSerializer.getValue(value::class) as KSerializer<DublinCoreModel>
        return encoder.encodeSerializableValue(serializer, value)
    }
}