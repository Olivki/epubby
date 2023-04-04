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

package net.ormr.epubby.internal.models.content

import dev.epubby.Epub3Feature
import dev.epubby.ReadingDirection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE_PREFIX
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE_URI
import net.ormr.epubby.internal.Namespaces.OPF_PREFIX
import net.ormr.epubby.internal.Namespaces.OPF_URI
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel
import net.ormr.epubby.internal.xml.QName
import net.ormr.epubby.internal.xml.XmlAdditionalNamespaces
import net.ormr.epubby.internal.xml.XmlElementsName
import net.ormr.epubby.internal.xml.XmlNamespace

// dc-metadata and x-metadata are NOT supported

// https://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.2
// https://www.w3.org/publishing/epub3/epub-packages.html#sec-metadata-elem
@Serializable
@SerialName("metadata")
@XmlAdditionalNamespaces([XmlNamespace(DUBLIN_CORE_PREFIX, DUBLIN_CORE_URI), XmlNamespace(OPF_PREFIX, OPF_URI)])
internal data class MetadataModel(
    val links: List<LinkModel> = emptyList(),
    @XmlNamespace(DUBLIN_CORE_PREFIX, DUBLIN_CORE_URI)
    val dublinCoreElements: List<DublinCoreModel> = emptyList(),
    @XmlElementsName("meta")
    val metaElements: List<OpfMeta> = emptyList(),
) {
    // identifiers, titles and languages are just contained in 'dublinCoreElements'

    // TODO: Opf2Meta & Op3Meta
    // TODO: make custom serializer that gives in an element so we can make a serializer for the opf elements

    @Serializable(with = MetadataOpfMetaSerializer::class)
    sealed interface OpfMeta

    // https://www.w3.org/TR/2004/WD-xhtml2-20040722/mod-meta.html#edef_meta_meta
    data class Opf2MetaModel(
        val charset: String?,
        val content: String?,
        val httpEquiv: String?,
        val name: String?,
        val scheme: String?,
        val attributes: Map<QName, String>,
    ) : OpfMeta

    data class Op3MetaModel(
        val value: String,
        val property: String,
        val identifier: String?,
        val direction: ReadingDirection?,
        val refines: String?,
        val scheme: String?,
        val language: String?,
    ) : OpfMeta

    @Serializable
    @SerialName("link")
    data class LinkModel(
        val href: String,
        @SerialName("rel")
        @property:Epub3Feature
        val relation: String?,
        @SerialName("media-type")
        val mediaType: String?,
        @SerialName("id")
        val identifier: String?,
        @property:Epub3Feature
        val properties: String?,
        @property:Epub3Feature
        val refines: String?,
    )
}