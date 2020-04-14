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

package moe.kanon.epubby.internal.models

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import moe.kanon.epubby.internal.ElementNamespaces.DUBLIN_CORE as DUBLIN_CORE_NAMESPACE
import moe.kanon.epubby.internal.ElementPrefixes.DUBLIN_CORE as DUBLIN_CORE_PREFIX

@Serializable
internal sealed class DublinCoreModel {
    abstract val identifier: String?
    abstract val content: String
    abstract val direction: String?
    abstract val language: String?

    @Serializable
    @XmlSerialName("date", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Date(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("format", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Format(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("identifier", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Identifier(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("language", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Language(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("source", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Source(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("type", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Type(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("contributor", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Contributor(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("coverage", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Coverage(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("creator", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Creator(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("description", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Description(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("publisher", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Publisher(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("relation", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Relation(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("rights", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Rights(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("subject", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Subject(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()

    @Serializable
    @XmlSerialName("title", DUBLIN_CORE_NAMESPACE, DUBLIN_CORE_PREFIX)
    data class Title(
        override val content: String,
        override val identifier: String? = null,
        override val direction: String? = null,
        override val language: String? = null
    ) : DublinCoreModel()
}