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

package moe.kanon.epubby.metainf

import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.malformed
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.notExists
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path

/**
 * Represents the [META-INF](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf)
 * directory found in the root of an epub container.
 *
 * This class contains references to the [container.xml][MetaInfContainer], [encryption.xml][MetaInfEncryption],
 * [manifest.xml][MetaInfManifest], [metadata.xml][MetaInfMetadata], [rights.xml][MetaInfRights] and
 * [signatures.xml][MetaInfSignatures] files located inside the `META-INF` directory.
 *
 * Note that *only* [container.xml][MetaInfContainer] is REQUIRED to exist in an epub, the others are OPTIONAL.
 *
 * @property [epub] The epub file that `this` meta-inf belongs to.
 * @property [directory] The path that points to the `META-INF` directory of the epub container.
 * @property [encryption] TODO
 * @property [manifest] TODO
 * @property [metadata] TODO
 * @property [rights] TODO
 * @property [signatures] TODO
 */
class MetaInf private constructor(
    val epub: Path,
    val directory: Path,
    val container: MetaInfContainer,
    val encryption: MetaInfEncryption?,
    val manifest: MetaInfManifest?,
    val metadata: MetaInfMetadata?,
    val rights: MetaInfRights?,
    val signatures: MetaInfSignatures?
) {
    @JvmSynthetic
    internal fun writeToFiles(fileSystem: FileSystem) {
        container.writeToFile(fileSystem)
        encryption?.also { it.writeToFile(fileSystem) }
        manifest?.also { it.writeToFile(fileSystem) }
        metadata?.also { it.writeToFile(fileSystem) }
        rights?.also { it.writeToFile(fileSystem) }
        signatures?.also { it.writeToFile(fileSystem) }
    }

    override fun toString(): String = buildString {
        append("MetaInf(epub='$epub', directory='$directory', container=$container")
        encryption?.also { append(", encryption=$it") }
        manifest?.also { append(", manifest=$it") }
        metadata?.also { append(", metadata=$it") }
        rights?.also { append(", rights=$it") }
        signatures?.also { append(", signatures=$it") }
        append(")")
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromDirectory(epub: Path, directory: Path, rootFile: Path): MetaInf {
            val containerFile = directory.resolve("container.xml")

            if (containerFile.notExists) {
                malformed(epub, directory, "required 'container.xml' is missing from meta-inf directory")
            }

            val container = MetaInfContainer.fromFile(epub, containerFile, rootFile)
            logger.debug { "Located package-document (OPF) at '${container.packageDocument.path}'" }
            val encryption = null
            val manifest = null
            val metadata = null

            val rightsFile = when {
                rootFile.resolve("rights.xml").exists -> rootFile.resolve("rights.xml")
                directory.resolve("rights.xml").exists -> directory.resolve("rights.xml")
                else -> null
            }
            val rights = rightsFile?.let { MetaInfRights.fromFile(epub, it, directory) }

            val signatures = null

            return MetaInf(epub, directory, container, encryption, manifest, metadata, rights, signatures).also {
                logger.trace { "Constructed meta-inf instance <$it>" }
            }
        }
    }
}