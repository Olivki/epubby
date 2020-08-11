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
import dev.epubby.Book
import dev.epubby.ParseStrictness
import dev.epubby.internal.`Reference | CustomReference`
import dev.epubby.internal.elementOf
import dev.epubby.internal.getAttributeValueOrThrow
import dev.epubby.internal.models.SerializedName
import dev.epubby.mapToValues
import dev.epubby.packages.PackageGuide
import dev.epubby.resources.PageResource
import dev.epubby.tryMap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import moe.kanon.kommons.collections.emptyEnumMap
import moe.kanon.kommons.func.Either
import org.apache.commons.collections4.map.CaseInsensitiveMap
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE
import dev.epubby.packages.PackageGuide.CustomReference as PackageCustomReference
import dev.epubby.packages.PackageGuide.Reference as PackageReference
import dev.epubby.packages.PackageGuide.Type.Companion as ReferenceType

@SerializedName("guide")
data class PackageGuideModel internal constructor(val references: ImmutableList<Reference>) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("guide", NAMESPACE) {
        references.forEach { reference -> it.addContent(reference.toElement()) }
    }

    @JvmSynthetic
    internal fun toPackageGuide(book: Book): PackageGuide {
        val allReferences = references.asSequence().map { it.toReference(book) }
        val references = allReferences.filter { it.isLeft }
            .map { it.leftValue }
            .associateByTo(emptyEnumMap()) { it.type }
        val customReferences = allReferences.filter { it.isRight }
            .map { it.rightValue }
            .associateByTo(CaseInsensitiveMap()) { it.type }
        return PackageGuide(book).also {
            it._references.putAll(references as Map<PackageGuide.Type, PackageReference>)
            it._customReferences.putAll(customReferences)
        }
    }

    @SerializedName("reference")
    data class Reference internal constructor(
        val type: String,
        val href: String,
        val title: String?
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
        internal fun toReference(book: Book): `Reference | CustomReference` {
            val resource = getPageResourceByHref(book)
            return when {
                hasCustomType -> Either.right(PackageCustomReference(book, type.substring(6), resource, title))
                else -> {
                    val type = ReferenceType.fromType(type)
                    Either.left(PackageReference(book, type, resource, title))
                }
            }
        }

        private fun getPageResourceByHref(book: Book): PageResource = TODO()

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(Reference::class)

            @JvmSynthetic
            internal fun fromElement(element: Element): Reference {
                val type = handleType(element.getAttributeValueOrThrow("type"), shouldLog = true)
                val href = element.getAttributeValueOrThrow("href")
                val title = element.getAttributeValue("href")
                return Reference(type, href, title)
            }

            private fun handleType(value: String, shouldLog: Boolean): String = when {
                !(value.startsWith("other.", ignoreCase = true)) && ReferenceType.isUnknownType(value) -> {
                    if (shouldLog) {
                        LOGGER.debug { "Fixing unknown guide type '$value' to 'other.$value'." }
                    }

                    "other.$value"
                }
                else -> value
            }

            @JvmSynthetic
            internal fun fromReference(origin: PackageReference): Reference {
                val type = origin.type.type
                val href = origin.reference.relativeHref.substringAfter("../")
                val title = origin.title
                return Reference(type, href, title)
            }

            @JvmSynthetic
            internal fun fromCustomReference(origin: PackageCustomReference): Reference {
                val type = handleType(origin.type, shouldLog = false)
                val href = origin.reference.relativeHref.substringAfter("../")
                val title = origin.title
                return Reference(type, href, title)
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageGuideModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageGuideModel {
            val references = element.getChildren("reference", element.namespace)
                .tryMap { Reference.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .toPersistentList()
            return PackageGuideModel(references)
        }

        @JvmSynthetic
        internal fun fromPackageGuide(origin: PackageGuide): PackageGuideModel {
            val references = origin._references
                .map { (_, ref) -> Reference.fromReference(ref) }
                .toPersistentList()
            val customReferences = origin._customReferences
                .map { (_, ref) -> Reference.fromCustomReference(ref) }
                .toPersistentList()
            return PackageGuideModel(references + customReferences)
        }
    }
}