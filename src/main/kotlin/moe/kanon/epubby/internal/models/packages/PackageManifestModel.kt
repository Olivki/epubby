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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import moe.kanon.epubby.internal.ElementNamespaces.OPF as OPF_NAMESPACE

@Serializable
@XmlSerialName("manifest", OPF_NAMESPACE, "")
internal data class PackageManifestModel(
    @SerialName("id") val identifier: String? = null,
    @XmlSerialName("item", OPF_NAMESPACE, "") val items: List<Item>
) {
    @Serializable
    data class Item(
        @SerialName("id") val identifier: String,
        val href: String,
        @SerialName("media-type") val mediaType: String? = null,
        val fallback: String? = null,
        @SerialName("media-overlay") val mediaOverlay: String? = null,
        val properties: String? = null
    )
}