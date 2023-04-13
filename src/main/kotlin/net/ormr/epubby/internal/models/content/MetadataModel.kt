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
import dev.epubby.Epub3LegacyFeature
import dev.epubby.ReadingDirection
import dev.epubby.xml.XmlAttribute
import net.ormr.epubby.internal.models.SerializedName
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel.IdentifierModel
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel.LanguageModel
import net.ormr.epubby.internal.models.dublincore.LocalizedDublinCoreModel.TitleModel

// https://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.2
// https://www.w3.org/publishing/epub3/epub-packages.html#sec-metadata-elem
@SerializedName("metadata")
@OptIn(Epub3Feature::class)
internal data class MetadataModel(
    val identifiers: List<IdentifierModel>, // +
    val titles: List<TitleModel>, // +
    val languages: List<LanguageModel>, // +
    val dublinCoreElements: List<DublinCoreModel>,
    val links: List<LinkModel>,
    val metaElements: List<OpfMeta>,
) {
    sealed interface OpfMeta

    // https://www.w3.org/TR/2004/WD-xhtml2-20040722/mod-meta.html#edef_meta_meta
    @Epub3LegacyFeature
    @SerializedName("meta")
    data class Opf2MetaModel(
        val charset: String?,
        val content: String?,
        val httpEquiv: String?,
        val name: String?,
        val scheme: String?,
        val extraAttributes: List<XmlAttribute>,
    ) : OpfMeta

    // https://www.w3.org/publishing/epub3/epub-packages.html#sec-meta-elem
    @Epub3Feature
    @SerializedName("meta")
    data class Opf3MetaModel(
        val value: String,
        val property: String,
        val identifier: String?,
        val direction: ReadingDirection?,
        // relative iri, we can only really handle direct id references
        val refines: String?,
        val scheme: String?,
        val language: String?,
    ) : OpfMeta

    // can't find any mention of this element in the epub2 spec, so treating this as epub3 exclusive
    // https://www.w3.org/publishing/epub3/epub-packages.html#elemdef-opf-link
    @Epub3Feature
    @SerializedName("link")
    data class LinkModel(
        val href: String,
        @SerializedName("rel")
        val relation: String?, // property
        @SerializedName("media-type")
        val mediaType: String?, // conditionally required
        @SerializedName("id")
        val identifier: String?,
        val properties: String?,
        val refines: String?,
    )
}