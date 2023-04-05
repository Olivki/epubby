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
import dev.epubby.content.ContentReadError
import dev.epubby.content.ContentReadError.MissingAttribute
import dev.epubby.content.ContentReadError.MissingElement
import dev.epubby.content.ManifestReadError.NoItemElements
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.WriterData
import net.ormr.epubby.internal.models.content.ManifestModel.ItemModel
import net.ormr.epubby.internal.models.supportsEpub3Features
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import org.jdom2.Element
import java.net.URLEncoder
import net.ormr.epubby.internal.Namespaces.OPF_NO_PREFIX as NAMESPACE

internal object ManifestModelXml : ModelXmlSerializer<ContentReadError>() {
    fun read(manifest: Element) = effect {
        val items = manifest
            .children("item", NAMESPACE)
            .map { readItem(it).bind() }
        ensure(items.isNotEmpty()) { NoItemElements }
        ManifestModel(
            identifier = manifest.optionalAttr("id"),
            items = items,
        )
    }

    private fun readItem(item: Element) = effect {
        ItemModel(
            identifier = item.attr("id").bind(),
            href = item.attr("href").bind(),
            mediaType = item.attr("media-type").bind(),
            fallback = item.optionalAttr("fallback"),
            mediaOverlay = item.optionalAttr("media-overlay"),
            properties = item.optionalAttr("properties"),
        )
    }

    fun write(manifest: ManifestModel, data: WriterData): Element = buildElement("manifest", NAMESPACE) {
        this["id"] = manifest.identifier
        addChildren(manifest.items) { writeItem(it, data) }
    }

    @OptIn(Epub3Feature::class)
    private fun writeItem(item: ItemModel, data: WriterData): Element = buildElement("item", NAMESPACE) {
        this["id"] = item.identifier
        this["href"] = URLEncoder.encode(item.href, Charsets.UTF_8)
        this["media-type"] = item.mediaType
        this["fallback"] = item.fallback
        this["media-overlay"] = item.mediaOverlay
        if (data.supportsEpub3Features()) {
            this["properties"] = item.properties
        }
    }

    override fun missingAttribute(name: String, path: String): ContentReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): ContentReadError = MissingElement(name, path)
}