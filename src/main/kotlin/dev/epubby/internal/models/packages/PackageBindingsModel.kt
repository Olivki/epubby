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
import dev.epubby.ParseMode
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.utils.elementOf
import dev.epubby.internal.utils.getAttributeValueOrThrow
import dev.epubby.internal.utils.mapToValues
import dev.epubby.internal.utils.tryMap
import dev.epubby.packages.PackageBindings
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("bindings")
internal data class PackageBindingsModel internal constructor(internal val mediaTypes: PersistentList<MediaTypeModel>) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("bindings", NAMESPACE) {
        mediaTypes.forEach { mediaType -> it.addContent(mediaType.toElement()) }
    }

    @JvmSynthetic
    internal fun toPackageBindings(epub: Epub): PackageBindings {
        TODO("'toPackageBindings' operation is not implemented yet.")
    }

    @SerializedName("mediaType")
    internal data class MediaTypeModel internal constructor(
        @SerializedName("media-type")
        internal val mediaType: String,
        internal val handler: String,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("mediaType", NAMESPACE) {
            it.setAttribute("media-type", mediaType)
            it.setAttribute("handler", handler)
        }

        @JvmSynthetic
        internal fun toMediaType(epub: Epub): PackageBindings.MediaType {
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
        internal fun fromElement(element: Element, mode: ParseMode): PackageBindingsModel {
            val mediaTypes = element.getChildren("mediaType", element.namespace)
                .tryMap { MediaTypeModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
                .toPersistentList()
            return PackageBindingsModel(mediaTypes)
        }

        @JvmSynthetic
        internal fun fromPackageBindings(origin: PackageBindings): PackageBindingsModel {
            TODO("'fromPackageBindings' operation is not implemented yet.")
        }
    }
}