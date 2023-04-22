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

@file:JvmName("MetadataLinks")

package dev.epubby.opf.metadata

import com.google.common.net.MediaType
import dev.epubby.Epub3Feature
import dev.epubby.opf.IdentifiableOpfElement
import dev.epubby.property.Properties
import dev.epubby.property.Relationship
import net.ormr.epubby.internal.opf.metadata.MetadataLinkImpl
import org.xbib.net.IRI

@Epub3Feature
public interface MetadataLink : IdentifiableOpfElement {
    public var href: IRI

    @Epub3Feature
    public var relation: Relationship?

    public var mediaType: MediaType?

    override var identifier: String?

    @Epub3Feature
    public val properties: Properties?

    @Epub3Feature
    public var refines: String?
}

@Epub3Feature
@JvmName("newLink")
public fun MetadataLink(
    href: IRI,
    relation: Relationship?,
    mediaType: MediaType?,
    identifier: String?,
    properties: Properties,
    refines: String?,
): MetadataLink = MetadataLinkImpl(
    href = href,
    relation = relation,
    mediaType = mediaType,
    identifier = identifier,
    properties = properties,
    refines = refines,
)