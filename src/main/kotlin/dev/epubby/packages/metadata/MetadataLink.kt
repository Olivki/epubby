/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.packages.metadata

import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.internal.IntroducedIn
import dev.epubby.properties.Properties
import dev.epubby.properties.Relationship
import java.net.URI

class MetadataLink(
    override val epub: Epub,
    var href: URI,
    @IntroducedIn(version = EPUB_3_0)
    var relation: Relationship? = null,
    var mediaType: MediaType? = null,
    var identifier: String? = null,
    // TODO: don't serialize these if version is 2.0
    @IntroducedIn(version = EPUB_3_0)
    var properties: Properties = Properties.empty(),
    @IntroducedIn(version = EPUB_3_0)
    // TODO: give this a proper type and not just a string?
    var refines: String? = null,
) : EpubElement {
    override val elementName: String
        get() = "PackageMetadata.Link"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is MetadataLink -> false
        relation != other.relation -> false
        mediaType != other.mediaType -> false
        identifier != other.identifier -> false
        properties != other.properties -> false
        refines != other.refines -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = relation.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()
        result = 31 * result + (refines?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Link(relation=$relation, mediaType=$mediaType, identifier=$identifier, properties=$properties, refines=$refines)"
}