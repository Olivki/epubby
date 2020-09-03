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
import dev.epubby.packages.PackageBindings
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("bindings")
data class PackageBindingsModel internal constructor(val mediaTypes: PersistentList<MediaTypeModel>) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("bindings", NAMESPACE) {
        mediaTypes.forEach { mediaType -> it.addContent(mediaType.toElement()) }
    }

    @JvmSynthetic
    internal fun toPackageBindings(book: Book): PackageBindings {
        TODO("'toPackageBindings' operation is not implemented yet.")
    }

    @SerializedName("mediaType")
    data class MediaTypeModel internal constructor(
        @SerializedName("media-type")
        val mediaType: String,
        val handler: String,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("mediaType", NAMESPACE) {
            it.setAttribute("media-type", mediaType)
            it.setAttribute("handler", handler)
        }

        @JvmSynthetic
        internal fun toMediaType(book: Book): PackageBindings.MediaType {
            TODO("'toMediaType' operation is not implemented yet.")
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): MediaTypeModel {
                val mediaType = element.getAttributeValueOrThrow("media-type")
                val handler = element.getAttributeValueOrThrow("handler")
                return MediaTypeModel(mediaType, handler)
            }

            @JvmSynthetic
            internal fun fromMediaType(origin: PackageBindings.MediaType): MediaTypeModel {
                TODO("'fromMediaType' operation is not implemented yet.")
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageBindingsModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageBindingsModel {
            val mediaTypes = element.getChildren("mediaType", element.namespace)
                .tryMap { MediaTypeModel.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .toPersistentList()
            return PackageBindingsModel(mediaTypes)
        }

        @JvmSynthetic
        internal fun fromPackageBindings(origin: PackageBindings): PackageBindings {
            TODO("'fromPackageBindings' operation is not implemented yet.")
        }
    }
}