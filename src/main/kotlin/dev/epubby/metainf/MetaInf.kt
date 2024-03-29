/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.metainf

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.files.DirectoryFile

class MetaInf internal constructor(
    override val epub: Epub,
    val directory: DirectoryFile,
    val container: MetaInfContainer,
    var encryption: MetaInfEncryption? = null,
    var manifest: MetaInfManifest? = null,
    var metadata: MetaInfMetadata? = null,
    var rights: MetaInfRights? = null,
    var signatures: MetaInfSignatures? = null
) : EpubElement {
    override val elementName: String
        get() = "/META-INF/"

    /**
     * Returns `true` if the epub that this meta-inf represents is encrypted in some manner, otherwise `false`.
     *
     * As the EPUB specification has set forth no standard for how a meta-inf [encryption] structure should look, it
     * states that as long as an `encryption` structure is present, that means that the epub is encrypted.
     */
    val isEncrypted: Boolean
        get() = encryption != null

    /**
     * Returns `true` if the epub that this meta-inf represents is governed rights *(DRM)* in some manner, otherwise
     * `false`.
     *
     * As the EPUB specification has set forth no standard for how a meta-inf [rights] structure should look, it states
     * that as long as an `rights` structure is present, that means that the epub is governed by rights *(DRM)* in some
     * manner.
     */
    val isGovernedByRights: Boolean
        get() = rights != null

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is MetaInf -> false
        container != other.container -> false
        encryption != other.encryption -> false
        manifest != other.manifest -> false
        metadata != other.metadata -> false
        rights != other.rights -> false
        signatures != other.signatures -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = container.hashCode()
        result = 31 * result + (encryption?.hashCode() ?: 0)
        result = 31 * result + (manifest?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (rights?.hashCode() ?: 0)
        result = 31 * result + (signatures?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "MetaInf(container=$container, encryption=$encryption, manifest=$manifest, metadata=$metadata, rights=$rights, signatures=$signatures)"
}