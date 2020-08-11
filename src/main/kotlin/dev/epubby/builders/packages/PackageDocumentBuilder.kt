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

package dev.epubby.builders.packages

import dev.epubby.Book
import dev.epubby.BookVersion.EPUB_2_0
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.BookVersion.EPUB_3_2
import dev.epubby.builders.AbstractModelBuilder
import dev.epubby.builders.OptionalValue
import dev.epubby.builders.RequiredValue
import dev.epubby.internal.IntroducedIn
import dev.epubby.internal.MarkedAsDeprecated
import dev.epubby.internal.MarkedAsLegacy
import dev.epubby.internal.models.packages.*
import dev.epubby.packages.PackageDocument
import dev.epubby.prefixes.Prefix
import dev.epubby.prefixes.Prefixes
import dev.epubby.prefixes.emptyPrefixes
import dev.epubby.prefixes.requireKnown
import dev.epubby.utils.Direction
import java.util.Locale
import java.util.UUID

/**
 * A builder for [PackageDocument] instances.
 */
class PackageDocumentBuilder :
    AbstractModelBuilder<PackageDocument, PackageDocumentModel>(PackageDocumentModel::class.java) {
    // TODO: documentation

    // -- ATTRIBUTES -- \\
    private var uniqueIdentifier: String? = null

    @RequiredValue
    fun uniqueIdentifier(uniqueIdentifier: String): PackageDocumentBuilder = apply {
        require(uniqueIdentifier.isNotBlank()) { "'uniqueIdentifier' must not be blank" }
        this.uniqueIdentifier = uniqueIdentifier
    }

    /**
     * Sets the `uniqueIdentifier` attribute to a [randomly generated uuid][UUID.randomUUID].
     */
    @RequiredValue
    fun uniqueIdentifier(): PackageDocumentBuilder = apply {
        this.uniqueIdentifier = UUID.randomUUID().toString()
    }

    private var direction: Direction? = null

    @OptionalValue
    fun direction(direction: Direction): PackageDocumentBuilder = apply {
        this.direction = direction
    }

    private var identifier: String? = null

    @OptionalValue
    fun identifier(identifier: String): PackageDocumentBuilder = apply {
        this.identifier = identifier
    }

    private var prefixes: Prefixes = emptyPrefixes()

    @OptionalValue
    fun prefixes(prefixes: Prefixes): PackageDocumentBuilder = apply {
        this.prefixes = prefixes
    }

    fun prefix(prefix: Prefix): PackageDocumentBuilder = apply {
        requireKnown(prefix)
        prefixes.add(prefix)
    }

    private var language: Locale? = null

    @OptionalValue
    fun language(language: Locale): PackageDocumentBuilder = apply {
        this.language = language
    }

    // -- REQUIRED CHILDREN -- \\
    private var metadata: PackageMetadataModel? = null

    @RequiredValue
    fun metadata(metadata: PackageMetadataModel): PackageDocumentBuilder = apply {
        this.metadata = metadata
    }

    private var manifest: PackageManifestModel? = null

    @RequiredValue
    fun manifest(manifest: PackageManifestModel): PackageDocumentBuilder = apply {
        this.manifest = manifest
    }

    private var spine: PackageSpineModel? = null

    @RequiredValue
    fun spine(spine: PackageSpineModel): PackageDocumentBuilder = apply {
        this.spine = spine
    }

    // -- OPTIONAL CHILDREN -- \\
    private var guide: PackageGuideModel? = null

    @RequiredValue
    @MarkedAsLegacy(`in` = EPUB_3_0)
    fun guide(guide: PackageGuideModel): PackageDocumentBuilder = apply {
        this.guide = guide
    }

    private var bindings: PackageBindingsModel? = null

    @RequiredValue
    @IntroducedIn(version = EPUB_3_0)
    @MarkedAsDeprecated(`in` = EPUB_3_2)
    fun bindings(bindings: PackageBindingsModel): PackageDocumentBuilder = apply {
        this.bindings = bindings
    }

    private var collection: PackageCollectionModel? = null

    @RequiredValue
    @IntroducedIn(version = EPUB_3_0)
    fun collection(collection: PackageCollectionModel): PackageDocumentBuilder = apply {
        this.collection = collection
    }

    private var tours: PackageToursModel? = null

    @RequiredValue
    @MarkedAsDeprecated(`in` = EPUB_2_0)
    fun tours(tours: PackageToursModel): PackageDocumentBuilder = apply {
        this.tours = tours
    }

    override fun build(): PackageDocumentModel {
        val uniqueIdentifier = verify<String>(this::uniqueIdentifier)
        val prefixes = prefixes.ifEmpty { null }?.toStringForm()
        val metadata = verify<PackageMetadataModel>(this::metadata)
        val manifest = verify<PackageManifestModel>(this::manifest)
        val spine = verify<PackageSpineModel>(this::spine)

        return PackageDocumentModel(
            "",
            "",
            uniqueIdentifier,
            direction?.attributeName,
            identifier,
            prefixes,
            language?.toLanguageTag(),
            metadata,
            manifest,
            spine,
            guide,
            bindings,
            collection,
            tours
        )
    }

    override fun build(book: Book): PackageDocument = build().toPackageDocument(book)
}