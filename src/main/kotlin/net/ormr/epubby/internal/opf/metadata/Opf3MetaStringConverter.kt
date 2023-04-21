/*
 * Copyright 2023 Oliver Berg
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

package net.ormr.epubby.internal.opf.metadata

import dev.epubby.Epub3Feature
import dev.epubby.ReadingDirection
import dev.epubby.opf.metadata.Opf3MetaString
import dev.epubby.property.Property
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@OptIn(Epub3Feature::class)
internal object Opf3MetaStringConverter : Opf3MetaConverter<String> {
    override fun decodeFromString(value: String): String = value

    override fun encodeToString(value: String): String = value

    override fun create(
        value: String,
        property: Property,
        scheme: Property?,
        refines: String?,
        identifier: String?,
        direction: ReadingDirection?,
        language: String?
    ): Opf3MetaStringImpl = Opf3MetaStringImpl(
        value = value,
        property = property,
        scheme = scheme,
        refines = refines,
        identifier = identifier,
        direction = direction,
        language = language,
    )

    override fun getType(): KType = typeOf<Opf3MetaString>()
}