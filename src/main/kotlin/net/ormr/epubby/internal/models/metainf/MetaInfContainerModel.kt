/*
 * Copyright 2019-2023 Oliver Berg
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

package net.ormr.epubby.internal.models.metainf

import dev.epubby.metainf.MetaInfContainerParseError.EmptyRootFiles
import dev.epubby.metainf.MetaInfContainerParseError.InvalidModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.util.effect
import net.ormr.epubby.internal.util.safeDecodeFromFile
import net.ormr.epubby.internal.xml.Xml
import net.ormr.epubby.internal.xml.XmlListWrapperElement
import net.ormr.epubby.internal.xml.XmlNamespace
import java.nio.file.Path
import net.ormr.epubby.internal.Namespaces.META_INF_CONTAINER_PREFIX as PREFIX
import net.ormr.epubby.internal.Namespaces.META_INF_CONTAINER_URI as URI

@Serializable
@SerialName("container")
@XmlNamespace(PREFIX, URI)
internal data class MetaInfContainerModel(
    val version: String,
    @XmlListWrapperElement("rootfiles")
    val rootFiles: List<RootFileModel>,
    @XmlListWrapperElement("links")
    val links: List<RootFileModel> = emptyList(),
) {
    @Serializable
    @SerialName("rootfile")
    data class RootFileModel(
        @SerialName("full-path")
        val fullPath: String,
        @SerialName("media-type")
        val mediaType: String,
    )

    @Serializable
    @SerialName("link")
    data class LinkModel(
        val href: String,
        @SerialName("rel")
        val relation: String?,
        val mediaType: String?,
    )

    internal companion object {
        fun fromFile(file: Path) = effect {
            val model = Xml.safeDecodeFromFile(serializer(), file, ::InvalidModel).bind()
            ensure(model.rootFiles.isNotEmpty()) { EmptyRootFiles }
            model
        }
    }
}