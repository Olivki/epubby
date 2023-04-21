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
import dev.epubby.KnownOpfMeta3SchemeException
import dev.epubby.ReadingDirection
import dev.epubby.opf.metadata.Opf3Meta
import dev.epubby.property.Property
import net.ormr.epubby.internal.Properties.MARC_RELATORS

@OptIn(Epub3Feature::class)
internal object Opf3MetaConverters {
    private val converters: Map<Property, Opf3MetaConverter<*>> = hashMapOf(
        MARC_RELATORS to Opf3MetaCreativeRoleConverter,
    )

    private fun getConverter(scheme: Property?): Opf3MetaConverter<*> =
        scheme?.let(converters::get) ?: Opf3MetaStringConverter

    fun checkScheme(scheme: Property) {
        val converter = converters[scheme]
        if (converter != null) {
            throw KnownOpfMeta3SchemeException("Use type '${converter.getType()}' for meta elements with scheme '${scheme.asString()}'")
        }
    }

    fun create(
        value: String,
        property: Property,
        scheme: Property?,
        refines: String?,
        identifier: String?,
        direction: ReadingDirection?,
        language: String?,
    ): Opf3Meta<*> {
        val converter = getConverter(scheme)
        val actualValue = converter.decodeFromString(value)
        return converter.create(
            value = actualValue,
            property = property,
            scheme = scheme,
            refines = refines,
            identifier = identifier,
            direction = direction,
            language = language,
        )
    }
}