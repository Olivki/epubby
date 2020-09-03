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

package dev.epubby.packages

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.BookVersion.EPUB_2_0
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.BookVersion.EPUB_3_2
import dev.epubby.builders.packages.PackageDocumentBuilder
import dev.epubby.files.DirectoryFile
import dev.epubby.files.RegularFile
import dev.epubby.internal.*
import dev.epubby.packages.guide.PackageGuide
import dev.epubby.packages.metadata.PackageMetadata
import dev.epubby.prefixes.Prefixes
import dev.epubby.prefixes.prefixesOf
import dev.epubby.utils.Direction
import java.util.Locale
import java.util.UUID

class PackageDocument(
    uniqueIdentifier: String,
    override val book: Book,
    val metadata: PackageMetadata,
    val manifest: PackageManifest,
    val spine: PackageSpine,
    var direction: Direction? = null,
    var identifier: String? = null,
    val prefixes: Prefixes = prefixesOf(),
    // TODO: change to 'String'?
    var language: Locale? = null,
    @MarkedAsLegacy(`in` = EPUB_3_0)
    var guide: PackageGuide? = null,
    @IntroducedIn(version = EPUB_3_0)
    @MarkedAsDeprecated(`in` = EPUB_3_2)
    bindings: PackageBindings? = null,
    @IntroducedIn(version = EPUB_3_0)
    collection: PackageCollection? = null,
    @MarkedAsDeprecated(`in` = EPUB_2_0)
    var tours: PackageTours? = null
) : BookElement {
    companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageDocument::class)

        // TODO: remove?
        @JvmStatic
        fun builder(): PackageDocumentBuilder = PackageDocumentBuilder()
    }

    /**
     * TODO
     *
     * @see [directory]
     */
    val file: RegularFile
        get() = book.metaInf.container.packageDocument.fullPath

    /**
     * The directory where the [file] is stored.
     *
     * Most of the time this directory will be called `OEBPS`, but that name is not mandatory.
     */
    val directory: DirectoryFile
        get() = file.parent ?: throw IllegalStateException("'PackageDocument.file' should never be null!")

    /**
     * TODO
     *
     * @see [generateNewUniqueIdentifier]
     */
    var uniqueIdentifier: String = uniqueIdentifier
        set(value) {
            require(value.isNotBlank()) { "unique-identifier must not be blank" }
            field = value
        }

    /**
     * Sets the [uniqueIdentifier] of the package-document to that of [a randomly generated uuid][UUID.randomUUID].
     */
    // TODO: rename to 'newUniqueIdentifier'?
    fun generateNewUniqueIdentifier() {
        uniqueIdentifier = UUID.randomUUID().toString()
    }

    @IntroducedIn(version = EPUB_3_0)
    @MarkedAsDeprecated(`in` = EPUB_3_2)
    var bindings: PackageBindings? by versioned(bindings, "bindings", EPUB_3_0)

    @IntroducedIn(version = EPUB_3_0)
    var collection: PackageCollection? by versioned(collection, "collection", EPUB_3_0)

    override val elementName: String
        get() = "PackageDocument"

    override fun toString(): String =
        "PackageDocument(uniqueIdentifier='$uniqueIdentifier', direction=$direction, identifier=$identifier, prefixes=$prefixes, language=$language)"
}