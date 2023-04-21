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
import dev.epubby.opf.metadata.Opf2MetaName
import dev.epubby.xml.XmlAttribute
import net.ormr.epubby.internal.opf.OpfImpl

@OptIn(Epub3LegacyFeature::class)
internal class Opf2MetaNameImpl(
    override var scheme: String?,
    override var name: String,
    override var content: String,
    override val extraAttributes: MutableList<XmlAttribute>,
) : Opf2MetaName, Opf2MetaImpl {
    override var opf: OpfImpl? = null

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Opf2MetaNameImpl -> false
        scheme != other.scheme -> false
        name != other.name -> false
        content != other.content -> false
        extraAttributes != other.extraAttributes -> false
        else -> opf == other.opf
    }

    override fun hashCode(): Int {
        var result = scheme?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + extraAttributes.hashCode()
        result = 31 * result + (opf?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Opf2MetaNameImpl(scheme=$scheme, name='$name', content='$content', extraAttributes=$extraAttributes)"
}