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

package moe.kanon.epubby.packages

import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.internal.DeprecatedFeature
import moe.kanon.epubby.internal.LegacyFeature
import moe.kanon.epubby.internal.NewFeature
import moe.kanon.epubby.prefixes.Prefixes
import moe.kanon.epubby.utils.Direction
import moe.kanon.kommons.requireThat
import java.util.Locale

class PackageDocument(
    val book: Book,
    uniqueIdentifier: String,
    var direction: Direction?,
    var identifier: String?,
    val prefix: Prefixes,
    var language: Locale?,
    val metadata: PackageMetadata,
    val manifest: PackageManifest,
    val spine: PackageSpine,
    @LegacyFeature(since = BookVersion.EPUB_3_0)
    var guide: PackageGuide?,
    @NewFeature(since = BookVersion.EPUB_3_0)
    @DeprecatedFeature(since = BookVersion.EPUB_3_2)
    var bindings: PackageBindings?,
    @NewFeature(since = BookVersion.EPUB_3_0)
    var collection: PackageCollection?,
    @DeprecatedFeature(since = BookVersion.EPUB_2_0)
    var tours: PackageTours?
) {
    var uniqueIdentifier: String = uniqueIdentifier
        set(value) {
            requireThat(value.isNotBlank()) { "unique-identifier can not be blank" }
            field = value
        }
}