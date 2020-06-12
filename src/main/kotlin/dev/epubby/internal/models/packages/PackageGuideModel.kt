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
import dev.epubby.internal.models.SerialName
import dev.epubby.mapToValues
import dev.epubby.packages.PackageGuide
import dev.epubby.resources.PageResource
import dev.epubby.tryMap
import moe.kanon.kommons.collections.emptyEnumMap
import moe.kanon.kommons.func.Either
import org.apache.commons.collections4.map.CaseInsensitiveMap
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerialName("guide")
internal data class PackageGuideModel internal constructor(internal val references: List<Reference>) {
    internal fun toElement(): Element = elementOf("guide", NAMESPACE) {
        references.forEach { reference -> it.addContent(reference.toElement()) }
    }

    internal fun toPackageGuide(book: Book): PackageGuide {
        val allReferences = references.asSequence().map { it.toReference(book) }
        val references = allReferences.filter { it.isLeft }
            .map { it.leftValue }
            .associateByTo(emptyEnumMap()) { it.type }
        val customReferences = allReferences.filter { it.isRight }
            .map { it.rightValue }
            .associateByTo(CaseInsensitiveMap()) { it.customType }
        return PackageGuide(book).also {
            it._references.putAll(references as Map<PackageGuide.Type, PackageGuide.Reference>)
            it._customReferences.putAll(customReferences)
        }
    }

    @SerialName("reference")
    data class Reference(internal val type: String, internal val href: String, internal val title: String?) {
        private val hasCustomType: Boolean
            get() = type.startsWith("other.", ignoreCase = true)

        internal fun toElement(): Element = elementOf("reference", NAMESPACE) {
            it.setAttribute("type", type)
            it.setAttribute("href", href)
            if (title != null) it.setAttribute("title", title)
        }

        internal fun toReference(book: Book): `Reference | CustomReference` {
            val resource = getPageResourceByHref(book)
            return when (hasCustomType) {
                false -> Either.left(PackageGuide.Reference(book, PackageGuide.Type.byType(type), resource, title))
                true -> Either.right(PackageGuide.CustomReference(book, type.substring(6), resource, title))
            }
        }

        private fun getPageResourceByHref(book: Book): PageResource = TODO()

        internal companion object {
            private val logger = InlineLogger(Reference::class)

            internal fun fromElement(element: Element): Reference {
                val type = handleType(element.getAttributeValueOrThrow("type"), shouldLog = true)
                val href = element.getAttributeValueOrThrow("href")
                val title = element.getAttributeValue("href")
                return Reference(type, href, title)
            }

            private fun handleType(value: String, shouldLog: Boolean): String = when {
                PackageGuide.Type.isUnknownType(value) && !(value.startsWith("other.", ignoreCase = true)) -> {
                    // TODO: lower to 'debug'?
                    // TODO: more verbose/better log message?
                    if (shouldLog) logger.info { "Fixing unknown guide type '$value' to 'other.$value'." }
                    "other.$value"
                }
                else -> value
            }

            internal fun fromReference(origin: PackageGuide.Reference): Reference {
                val type = origin.type.type
                val href = origin.reference.relativeHref.substringAfter("../")
                val title = origin.title
                return Reference(type, href, title)
            }

            internal fun fromCustomReference(origin: PackageGuide.CustomReference): Reference {
                val type = handleType(origin.customType, shouldLog = false)
                val href = origin.reference.relativeHref.substringAfter("../")
                val title = origin.title
                return Reference(type, href, title)
            }
        }
    }

    internal companion object {
        private val logger = InlineLogger(PackageGuideModel::class)

        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageGuideModel {
            val references = element.getChildren("reference", element.namespace)
                .tryMap { Reference.fromElement(it) }
                .mapToValues(logger, strictness)
            return PackageGuideModel(references)
        }

        internal fun fromPackageGuide(origin: PackageGuide): PackageGuideModel {
            val references = origin._references.map { (_, ref) -> Reference.fromReference(ref) }
            val customReferences = origin._customReferences.map { (_, ref) -> Reference.fromCustomReference(ref) }
            return PackageGuideModel((references + customReferences))
        }
    }
}