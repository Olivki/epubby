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

package dev.epubby.builders.metainf

import dev.epubby.Book
import dev.epubby.builders.AbstractModelBuilder
import dev.epubby.builders.OptionalValue
import dev.epubby.builders.RequiredValue
import dev.epubby.internal.models.metainf.*
import dev.epubby.metainf.MetaInf
import dev.epubby.prefixes.Prefixes

/**
 * A builder for [MetaInf] instances.
 */
class MetaInfBuilder : AbstractModelBuilder<MetaInf, MetaInfModel>(MetaInfModel::class.java) {
    // TODO: documentation

    private var container: MetaInfContainerModel? = null

    @RequiredValue
    fun container(container: MetaInfContainerModel): MetaInfBuilder = apply {
        this.container = container
    }

    private var encryption: MetaInfEncryptionModel? = null

    @OptionalValue
    fun encryption(encryption: MetaInfEncryptionModel): MetaInfBuilder = apply {
        this.encryption = encryption
    }

    private var manifest: MetaInfManifestModel? = null

    @OptionalValue
    fun manifest(manifest: MetaInfManifestModel): MetaInfBuilder = apply {
        this.manifest = manifest
    }

    private var metadata: MetaInfMetadataModel? = null

    @OptionalValue
    fun metadata(metadata: MetaInfMetadataModel): MetaInfBuilder = apply {
        this.metadata = metadata
    }

    private var rights: MetaInfRightsModel? = null

    @OptionalValue
    fun rights(rights: MetaInfRightsModel): MetaInfBuilder = apply {
        this.rights = rights
    }

    private var signatures: MetaInfSignaturesModel? = null

    @OptionalValue
    fun signatures(signatures: MetaInfSignaturesModel): MetaInfBuilder = apply {
        this.signatures = signatures
    }

    override fun build(): MetaInfModel {
        val container = verify<MetaInfContainerModel>(this::container)

        return MetaInfModel(container, encryption, manifest, metadata, rights, signatures)
    }

    override fun build(book: Book): MetaInf = build().toMetaInf(book, Prefixes.empty())
}