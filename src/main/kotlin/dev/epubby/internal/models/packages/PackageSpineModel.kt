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
import dev.epubby.Epub
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.MalformedBookException
import dev.epubby.ParseMode
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.utils.elementOf
import dev.epubby.internal.utils.getAttributeValueOrThrow
import dev.epubby.internal.utils.mapToValues
import dev.epubby.internal.utils.tryMap
import dev.epubby.packages.PackageManifest
import dev.epubby.packages.PackageSpine
import dev.epubby.page.Page
import dev.epubby.prefixes.Prefixes
import dev.epubby.properties.encodeToString
import dev.epubby.properties.propertiesOf
import dev.epubby.properties.resolveSpineProperties
import dev.epubby.resources.NcxResource
import dev.epubby.resources.PageResource
import dev.epubby.utils.PageProgressionDirection
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jdom2.Element
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createFile
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("spine")
internal data class PackageSpineModel internal constructor(
    @SerializedName("id")
    internal val identifier: String?,
    @SerializedName("page-progression-direction")
    internal val pageProgressionDirection: String?,
    @SerializedName("toc")
    internal val toc: String?,
    internal val references: PersistentList<ItemReferenceModel>,
) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("spine", NAMESPACE) {
        if (identifier != null) it.setAttribute("id", identifier)
        if (pageProgressionDirection != null) it.setAttribute("page-progression-direction", pageProgressionDirection)
        if (toc != null) it.setAttribute("toc", toc)

        for (reference in references) {
            it.addContent(reference.toElement())
        }
    }

    @JvmSynthetic
    internal fun toPackageSpine(epub: Epub, prefixes: Prefixes, manifest: PackageManifest): PackageSpine {
        val pageProgressionDirection = this.pageProgressionDirection?.let { PageProgressionDirection.fromTag(it) }
        val tableOfContents = getNcxResource(manifest)
        val pages = references.mapTo(mutableListOf()) { it.toPage(manifest, prefixes) }

        return PackageSpine(epub, pages, identifier, pageProgressionDirection, tableOfContents)
    }

    private fun getNcxResource(manifest: PackageManifest): NcxResource {
        val resource = toc?.let { manifest.getLocalResourceOrNull(it) }
            ?: manifest.localResources.values.filterIsInstance<NcxResource>().firstOrNull()
            ?: createNcxResource(manifest)

        if (resource !is NcxResource) {
            throw MalformedBookException("toc '$toc' references a resource that is not a page resource.")
        }

        return resource
    }

    private fun createNcxResource(manifest: PackageManifest): NcxResource {
        LOGGER.debug { "Creating dummy NCX resource." }
        val file = manifest.epub.opfDirectory.resolveFile("toc.ncx")

        if (file.notExists) {
            file.delegate.createFile()
        }

        val identifier = createUniqueIdentifier(manifest)
        val resource = NcxResource(manifest.epub, identifier, file)

        manifest.addLocalResource(resource)

        return resource
    }

    private fun createUniqueIdentifier(manifest: PackageManifest): String {
        if ("x_toc.ncx" !in manifest) {
            return "x_toc.ncx"
        }

        val incrementer = AtomicInteger(0)
        var identifier = "x_toc.ncx_${incrementer.getAndIncrement()}"

        while (identifier in manifest) {
            identifier = "x_toc.ncx_${incrementer.getAndIncrement()}"
        }

        return identifier
    }

    @SerializedName("itemref")
    internal data class ItemReferenceModel internal constructor(
        @SerializedName("idref")
        internal val idRef: String,
        @SerializedName("id")
        internal val identifier: String?,
        @SerializedName("linear")
        internal val isLinear: Boolean, //  = true
        internal val properties: String?,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("itemref", NAMESPACE) {
            it.setAttribute("idref", idRef)
            if (identifier != null) it.setAttribute("id", identifier)
            it.setAttribute("linear", if (isLinear) "yes" else "no")
            if (properties != null) it.setAttribute("properties", properties)
        }

        @JvmSynthetic
        internal fun toPage(manifest: PackageManifest, prefixes: Prefixes): Page {
            val resource = manifest.getLocalResourceOrNull(idRef)
                ?: throw MalformedBookException("'idRef' of $this references an unknown resource.")

            if (resource !is PageResource) {
                throw MalformedBookException("'idRef' of $this points to a resource that is not a page ($resource).")
            }

            val properties = this.properties?.let { resolveSpineProperties(it, prefixes) } ?: propertiesOf()

            return Page.invoke(resource, isLinear, identifier, properties)
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): ItemReferenceModel {
                val identifierReference = element.getAttributeValueOrThrow("idref")
                val identifier = element.getAttributeValue("id")
                val isLinear = element.getAttributeValue("linear")?.let(::parseLinear) ?: true
                val properties = element.getAttributeValue("properties")

                return ItemReferenceModel(identifierReference, identifier, isLinear, properties)
            }

            // TODO: this used a more lenient boolean parsing function before, but now we're just matching directly
            private fun parseLinear(value: String): Boolean = when (value) {
                "yes" -> true
                "no" -> false
                else -> throw MalformedBookException("Expected value of 'linear' is 'yes' or 'no', was '$value'.")
            }

            @JvmSynthetic
            internal fun fromPage(origin: Page): ItemReferenceModel {
                val idRef = origin.reference.identifier
                val properties = when {
                    origin.epub.version.isOlder(EPUB_3_0) -> null
                    else -> origin.properties.ifEmpty { null }?.encodeToString()
                }

                return ItemReferenceModel(idRef, origin.identifier, origin.isLinear, properties)
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageSpineModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, mode: ParseMode): PackageSpineModel {
            val identifier = element.getAttributeValue("id")
            val pageProgressionDirection = element.getAttributeValue("page-progression-direction")
            val tableOfContentsIdentifier = element.getAttributeValue("toc")
            val references = element.getChildren("itemref", element.namespace)
                .tryMap { ItemReferenceModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
                .ifEmpty { throw MalformedBookException.forMissing("spine", "itemref") }
                .toPersistentList()

            return PackageSpineModel(identifier, pageProgressionDirection, tableOfContentsIdentifier, references)
        }

        @JvmSynthetic
        internal fun fromPackageSpine(origin: PackageSpine): PackageSpineModel {
            val pageProgressionDirection = origin.pageProgressionDirection?.value
            val toc = origin.tableOfContents.identifier
            val references = origin.pages.map { ItemReferenceModel.fromPage(it) }.toPersistentList()

            return PackageSpineModel(origin.identifier, pageProgressionDirection, toc, references)
        }
    }
}