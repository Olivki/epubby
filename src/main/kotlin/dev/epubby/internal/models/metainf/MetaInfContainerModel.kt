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
import dev.epubby.internal.*
import dev.epubby.internal.models.SerializedName
import dev.epubby.metainf.ContainerVersion
import dev.epubby.metainf.MetaInfContainer
import dev.epubby.prefixes.Prefixes
import dev.epubby.properties.resolveLinkRelationship
import dev.epubby.properties.toStringForm
import dev.epubby.utils.toNonEmptyList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.jdom2.Document
import org.jdom2.Element
import java.nio.file.FileSystem
import java.nio.file.Path
import dev.epubby.internal.Namespaces.META_INF_CONTAINER as NAMESPACE

data class MetaInfContainerModel internal constructor(
    val version: String,
    val rootFiles: ImmutableList<RootFile>,
    val links: ImmutableList<Link>
) {
    private fun toDocument(): Document = documentOf("container", NAMESPACE) { _, root ->
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
    internal fun toMetaInfContainer(book: Book, prefixes: Prefixes): MetaInfContainer {
        val version = try {
            ContainerVersion.fromString(version)
        } catch (e: IllegalArgumentException) {
            throw MalformedBookException("Could not parse version ($version) into a container-version; ${e.message}", e)
        }
        val rootFiles = rootFiles.map { it.toRootFile(book) }.toNonEmptyList()
        val links = links.mapTo(mutableListOf()) { it.toLink(book, prefixes) }
        return MetaInfContainer(book, version, rootFiles, links)
    }

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        toDocument().writeTo(fileSystem.getPath("/META-INF/container.xml"))
    }

    @SerializedName("rootfile")
    data class RootFile internal constructor(
        @SerializedName("full-path")
        val fullPath: String,
        @SerializedName("media-type")
        val mediaType: String
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("rootfile", NAMESPACE) {
            it.setAttribute("full-path", fullPath)
            it.setAttribute("media-type", mediaType)
        }

        @JvmSynthetic
        internal fun toRootFile(book: Book): MetaInfContainer.RootFile {
            val fullPath = book.fileSystem.getPath(fullPath)
            val mediaType = MediaType.parse(mediaType)
            return MetaInfContainer.RootFile(book, fullPath, mediaType)
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): RootFile {
                val fullPath = element.getAttributeValueOrThrow("full-path")
                val mediaType = element.getAttributeValueOrThrow("media-type")
                return RootFile(fullPath, mediaType)
            }

            @JvmSynthetic
            internal fun fromRootFile(origin: MetaInfContainer.RootFile): RootFile {
                val fullPath = origin.fullPath.toString().substring(1)
                val mediaType = origin.mediaType.toString()
                return RootFile(fullPath, mediaType)
            }
        }
    }

    @SerializedName("link")
    data class Link internal constructor(
        val href: String,
        @SerializedName("rel")
        val relation: String? = null,
        val mediaType: String? = null
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("link", NAMESPACE) {
            it.setAttribute("href", href)
            relation.ifNotNull { rel -> it.setAttribute("rel", rel) }
            mediaType.ifNotNull { mediaType -> it.setAttribute("mediaType", mediaType) }
        }

        @JvmSynthetic
        internal fun toLink(book: Book, prefixes: Prefixes): MetaInfContainer.Link {
            val relation = relation?.let { resolveLinkRelationship(it, prefixes) }
            val mediaType = mediaType?.let(MediaType::parse)
            return MetaInfContainer.Link(book, href, relation, mediaType)
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): Link {
                val href = element.getAttributeValueOrThrow("href")
                val relation = element.getAttributeValue("rel")
                val mediaType = element.getAttributeValue("mediaType")
                return Link(href, relation, mediaType)
            }

            @JvmSynthetic
            internal fun fromLink(origin: MetaInfContainer.Link): Link {
                val href = origin.href
                val relation = when {
                    origin.book.version.isNewer(BookVersion.EPUB_2_0) -> origin.relation?.toStringForm()
                    else -> null
                }
                val mediaType = origin.mediaType.toString()
                return Link(href, relation, mediaType)
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(MetaInfContainerModel::class)

        @JvmSynthetic
        internal fun fromFile(
            file: Path,
            strictness: ParseStrictness
        ): MetaInfContainerModel = documentFrom(file).use { _, root ->
            val version = root.getAttributeValueOrThrow("version")
            val rootFiles = root.getChild("rootfiles", root.namespace)
                .getChildren("rootfile", root.namespace)
                .tryMap { RootFile.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .ifEmpty { throw MalformedBookException("'rootfiles' in meta-inf container is empty.") }
                .toPersistentList()
            val links = root.getChild("links", root.namespace)
                .getChildren("link", root.namespace)
                .tryMap { Link.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .toPersistentList()
            return@use MetaInfContainerModel(version, rootFiles, links)
        }

        @JvmSynthetic
        internal fun fromMetaInfContainer(origin: MetaInfContainer): MetaInfContainerModel {
            val version = origin.version.toString()
            val rootFiles = origin.rootFiles.map { RootFile.fromRootFile(it) }.toPersistentList()
            val links = origin.links.map { Link.fromLink(it) }.toPersistentList()
            return MetaInfContainerModel(version, rootFiles, links)
        }
    }
}