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
import dev.epubby.dublincore.DateEvent
import dev.epubby.dublincore.DublinCore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.Namespaces.OPF_PREFIX
import net.ormr.epubby.internal.Namespaces.OPF_URI
import net.ormr.epubby.internal.xml.XmlNamespace
import net.ormr.epubby.internal.xml.XmlTextValue
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE_PREFIX as PREFIX
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE_URI as URI

@Serializable(with = DublinCoreModelSerializer::class)
@XmlNamespace(PREFIX, URI)
internal sealed interface DublinCoreModel {
    val identifier: String?
    val content: String?

    fun toDublinCore(): DublinCore

    @Serializable
    @SerialName("date")
    data class DateModel(
        @SerialName("id")
        override val identifier: String?,
        @property:Epub2Feature
        @XmlNamespace(OPF_PREFIX, OPF_URI)
        val event: DateEvent?,
        @XmlTextValue
        override val content: String?,
    ) : DublinCoreModel {
        override fun toDublinCore(): DublinCore.Date = DublinCore.Date(identifier, event, content)
    }

    @Serializable
    @SerialName("format")
    data class FormatModel(
        @SerialName("id")
        override val identifier: String?,
        @XmlTextValue
        override val content: String?,
    ) : DublinCoreModel {
        override fun toDublinCore(): DublinCore.Format = DublinCore.Format(identifier, content)
    }

    @Serializable
    @SerialName("identifier")
    data class IdentifierModel(
        @SerialName("id")
        override val identifier: String?,
        @property:Epub2Feature
        @XmlNamespace(OPF_PREFIX, OPF_URI)
        val scheme: String?,
        @XmlTextValue
        override val content: String?,
    ) : DublinCoreModel {
        override fun toDublinCore(): DublinCore.Identifier = DublinCore.Identifier(identifier, scheme, content)
    }

    @Serializable
    @SerialName("language")
    data class LanguageModel(
        @SerialName("id")
        override val identifier: String?,
        @XmlTextValue
        override val content: String?,
    ) : DublinCoreModel {
        override fun toDublinCore(): DublinCore.Language = DublinCore.Language(identifier, content)
    }

    @Serializable
    @SerialName("source")
    data class SourceModel(
        @SerialName("id")
        override val identifier: String?,
        @XmlTextValue
        override val content: String?,
    ) : DublinCoreModel {
        override fun toDublinCore(): DublinCore.Source = DublinCore.Source(identifier, content)
    }

    @Serializable
    @SerialName("type")
    data class TypeModel(
        @SerialName("id")
        override val identifier: String?,
        @XmlTextValue
        override val content: String?,
    ) : DublinCoreModel {
        override fun toDublinCore(): DublinCore.Type = DublinCore.Type(identifier, content)
    }
}