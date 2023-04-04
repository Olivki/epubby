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

package net.ormr.epubby.internal.xml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jdom2.Namespace

@Serializable(with = QNameSerializer::class)
internal data class QName(val namespace: Namespace, val name: String) {
    val prefix: String?
        get() = namespace.prefix.ifEmpty { null }
}

internal object QNameSerializer : KSerializer<QName> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("net.ormr.epubby.internal.xml.QName", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): QName = error("QName should never be serialized")

    override fun serialize(encoder: Encoder, value: QName) {
        error("QName should never be serialized")
    }
}