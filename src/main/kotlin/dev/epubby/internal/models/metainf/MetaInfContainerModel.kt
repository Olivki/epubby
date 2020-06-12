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
import dev.epubby.Book
import dev.epubby.BookVersion
import dev.epubby.MalformedBookException
import dev.epubby.ParseStrictness
import dev.epubby.internal.documentFrom
import dev.epubby.internal.documentOf
import dev.epubby.internal.elementOf
import dev.epubby.internal.getAttributeValueOrThrow
import dev.epubby.internal.models.SerialName
import dev.epubby.internal.use
import dev.epubby.internal.writeTo
import dev.epubby.mapToValues
import dev.epubby.metainf.MetaInfContainer
import dev.epubby.prefixes.Prefixes
import dev.epubby.props.resolveLinkRelationship
import dev.epubby.props.toStringForm
import dev.epubby.tryMap
import dev.epubby.utils.toNonEmptyList
import org.jdom2.Document
import org.jdom2.Element
import java.nio.file.FileSystem
import java.nio.file.Path
import dev.epubby.internal.Namespaces.META_INF_CONTAINER as NAMESPACE

internal data class MetaInfContainerModel(
    internal val version: String,
    internal val rootFiles: List<RootFile>,
    internal val links: List<Link>
) {
    private fun toDocument(): Document = documentOf("container", NAMESPACE) { _, root ->
        root.addContent(elementOf("rootfiles", root.namespace) { element ->
            rootFiles.forEach { element.addContent(it.toElement()) }
        })

        if (links.isNotEmpty()) {
            root.addContent(elementOf("links", root.namespace) { element ->
                links.forEach { element.addContent(it.toElement()) }
            })
        }
    }

    internal fun toMetaInfContainer(book: Book, prefixes: Prefixes): MetaInfContainer {
        val rootFiles = rootFiles.map { it.toRootFile(book) }.toNonEmptyList()
        val links = links.mapTo(mutableListOf()) { it.toLink(book, prefixes) }
        return MetaInfContainer(book, version, rootFiles, links)
    }

    internal fun writeToFile(fileSystem: FileSystem) {
        toDocument().writeTo(fileSystem.getPath("/META-INF/container.xml"))
    }

    @SerialName("rootfile")
    data class RootFile(
        @SerialName("full-path") internal val fullPath: String,
        @SerialName("media-type") internal val mediaType: String
    ) {
        internal fun toElement(): Element = elementOf("rootfile", NAMESPACE) {
            it.setAttribute("full-path", fullPath)
            it.setAttribute("media-type", mediaType)
        }

        internal fun toRootFile(book: Book): MetaInfContainer.RootFile {
            val fullPath = book.fileSystem.getPath(fullPath)
            val mediaType = MediaType.parse(mediaType)
            return MetaInfContainer.RootFile(book, fullPath, mediaType)
        }

        internal companion object {
            internal fun fromElement(element: Element): RootFile {
                val fullPath = element.getAttributeValueOrThrow("full-path")
                val mediaType = element.getAttributeValueOrThrow("media-type")
                return RootFile(fullPath, mediaType)
            }

            internal fun fromRootFile(origin: MetaInfContainer.RootFile): RootFile {
                val fullPath = origin.fullPath.toString().substring(1)
                val mediaType = origin.mediaType.toString()
                return RootFile(fullPath, mediaType)
            }
        }
    }

    @SerialName("link")
    data class Link(
        internal val href: String,
        @SerialName("rel") internal val relation: String? = null,
        internal val mediaType: String? = null
    ) {
        internal fun toElement(): Element = elementOf("link", NAMESPACE) {
            it.setAttribute("href", href)
            if (relation != null) it.setAttribute("rel", relation)
            if (mediaType != null) it.setAttribute("mediaType", mediaType)
        }

        internal fun toLink(book: Book, prefixes: Prefixes): MetaInfContainer.Link {
            val relation = relation?.let { resolveLinkRelationship(it, prefixes) }
            val mediaType = mediaType?.let(MediaType::parse)
            return MetaInfContainer.Link(book, href, relation, mediaType)
        }

        internal companion object {
            internal fun fromElement(element: Element): Link {
                val href = element.getAttributeValueOrThrow("href")
                val relation = element.getAttributeValue("rel")
                val mediaType = element.getAttributeValue("mediaType")
                return Link(href, relation, mediaType)
            }

            internal fun fromLink(origin: MetaInfContainer.Link): Link {
                val href = origin.href
                val relation = when {
                    origin.book.version.isNewerThan(BookVersion.EPUB_2_0) -> origin.relation?.toStringForm()
                    else -> null
                }
                val mediaType = origin.mediaType.toString()
                return Link(href, relation, mediaType)
            }
        }
    }

    internal companion object {
        private val logger = InlineLogger(MetaInfContainerModel::class)

        internal fun fromFile(
            file: Path,
            strictness: ParseStrictness
        ): MetaInfContainerModel = documentFrom(file).use { _, root ->
            val version = root.getAttributeValueOrThrow("version")
            val rootFiles = root.getChild("rootfiles", root.namespace)
                .getChildren("rootfile", root.namespace)
                .tryMap { RootFile.fromElement(it) }
                .mapToValues(logger, strictness)
                .ifEmpty { throw MalformedBookException("'rootfiles' in meta-inf container is empty.") }
            val links = root.getChild("links", root.namespace)
                .getChildren("link", root.namespace)
                .tryMap { Link.fromElement(it) }
                .mapToValues(logger, strictness)
            return MetaInfContainerModel(version, rootFiles, links)
        }

        internal fun fromMetaInfContainer(origin: MetaInfContainer): MetaInfContainerModel {
            val rootFiles = origin.rootFiles.map { RootFile.fromRootFile(it) }
            val links = origin.links.map { Link.fromLink(it) }
            return MetaInfContainerModel(origin.version, rootFiles, links)
        }
    }
}