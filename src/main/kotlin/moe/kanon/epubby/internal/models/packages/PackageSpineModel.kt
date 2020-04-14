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
import moe.kanon.epubby.internal.ElementNamespaces
import moe.kanon.epubby.internal.YesNoBooleanSerializer
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import moe.kanon.epubby.internal.ElementNamespaces.OPF as OPF_NAMESPACE

@Serializable
@XmlSerialName("spine", OPF_NAMESPACE, "")
internal data class PackageSpineModel(
    @SerialName("id") val identifier: String? = null,
    @SerialName("page-progression-direction") val pageProgressionDirection: String? = null,
    @SerialName("toc") val tableOfContentsIdentifier: String? = null,
    @XmlSerialName("itemref", OPF_NAMESPACE, "") val references: List<ItemReference>
) {
    @Serializable
    data class ItemReference(
        @SerialName("idref") val identifierReference: String,
        @SerialName("id") val identifier: String? = null,
        @Serializable(with = YesNoBooleanSerializer::class)
        @SerialName("linear") val isLinear: Boolean = true,
        val properties: String? = null
    )
}