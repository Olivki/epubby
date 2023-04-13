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

package net.ormr.epubby.internal.models.content

import dev.epubby.Epub3Feature
import dev.epubby.ReadingDirection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE_PREFIX
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE_URI
import net.ormr.epubby.internal.Namespaces.OPF_PREFIX
import net.ormr.epubby.internal.Namespaces.OPF_URI
import net.ormr.epubby.internal.Namespaces.XML_PREFIX
import net.ormr.epubby.internal.Namespaces.XML_URI
import net.ormr.epubby.internal.models.SerializedName
import net.ormr.epubby.internal.xml.XmlAdditionalNamespaces
import net.ormr.epubby.internal.xml.XmlInheritNamespace
import net.ormr.epubby.internal.xml.XmlNamespace

// https://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm
// https://www.w3.org/publishing/epub3/epub-packages.html
// TODO: 'id' attributes must be unique within the document scope
//       this means that all elements in a 'Package' when an 'id' is defined
//       it needs to be fully unique
@Serializable
@SerialName("package")
@XmlNamespace(prefix = "", OPF_URI)
@SerializedName("package")
internal data class PackageModel(
    val version: String,
    @SerialName("unique-identifier")
    val uniqueIdentifier: String,
    @SerialName("dir")
    val readingDirection: ReadingDirection?,
    @SerialName("id")
    val identifier: String?,
    @SerialName("prefix")
    @property:Epub3Feature
    val prefixes: String?,
    @SerialName("lang")
    @XmlNamespace(XML_PREFIX, XML_URI)
    val language: String?,
    @XmlInheritNamespace
    @XmlAdditionalNamespaces([XmlNamespace(DUBLIN_CORE_PREFIX, DUBLIN_CORE_URI), XmlNamespace(OPF_PREFIX, OPF_URI)])
    val metadata: MetadataModel,
    @XmlInheritNamespace
    val manifest: ManifestModel,
    // TODO: spine, guide, bindings, collection and tours
)