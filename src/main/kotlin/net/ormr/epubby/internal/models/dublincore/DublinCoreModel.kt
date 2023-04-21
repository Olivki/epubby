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
import dev.epubby.dublincore.*
import net.ormr.epubby.internal.models.SerializedName

// https://www.dublincore.org/specifications/dublin-core/dces/
internal sealed class DublinCoreModel(val name: String) {
    @SerializedName("id")
    abstract val identifier: String?
    abstract val content: String?

    abstract fun toDublinCore(): DublinCore

    @SerializedName("date")
    data class DateModel(
        override val identifier: String?,
        @property:Epub2Feature
        val event: DateEvent?,
        override val content: String?,
    ) : DublinCoreModel("date") {
        override fun toDublinCore(): DublinCoreDate = DublinCoreDate(identifier, event, content)
    }

    @SerializedName("format")
    data class FormatModel(
        override val identifier: String?,
        override val content: String?,
    ) : DublinCoreModel("format") {
        override fun toDublinCore(): DublinCoreFormat = DublinCoreFormat(identifier, content)
    }

    @SerializedName("identifier")
    data class IdentifierModel(
        override val identifier: String?,
        @property:Epub2Feature
        val scheme: String?,
        override val content: String?,
    ) : DublinCoreModel("identifier") {
        override fun toDublinCore(): DublinCoreIdentifier = DublinCoreIdentifier(identifier, scheme, content)
    }

    @SerializedName("language")
    data class LanguageModel(
        override val identifier: String?,
        override val content: String?,
    ) : DublinCoreModel("language") {
        override fun toDublinCore(): DublinCoreLanguage = DublinCoreLanguage(identifier, content)
    }

    @SerializedName("source")
    data class SourceModel(
        override val identifier: String?,
        override val content: String?,
    ) : DublinCoreModel("source") {
        override fun toDublinCore(): DublinCoreSource = DublinCoreSource(identifier, content)
    }

    @SerializedName("type")
    data class TypeModel(
        override val identifier: String?,
        override val content: String?,
    ) : DublinCoreModel("type") {
        override fun toDublinCore(): DublinCoreType = DublinCoreType(identifier, content)
    }
}