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

@file:OptIn(Epub2Feature::class)

package net.ormr.epubby.internal.models.dublincore

import dev.epubby.Epub2Feature
import dev.epubby.ReadingDirection
import dev.epubby.dublincore.CreativeRole
import dev.epubby.dublincore.LocalizedDublinCore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.Namespaces.OPF_PREFIX
import net.ormr.epubby.internal.Namespaces.OPF_URI
import net.ormr.epubby.internal.xml.XmlNamespace
import net.ormr.epubby.internal.xml.XmlTextValue

internal sealed interface LocalizedDublinCoreModel : DublinCoreModel {
    val direction: ReadingDirection?
    val language: String?

    override fun toDublinCore(): LocalizedDublinCore

    @Serializable
    @SerialName("contributor")
    data class ContributorModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
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
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Contributor =
            LocalizedDublinCore.Contributor(identifier, direction, language, role, fileAs, content)
    }

    @Serializable
    @SerialName("coverage")
    data class CoverageModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Coverage =
            LocalizedDublinCore.Coverage(identifier, direction, language, content)
    }

    @Serializable
    @SerialName("creator")
    data class CreatorModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
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
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Creator =
            LocalizedDublinCore.Creator(identifier, direction, language, role, fileAs, content)
    }

    @Serializable
    @SerialName("description")
    data class DescriptionModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Description =
            LocalizedDublinCore.Description(identifier, direction, language, content)
    }

    @Serializable
    @SerialName("publisher")
    data class PublisherModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Publisher =
            LocalizedDublinCore.Publisher(identifier, direction, language, content)
    }

    @Serializable
    @SerialName("relation")
    data class RelationModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Relation =
            LocalizedDublinCore.Relation(identifier, direction, language, content)
    }

    @Serializable
    @SerialName("rights")
    data class RightsModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Rights =
            LocalizedDublinCore.Rights(identifier, direction, language, content)
    }

    @Serializable
    @SerialName("subject")
    data class SubjectModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Subject =
            LocalizedDublinCore.Subject(identifier, direction, language, content)
    }

    @Serializable
    @SerialName("title")
    data class TitleModel(
        @SerialName("id")
        override val identifier: String?,
        @SerialName("dir")
        override val direction: ReadingDirection?,
        @SerialName("lang")
        override val language: String?,
        @XmlTextValue
        override val content: String?,
    ) : LocalizedDublinCoreModel {
        override fun toDublinCore(): LocalizedDublinCore.Title =
            LocalizedDublinCore.Title(identifier, direction, language, content)
    }
}