/*
 * Copyright 2019-2020 Oliver Berg
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

package moe.kanon.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import moe.kanon.epubby.Book
import moe.kanon.epubby.MalformedBookException
import moe.kanon.epubby.ParseStrictness
import moe.kanon.epubby.internal.`Resource | RemoteItem`
import moe.kanon.epubby.internal.elementOf
import moe.kanon.epubby.internal.getAttributeValueOrThrow
import moe.kanon.epubby.internal.models.SerialName
import moe.kanon.epubby.mapToValues
import moe.kanon.epubby.packages.PackageManifest
import moe.kanon.epubby.prefixes.Prefixes
import moe.kanon.epubby.tryMap
import org.apache.logging.log4j.kotlin.loggerOf
import org.jdom2.Element
import moe.kanon.epubby.internal.Namespaces.OPF as NAMESPACE

@SerialName("manifest")
internal data class PackageManifestModel internal constructor(
    @SerialName("id") internal val identifier: String?,
    internal val items: List<Item>
) {
    internal fun toElement(): Element = elementOf("manifest", NAMESPACE) {
        if (identifier != null) it.setAttribute("id", identifier)
        items.forEach { item -> it.addContent(item.toElement()) }
    }

    internal fun toPackageManifest(book: Book, prefixes: Prefixes): PackageManifest {
        TODO("'toPackageManifest' operation is not implemented yet.")
    }

    @SerialName("item")
    data class Item(
        @SerialName("id") internal val identifier: String,
        internal val href: String,
        @SerialName("media-type") internal val mediaType: String?,
        internal val fallback: String?,
        @SerialName("media-overlay") internal val mediaOverlay: String?,
        internal val properties: String?
    ) {
        internal fun toElement(): Element = elementOf("item", NAMESPACE) {
            it.setAttribute("id", identifier)
            it.setAttribute("href", href)
            if (mediaType != null) it.setAttribute("media-type", mediaType)
            if (fallback != null) it.setAttribute("fallback", fallback)
            if (mediaOverlay != null) it.setAttribute("media-overlay", mediaOverlay)
            if (properties != null) it.setAttribute("properties", properties)
        }

        internal fun toItem(book: Book): `Resource | RemoteItem` {
            TODO("'toItem' operation is not implemented yet.")
        }

        internal companion object {
            internal fun fromElement(element: Element): Item {
                val identifier = element.getAttributeValueOrThrow("id")
                val href = element.getAttributeValueOrThrow("href")
                val fallback = element.getAttributeValue("fallback")
                val mediaType = element.getAttributeValue("media-type")
                val mediaOverlay = element.getAttributeValue("media-overlay")
                val properties = element.getAttributeValue("properties")
                return Item(identifier, href, mediaType, fallback, mediaOverlay, properties)
            }

            // TODO: only accept 'properties' if book version is newer than 2.0 (aka 3.x and up)
            internal fun fromItem(origin: `Resource | RemoteItem`): Item {
                TODO("'fromItem' operation is not implemented yet.")
            }
        }
    }

    internal companion object {
        private val logger = InlineLogger(PackageManifestModel::class)

        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageManifestModel {
            val identifier = element.getAttributeValue("id")
            val items = element.getChildren("item", element.namespace)
                .tryMap { Item.fromElement(it) }
                .mapToValues(logger, strictness)
                .ifEmpty { throw MalformedBookException.forMissing("manifest", "item") }
            return PackageManifestModel(identifier, items)
        }

        internal fun fromPackageManifest(origin: PackageManifest): PackageManifestModel {
            TODO("'fromPackageManifest' operation is not implemented yet.")
        }
    }
}