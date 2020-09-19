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

package dev.epubby.internal.models.metainf

import com.github.michaelbull.logging.InlineLogger
import com.google.common.net.MediaType
import dev.epubby.*
import dev.epubby.files.RegularFile
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.utils.*
import dev.epubby.metainf.MetaInfContainer
import dev.epubby.prefixes.Prefixes
import dev.epubby.properties.encodeToString
import dev.epubby.properties.resolveLinkRelationship
import dev.epubby.utils.toNonEmptyList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jdom2.Document
import org.jdom2.Element
import java.nio.file.FileSystem
import java.nio.file.Path
import dev.epubby.internal.Namespaces.META_INF_CONTAINER as NAMESPACE

internal data class MetaInfContainerModel internal constructor(
    internal val file: Path,
    internal val version: String,
    internal val rootFiles: PersistentList<RootFileModel>,
    internal val links: PersistentList<LinkModel>,
) {
    private fun toDocument(): Document = documentOf("container", NAMESPACE) { _, root ->
        root.setAttribute("version", version)

        root.addContent(elementOf("rootfiles", root.namespace) {
            for (file in rootFiles) {
                it.addContent(file.toElement())
            }
        })

        if (links.isNotEmpty()) {
            root.addContent(elementOf("links", root.namespace) {
                for (link in links) {
                    it.addContent(link.toElement())
                }
            })
        }
    }

    @JvmSynthetic
    internal fun toMetaInfContainer(epub: Epub, prefixes: Prefixes): MetaInfContainer {
        val file = RegularFile.invoke(file, epub)
        val rootFiles = rootFiles.map { it.toRootFile(epub) }.toNonEmptyList()
        val links = links.mapTo(mutableListOf()) { it.toLink(epub, prefixes) }
        return MetaInfContainer(epub, file, version, rootFiles, links)
    }

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        toDocument().writeTo(fileSystem.getPath("/META-INF/container.xml"))
    }

    @SerializedName("rootfile")
    data class RootFileModel internal constructor(
        @SerializedName("full-path")
        val fullPath: String,
        @SerializedName("media-type")
        val mediaType: String,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("rootfile", NAMESPACE) {
            it.setAttribute("full-path", fullPath)
            it.setAttribute("media-type", mediaType)
        }

        @JvmSynthetic
        internal fun toRootFile(epub: Epub): MetaInfContainer.RootFile {
            val fullPath = when (val file = epub.getFile(fullPath)) {
                is RegularFile -> file
                else -> throw MalformedBookException("'fullPath' in $this points to a non regular file.")
            }
            val mediaType = MediaType.parse(mediaType)
            return MetaInfContainer.RootFile(epub, fullPath, mediaType)
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): RootFileModel {
                val fullPath = element.getAttributeValueOrThrow("full-path")
                val mediaType = element.getAttributeValueOrThrow("media-type")
                return RootFileModel(fullPath, mediaType)
            }

            @JvmSynthetic
            internal fun fromRootFile(origin: MetaInfContainer.RootFile): RootFileModel {
                val fullPath = origin.epub.root.relativize(origin.fullPath).toString()
                val mediaType = origin.mediaType.toString()
                return RootFileModel(fullPath, mediaType)
            }
        }
    }

    @SerializedName("link")
    data class LinkModel internal constructor(
        val href: String,
        @SerializedName("rel")
        val relation: String? = null,
        val mediaType: String? = null,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("link", NAMESPACE) {
            it.setAttribute("href", href)
            relation.ifNotNull { rel -> it.setAttribute("rel", rel) }
            mediaType.ifNotNull { mediaType -> it.setAttribute("mediaType", mediaType) }
        }

        @JvmSynthetic
        internal fun toLink(epub: Epub, prefixes: Prefixes): MetaInfContainer.Link {
            val relation = relation?.let { resolveLinkRelationship(it, prefixes) }
            val mediaType = mediaType?.let(MediaType::parse)
            return MetaInfContainer.Link(epub, href, relation, mediaType)
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): LinkModel {
                val href = element.getAttributeValueOrThrow("href")
                val relation = element.getAttributeValue("rel")
                val mediaType = element.getAttributeValue("mediaType")
                return LinkModel(href, relation, mediaType)
            }

            @JvmSynthetic
            internal fun fromLink(origin: MetaInfContainer.Link): LinkModel {
                val href = origin.href
                val relation = when {
                    origin.epub.version.isNewer(EpubVersion.EPUB_2_0) -> origin.relation?.encodeToString()
                    else -> null
                }
                val mediaType = origin.mediaType.toString()
                return LinkModel(href, relation, mediaType)
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(MetaInfContainerModel::class)

        @JvmSynthetic
        internal fun fromFile(
            file: Path,
            mode: ParseMode,
        ): MetaInfContainerModel = documentFrom(file).use { _, root ->
            val version = root.getAttributeValueOrThrow("version")
            val rootFiles = root.getChild("rootfiles", root.namespace)
                .getChildren("rootfile", root.namespace)
                .tryMap { RootFileModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
                .ifEmpty { throw MalformedBookException("'rootfiles' in meta-inf container is empty.") }
                .toPersistentList()
            val links = root.getChild("links", root.namespace)
                ?.getChildren("link", root.namespace)
                ?.tryMap { LinkModel.fromElement(it) }
                ?.mapToValues(LOGGER, mode)
                ?.toPersistentList() ?: persistentListOf()
            return@use MetaInfContainerModel(file, version, rootFiles, links)
        }

        @JvmSynthetic
        internal fun fromMetaInfContainer(origin: MetaInfContainer): MetaInfContainerModel {
            val rootFiles = origin.rootFiles.map { RootFileModel.fromRootFile(it) }.toPersistentList()
            val links = origin.links.map { LinkModel.fromLink(it) }.toPersistentList()
            return MetaInfContainerModel(origin.file.delegate, origin.version, rootFiles, links)
        }
    }
}