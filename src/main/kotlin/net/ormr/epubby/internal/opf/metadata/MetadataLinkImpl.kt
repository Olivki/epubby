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

import com.google.common.net.MediaType
import dev.epubby.Epub3Feature
import dev.epubby.opf.metadata.MetadataLink
import dev.epubby.property.Properties
import dev.epubby.property.Relationship
import net.ormr.epubby.internal.identifierDelegate
import net.ormr.epubby.internal.opf.InternalIdentifiableOpfElement
import net.ormr.epubby.internal.opf.OpfImpl
import org.xbib.net.IRI

@OptIn(Epub3Feature::class)
internal class MetadataLinkImpl(
    override var href: IRI,
    override var relation: Relationship?,
    override var mediaType: MediaType?,
    identifier: String?,
    override val properties: Properties,
    override var refines: String?,
) : MetadataLink, InternalIdentifiableOpfElement {
    override var identifier: String? by identifierDelegate(identifier)

    override var opf: OpfImpl? = null

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is MetadataLinkImpl -> false
        href != other.href -> false
        relation != other.relation -> false
        mediaType != other.mediaType -> false
        properties != other.properties -> false
        refines != other.refines -> false
        identifier != other.identifier -> false
        else -> opf == other.opf
    }

    override fun hashCode(): Int {
        var result = href.hashCode()
        result = 31 * result + (relation?.hashCode() ?: 0)
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()
        result = 31 * result + (refines?.hashCode() ?: 0)
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + (opf?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "MetadataLinkImpl(href=$href, relation=$relation, mediaType=$mediaType, properties=$properties, refines=$refines, identifier=$identifier)"
}