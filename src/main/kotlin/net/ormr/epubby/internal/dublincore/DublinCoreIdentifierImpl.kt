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

package net.ormr.epubby.internal.dublincore

import dev.epubby.Epub2Feature
import dev.epubby.dublincore.DublinCoreIdentifier
import net.ormr.epubby.internal.identifierDelegate
import net.ormr.epubby.internal.opf.OpfImpl

@OptIn(Epub2Feature::class)
internal class DublinCoreIdentifierImpl(
    identifier: String? = null,
    override var scheme: String? = null,
    override var content: String?,
) : DublinCoreIdentifier, DublinCoreImpl {
    override var identifier: String? by identifierDelegate(identifier)

    override var opf: OpfImpl? = null
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is DublinCoreIdentifierImpl -> false
        scheme != other.scheme -> false
        content != other.content -> false
        identifier != other.identifier -> false
        else -> opf == other.opf
    }

    override fun hashCode(): Int {
        var result = scheme?.hashCode() ?: 0
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + (opf?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "DublinCoreIdentifierImpl(scheme=$scheme, content=$content, identifier=$identifier, opf=$opf)"
}