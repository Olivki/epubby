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
import moe.kanon.epubby.ParseStrictness
import moe.kanon.epubby.internal.elementOf
import moe.kanon.epubby.internal.getAttributeOrThrow
import moe.kanon.epubby.internal.getAttributeValueOrThrow
import moe.kanon.epubby.internal.models.SerialName
import moe.kanon.epubby.mapToValues
import moe.kanon.epubby.packages.PackageBindings
import moe.kanon.epubby.tryMap
import org.apache.logging.log4j.kotlin.loggerOf
import org.jdom2.Element
import moe.kanon.epubby.internal.Namespaces.OPF as NAMESPACE

@SerialName("bindings")
internal data class PackageBindingsModel internal constructor(val mediaTypes: List<MediaType>) {
    internal fun toElement(): Element = elementOf("bindings", NAMESPACE) {
        mediaTypes.forEach { mediaType -> it.addContent(mediaType.toElement()) }
    }

    internal fun toPackageBindings(book: Book): PackageBindings {
        TODO("'toPackageBindings' operation is not implemented yet.")
    }

    @SerialName("mediaType")
    data class MediaType(@SerialName("media-type") val mediaType: String, val handler: String) {
        internal fun toElement(): Element = elementOf("mediaType", NAMESPACE) {
            it.setAttribute("media-type", mediaType)
            it.setAttribute("handler", handler)
        }

        internal fun toMediaType(book: Book): PackageBindings.MediaType {
            TODO("'toMediaType' operation is not implemented yet.")
        }

        internal companion object {
            internal fun fromElement(element: Element): MediaType {
                val mediaType = element.getAttributeValueOrThrow("media-type")
                val handler = element.getAttributeValueOrThrow("handler")
                return MediaType(mediaType, handler)
            }

            internal fun fromMediaType(origin: PackageBindings.MediaType): MediaType {
                TODO("'fromMediaType' operation is not implemented yet.")
            }
        }
    }

    internal companion object {
        private val logger = InlineLogger(PackageBindingsModel::class)

        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageBindingsModel {
            val mediaTypes = element.getChildren("mediaType", element.namespace)
                .tryMap { MediaType.fromElement(it) }
                .mapToValues(logger, strictness)
            return PackageBindingsModel(mediaTypes)
        }

        internal fun fromPackageBindings(origin: PackageBindings): PackageBindings {
            TODO("'fromPackageBindings' operation is not implemented yet.")
        }
    }
}