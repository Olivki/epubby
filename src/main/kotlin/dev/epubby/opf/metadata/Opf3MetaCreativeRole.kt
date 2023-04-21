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
import dev.epubby.marc.CreativeRole
import dev.epubby.property.Property
import dev.epubby.property.ResolvedProperty
import net.ormr.epubby.internal.opf.metadata.Opf3MetaCreativeRoleImpl

@Epub3Feature
public interface Opf3MetaCreativeRole : Opf3Meta<CreativeRole> {
    override val scheme: ResolvedProperty
}

@Epub3Feature
@JvmName("newCreativeRole")
public fun Opf3MetaCreativeRole(
    value: CreativeRole,
    property: Property,
    refines: String? = null,
    identifier: String? = null,
    direction: ReadingDirection? = null,
    language: String? = null,
): Opf3MetaCreativeRole = Opf3MetaCreativeRoleImpl(
    value = value,
    property = property,
    refines = refines,
    identifier = identifier,
    direction = direction,
    language = language,
)