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

import arrow.core.Either
import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Epub
import dev.epubby.ParseMode
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.utils.*
import dev.epubby.packages.PackageManifest
import dev.epubby.packages.guide.CustomGuideReference
import dev.epubby.packages.guide.GuideReference
import dev.epubby.packages.guide.PackageGuide
import dev.epubby.packages.guide.ReferenceType
import dev.epubby.resources.DefaultResourceVisitor
import dev.epubby.resources.ManifestResource
import dev.epubby.resources.PageResource
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import krautils.collections.emptyEnumMap
import org.apache.commons.collections4.map.CaseInsensitiveMap
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("guide")
internal data class PackageGuideModel internal constructor(internal val references: PersistentList<ReferenceModel>) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("guide", NAMESPACE) {
        for (reference in references) {
            it.addContent(reference.toElement())
        }
    }

    @JvmSynthetic
    internal fun toPackageGuide(epub: Epub, manifest: PackageManifest): PackageGuide {
        val allReferences = references.asSequence()
            .mapNotNull { it.toReference(epub, manifest) }
        val references = allReferences.filterIsInstance<Either.Left<GuideReference>>()
            .map { it.value }
            .associateByTo(emptyEnumMap()) { it.type }
        val customReferences = allReferences.filterIsInstance<Either.Right<CustomGuideReference>>()
            .map { it.value }
            .associateByTo(CaseInsensitiveMap()) { it.type }
        return PackageGuide(epub, references, customReferences)
    }

    @SerializedName("reference")
    internal data class ReferenceModel internal constructor(
        internal val type: String,
        internal val href: String,
        internal val title: String?,
    ) {
        private val hasCustomType: Boolean
            get() = type.startsWith("other.", ignoreCase = true)

        @JvmSynthetic
        internal fun toElement(): Element = elementOf("reference", NAMESPACE) {
            it.setAttribute("type", type)
            it.setAttribute("href", href)
            if (title != null) it.setAttribute("title", title)
        }

        @JvmSynthetic
        internal fun toReference(epub: Epub, manifest: PackageManifest): `Reference | CustomReference`? {
            val resource = getPageResource(manifest) ?: return null
            return when {
                hasCustomType -> Either.Right(CustomGuideReference(epub, type.substring(6), resource, title))
                else -> {
                    val type = ReferenceType.fromType(type)
                    Either.Left(GuideReference(epub, type, resource, title))
                }
            }
        }

        private fun getPageResource(manifest: PackageManifest): PageResource? {
            val resource = manifest.collectResources(PageResourceVisitor, MatchingHrefFilter(href)).firstOrNull()

            if (resource == null) {
                LOGGER.error { "The 'href' attribute of guide reference element $this references an non-existent resource file." }
            }

            return resource
        }

        private object PageResourceVisitor : DefaultResourceVisitor<PageResource> {
            override fun getDefaultValue(resource: ManifestResource): PageResource = throw IllegalStateException()

            override fun visitPage(resource: PageResource): PageResource = resource
        }

        private class MatchingHrefFilter(val href: String) : DefaultResourceVisitor<Boolean> {
            override fun getDefaultValue(resource: ManifestResource): Boolean =
                resource is PageResource && resource.isHrefEqual(href)
        }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(ReferenceModel::class)

            @JvmSynthetic
            internal fun fromElement(element: Element): ReferenceModel {
                val type = handleType(element.getAttributeValueOrThrow("type"), shouldLog = true)
                val href = element.getAttributeValueOrThrow("href")
                val title = element.getAttributeValue("title")
                return ReferenceModel(type, href, title)
            }

            private fun handleType(value: String, shouldLog: Boolean): String = when {
                !(value.startsWith("other.", ignoreCase = true)) && ReferenceType.isUnknownType(value) -> {
                    if (shouldLog) {
                        LOGGER.debug { "Prepending unknown guide reference type '$value' with 'other.'" }
                    }

                    "other.$value"
                }

                else -> value
            }

            @JvmSynthetic
            internal fun fromReference(origin: GuideReference): ReferenceModel {
                val type = origin.type.type
                val href = origin.reference.href
                val title = origin.title
                return ReferenceModel(type, href, title)
            }

            @JvmSynthetic
            internal fun fromCustomReference(origin: CustomGuideReference): ReferenceModel {
                val type = handleType(origin.type, shouldLog = false)
                val href = origin.reference.href
                val title = origin.title
                return ReferenceModel(type, href, title)
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageGuideModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, mode: ParseMode): PackageGuideModel {
            val references = element.getChildren("reference", element.namespace)
                .tryMap { ReferenceModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
                .toPersistentList()
            return PackageGuideModel(references)
        }

        @JvmSynthetic
        internal fun fromPackageGuide(origin: PackageGuide): PackageGuideModel {
            val references = origin.references
                .map { (_, ref) -> ReferenceModel.fromReference(ref) }
                .toPersistentList()
            val customReferences = origin.customReferences
                .map { (_, ref) -> ReferenceModel.fromCustomReference(ref) }
                .toPersistentList()
            return PackageGuideModel(references + customReferences)
        }
    }
}