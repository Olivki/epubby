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
import dev.epubby.*
import dev.epubby.internal.`Reference | CustomReference`
import dev.epubby.internal.elementOf
import dev.epubby.internal.getAttributeValueOrThrow
import dev.epubby.internal.models.SerializedName
import dev.epubby.packages.PackageManifest
import dev.epubby.packages.guide.*
import dev.epubby.resources.PageResource
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import moe.kanon.kommons.collections.emptyEnumMap
import moe.kanon.kommons.func.Either
import org.apache.commons.collections4.map.CaseInsensitiveMap
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("guide")
data class PackageGuideModel internal constructor(val references: PersistentList<ReferenceModel>) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("guide", NAMESPACE) {
        for (reference in references) {
            it.addContent(reference.toElement())
        }
    }

    @JvmSynthetic
    internal fun toPackageGuide(book: Book, manifest: PackageManifest): PackageGuide {
        val allReferences = references.asSequence()
            .mapNotNull { it.toReference(book, manifest) }
        val references = allReferences.filter { it.isLeft }
            .map { it.leftValue }
            .associateByTo(emptyEnumMap()) { it.type }
        val customReferences = allReferences.filter { it.isRight }
            .map { it.rightValue }
            .associateByTo(CaseInsensitiveMap()) { it.type }
        return PackageGuide(book, references, customReferences)
    }

    @SerializedName("reference")
    data class ReferenceModel internal constructor(val type: String, val href: String, val title: String?) {
        private val hasCustomType: Boolean
            get() = type.startsWith("other.", ignoreCase = true)

        @JvmSynthetic
        internal fun toElement(): Element = elementOf("reference", NAMESPACE) {
            it.setAttribute("type", type)
            it.setAttribute("href", href)
            if (title != null) it.setAttribute("title", title)
        }

        @JvmSynthetic
        internal fun toReference(book: Book, manifest: PackageManifest): `Reference | CustomReference`? {
            val resource = getPageResourceOrNull(manifest) ?: return null
            return when {
                hasCustomType -> Either.right(CustomGuideReference(book, type.substring(6), resource, title))
                else -> {
                    val type = ReferenceType.fromType(type)
                    Either.left(GuideReference(book, type, resource, title))
                }
            }
        }

        private fun getPageResourceOrNull(manifest: PackageManifest): PageResource? = manifest.localResources
            .asSequence()
            .filterIsInstance<PageResource>()
            .filter { it.isHrefEqual(href) }
            .firstOrNull().also {
                if (it == null) {
                    LOGGER.error { "Could not find a resource with a 'href' matching the 'href' of $this." }
                }
            }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(ReferenceModel::class)

            @JvmSynthetic
            internal fun fromElement(element: Element): ReferenceModel {
                val type = handleType(element.getAttributeValueOrThrow("type"), shouldLog = true)
                val href = element.getAttributeValueOrThrow("href")
                val title = element.getAttributeValue("href")
                return ReferenceModel(type, href, title)
            }

            private fun handleType(value: String, shouldLog: Boolean): String = when {
                !(value.startsWith("other.", ignoreCase = true)) && value !in ReferenceType -> {
                    if (shouldLog) {
                        LOGGER.debug { "Fixing unknown guide type '$value' to 'other.$value'." }
                    }

                    "other.$value"
                }
                else -> value
            }

            @JvmSynthetic
            internal fun fromReference(origin: GuideReference): ReferenceModel {
                val type = origin.type.type
                val href = origin.reference.relativeHref.substringAfter("../")
                val title = origin.title
                return ReferenceModel(type, href, title)
            }

            @JvmSynthetic
            internal fun fromCustomReference(origin: CustomGuideReference): ReferenceModel {
                val type = handleType(origin.type, shouldLog = false)
                val href = origin.reference.relativeHref.substringAfter("../")
                val title = origin.title
                return ReferenceModel(type, href, title)
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageGuideModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageGuideModel {
            val references = element.getChildren("reference", element.namespace)
                .tryMap { ReferenceModel.fromElement(it) }
                .mapToValues(LOGGER, strictness)
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