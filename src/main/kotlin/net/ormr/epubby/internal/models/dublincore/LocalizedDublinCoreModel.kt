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

package net.ormr.epubby.internal.models.dublincore

import dev.epubby.Epub2Feature
import dev.epubby.dublincore.CreativeRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.Namespaces.OPF_PREFIX
import net.ormr.epubby.internal.Namespaces.OPF_URI
import net.ormr.epubby.internal.xml.XmlNamespace
import net.ormr.epubby.internal.xml.XmlTextValue

internal sealed interface LocalizedDublinCoreModel : DublinCoreModel {
    val direction: String?
    val language: String?

    @Serializable
    @SerialName("contributor")
    data class ContributorModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @property:Epub2Feature
        @XmlNamespace(OPF_PREFIX, OPF_URI)
        val role: CreativeRole?,
        @SerialName("file-as")
        @property:Epub2Feature
        @XmlNamespace(OPF_PREFIX, OPF_URI)
        val fileAs: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel

    @Serializable
    @SerialName("coverage")
    data class CoverageModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel

    @Serializable
    @SerialName("creator")
    data class CreatorModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @property:Epub2Feature
        @XmlNamespace(OPF_PREFIX, OPF_URI)
        val role: CreativeRole?,
        @SerialName("file-as")
        @property:Epub2Feature
        @XmlNamespace(OPF_PREFIX, OPF_URI)
        val fileAs: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel

    @Serializable
    @SerialName("description")
    data class DescriptionModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel

    @Serializable
    @SerialName("publisher")
    data class PublisherModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel

    @Serializable
    @SerialName("relation")
    data class RelationModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel

    @Serializable
    @SerialName("rights")
    data class RightsModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel

    @Serializable
    @SerialName("subject")
    data class SubjectModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel

    @Serializable
    @SerialName("title")
    data class TitleModel internal constructor(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: String?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel
}