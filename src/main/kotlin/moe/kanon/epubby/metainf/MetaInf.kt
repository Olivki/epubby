/*
 * Copyright 2019 Oliver Berg
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

@file:Suppress("DataClassPrivateConstructor")

package moe.kanon.epubby.metainf

import java.nio.file.Path

/**
 * Represents the [META-INF](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf)
 * directory found in the root of an epub container.
 *
 * This class contains references to the [container.xml][Container], [encryption.xml][Encryption],
 * [manifest.xml][Manifest], [metadata.xml][Metadata], [rights.xml][Rights] and [signatures.xml][Signatures] files
 * located inside the `META-INF` directory.
 *
 * Note that *only* [container.xml][epubFile] is REQUIRED to exist in an epub, the others are OPTIONAL.
 *
 * @property [directory] The path that points to the `META-INF` directory of the epub container.
 * @property [epubFile] TODO
 * @property [encryption] TODO
 * @property [manifest] TODO
 * @property [metadata] TODO
 * @property [rights] TODO
 * @property [signatures] TODO
 */
data class MetaInf private constructor(
    val epubFile: Path,
    val directory: Path,
    val container: MetaInfContainer,
    val encryption: MetaInfEncryption?,
    val manifest: MetaInfManifest?,
    val metadata: MetaInfMetadata?,
    val rights: MetaInfRights?,
    val signatures: MetaInfSignatures?
) {
    companion object {

    }

    override fun toString(): String = "MetaInf[$epubFile]"
}