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
import moe.kanon.epubby.internal.ElementNamespaces
import moe.kanon.epubby.internal.ElementPrefixes
import moe.kanon.epubby.internal.parse
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.nio.file.Path
import moe.kanon.epubby.internal.ElementNamespaces.OPF as OPF_NAMESPACE
import moe.kanon.epubby.internal.ElementNamespaces.XML as XML_NAMESPACE
import moe.kanon.epubby.internal.ElementPrefixes.XML as XML_PREFIX

@Serializable
@XmlSerialName("package", OPF_NAMESPACE, "")
internal data class PackageDocumentModel(
    val version: String,
    @SerialName("unique-identifier") val uniqueIdentifier: String,
    @SerialName("dir") val direction: String? = null,
    @SerialName("id") val identifier: String? = null,
    val prefix: String? = null,
    @XmlSerialName("lang", XML_NAMESPACE, XML_PREFIX) val language: String? = null,
    val metadata: PackageMetadataModel,
    val manifest: PackageManifestModel,
    val spine: PackageSpineModel,
    val guide: PackageGuideModel? = null,
    val bindings: PackageBindingsModel? = null,
    val collection: PackageCollectionModel? = null,
    val tours: PackageToursModel? = null
) {
    companion object {
        internal fun fromFile(file: Path): PackageDocumentModel {
            val xml = XML()
            return xml.parse(file)
        }
    }
}