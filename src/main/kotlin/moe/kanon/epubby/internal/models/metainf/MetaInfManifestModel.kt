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

package moe.kanon.epubby.internal.models.metainf

import kotlinx.serialization.Serializable
import moe.kanon.epubby.internal.ElementNamespaces.META_INF_MANIFEST
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * Represents the [manifest.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-manifest.xml)
 * meta-inf file.
 */
// note that this feature exists only for compatibility with the ODF file format
@Serializable
@XmlSerialName("manifest", META_INF_MANIFEST, "manifest")
internal data class MetaInfManifestModel(
    @XmlSerialName("file-entry", META_INF_MANIFEST, "manifest") val fileEntries: List<FileEntry>
) {
    @Serializable
    data class FileEntry(
        @XmlSerialName("media-type", META_INF_MANIFEST, ".") val mediaType: String,
        @XmlSerialName("full-path", META_INF_MANIFEST, ".") val fullPath: String,
        @XmlSerialName("size", META_INF_MANIFEST, "manifest") val size: Int? = null, // non-negative
        @XmlSerialName("encryption-data", META_INF_MANIFEST, "manifest") val encryptionData: EncryptionData? = null
    ) {
        @Serializable
        data class EncryptionData(
            // 'checksum-type' & 'checksum' are not defined as optional elements, but yet in the example XML provided
            // in the documentation they use 'encryption-data' multiple times, but 'checksum-type' & 'checksum' is
            // never defined on any of them, so leaving them 'null' here for now
            @XmlSerialName("checksum-type", META_INF_MANIFEST, "..") val checksumType: String? = null,
            @XmlSerialName("checksum", META_INF_MANIFEST, "..") val checksum: String? = null,
            @XmlSerialName("algorithm", META_INF_MANIFEST, ".") val algorithm: Algorithm,
            @XmlSerialName("key-derivation", META_INF_MANIFEST, ".") val keyDerivation: KeyDerivation
        ) {
            @Serializable
            data class Algorithm(
                @XmlSerialName("algorithm-name", META_INF_MANIFEST, "...") val name: String,
                @XmlSerialName("initialisation-vector", META_INF_MANIFEST, "...") val initialisationVector: String
            )

            @Serializable
            data class KeyDerivation(
                @XmlSerialName("key-derivation-name", META_INF_MANIFEST, "...") val name: String,
                @XmlSerialName("iteration-count", META_INF_MANIFEST, "...") val iterationCount: Int, // non-negative
                @XmlSerialName("salt", META_INF_MANIFEST, "...") val salt: String
            )
        }
    }
}