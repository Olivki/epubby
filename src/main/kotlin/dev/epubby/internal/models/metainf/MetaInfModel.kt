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

package dev.epubby.internal.models.metainf

import dev.epubby.Book
import dev.epubby.ParseStrictness
import dev.epubby.metainf.MetaInf
import dev.epubby.prefixes.Prefixes
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.requireFileExistence
import java.nio.file.FileSystem
import java.nio.file.Path

data class MetaInfModel internal constructor(
    internal val container: MetaInfContainerModel,
    internal val encryption: MetaInfEncryptionModel?,
    internal val manifest: MetaInfManifestModel?,
    internal val metadata: MetaInfMetadataModel?,
    internal val rights: MetaInfRightsModel?,
    internal val signatures: MetaInfSignaturesModel?
) {
    // TODO: 'prefixes' should not be needed here, as the meta-inf properties should *not* be inheriting from the
    //        prefixes defined in the package-document as the two don't know anything of each other, so a default
    //        value should be used instead, however i'm unsure of what default prefix meta-inf 'link' elements resolve
    //        against
    @JvmSynthetic
    internal fun toMetaInf(book: Book, prefixes: Prefixes): MetaInf {
        val container = container.toMetaInfContainer(book, prefixes)
        val encryption = encryption?.toMetaInfEncryption(book)
        val manifest = manifest?.toMetaInfManifest(book)
        val metadata = metadata?.toMetaInfMetadata(book)
        val rights = rights?.toMetaInfRights(book)
        val signatures = signatures?.toMetaInfSignatures(book)
        return MetaInf(book, container, encryption, manifest, metadata, rights, signatures)
    }

    @JvmSynthetic
    internal fun writeToDirectory(fileSystem: FileSystem) {
        container.writeToFile(fileSystem)
        encryption?.writeToFile(fileSystem)
        manifest?.writeToFile(fileSystem)
        metadata?.writeToFile(fileSystem)
        rights?.writeToFile(fileSystem)
        signatures?.writeToFile(fileSystem)
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromDirectory(directory: Path, strictness: ParseStrictness): MetaInfModel {
            val containerFile = directory.resolve("container.xml")

            // TODO: better error message/custom exception?
            requireFileExistence(containerFile)

            val container = MetaInfContainerModel.fromFile(containerFile, strictness)
            val encryption = directory.resolve("encryption.xml")
                .takeIf { it.exists }
                ?.let { MetaInfEncryptionModel.fromFile(it) }
            val manifest = directory.resolve("manifest.xml")
                .takeIf { it.exists }
                ?.let { MetaInfManifestModel.fromFile(it) }
            val metadata = directory.resolve("metadata.xml")
                .takeIf { it.exists }
                ?.let { MetaInfMetadataModel.fromFile(it) }
            val rights = directory.resolve("rights.xml")
                .takeIf { it.exists }
                ?.let { MetaInfRightsModel.fromFile(it) }
            val signatures = directory.resolve("signatures.xml")
                .takeIf { it.exists }
                ?.let { MetaInfSignaturesModel.fromFile(it) }

            return MetaInfModel(container, encryption, manifest, metadata, rights, signatures)
        }

        @JvmSynthetic
        internal fun fromMetaInf(origin: MetaInf): MetaInfModel {
            val container = MetaInfContainerModel.fromMetaInfContainer(origin.container)
            val encryption = origin.encryption?.let { MetaInfEncryptionModel.fromMetaInfEncryption(it) }
            val manifest = origin.manifest?.let { MetaInfManifestModel.fromMetaInfManifest(it) }
            val metadata = origin.metadata?.let { MetaInfMetadataModel.fromMetaInfMetadata(it) }
            val rights = origin.rights?.let { MetaInfRightsModel.fromMetaInfRights(it) }
            val signatures = origin.signatures?.let { MetaInfSignaturesModel.fromMetaInfSignatures(it) }
            return MetaInfModel(container, encryption, manifest, metadata, rights, signatures)
        }
    }
}