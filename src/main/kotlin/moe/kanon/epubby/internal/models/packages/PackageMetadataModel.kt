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
import moe.kanon.epubby.internal.models.DublinCoreModel
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import moe.kanon.epubby.internal.ElementNamespaces.DUBLIN_CORE as DC_NAMESPACE
import moe.kanon.epubby.internal.ElementNamespaces.OPF as OPF_NAMESPACE
import moe.kanon.epubby.internal.ElementPrefixes.DUBLIN_CORE as DC_PREFIX

@Serializable
@XmlSerialName("metadata", OPF_NAMESPACE, "")
internal data class PackageMetadataModel(
    @XmlSerialName("identifier", DC_NAMESPACE, DC_PREFIX) val identifiers: List<DublinCoreModel.Identifier>,
    @XmlSerialName("title", DC_NAMESPACE, DC_PREFIX) val titles: List<DublinCoreModel.Title>,
    @XmlSerialName("language", DC_NAMESPACE, DC_PREFIX) val languages: List<DublinCoreModel.Language>,
    @XmlSerialName("date", DC_NAMESPACE, DC_PREFIX) val dates: List<DublinCoreModel.Date>? = null,
    @XmlSerialName("format", DC_NAMESPACE, DC_PREFIX) val formats: List<DublinCoreModel.Format>? = null,
    @XmlSerialName("source", DC_NAMESPACE, DC_PREFIX) val sources: List<DublinCoreModel.Source>? = null,
    @XmlSerialName("type", DC_NAMESPACE, DC_PREFIX) val types: List<DublinCoreModel.Type>? = null,
    @XmlSerialName("contributor", DC_NAMESPACE, DC_PREFIX) val contributors: List<DublinCoreModel.Contributor>? = null,
    @XmlSerialName("coverage", DC_NAMESPACE, DC_PREFIX) val coverages: List<DublinCoreModel.Coverage>? = null,
    @XmlSerialName("creator", DC_NAMESPACE, DC_PREFIX) val creators: List<DublinCoreModel.Creator>? = null,
    @XmlSerialName("description", DC_NAMESPACE, DC_PREFIX) val descriptions: List<DublinCoreModel.Description>? = null,
    @XmlSerialName("publisher", DC_NAMESPACE, DC_PREFIX) val publishers: List<DublinCoreModel.Publisher>? = null,
    @XmlSerialName("relation", DC_NAMESPACE, DC_PREFIX) val relations: List<DublinCoreModel.Relation>? = null,
    @XmlSerialName("rights", DC_NAMESPACE, DC_PREFIX) val rights: List<DublinCoreModel.Rights>? = null,
    @XmlSerialName("subject", DC_NAMESPACE, DC_PREFIX) val subjects: List<DublinCoreModel.Subject>? = null,
    @XmlSerialName("meta", OPF_NAMESPACE, "") val meta: List<Meta>,
    @XmlSerialName("link", OPF_NAMESPACE, "") val links: List<Link>?
) {
    @Serializable
    data class Meta(
        // opf-2
        val charset: String? = null,
        val content: String? = null,
        @SerialName("http-equiv") val httpEquivalent: String? = null,
        val name: String? = null,
        val scheme: String? = null,
        // opf-3
        @XmlValue(true) val value: String? = null,
        val property: String? = null,
        val identifier: String? = null,
        @SerialName("dir") val direction: String? = null,
        val refines: String? = null,
        // val scheme: String? = null,
        val language: String? = null
    )

    @Serializable
    data class Link(
        val href: String,
        @SerialName("rel") val relation: String? = null,
        @SerialName("media-type") val mediaType: String? = null,
        @SerialName("id") val identifier: String? = null,
        val properties: String? = null,
        val refines: String? = null
    )
}