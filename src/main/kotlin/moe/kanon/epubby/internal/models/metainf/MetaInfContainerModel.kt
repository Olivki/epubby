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

package moe.kanon.epubby.internal.models.metainf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.kanon.epubby.internal.ElementNamespaces
import moe.kanon.epubby.internal.ElementNamespaces.META_INF_CONTAINER
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("container", META_INF_CONTAINER, "")
internal data class MetaInfContainerModel(
    val version: String,
    @XmlSerialName("rootfiles", META_INF_CONTAINER, "") val rootFiles: RootFiles,
    @XmlSerialName("links", META_INF_CONTAINER, "") val links: Links? = null
) {
    @Serializable
    data class RootFiles(@XmlSerialName("rootfile", META_INF_CONTAINER, "") val rootFiles: List<RootFile>)

    @Serializable
    data class RootFile(@SerialName("full-path") val fullPath: String, @SerialName("media-type") val mediaType: String)

    @Serializable
    data class Links(@XmlSerialName("link", META_INF_CONTAINER, "") val links: List<Link>)

    @Serializable
    data class Link(val href: String, @SerialName("rel") val relation: String, val mediaType: String? = null)
}