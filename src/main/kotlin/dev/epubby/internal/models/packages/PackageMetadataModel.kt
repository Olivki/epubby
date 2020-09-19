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
import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.MalformedBookException
import dev.epubby.ParseMode
import dev.epubby.dublincore.DublinCore.Identifier
import dev.epubby.dublincore.DublinCore.Language
import dev.epubby.dublincore.LocalizedDublinCore.Title
import dev.epubby.internal.IntroducedIn
import dev.epubby.internal.Namespaces
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.models.dublincore.DublinCoreModel
import dev.epubby.internal.models.dublincore.DublinCoreModel.IdentifierModel
import dev.epubby.internal.models.dublincore.DublinCoreModel.LanguageModel
import dev.epubby.internal.models.dublincore.LocalizedDublinCoreModel.TitleModel
import dev.epubby.internal.utils.*
import dev.epubby.packages.metadata.*
import dev.epubby.prefixes.Prefixes
import dev.epubby.properties.*
import dev.epubby.utils.Direction
import dev.epubby.utils.toNonEmptyList
import kotlinx.collections.immutable.*
import org.jdom2.Element
import org.jdom2.Namespace
import java.net.URI
import java.nio.charset.Charset
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("metadata")
internal data class PackageMetadataModel internal constructor(
    internal val identifiers: PersistentList<IdentifierModel>,
    internal val titles: PersistentList<TitleModel>,
    internal val languages: PersistentList<LanguageModel>,
    internal val dublinCoreEntries: PersistentList<DublinCoreModel>,
    internal val opf2MetaEntries: PersistentList<Opf2MetaModel>,
    internal val opf3MetaEntries: PersistentList<Opf3MetaModel>,
    internal val links: PersistentList<LinkModel>,
) {
    // TODO: make sure we don't add any new elements in old epub versions?
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("metadata", NAMESPACE) {
        it.addNamespaceDeclaration(Namespaces.DUBLIN_CORE)
        it.addNamespaceDeclaration(Namespaces.OPF_WITH_PREFIX)

        val opf3Meta = opf3MetaEntries.asSequence()

        fun groupedRefinements(): Map<String, List<Opf3MetaModel>> {
            val refiningEntries = opf3Meta.filter { meta -> meta.refines != null }
            val group = hashMapOf<String, MutableList<Opf3MetaModel>>()

            for (entry in refiningEntries) {
                val key = if (entry.refines!!.startsWith('#')) entry.refines.drop(1) else entry.refines
                group.getOrPut(key) { mutableListOf() } += entry
            }

            return group
        }

        val refiningOpf3 = groupedRefinements()

        fun addRefining(key: String?) {
            val refinements = when (key) {
                null -> emptyList()
                else -> refiningOpf3[key] ?: emptyList()
            }

            for (refining in refinements) {
                it.addContent(refining.toElement())
            }
        }

        val nonRefiningOpf3 = opf3Meta.filter { meta -> meta.refines == null }

        for (identifier in identifiers) {
            it.addContent(identifier.toElement())
            addRefining(identifier.identifier)
        }

        for (title in titles) {
            it.addContent(title.toElement())
            addRefining(title.identifier)
        }

        for (language in languages) {
            it.addContent(language.toElement())
            addRefining(language.identifier)
        }

        for (dublinCore in dublinCoreEntries) {
            it.addContent(dublinCore.toElement())
            addRefining(dublinCore.identifier)
        }

        for (meta in nonRefiningOpf3) {
            it.addContent(meta.toElement())
        }

        for (meta in opf2MetaEntries) {
            it.addContent(meta.toElement())
        }

        for (link in links) {
            it.addContent(link.toElement())
        }
    }

    @JvmSynthetic
    internal fun toPackageMetadata(epub: Epub, prefixes: Prefixes): PackageMetadata {
        val identifiers = identifiers.map { it.toDublinCore(epub) as Identifier }.toNonEmptyList()
        val titles = titles.map { it.toDublinCore(epub) as Title }.toNonEmptyList()
        val languages = languages.map { it.toDublinCore(epub) as Language }.toNonEmptyList()
        val dublinCoreEntries = dublinCoreEntries.map { it.toDublinCore(epub) }.toMutableList()
        val allDublinCoreEntries = this.identifiers + this.titles + this.languages + this.dublinCoreEntries
        val opf2MetaEntries = opf2MetaEntries.asSequence()
            .mapNotNull { it.toOpf2Meta(epub) }
            .toMutableList()
        val opf3MetaEntries = opf3MetaEntries.asSequence()
            .map { it.toOpf3Meta(epub, prefixes, allDublinCoreEntries) }
            .onEachIndexed { i, it -> if (it == null) LOGGER.error { "Skipping ${opf3MetaEntries[i]} because it contains an unknown property." } }
            .filterNotNull()
            .toMutableList()
        val links = links.asSequence()
            .map { it.toLink(epub, prefixes) }
            .onEachIndexed { i, it -> if (it == null) LOGGER.error { "Skipping ${links[i]} because it contains unknown relation/properties." } }
            .filterNotNull()
            .toMutableList()

        return PackageMetadata(
            epub,
            identifiers,
            titles,
            languages,
            dublinCoreEntries,
            opf2MetaEntries,
            opf3MetaEntries,
            links
        )
    }

    @SerializedName("meta")
    internal data class Opf2MetaModel internal constructor(
        internal val charset: String? = null,
        internal val content: String? = null,
        @SerializedName("http-equiv")
        internal val httpEquiv: String? = null,
        internal val name: String? = null,
        internal val scheme: String? = null,
        internal val attributes: PersistentMap<String, String>,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("meta", NAMESPACE) {
            if (charset != null) it.setAttribute("charset", charset)
            if (content != null) it.setAttribute("content", content)
            if (httpEquiv != null) it.setAttribute("http-equiv", httpEquiv)
            if (name != null) it.setAttribute("name", name)
            if (scheme != null) it.setAttribute("scheme", scheme)

            for ((key, value) in attributes) {
                it.setAttribute(key, value)
            }
        }

        // TODO: make this either throw an exception or log depending on a strictness?
        @JvmSynthetic
        internal fun toOpf2Meta(epub: Epub): Opf2Meta? {
            val charset = charset?.let(Charset::forName)
            return when {
                (httpEquiv != null && content != null) && (name == null && charset == null) -> {
                    Opf2Meta.HttpEquiv(epub, httpEquiv, content, scheme, attributes)
                }
                (name != null && content != null) && (httpEquiv == null && charset == null) -> {
                    Opf2Meta.Name(epub, name, content, scheme, attributes)
                }
                (charset != null) && (name == null && httpEquiv == null && content == null) -> {
                    Opf2Meta.Charset(epub, charset, scheme, attributes)
                }
                else -> {
                    LOGGER.error { "Can't convert $this to either of HttpEquiv/Name/Charset as it has a faulty combination of attributes." }
                    null
                }
            }
        }

        internal companion object {
            @get:JvmSynthetic
            internal val metaAttributes: Set<String> =
                persistentSetOf("charset", "content", "http-equiv", "name", "scheme")

            @JvmSynthetic
            internal fun fromElement(element: Element): Opf2MetaModel {
                val charset = element.getAttributeValue("charset")
                val content = element.getAttributeValue("content")
                val httpEquivalent = element.getAttributeValue("http-equiv")
                val name = element.getAttributeValue("name")
                val scheme = element.getAttributeValue("scheme")
                val attributes = element.attributes
                    .asSequence()
                    .filterNot { it.name in metaAttributes }
                    .associate { it.name to it.value }
                    .toPersistentMap()
                return Opf2MetaModel(charset, content, httpEquivalent, name, scheme, attributes)
            }

            @JvmSynthetic
            internal fun fromOpf2Meta(meta: Opf2Meta): Opf2MetaModel = meta.accept(Opf2MetaToModel)

            private object Opf2MetaToModel : Opf2MetaVisitor<Opf2MetaModel> {
                override fun visitHttpEquiv(httpEquiv: Opf2Meta.HttpEquiv): Opf2MetaModel = Opf2MetaModel(
                    httpEquiv = httpEquiv.httpEquiv,
                    content = httpEquiv.content,
                    scheme = httpEquiv.scheme,
                    attributes = httpEquiv.globalAttributes
                )

                override fun visitName(name: Opf2Meta.Name): Opf2MetaModel = Opf2MetaModel(
                    name = name.name,
                    content = name.content,
                    scheme = name.scheme,
                    attributes = name.globalAttributes
                )

                override fun visitCharset(charset: Opf2Meta.Charset): Opf2MetaModel = Opf2MetaModel(
                    content = charset.charset.name(),
                    scheme = charset.scheme,
                    attributes = charset.globalAttributes
                )
            }
        }
    }

    @SerializedName("meta")
    internal data class Opf3MetaModel internal constructor(
        internal val content: String,
        internal val property: String,
        @SerializedName("id")
        internal val identifier: String?,
        @SerializedName("dir")
        internal val direction: String?,
        internal val refines: String?,
        internal val scheme: String?,
        @SerializedName("xml:lang")
        internal val language: String?,
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

        @JvmSynthetic
        internal fun toOpf3Meta(epub: Epub, prefixes: Prefixes, dublinCoreElements: List<DublinCoreModel>): Opf3Meta? {
            fun invalidRefines(): Nothing =
                throw MalformedBookException("'refines' of $this does not point to any known dublin-core elements")

            // "a reading system should NOT fail when encountering an unknown property, rather it should skip that element"
            val property = resolveMetaProperty(property, prefixes) ?: return null
            val direction = direction?.let { Direction.fromString(it) }
            val refines = when (refines) {
                null -> null
                else -> when {
                    // TODO: this might not be needed?
                    refines.startsWith("#") -> dublinCoreElements
                        .firstOrNull { it.identifier == refines.drop(1) } ?: invalidRefines()
                    else -> dublinCoreElements.firstOrNull { it.identifier == refines } ?: invalidRefines()
                }
            }?.toDublinCore(epub)

            return Opf3Meta(epub, content, property, identifier, direction, refines, scheme, language)
        }

        internal companion object {
            @get:JvmSynthetic
            internal val metaAttributes: Set<String> =
                persistentSetOf("property", "id", "dir", "refines", "scheme", "lang")

            @JvmSynthetic
            internal fun fromElement(element: Element): Opf3MetaModel {
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
                return Opf3MetaModel(content, property, identifier, direction, refines, scheme, language)
            }

            @JvmSynthetic
            internal fun fromOpf3Meta(meta: Opf3Meta): Opf3MetaModel {
                val property = meta.property.encodeToString()
                val direction = meta.direction?.value
                val refines = meta.refines?.identifier?.let { "#$it" }

                return Opf3MetaModel(
                    meta.value,
                    property,
                    meta.identifier,
                    direction,
                    refines,
                    meta.scheme,
                    meta.language
                )
            }

            private fun raiseError(reason: String): Nothing =
                throw MalformedBookException("'meta' element in 'metadata' is faulty; $reason.")
        }
    }

    @SerializedName("link")
    internal data class LinkModel internal constructor(
        internal val href: String,
        @SerializedName("rel")
        @IntroducedIn(version = EPUB_3_0)
        internal val relation: String?,
        @SerializedName("media-type")
        internal val mediaType: String?,
        @SerializedName("id")
        internal val identifier: String?,
        @IntroducedIn(version = EPUB_3_0)
        internal val properties: String?,
        @IntroducedIn(version = EPUB_3_0)
        internal val refines: String?,
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
        internal fun toLink(epub: Epub, prefixes: Prefixes): MetadataLink? {
            val href = URI(href)
            val relation = when {
                epub.version.isOlder(EPUB_3_0) -> null
                else -> relation?.let { resolveLinkRelationship(it, prefixes) ?: return null }
            }
            val mediaType = mediaType?.let(MediaType::parse)
            val properties = when {
                epub.version.isOlder(EPUB_3_0) -> null
                else -> properties?.let { resolveLinkProperties(it, prefixes).ifEmpty { return null } }
            } ?: Properties.empty()

            return MetadataLink(epub, href, relation, mediaType, identifier, properties, refines)
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): LinkModel {
                val href = element.getAttributeValueOrThrow("href")
                val relation = element.getAttributeValue("rel")
                val mediaType = element.getAttributeValue("media-type")
                val identifier = element.getAttributeValue("id")
                val properties = element.getAttributeValue("properties")
                val refines = element.getAttributeValue("refines")
                return LinkModel(href, relation, mediaType, identifier, properties, refines)
            }

            @JvmSynthetic
            internal fun fromLink(link: MetadataLink): LinkModel {
                val href = link.href.toString()
                val relation = when {
                    link.epub.version.isOlder(EPUB_3_0) -> null
                    else -> link.relation?.encodeToString()
                }
                val mediaType = link.mediaType?.toString()
                val properties = when {
                    link.epub.version.isOlder(EPUB_3_0) -> null
                    else -> link.properties.ifEmpty { null }?.encodeToString()
                }

                return LinkModel(href, relation, mediaType, link.identifier, properties, link.refines)
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
        internal fun fromElement(element: Element, mode: ParseMode): PackageMetadataModel {
            val allDublinCore = (element.getChild("dc-metadata", element.namespace)?.children ?: element.children)
                .asSequence()
                .filter { it.namespace == Namespaces.DUBLIN_CORE }
                .tryMap { DublinCoreModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
            val dublinCore = allDublinCore
                .filterNot { it.name == "identifier" }
                .filterNot { it.name == "title" }
                .filterNot { it.name == "language" }
                .asIterable()
                .toPersistentList()
            val identifiers = allDublinCore.filterIsInstance<IdentifierModel>()
                .ifEmpty { throw MalformedBookException.forMissing("metadata", "dc:identifier") }
                .asIterable()
                .toPersistentList()
            val titles = allDublinCore.filterIsInstance<TitleModel>()
                .ifEmpty { throw MalformedBookException.forMissing("metadata", "dc:title") }
                .asIterable()
                .toPersistentList()
            val languages = allDublinCore.filterIsInstance<LanguageModel>()
                .ifEmpty { throw MalformedBookException.forMissing("metadata", "dc:language") }
                .asIterable()
                .toPersistentList()
            val metaElements =
                (element.getChild("x-metadata", element.namespace)?.getChildren("meta", element.namespace)
                    ?: element.getChildren("meta", element.namespace)).asSequence()
            val opf2Meta = metaElements.filter { it.isOpf2MetaElement() }
                .tryMap { Opf2MetaModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
                .asIterable()
                .toPersistentList()
            val opf3Meta = metaElements.filter { it.isOpf3MetaElement() }
                .tryMap { Opf3MetaModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
                .asIterable()
                .toPersistentList()
            val links = element.getChildren("link", element.namespace)
                .tryMap { LinkModel.fromElement(it) }
                .mapToValues(LOGGER, mode)
                .toPersistentList()
            return PackageMetadataModel(identifiers, titles, languages, dublinCore, opf2Meta, opf3Meta, links)
        }

        @JvmSynthetic
        internal fun fromPackageMetadata(origin: PackageMetadata): PackageMetadataModel {
            val identifiers = origin.identifiers
                .map { DublinCoreModel.fromDublinCore(it) as IdentifierModel }
                .toPersistentList()
            val titles = origin.titles
                .map { DublinCoreModel.fromDublinCore(it) as TitleModel }
                .toPersistentList()
            val languages = origin.languages
                .map { DublinCoreModel.fromDublinCore(it) as LanguageModel }
                .toPersistentList()
            val dublinCoreEntries = origin.dublinCoreEntries
                .map { DublinCoreModel.fromDublinCore(it) }
                .toPersistentList()
            val opf2MetaEntries = origin.opf2MetaEntries
                .map { Opf2MetaModel.fromOpf2Meta(it) }
                .toPersistentList()
            val opf3MetaEntries = origin.opf3MetaEntries
                .map { Opf3MetaModel.fromOpf3Meta(it) }
                .toPersistentList()
            val links = origin.links
                .map { LinkModel.fromLink(it) }
                .toPersistentList()

            return PackageMetadataModel(
                identifiers,
                titles,
                languages,
                dublinCoreEntries,
                opf2MetaEntries,
                opf3MetaEntries,
                links
            )
        }

        private fun Element.isOpf2MetaElement(): Boolean = attributes.none { it.name in Opf3MetaModel.metaAttributes }
            && attributes.any { it.name in Opf2MetaModel.metaAttributes }

        private fun Element.isOpf3MetaElement(): Boolean = attributes.any { it.name in Opf3MetaModel.metaAttributes }
            && attributes.none { it.name in Opf2MetaModel.metaAttributes }
    }
}