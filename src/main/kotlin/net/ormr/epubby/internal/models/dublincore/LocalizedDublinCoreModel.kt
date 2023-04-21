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
import dev.epubby.dublincore.*
import dev.epubby.marc.CreativeRole
import net.ormr.epubby.internal.models.SerializedName

// // https://www.dublincore.org/specifications/dublin-core/dces/
internal sealed class LocalizedDublinCoreModel(name: String) : DublinCoreModel(name) {
    @SerializedName("dir")
    abstract val direction: ReadingDirection?

    @SerializedName("xml:lang")
    abstract val language: String?

    abstract override fun toDublinCore(): LocalizedDublinCore

    @SerializedName("contributor")
    data class ContributorModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        @property:Epub2Feature
        val role: CreativeRole?,
        @property:Epub2Feature
        val fileAs: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("contributor") {
        override fun toDublinCore(): DublinCoreContributor =
            DublinCoreContributor(identifier, direction, language, role, fileAs, content)
    }

    @SerializedName("coverage")
    data class CoverageModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("coverage") {
        override fun toDublinCore(): DublinCoreCoverage =
            DublinCoreCoverage(identifier, direction, language, content)
    }

    @SerializedName("creator")
    data class CreatorModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        @property:Epub2Feature
        val role: CreativeRole?,
        @property:Epub2Feature
        val fileAs: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("creator") {
        override fun toDublinCore(): DublinCoreCreator =
            DublinCoreCreator(identifier, direction, language, role, fileAs, content)
    }

    @SerializedName("description")
    data class DescriptionModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("description") {
        override fun toDublinCore(): DublinCoreDescription =
            DublinCoreDescription(identifier, direction, language, content)
    }

    @SerializedName("publisher")
    data class PublisherModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("publisher") {
        override fun toDublinCore(): DublinCorePublisher =
            DublinCorePublisher(identifier, direction, language, content)
    }

    @SerializedName("relation")
    data class RelationModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("relation") {
        override fun toDublinCore(): DublinCoreRelation =
            DublinCoreRelation(identifier, direction, language, content)
    }

    @SerializedName("rights")
    data class RightsModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("rights") {
        override fun toDublinCore(): DublinCoreRights =
            DublinCoreRights(identifier, direction, language, content)
    }

    @SerializedName("subject")
    data class SubjectModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("subject") {
        override fun toDublinCore(): DublinCoreSubject =
            DublinCoreSubject(identifier, direction, language, content)
    }

    @SerializedName("title")
    data class TitleModel(
        override val identifier: String?,
        override val direction: ReadingDirection?,
        override val language: String?,
        override val content: String?,
    ) : LocalizedDublinCoreModel("title") {
        override fun toDublinCore(): DublinCoreTitle =
            DublinCoreTitle(identifier, direction, language, content)
    }
}