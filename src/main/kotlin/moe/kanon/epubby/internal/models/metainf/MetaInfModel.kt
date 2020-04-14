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

import moe.kanon.epubby.internal.parse
import moe.kanon.epubby.internal.parseIfExists
import moe.kanon.kommons.io.requireFileExistence
import nl.adaptivity.xmlutil.serialization.XML
import org.apache.logging.log4j.kotlin.logger
import java.nio.file.Path

internal data class MetaInfModel private constructor(
    val container: MetaInfContainerModel,
    val encryption: MetaInfEncryptionModel?,
    val manifest: MetaInfManifestModel?,
    val metadata: MetaInfMetadataModel?,
    val rights: MetaInfRightsModel?,
    val signatures: MetaInfSignaturesModel?
) {
    companion object {
        private val logger = logger()

        internal fun fromDirectory(directory: Path): MetaInfModel {
            val containerFile = directory.resolve("container.xml")
            // TODO: better error message and maybe custom exception?
            requireFileExistence(containerFile)

            val xml = XML {
                omitXmlDecl = false
                indent = 4
                unknownChildHandler = { input, _, name, _ ->
                    logger.error { "Encountered unknown child '$name', info [\n${input.locationInfo}]." }
                }
            }

            val container: MetaInfContainerModel = xml.parse(containerFile)
            val encryption: MetaInfEncryptionModel? = null //xml.parseIfExists(directory.resolve("encryption.xml"))
            val manifest: MetaInfManifestModel? = xml.parseIfExists(directory.resolve("manifest.xml"))
            val metadata: MetaInfMetadataModel? = xml.parseIfExists(directory.resolve("metadata.xml"))
            val rights: MetaInfRightsModel? = xml.parseIfExists(directory.resolve("encryption.xml"))
            val signatures: MetaInfSignaturesModel? = null //xml.parseIfExists(directory.resolve("signatures.xml"))
            return MetaInfModel(container, encryption, manifest, metadata, rights, signatures)
        }
    }
}