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
import moe.kanon.epubby.internal.elementOf
import moe.kanon.epubby.internal.getAttributeValueOrThrow
import moe.kanon.epubby.internal.models.SerialName
import moe.kanon.epubby.mapToValues
import moe.kanon.epubby.packages.PackageSpine
import moe.kanon.epubby.prefixes.Prefixes
import moe.kanon.epubby.tryMap
import moe.kanon.kommons.lang.ParseException
import moe.kanon.kommons.lang.parse
import org.apache.logging.log4j.kotlin.loggerOf
import org.jdom2.Element
import moe.kanon.epubby.internal.Namespaces.OPF as NAMESPACE

@SerialName("spine")
internal data class PackageSpineModel internal constructor(
    @SerialName("id") internal val identifier: String?,
    @SerialName("page-progression-direction") internal val pageProgressionDirection: String?,
    @SerialName("toc") internal val tableOfContentsIdentifier: String?,
    internal val references: List<ItemReference>
) {
    internal fun toElement(): Element = elementOf("spine", NAMESPACE) {
        TODO("'toElement' operation is not implemented.")
    }

    internal fun toPackageSpine(book: Book, prefixes: Prefixes): PackageSpine {
        TODO("'toPackageSpine' operation is not implemented yet.")
    }

    @SerialName("itemref")
    data class ItemReference(
        @SerialName("idref") val identifierReference: String,
        @SerialName("id") val identifier: String?,
        @SerialName("linear") val isLinear: Boolean, //  = true
        val properties: String?
    ) {
        internal fun toElement(): Element = elementOf("itemref", NAMESPACE) {
            it.setAttribute("idref", identifierReference)
            if (identifier != null) it.setAttribute("id", identifier)
            it.setAttribute("linear", if (isLinear) "yes" else "no")
            if (properties != null) it.setAttribute("properties", properties)
        }

        internal fun toItemReference(book: Book): PackageSpine.ItemReference {
            TODO("'toItemReference' operation is not implemented yet.")
        }

        internal companion object {
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

            internal fun fromItemReference(origin: PackageSpine.ItemReference): ItemReference {
                TODO("'fromItemReference' operation is not implemented yet.")
            }
        }
    }

    internal companion object {
        private val logger = InlineLogger(PackageSpineModel::class)

        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageSpineModel {
            val identifier = element.getAttributeValue("id")
            val pageProgressionDirection = element.getAttributeValue("page-progression-direction")
            val tableOfContentsIdentifier = element.getAttributeValue("toc")
            val references = element.getChildren("itemref", element.namespace)
                .tryMap { ItemReference.fromElement(it) }
                .mapToValues(logger, strictness)
                .ifEmpty { throw MalformedBookException.forMissing("spine", "itemref") }
            return PackageSpineModel(identifier, pageProgressionDirection, tableOfContentsIdentifier, references)
        }

        internal fun fromPackageSpine(origin: PackageSpine): PackageSpineModel {
            TODO("'fromPackageSpine' operation is not implemented yet.")
        }
    }
}