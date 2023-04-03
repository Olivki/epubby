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

import dev.epubby.dublincore.CreativeRole
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object CreativeRoleSerializer : KSerializer<CreativeRole> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("dev.epubby.dublincore.CreativeRole", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CreativeRole = CreativeRole.create(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: CreativeRole) {
        encoder.encodeString(value.code)
    }
}