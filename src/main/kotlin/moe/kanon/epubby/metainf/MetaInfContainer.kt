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

package moe.kanon.epubby.metainf

import com.google.common.net.MediaType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.structs.props.Relationship
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.child
import moe.kanon.epubby.utils.docScope
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.epubby.utils.writeTo
import moe.kanon.kommons.io.paths.notExists
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.FileSystem
import java.nio.file.Path

/**
 * [container.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-container.xml)
 */
class MetaInfContainer private constructor(
    val file: Path,
    private val _rootFiles: MutableList<RootFile>,
    private val _links: MutableList<Link>
) {
    /**
     * Returns a list of all the [root-files][RootFile] inside of `this` container.
     */
    val rootFiles: ImmutableList<RootFile> get() = _rootFiles.toImmutableList()

    /**
     * Returns a list of all the [links][Link] inside of `this` container, or an empty list if `this` container has no
     * `links`.
     */
    val links: ImmutableList<Link> get() = _links.toImmutableList()

    /**
     * Returns the first [root file][RootFile] stored in this container.
     *
     * Per the epub ocf specification, the first element inside of `rootfiles` should *always* be pointing towards the
     * [package document][Package];
     *
     * > An OCF Processor *MUST* consider the first `rootfile` element within the `rootfiles` element to represent the
     * Default Rendition for the contained EPUB Publication.
     */
    val packageDocument: RootFile get() = _rootFiles[0]

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        toDocument().writeTo(fileSystem.getPath(file.toString()))
    }

    @JvmSynthetic
    internal fun toDocument(): Document = Document(Element("container", Namespaces.META_INF_CONTAINER)).docScope {
        addContent(Element("rootfiles", namespace).also {
            for (rootFile in _rootFiles) {
                it.addContent(rootFile.toElement(namespace))
            }
        })

        if (_links.isNotEmpty()) {
            addContent(Element("links", namespace).also {
                for (link in _links) {
                    it.addContent(link.toElement(namespace))
                }
            })
        }
    }

    override fun toString(): String = "MetaInfContainer(rootFiles=$_rootFiles, links=$_links)"

    // TODO: Document RootFile & Link
    data class RootFile internal constructor(val path: Path, val mediaType: MediaType) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace): Element = Element("rootfile", namespace).apply {
            setAttribute("full-path", path.toString().substring(1))
            setAttribute("media-type", mediaType.toString())
        }
    }

    data class Link internal constructor(val href: Path, val relation: Relationship, val mediaType: MediaType?) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace): Element = Element("link", namespace).apply {
            setAttribute("href", href.toString())
            setAttribute(relation.toAttribute(name = "rel"))
            mediaType?.also { setAttribute("mediaType", it.toString()) }
        }
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromFile(
            epub: Path,
            container: Path,
            rootFile: Path
        ): MetaInfContainer = parseXmlFile(container) { _, root ->
            // as stated in the specification; "OCF Processors MUST ignore foreign elements and attributes within a
            // container.xml file.", which means that we want to ONLY filter for elements named "rootfile"
            val rootFiles = root.child("rootfiles", epub, container, root.namespace)
                .children
                .asSequence()
                .filter { it.name == "rootfile" }
                .mapTo(mutableListOf()) { createRootFile(it, rootFile, epub, container) }
            val links: MutableList<Link> = root.getChild("links", root.namespace)
                ?.children
                ?.asSequence()
                ?.filter { it.name == "link" }
                ?.mapTo(mutableListOf()) { createLink(it, rootFile, epub, container) } ?: mutableListOf()
            val packageDocument = rootFiles[0].path
            if (packageDocument.notExists) {
                malformed(
                    epub,
                    container,
                    "first root-file <${rootFiles[0]}> points towards a non-existent file <$packageDocument>"
                )
            }
            return MetaInfContainer(container, rootFiles, links).also {
                logger.trace { "Constructed meta-inf-container instance <$it>" }
            }
        }

        private fun createRootFile(element: Element, root: Path, epub: Path, document: Path): RootFile {
            val fullPath = element.attr("full-path", epub, document).let(root::resolve)
            val mediaType = element.attr("media-type", epub, document).let(MediaType::parse)
            return RootFile(fullPath, mediaType).also {
                logger.trace { "Constructed meta-inf root-file instance <$it>" }
            }
        }

        private fun createLink(element: Element, root: Path, epub: Path, document: Path): Link {
            val href = element.attr("href", epub, document).let(root::resolve)
            val relation = element.attr("rel", epub, document).let { Properties.parse(Link::class, it) }
            val mediaType = element.getAttributeValue("mediaType")?.let(MediaType::parse)
            return Link(href, relation, mediaType).also {
                logger.trace { "Constructed meta-inf link instance <$it>" }
            }
        }
    }
}