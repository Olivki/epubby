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

@file:JvmMultifileClass
@file:JvmName("Opf3Metas")

package dev.epubby.opf.metadata

import dev.epubby.Epub3Feature
import dev.epubby.ReadingDirection
import dev.epubby.property.Property
import net.ormr.epubby.internal.opf.metadata.Opf3MetaStringImpl

@Epub3Feature
public interface Opf3MetaString : Opf3Meta<String>

// TODO: document that this throws if 'scheme' is a known scheme we have proper type mappings for
//       this is to prevent users from trying to escape restrictions and write faulty data
@Epub3Feature
@JvmName("newString")
public fun Opf3MetaString(
    value: String,
    property: Property,
    scheme: Property? = null,
    refines: String? = null,
    identifier: String? = null,
    direction: ReadingDirection? = null,
    language: String? = null,
): Opf3MetaString = Opf3MetaStringImpl(
    value = value,
    property = property,
    scheme = scheme,
    refines = refines,
    identifier = identifier,
    direction = direction,
    language = language,
)