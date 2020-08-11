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

package dev.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Book
import dev.epubby.BookVersion
import dev.epubby.ParseStrictness
import dev.epubby.internal.*
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.parser.PrefixesParser
import dev.epubby.packages.PackageDocument
import dev.epubby.prefixes.emptyPrefixes
import dev.epubby.utils.Direction
import moe.kanon.kommons.io.paths.name
import org.jdom2.Document
import org.jdom2.Namespace
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.Locale

@SerializedName("package")
data class PackageDocumentModel internal constructor(
    private val filePath: String,
    val version: String,
    @SerializedName("unique-identifier")
    val uniqueIdentifier: String,
    @SerializedName("dir")
    val direction: String?,
    @SerializedName("id")
    val identifier: String?,
    @SerializedName("prefix")
    val prefixes: String?,
    @SerializedName("xml:lang")
    val language: String?,
    val metadata: PackageMetadataModel,
    val manifest: PackageManifestModel,
    val spine: PackageSpineModel,
    val guide: PackageGuideModel?,
    val bindings: PackageBindingsModel?,
    val collection: PackageCollectionModel?,
    val tours: PackageToursModel?
) {
    private fun toDocument(): Document = documentOf("package", Namespaces.OPF) { _, root ->
        root.setAttribute("version", version)
        root.setAttribute("unique-identifier", uniqueIdentifier)
        if (direction != null) root.setAttribute("dir", direction)
        if (identifier != null) root.setAttribute("id", identifier)
        if (prefixes != null) root.setAttribute("prefix", prefixes)
        if (language != null) root.setAttribute("lang", language, Namespace.XML_NAMESPACE)

        root.addContent(metadata.toElement())
        root.addContent(manifest.toElement())
        root.addContent(spine.toElement())
        if (guide != null) root.addContent(guide.toElement())
        if (bindings != null) root.addContent(bindings.toElement())
        if (collection != null) root.addContent(collection.toElement())
        if (tours != null) root.addContent(tours.toElement())
    }

    @JvmSynthetic
    internal fun toPackageDocument(book: Book): PackageDocument {
        // TODO: make use of 'version' ?
        val version = BookVersion.parse(version)
        val direction = direction?.let { Direction.fromTag(it) }
        val prefixes = prefixes?.let { PrefixesParser.parse(it) } ?: emptyPrefixes()
        val language = language?.let(Locale::forLanguageTag)
        val metadata = metadata.toPackageMetadata(book, prefixes)
        val manifest = manifest.toPackageManifest(book, prefixes)
        val spine = spine.toPackageSpine(book, prefixes)
        val guide = guide?.toPackageGuide(book)
        val bindings = bindings?.toPackageBindings(book)
        val collection = collection?.toPackageCollection(book, prefixes)
        val tours = tours?.toPackageTours(book)

        return PackageDocument(
            uniqueIdentifier,
            book,
            metadata,
            manifest,
            spine,
            direction,
            identifier,
            prefixes,
            language,
            guide,
            bindings,
            collection,
            tours
        )
    }

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        toDocument().writeTo(fileSystem.getPath(filePath))
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageDocumentModel::class)

        @JvmSynthetic
        internal fun fromFile(
            file: Path,
            strictness: ParseStrictness
        ): PackageDocumentModel = documentFrom(file).use { _, root ->
            val version = root.getAttributeValueOrThrow("version")
            val uniqueIdentifier = root.getAttributeValueOrThrow("unique-identifier")
            val direction = root.getAttributeValue("dir")
            val identifier = root.getAttributeValue("id")
            val prefixes = root.getAttributeValue("prefix")
            val language = root.getAttributeValue("lang", Namespace.XML_NAMESPACE)

            val metadata = root.getChildOrThrow("metadata", root.namespace)
                .let { PackageMetadataModel.fromElement(it, strictness) }
            val manifest = root.getChildOrThrow("manifest", root.namespace)
                .let { PackageManifestModel.fromElement(it, strictness) }
            val spine = root.getChildOrThrow("spine", root.namespace)
                .let { PackageSpineModel.fromElement(it, strictness) }

            val guide = root.getChild("guide", root.namespace)
                ?.let { PackageGuideModel.fromElement(it, strictness) }
            val bindings = root.getChild("bindings", root.namespace)
                ?.let { PackageBindingsModel.fromElement(it, strictness) }
            val collection = root.getChild("collection", root.namespace)
                ?.let { PackageCollectionModel.fromElement(it, strictness) }
            val tours = root.getChild("tours", root.namespace)
                ?.let { PackageToursModel.fromElement(it, strictness) }

            return@use PackageDocumentModel(
                file.name,
                version,
                uniqueIdentifier,
                direction,
                identifier,
                prefixes,
                language,
                metadata,
                manifest,
                spine,
                guide,
                bindings,
                collection,
                tours
            )
        }

        @JvmSynthetic
        internal fun fromPackageDocument(origin: PackageDocument): PackageDocumentModel {
            val filePath = origin.book.metaInf.container
            TODO("'fromPackageDocument' operation is not implemented yet.")
        }
    }
}