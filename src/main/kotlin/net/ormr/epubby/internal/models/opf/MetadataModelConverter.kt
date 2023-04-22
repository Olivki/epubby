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

package net.ormr.epubby.internal.models.opf

import dev.epubby.Epub3Feature
import dev.epubby.Epub3LegacyFeature
import dev.epubby.opf.metadata.Opf2Meta
import dev.epubby.opf.metadata.Opf3Meta
import dev.epubby.xml.XmlAttribute
import net.ormr.epubby.internal.ModelConversionData
import net.ormr.epubby.internal.models.opf.MetadataModel.*
import net.ormr.epubby.internal.opf.metadata.*
import net.ormr.epubby.internal.property.PropertyResolver.resolveLink
import net.ormr.epubby.internal.property.PropertyResolver.resolveLinkRel
import net.ormr.epubby.internal.property.PropertyResolver.resolveMany
import net.ormr.epubby.internal.property.PropertyResolver.resolveMeta
import net.ormr.epubby.internal.util.ifNotNull
import org.xbib.net.IRI

@OptIn(Epub3Feature::class, Epub3LegacyFeature::class)
internal object MetadataModelConverter {
    fun Opf2MetaModel.toOpf2Meta(): Opf2Meta = when {
        (httpEquiv != null && content != null) && (name == null && charset == null) -> Opf2MetaHttpEquivImpl(
            scheme = scheme,
            httpEquiv = httpEquiv,
            content = content,
            extraAttributes = extraAttributes.toMutableList(),
        )
        (name != null && content != null) && (httpEquiv == null && charset == null) -> Opf2MetaNameImpl(
            scheme = scheme,
            name = name,
            content = content,
            extraAttributes = extraAttributes.toMutableList(),
        )
        (charset != null) && (name == null && httpEquiv == null && content == null) -> Opf2MetaCharsetImpl(
            scheme = scheme,
            charset = charset,
            extraAttributes = extraAttributes.toMutableList(),
        )
        else -> Opf2MetaUnknownImpl(
            content = content,
            extraAttributes = buildList(4 + extraAttributes.size) {
                ifNotNull(charset) { add(XmlAttribute("charset", it, namespace = null)) }
                ifNotNull(httpEquiv) { add(XmlAttribute("http-equiv", it, namespace = null)) }
                ifNotNull(name) { add(XmlAttribute("name", it, namespace = null)) }
                ifNotNull(scheme) { add(XmlAttribute("scheme", it, namespace = null)) }
                addAll(extraAttributes)
            }.toMutableList(),
        )
    }

    fun Opf3MetaModel.toOpf3Meta(data: ModelConversionData): Opf3Meta<*> = Opf3MetaConverters.create(
        value = value,
        property = resolveMeta(property, data.prefixes),
        // TODO: should scheme be resolved the same way as the property is?
        scheme = scheme?.let { resolveMeta(property, data.prefixes) },
        refines = refines,
        identifier = identifier,
        direction = direction,
        language = language,
    )

    fun LinkModel.toLink(data: ModelConversionData): MetadataLinkImpl = MetadataLinkImpl(
        href = IRI.create(href),
        relation = relation?.let { resolveLinkRel(it, data.prefixes) },
        mediaType = mediaType,
        identifier = identifier,
        properties = properties?.let { resolveMany(it, data.prefixes, ::resolveLink) },
        refines = refines,
    )
}