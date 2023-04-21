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
import dev.epubby.opf.metadata.Opf3Meta
import dev.epubby.property.Property
import kotlin.reflect.KType

@Epub3Feature
internal sealed interface Opf3MetaConverter<out T : Any> {
    fun decodeFromString(value: String): T

    fun encodeToString(value: @UnsafeVariance T): String

    fun create(
        value: @UnsafeVariance T,
        property: Property,
        scheme: Property?,
        refines: String?,
        identifier: String?,
        direction: ReadingDirection?,
        language: String?,
    ): Opf3Meta<@UnsafeVariance T>

    fun getType(): KType
}