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

package moe.kanon.epubby

import moe.kanon.epubby.packages.Bindings
import moe.kanon.epubby.packages.Collection
import moe.kanon.epubby.packages.Guide
import moe.kanon.epubby.packages.Manifest
import moe.kanon.epubby.packages.Metadata
import moe.kanon.epubby.packages.PackageDocument
import moe.kanon.epubby.packages.Spine
import moe.kanon.epubby.packages.Tours
import moe.kanon.epubby.utils.internal.logger

internal class BookValidator internal constructor(val book: Book) {
    @JvmSynthetic
    internal fun validate() {
        logger.info { "Starting validation process of book <$book>.." }
        validatePackageDocument(book.packageDocument)
    }

    // -- PACKAGE-DOCUMENT -- \\
    private fun validatePackageDocument(packageDocument: PackageDocument) {
        // TODO: Validate that the 'prefix' on the 'package-document' only contains valid prefixes

        // parts
        validateMetadata(packageDocument.metadata)
        validateManifest(packageDocument.manifest)
        validateSpine(packageDocument.spine)
        packageDocument.guide?.also { validateGuide(it) }
        packageDocument.bindings?.also { validateBindings(it) }
        packageDocument.collection?.also { validateCollection(it) }
        packageDocument.tours?.also { validateTours(it) }
    }

    private fun validateMetadata(metadata: Metadata) {
        if (metadata.identifiers.isEmpty()) fail("metadata 'identifiers' is not allowed to be empty")
        if (metadata.titles.isEmpty()) fail("metadata 'titles' is not allowed to be empty")
        if (metadata.languages.isEmpty()) fail("metadata 'languages' is not allowed to be empty")
        if (metadata.metaElements.isEmpty()) fail("metadata 'meta-elements' is not allowed to be empty")
    }

    private fun validateManifest(manifest: Manifest) {

    }

    private fun validateSpine(spine: Spine) {

    }

    private fun validateGuide(guide: Guide) {

    }

    private fun validateBindings(bindings: Bindings) {

    }

    private fun validateCollection(collection: Collection) {

    }

    private fun validateTours(tours: Tours) {
        for ((_, tour) in tours.entries) {
            if (tour.sites.isEmpty()) fail("tour 'sites' is not allowed to be empty")
        }
    }

    private fun fail(reason: String): Nothing = throw InvalidBookException(reason)
}