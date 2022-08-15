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

package dev.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Epub
import dev.epubby.EpubVersion
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.ParseMode
import dev.epubby.internal.IntroducedIn
import dev.epubby.internal.Namespaces
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.parser.PrefixesParser
import dev.epubby.internal.utils.*
import dev.epubby.packages.PackageDocument
import dev.epubby.prefixes.encodeToString
import dev.epubby.prefixes.prefixesOf
import dev.epubby.utils.Direction
import org.jdom2.Document
import org.jdom2.Namespace
import java.nio.file.Path
import java.util.*

@SerializedName("package")
internal data class PackageDocumentModel internal constructor(
    private val opfFile: Path,
    internal val version: String,
    @SerializedName("unique-identifier")
    internal val uniqueIdentifier: String,
    @SerializedName("dir")
    internal val direction: String?,
    @SerializedName("id")
    internal val identifier: String?,
    @SerializedName("prefix")
    @IntroducedIn(version = EPUB_3_0)
    internal val prefixes: String?,
    @SerializedName("xml:lang")
    internal val language: String?,
    internal val metadata: PackageMetadataModel,
    internal val manifest: PackageManifestModel,
    internal val spine: PackageSpineModel,
    internal val guide: PackageGuideModel?,
    internal val bindings: PackageBindingsModel?,
    internal val collection: PackageCollectionModel?,
    internal val tours: PackageToursModel?,
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
    internal fun toPackageDocument(epub: Epub): PackageDocument {
        // TODO: make use of 'version' ?
        val version = EpubVersion.parse(version)
        val opfFile = epub.metaInf.container.opf.fullPath
        val direction = direction?.let { Direction.fromString(it) }
        val prefixes = prefixes?.let { PrefixesParser.parse(it) } ?: prefixesOf()
        val language = language?.let(Locale::forLanguageTag)

        val metadata = metadata.toPackageMetadata(epub, prefixes)
        val manifest = manifest.toPackageManifest(epub, prefixes, opfFile)
        val spine = spine.toPackageSpine(epub, prefixes, manifest)
        val guide = guide?.toPackageGuide(epub, manifest)
        val bindings = bindings?.toPackageBindings(epub)
        val collection = collection?.toPackageCollection(epub, prefixes)
        val tours = tours?.toPackageTours(epub)

        return PackageDocument(
            uniqueIdentifier,
            epub,
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
    internal fun writeToFile() {
        toDocument().writeTo(opfFile)
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageDocumentModel::class)

        @JvmSynthetic
        internal fun fromDocument(
            document: Document,
            opfFile: Path,
            mode: ParseMode,
        ): PackageDocumentModel = document.use { _, root ->
            val version = root.getAttributeValueOrThrow("version")
            val uniqueIdentifier = root.getAttributeValueOrThrow("unique-identifier")
            val direction = root.getAttributeValue("dir")
            val identifier = root.getAttributeValue("id")
            val prefixes = root.getAttributeValue("prefix")
            val language = root.getAttributeValue("lang", Namespace.XML_NAMESPACE)

            val metadata = root.getChildOrThrow("metadata", root.namespace)
                .let { PackageMetadataModel.fromElement(it, mode) }
            val manifest = root.getChildOrThrow("manifest", root.namespace)
                .let { PackageManifestModel.fromElement(it, mode) }
            val spine = root.getChildOrThrow("spine", root.namespace)
                .let { PackageSpineModel.fromElement(it, mode) }

            val guide = root.getChild("guide", root.namespace)
                ?.let { PackageGuideModel.fromElement(it, mode) }
            val bindings = root.getChild("bindings", root.namespace)
                ?.let { PackageBindingsModel.fromElement(it, mode) }
            val collection = root.getChild("collection", root.namespace)
                ?.let { PackageCollectionModel.fromElement(it, mode) }
            val tours = root.getChild("tours", root.namespace)
                ?.let { PackageToursModel.fromElement(it, mode) }

            return@use PackageDocumentModel(
                opfFile,
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
            val opfFile = origin.epub.metaInf.container.opf.fullPath.delegate
            val version = origin.epub.version.toString()
            val direction = origin.direction?.value
            val prefixes = when {
                origin.epub.version.isOlder(EPUB_3_0) -> null
                else -> origin.prefixes.ifEmpty { null }?.encodeToString()
            }
            val language = origin.language?.toLanguageTag()
            val metadata = PackageMetadataModel.fromPackageMetadata(origin.metadata)
            val manifest = PackageManifestModel.fromPackageManifest(origin.manifest)
            val spine = PackageSpineModel.fromPackageSpine(origin.spine)
            val guide = origin.guide?.let { PackageGuideModel.fromPackageGuide(it) }
            val bindings = origin.bindings?.let { PackageBindingsModel.fromPackageBindings(it) }
            val collection = origin.collection?.let { PackageCollectionModel.fromPackageCollection(it) }
            val tours = origin.tours?.let { PackageToursModel.fromPackageTours(it) }

            return PackageDocumentModel(
                opfFile,
                version,
                origin.uniqueIdentifier,
                direction,
                origin.identifier,
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
    }
}