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

package dev.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.MalformedBookException
import dev.epubby.ParseMode
import dev.epubby.files.DirectoryFile
import dev.epubby.files.GhostFile
import dev.epubby.files.RegularFile
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.utils.elementOf
import dev.epubby.internal.utils.getAttributeValueOrThrow
import dev.epubby.internal.utils.mapToValues
import dev.epubby.internal.utils.tryMap
import dev.epubby.packages.PackageManifest
import dev.epubby.prefixes.Prefixes
import dev.epubby.properties.Properties
import dev.epubby.properties.encodeToString
import dev.epubby.properties.propertiesOf
import dev.epubby.properties.resolveManifestProperties
import dev.epubby.resources.ExternalResource
import dev.epubby.resources.LocalResource
import dev.epubby.resources.ManifestResource
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.apache.commons.validator.routines.UrlValidator
import org.jdom2.Element
import java.net.URLDecoder
import java.net.URLEncoder
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("manifest")
internal data class PackageManifestModel internal constructor(
    @SerializedName("id")
    internal val identifier: String?,
    internal val items: PersistentList<ItemModel>,
) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("manifest", NAMESPACE) {
        if (identifier != null) it.setAttribute("id", identifier)

        for (item in items) {
            it.addContent(item.toElement())
        }
    }

    @JvmSynthetic
    internal fun toPackageManifest(epub: Epub, prefixes: Prefixes, opfFile: RegularFile): PackageManifest {
        val resources = items.asSequence()
            //.map { it.toManifestResource(epub, prefixes, opfFile) }
            // TODO: this is a hack job
            .map {
                kotlin.runCatching { it.toManifestResource(epub, prefixes, opfFile) }
            }
            .mapNotNull { it.getOrNull() }
            .withIndex()
            .associateByTo(hashMapOf()) { it.value.identifier }
        val resolvedResources = resources.values
            .asSequence()
            .onEach { (i, resource) -> resolveFallback(i, resource, resources) }
            .map { it.value }
        val localResources = resolvedResources
            .filterIsInstance<LocalResource>()
            .associateByTo(hashMapOf()) { it.identifier }
        val fileToLocalResource = localResources.values.associateByTo(hashMapOf()) { it.file.fullPath }
        val externalResources = resolvedResources
            .filterIsInstance<ExternalResource>()
            .associateByTo(hashMapOf()) { it.identifier }

        return PackageManifest(epub, identifier, localResources, fileToLocalResource, externalResources)
    }

    private fun resolveFallback(
        index: Int,
        resource: ManifestResource,
        resources: Map<String, IndexedValue<ManifestResource>>,
    ) {
        val model = items[index]
        val fallback = model.fallback?.let { key -> resources[key]?.value }

        if (fallback != null) {
            resource.fallback = fallback
        }
    }

    @SerializedName("item")
    internal data class ItemModel internal constructor(
        @SerializedName("id")
        internal val identifier: String,
        internal val href: String,
        @SerializedName("media-type")
        internal val mediaType: String,
        internal val fallback: String?, // conditionally required
        @SerializedName("media-overlay")
        internal val mediaOverlay: String?,
        internal val properties: String?,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("item", NAMESPACE) {
            it.setAttribute("id", identifier)
            it.setAttribute("href", URLEncoder.encode(href, Charsets.UTF_8))
            it.setAttribute("media-type", mediaType)
            if (fallback != null) it.setAttribute("fallback", fallback)
            if (mediaOverlay != null) it.setAttribute("media-overlay", mediaOverlay)
            if (properties != null) it.setAttribute("properties", properties)
        }

        /**
         * Returns a new [ManifestResource] instance based on this model.
         *
         * The returned `ManifestResource` instance will *not* have its [fallback][ManifestResource.fallback] property
         * set, even if a [fallback] property is defined on this model. The resolving of a resources `fallback`
         * property needs to be handled by the function that invokes this.
         */
        @JvmSynthetic
        internal fun toManifestResource(epub: Epub, prefixes: Prefixes, opfFile: RegularFile): ManifestResource {
            val mediaType = MediaType.parse(this.mediaType)
            val properties = properties?.let { resolveManifestProperties(it, prefixes) } ?: propertiesOf()
            // we intentionally pass in 'fallback' as null for both external and local resources as that needs to be
            // resolved at a later point by the caller function
            return when {
                UrlValidator.getInstance().isValid(href) -> toExternalResource(epub, mediaType, properties)
                else -> toLocalResource(epub, mediaType, properties, opfFile)
            }
        }

        private fun toLocalResource(
            epub: Epub,
            mediaType: MediaType,
            properties: Properties,
            opfFile: RegularFile,
        ): LocalResource {
            val href = getFileFromHref(opfFile)
            return LocalResource.create(href, epub, identifier, mediaType).also {
                it.properties.addAll(properties)
                it.mediaOverlay = mediaOverlay
            }
        }

        private fun getFileFromHref(opf: RegularFile): RegularFile =
            when (val file = opf.resolveSibling(URLDecoder.decode(this.href, Charsets.UTF_8))) {
                is RegularFile -> file
                is DirectoryFile -> throw MalformedBookException("'href' of item $this points towards a directory file.")
                is GhostFile -> throw MalformedBookException("'href' of item $this points towards non-existent file.")
            }

        private fun toExternalResource(
            epub: Epub,
            mediaType: MediaType,
            properties: Properties,
        ): ExternalResource =
            ExternalResource(epub, identifier, href, mediaType, fallback = null, mediaOverlay, properties)

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): ItemModel {
                val identifier = element.getAttributeValueOrThrow("id")
                val href = element.getAttributeValueOrThrow("href")
                val fallback = element.getAttributeValue("fallback")
                val mediaType = element.getAttributeValueOrThrow("media-type")
                val mediaOverlay = element.getAttributeValue("media-overlay")
                val properties = element.getAttributeValue("properties")
                return ItemModel(identifier, href, mediaType, fallback, mediaOverlay, properties)
            }

            @JvmSynthetic
            internal fun fromManifestResource(origin: ManifestResource): ItemModel {
                val properties = when {
                    origin.epub.version.isOlder(EPUB_3_0) -> null
                    else -> origin.properties.ifEmpty { null }?.encodeToString()
                }

                return when (origin) {
                    is ExternalResource -> ItemModel(
                        origin.identifier,
                        origin.href,
                        origin.mediaType.toString(),
                        origin.fallback?.identifier,
                        origin.mediaOverlay,
                        properties
                    )

                    is LocalResource -> {
                        val href = origin.href
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
        internal fun fromElement(element: Element, mode: ParseMode): PackageManifestModel {
            val identifier = element.getAttributeValue("id")
            val items = element.getChildren("item", element.namespace)
                .tryMap { ItemModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
                .ifEmpty { throw MalformedBookException.forMissing("manifest", "item") }
                .toPersistentList()
            return PackageManifestModel(identifier, items)
        }

        @JvmSynthetic
        internal fun fromPackageManifest(origin: PackageManifest): PackageManifestModel {
            val items = origin
                .sortedBy { it.mediaType.toString() }
                .map { ItemModel.fromManifestResource(it) }
                .toPersistentList()

            return PackageManifestModel(origin.identifier, items)
        }
    }
}