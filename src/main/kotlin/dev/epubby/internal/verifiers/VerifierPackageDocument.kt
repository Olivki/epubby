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

package dev.epubby.internal.verifiers

import dev.epubby.EpubVersion
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.packages.PackageDocument

internal object VerifierPackageDocument {
    // TODO: verify more things
    internal fun verify(packageDocument: PackageDocument) {
        checkFeaturesAlignsWithVersion(packageDocument.epub.version, packageDocument)
    }

    private fun checkFeaturesAlignsWithVersion(
        version: EpubVersion,
        packageDocument: PackageDocument,
    ): Unit = checkVersion("PackageDocument", version) {
        verify(EPUB_3_0, packageDocument::bindings)
        verify(EPUB_3_0, packageDocument::collection)
    }
}