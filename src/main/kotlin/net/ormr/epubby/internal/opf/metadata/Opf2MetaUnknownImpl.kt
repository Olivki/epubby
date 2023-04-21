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

import dev.epubby.Epub3LegacyFeature
import dev.epubby.opf.metadata.Opf2MetaUnknown
import dev.epubby.xml.XmlAttribute
import net.ormr.epubby.internal.opf.OpfImpl

@OptIn(Epub3LegacyFeature::class)
internal class Opf2MetaUnknownImpl(
    override val extraAttributes: MutableList<XmlAttribute>,
) : Opf2MetaUnknown, Opf2MetaImpl {
    override var opf: OpfImpl? = null

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Opf2MetaUnknownImpl -> false
        extraAttributes != other.extraAttributes -> false
        else -> opf == other.opf
    }

    override fun hashCode(): Int {
        var result = extraAttributes.hashCode()
        result = 31 * result + (opf?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Opf2MetaUnknownImpl(extraAttributes=$extraAttributes)"
}