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

package dev.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import com.google.common.net.MediaType
import dev.epubby.*
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.internal.elementOf
import dev.epubby.internal.getAttributeValueOrThrow
import dev.epubby.internal.models.SerializedName
import dev.epubby.packages.PackageManifest
import dev.epubby.prefixes.Prefixes
import dev.epubby.properties.encodeToString
import dev.epubby.properties.propertiesOf
import dev.epubby.properties.resolveManifestProperties
import dev.epubby.resources.LocalResource
import dev.epubby.resources.ManifestResource
import dev.epubby.resources.RemoteResource
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("manifest")
data class PackageManifestModel internal constructor(
    @SerializedName("id")
    val identifier: String?,
    val items: PersistentList<ItemModel>,
) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("manifest", NAMESPACE) {
        if (identifier != null) it.setAttribute("id", identifier)

        for (item in items) {
            it.addContent(item.toElement())
        }
    }

    @JvmSynthetic
    internal fun toPackageManifest(book: Book, prefixes: Prefixes): PackageManifest {
        // TODO: once we have converted all 'items' to their appropriate 'ManifestResource' go over them once again
        //       to set their fallback property correctly, if it exists.
        TODO("'toPackageManifest' operation is not implemented yet.")
    }

    @SerializedName("item")
    data class ItemModel internal constructor(
        @SerializedName("id")
        val identifier: String,
        val href: String,
        @SerializedName("media-type")
        val mediaType: String?,
        val fallback: String?,
        @SerializedName("media-overlay")
        val mediaOverlay: String?,
        val properties: String?,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("item", NAMESPACE) {
            it.setAttribute("id", identifier)
            it.setAttribute("href", href)
            if (mediaType != null) it.setAttribute("media-type", mediaType)
            if (fallback != null) it.setAttribute("fallback", fallback)
            if (mediaOverlay != null) it.setAttribute("media-overlay", mediaOverlay)
            if (properties != null) it.setAttribute("properties", properties)
        }

        @JvmSynthetic
        internal fun toManifestResource(book: Book, prefixes: Prefixes): ManifestResource {
            val mediaType = mediaType?.let(MediaType::parse)
            val properties = properties?.let { resolveManifestProperties(it, prefixes) } ?: propertiesOf()
            // TODO: just pass in 'fallback' as 'null' for now and handle it properly in 'toPackageManifest'
            TODO("'toItem' operation is not implemented yet.")
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): ItemModel {
                val identifier = element.getAttributeValueOrThrow("id")
                val href = element.getAttributeValueOrThrow("href")
                val fallback = element.getAttributeValue("fallback")
                val mediaType = element.getAttributeValue("media-type")
                val mediaOverlay = element.getAttributeValue("media-overlay")
                val properties = element.getAttributeValue("properties")
                return ItemModel(identifier, href, mediaType, fallback, mediaOverlay, properties)
            }

            @JvmSynthetic
            internal fun fromManifestResource(origin: ManifestResource): ItemModel {
                val properties = when {
                    origin.book.version.isOlder(EPUB_3_0) -> null
                    else -> origin.properties.encodeToString()
                }

                return when (origin) {
                    is RemoteResource -> {
                        val mediaType = origin.mediaType?.toString()
                        ItemModel(
                            origin.identifier,
                            origin.href,
                            mediaType,
                            origin.fallback,
                            origin.mediaOverlay,
                            properties
                        )
                    }
                    is LocalResource -> {
                        val href = origin.relativeHref.substringAfter("../")
                        val mediaType = origin.mediaType.toString()
                        val fallback = origin.fallback?.identifier
                        ItemModel(origin.identifier, href, mediaType, fallback, origin.mediaOverlay, properties)
                    }
                }
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageManifestModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageManifestModel {
            val identifier = element.getAttributeValue("id")
            val items = element.getChildren("item", element.namespace)
                .tryMap { ItemModel.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .ifEmpty { throw MalformedBookException.forMissing("manifest", "item") }
                .toPersistentList()
            return PackageManifestModel(identifier, items)
        }

        @JvmSynthetic
        internal fun fromPackageManifest(origin: PackageManifest): PackageManifestModel {
            TODO("'fromPackageManifest' operation is not implemented yet.")
        }
    }
}