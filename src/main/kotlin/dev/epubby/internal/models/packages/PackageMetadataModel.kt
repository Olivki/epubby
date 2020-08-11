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
import dev.epubby.*
import dev.epubby.internal.IntroducedIn
import dev.epubby.internal.Namespaces
import dev.epubby.internal.elementOf
import dev.epubby.internal.getAttributeValueOrThrow
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.models.dublincore.DublinCoreModel
import dev.epubby.internal.models.dublincore.LocalizedDublinCoreModel
import dev.epubby.packages.PackageMetadata
import dev.epubby.prefixes.Prefixes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import org.jdom2.Element
import org.jdom2.Namespace
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("metadata")
data class PackageMetadataModel internal constructor(
    val identifiers: ImmutableList<DublinCoreModel.Identifier>,
    val titles: ImmutableList<LocalizedDublinCoreModel.Title>,
    val languages: ImmutableList<DublinCoreModel.Language>,
    val dublinCore: ImmutableList<DublinCoreModel>,
    val opf2Meta: ImmutableList<Opf2Meta>,
    val opf3Meta: ImmutableList<Opf3Meta>,
    val links: ImmutableList<Link>
) {
    // TODO: group dublin-core elements with any opf3meta elements that refine it
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("metadata", NAMESPACE) {
        it.addNamespaceDeclaration(Namespaces.DUBLIN_CORE)
        it.addNamespaceDeclaration(Namespaces.OPF_WITH_PREFIX)

        identifiers.forEach { dc -> it.addContent(dc.toElement()) }
        titles.forEach { dc -> it.addContent(dc.toElement()) }
        languages.forEach { dc -> it.addContent(dc.toElement()) }
        dublinCore.forEach { dc -> it.addContent(dc.toElement()) }
        opf3Meta.forEach { m -> it.addContent(m.toElement()) }
        opf2Meta.forEach { m -> it.addContent(m.toElement()) }
        links.forEach { l -> it.addContent(l.toElement()) }
    }

    @JvmSynthetic
    internal fun toPackageMetadata(book: Book, prefixes: Prefixes): PackageMetadata {
        TODO("'toPackageMetadata' operation is not implemented yet.")
    }

    @SerializedName("meta")
    data class Opf2Meta internal constructor(
        val charset: String?,
        val content: String?,
        @SerializedName("http-equiv")
        val httpEquivalent: String?,
        val name: String?,
        val scheme: String?,
        val attributes: Map<String, String>
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("meta", NAMESPACE) {
            if (charset != null) it.setAttribute("charset", charset)
            if (content != null) it.setAttribute("content", content)
            if (httpEquivalent != null) it.setAttribute("http-equiv", httpEquivalent)
            if (name != null) it.setAttribute("name", name)
            if (scheme != null) it.setAttribute("scheme", scheme)
            attributes.forEach { (k, v) -> it.setAttribute(k, v) }
        }

        @JvmSynthetic
        internal fun toOpf2Meta(book: Book): PackageMetadata.Opf2Meta {
            TODO("'toOpf2Meta' operation is not implemented yet.")
        }

        internal companion object {
            @get:JvmSynthetic
            internal val metaAttributes: Set<String> =
                persistentSetOf("charset", "content", "http-equiv", "name", "scheme")

            @JvmSynthetic
            internal fun fromElement(element: Element): Opf2Meta {
                val charset = element.getAttributeValue("charset")
                val content = element.getAttributeValue("content")
                val httpEquivalent = element.getAttributeValue("http-equiv")
                val name = element.getAttributeValue("name")
                val scheme = element.getAttributeValue("scheme")
                val attributes = element.attributes
                    .filterNot { it.name in metaAttributes }
                    .associate { it.name to it.value }
                    .toPersistentMap()
                return Opf2Meta(charset, content, httpEquivalent, name, scheme, attributes)
            }
        }
    }

    @SerializedName("meta")
    data class Opf3Meta internal constructor(
        val content: String,
        val property: String,
        @SerializedName("id")
        val identifier: String?,
        @SerializedName("dir")
        val direction: String?,
        val refines: String?,
        val scheme: String?,
        @SerializedName("xml:lang")
        val language: String?
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("meta", NAMESPACE) {
            it.setAttribute("property", property)
            if (identifier != null) it.setAttribute("id", identifier)
            if (direction != null) it.setAttribute("dir", direction)
            if (refines != null) it.setAttribute("refines", refines)
            if (scheme != null) it.setAttribute("scheme", scheme)
            if (language != null) it.setAttribute("lang", language, Namespace.XML_NAMESPACE)
            it.text = content
        }

        // TODO: "a reading system should NOT fail when encountering an unknown property, rather it should skip that
        //       element", so make this return Try<T> or something
        @JvmSynthetic
        internal fun toOpf3Meta(book: Book): PackageMetadata.Opf3Meta {
            TODO("'toOpf3Meta' operation is not implemented yet.")
        }

        internal companion object {
            @get:JvmSynthetic
            internal val metaAttributes: Set<String> =
                persistentSetOf("property", "id", "dir", "refines", "scheme", "lang")

            @JvmSynthetic
            internal fun fromElement(element: Element): Opf3Meta {
                if (element.textNormalize.isBlank()) {
                    raiseError("value/text is blank")
                }

                val content = element.textNormalize
                val property = element.getAttributeValueOrThrow("property")
                val identifier = element.getAttributeValue("id")
                val direction = element.getAttributeValue("dir")
                val refines = element.getAttributeValue("refines")
                val scheme = element.getAttributeValue("scheme")
                val language = element.getAttributeValue("lang", Namespace.XML_NAMESPACE)
                return Opf3Meta(content, property, identifier, direction, refines, scheme, language)
            }

            private fun raiseError(reason: String): Nothing =
                throw MalformedBookException("'meta' element in 'metadata' is faulty; $reason.")
        }
    }

    @SerializedName("link")
    data class Link internal constructor(
        val href: String,
        @SerializedName("rel")
        @IntroducedIn(version = BookVersion.EPUB_3_0)
        val relation: String?,
        @SerializedName("media-type")
        val mediaType: String?,
        @SerializedName("id")
        val identifier: String?,
        @IntroducedIn(version = BookVersion.EPUB_3_0)
        val properties: String?,
        @IntroducedIn(version = BookVersion.EPUB_3_0)
        val refines: String?
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("link", NAMESPACE) {
            it.setAttribute("href", href)
            if (relation != null) it.setAttribute("rel", relation)
            if (mediaType != null) it.setAttribute("media-type", mediaType)
            if (identifier != null) it.setAttribute("id", identifier)
            if (properties != null) it.setAttribute("properties", properties)
            if (refines != null) it.setAttribute("refines", refines)
        }

        @JvmSynthetic
        internal fun toLink(book: Book): PackageMetadata.Link {
            TODO("'toLink' operation is not implemented yet.")
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): Link {
                val href = element.getAttributeValueOrThrow("href")
                val relation = element.getAttributeValue("rel")
                val mediaType = element.getAttributeValue("media-type")
                val identifier = element.getAttributeValue("id")
                val properties = element.getAttributeValue("properties")
                val refines = element.getAttributeValue("refines")
                return Link(href, relation, mediaType, identifier, properties, refines)
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageMetadataModel::class)

        // TODO:  It may contain Dublin Core metadata elements directly or within a (now deprecated) dc-metadata sub-element.
        //        Supplemental metadata can also be specified directly or within a (now deprecated) x-metadata sub-element.
        //        make sure to always check if the elements mentioned above exist and then parse those too, for the sake of
        //        backwards compatibility
        @JvmSynthetic
        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageMetadataModel {
            val allDublinCore = (element.getChild("dc-metadata", element.namespace)?.children ?: element.children)
                .asSequence()
                .filter { it.namespace == Namespaces.DUBLIN_CORE }
                .tryMap { DublinCoreModel.fromElement(it) }
                .mapToValues(LOGGER, strictness)
            val dublinCore = allDublinCore
                .filterNot { it.name == "identifier" }
                .filterNot { it.name == "title" }
                .filterNot { it.name == "language" }
                .asIterable()
                .toPersistentList()
            val identifiers = allDublinCore.filterIsInstance<DublinCoreModel.Identifier>()
                .ifEmpty { throw MalformedBookException.forMissing("metadata", "dc:identifier") }
                .asIterable()
                .toPersistentList()
            val titles = allDublinCore.filterIsInstance<LocalizedDublinCoreModel.Title>()
                .ifEmpty { throw MalformedBookException.forMissing("metadata", "dc:title") }
                .asIterable()
                .toPersistentList()
            val languages = allDublinCore.filterIsInstance<DublinCoreModel.Language>()
                .ifEmpty { throw MalformedBookException.forMissing("metadata", "dc:language") }
                .asIterable()
                .toPersistentList()
            val metaElements =
                (element.getChild("x-metadata", element.namespace)?.getChildren("meta", element.namespace)
                    ?: element.getChildren("meta", element.namespace)).asSequence()
            val opf2Meta = metaElements.filter { it.isOpf2MetaElement() }
                .tryMap { Opf2Meta.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .asIterable()
                .toPersistentList()
            val opf3Meta = metaElements.filter { it.isOpf3MetaElement() }
                .tryMap { Opf3Meta.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .asIterable()
                .toPersistentList()
            val links = element.getChildren("link", element.namespace)
                .tryMap { Link.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .toPersistentList()
            return PackageMetadataModel(identifiers, titles, languages, dublinCore, opf2Meta, opf3Meta, links)
        }

        @JvmSynthetic
        internal fun fromPackageMetadata(origin: PackageMetadata): PackageMetadataModel {
            TODO("'fromPackageMetadata' operation is not implemented yet.")
        }

        private fun Element.isOpf2MetaElement(): Boolean = attributes.none { it.name in Opf3Meta.metaAttributes }
            && attributes.any { it.name in Opf2Meta.metaAttributes }

        private fun Element.isOpf3MetaElement(): Boolean = attributes.any { it.name in Opf3Meta.metaAttributes }
            && attributes.none { it.name in Opf2Meta.metaAttributes }
    }
}