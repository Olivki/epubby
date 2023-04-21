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
import dev.epubby.marc.CreativeRole
import dev.epubby.opf.metadata.Opf3MetaCreativeRole
import dev.epubby.property.Property
import dev.epubby.property.ResolvedProperty
import net.ormr.epubby.internal.InternalOpfElement
import net.ormr.epubby.internal.Properties
import net.ormr.epubby.internal.identifierDelegate
import net.ormr.epubby.internal.opf.OpfImpl

@OptIn(Epub3Feature::class)
internal class Opf3MetaCreativeRoleImpl(
    override var value: CreativeRole,
    override var property: Property,
    override var refines: String? = null,
    identifier: String? = null,
    override var direction: ReadingDirection? = null,
    override var language: String? = null,
) : Opf3MetaCreativeRole, InternalOpfElement {
    override var identifier: String? by identifierDelegate(identifier)

    override val scheme: ResolvedProperty
        get() = Properties.MARC_RELATORS

    override var opf: OpfImpl? = null

    override fun getValueAsString(): String = value.code

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Opf3MetaCreativeRoleImpl -> false
        value != other.value -> false
        property != other.property -> false
        refines != other.refines -> false
        direction != other.direction -> false
        language != other.language -> false
        identifier != other.identifier -> false
        scheme != other.scheme -> false
        else -> opf == other.opf
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + property.hashCode()
        result = 31 * result + (refines?.hashCode() ?: 0)
        result = 31 * result + (direction?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + scheme.hashCode()
        result = 31 * result + (opf?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Opf3MetaCreativeRoleImpl(value=$value, property=$property, refines=$refines, direction=$direction, language=$language, identifier=$identifier, scheme=$scheme)"
}