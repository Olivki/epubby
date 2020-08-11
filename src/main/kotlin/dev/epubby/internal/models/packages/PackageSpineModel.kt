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
import dev.epubby.internal.elementOf
import dev.epubby.internal.getAttributeValueOrThrow
import dev.epubby.internal.models.SerializedName
import dev.epubby.packages.PackageSpine
import dev.epubby.page.Page
import dev.epubby.prefixes.Prefixes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import moe.kanon.kommons.lang.ParseException
import moe.kanon.kommons.lang.parse
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("spine")
data class PackageSpineModel internal constructor(
    @SerializedName("id")
    val identifier: String?,
    @SerializedName("page-progression-direction")
    val pageProgressionDirection: String?,
    @SerializedName("toc")
    val tableOfContentsIdentifier: String?,
    val references: ImmutableList<ItemReference>
) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("spine", NAMESPACE) {
        if (identifier != null) it.setAttribute("id", identifier)
        if (pageProgressionDirection != null) it.setAttribute("page-progression-direction", pageProgressionDirection)
        if (tableOfContentsIdentifier != null) it.setAttribute("toc", tableOfContentsIdentifier)

        for (reference in references) {
            it.addContent(reference.toElement())
        }
    }

    @JvmSynthetic
    internal fun toPackageSpine(book: Book, prefixes: Prefixes): PackageSpine {
        TODO("'toPackageSpine' operation is not implemented yet.")
    }

    @SerializedName("itemref")
    data class ItemReference internal constructor(
        @SerializedName("idref")
        val identifierReference: String,
        @SerializedName("id")
        val identifier: String?,
        @SerializedName("linear")
        val isLinear: Boolean, //  = true
        val properties: String?
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("itemref", NAMESPACE) {
            it.setAttribute("idref", identifierReference)
            if (identifier != null) it.setAttribute("id", identifier)
            it.setAttribute("linear", if (isLinear) "yes" else "no")
            if (properties != null) it.setAttribute("properties", properties)
        }

        @JvmSynthetic
        internal fun toPage(book: Book): Page {
            TODO("'toItemReference' operation is not implemented yet.")
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): ItemReference {
                val identifierReference = element.getAttributeValueOrThrow("idref")
                val identifier = element.getAttributeValue("id")
                val isLinear = element.getAttributeValue("linear")?.let(::parseLinear) ?: true
                val properties = element.getAttributeValue("properties")
                return ItemReference(identifierReference, identifier, isLinear, properties)
            }

            private fun parseLinear(value: String): Boolean = try {
                Boolean.parse(value)
            } catch (e: ParseException) {
                throw MalformedBookException("Expected value of 'linear' is 'yes' or 'no', was '$value'.", e)
            }

            @JvmSynthetic
            internal fun fromPage(origin: Page): ItemReference {
                TODO("'fromItemReference' operation is not implemented yet.")
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageSpineModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageSpineModel {
            val identifier = element.getAttributeValue("id")
            val pageProgressionDirection = element.getAttributeValue("page-progression-direction")
            val tableOfContentsIdentifier = element.getAttributeValue("toc")
            val references = element.getChildren("itemref", element.namespace)
                .tryMap { ItemReference.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .ifEmpty { throw MalformedBookException.forMissing("spine", "itemref") }
                .toPersistentList()
            return PackageSpineModel(identifier, pageProgressionDirection, tableOfContentsIdentifier, references)
        }

        @JvmSynthetic
        internal fun fromPackageSpine(origin: PackageSpine): PackageSpineModel {
            TODO("'fromPackageSpine' operation is not implemented yet.")
        }
    }
}