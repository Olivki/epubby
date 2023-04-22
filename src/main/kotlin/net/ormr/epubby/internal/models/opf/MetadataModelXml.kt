/*
 * Copyright 2019-2023 Oliver Berg
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

package net.ormr.epubby.internal.models.opf

import cc.ekblad.konbini.ParserResult
import com.github.michaelbull.result.flatMap
import dev.epubby.Epub3Feature
import dev.epubby.Epub3LegacyFeature
import dev.epubby.opf.MetadataReadError.*
import dev.epubby.opf.OpfReadError
import dev.epubby.opf.OpfReadError.*
import dev.epubby.xml.XmlAttribute
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE
import net.ormr.epubby.internal.Namespaces.OPF
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.WriterData
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel.IdentifierModel
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel.LanguageModel
import net.ormr.epubby.internal.models.dublincore.DublinCoreModelXml
import net.ormr.epubby.internal.models.dublincore.LocalizedDublinCoreModel.TitleModel
import net.ormr.epubby.internal.models.opf.MetadataModel.*
import net.ormr.epubby.internal.models.supportsEpub3Features
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import net.ormr.epubby.internal.util.getOrNullIfNone
import net.ormr.epubby.internal.util.getOwnText
import org.jdom2.Element
import org.jdom2.Namespace.NO_NAMESPACE
import org.jdom2.Namespace.XML_NAMESPACE
import net.ormr.epubby.internal.Namespaces.OPF_NO_PREFIX as NAMESPACE

@OptIn(Epub3Feature::class, Epub3LegacyFeature::class)
internal object MetadataModelXml : ModelXmlSerializer<OpfReadError>() {
    private val opf2Attributes: Set<String> = hashSetOf("charset", "content", "http-equiv", "name", "scheme")
    private val specialDcNames: Set<String> = hashSetOf("identifier", "title", "language")

    fun read(root: Element) = effect {
        // "It may contain Dublin Core metadata elements directly or within a (now deprecated) dc-metadata sub-element."
        val dcMetadata = root.getChild("dc-metadata", NAMESPACE) ?: root
        val dcElements = dcMetadata
            .children
            .asSequence()
            .filter { it.namespace == DUBLIN_CORE }
            .map { DublinCoreModelXml.read(it).bind(::DublinCoreError) }
            .toList()
        val normalDcElements = dcElements.filter { it.name !in specialDcNames }
        val identifiers = dcElements.filterIsInstance<IdentifierModel>()
        ensure(dcElements.isNotEmpty()) { MissingIdentifier }
        val titles = dcElements.filterIsInstance<TitleModel>()
        ensure(titles.isNotEmpty()) { MissingTitle }
        val languages = dcElements.filterIsInstance<LanguageModel>()
        ensure(languages.isNotEmpty()) { MissingLanguage }
        // "Supplemental metadata can also be specified directly or within a (now deprecated) x-metadata sub-element."
        val xMetadata = root.getChild("x-metadata", NAMESPACE) ?: root
        val metaElements = xMetadata
            .getChildren("meta", NAMESPACE)
            .map { (if (it.isOpf3()) readOpf3Meta(it) else readOpf2Meta(it)).bind() }
        val links = root
            .getChildren("link", NAMESPACE)
            .map { readLink(it).bind() }
        MetadataModel(
            identifiers = identifiers,
            titles = titles,
            languages = languages,
            dublinCoreElements = normalDcElements,
            links = links,
            metaElements = metaElements,
        )
    }

    private fun readOpf2Meta(meta: Element) = effect<_, OpfReadError> {
        Opf2MetaModel(
            charset = meta.optionalAttr("charset"),
            content = meta.optionalAttr("content"),
            httpEquiv = meta.optionalAttr("http-equiv"),
            name = meta.optionalAttr("name"),
            scheme = meta.optionalAttr("scheme"),
            extraAttributes = meta
                .attributes
                .asSequence()
                .filter { it.qualifiedName !in opf2Attributes }
                .map { XmlAttribute(it.name, it.value, it.namespace.getOrNullIfNone()) }
                .toList(),
        )
    }

    private fun readOpf3Meta(meta: Element) = effect {
        Opf3MetaModel(
            // "Unless an individual property explicitly defines a different white space normalization algorithm,
            // Reading Systems MUST trim all leading and trailing white space [XML] from the meta element values before
            // further processing them."
            // TODO: check that 'trim's definition of whitespace overlaps with that of XML
            // TODO: we should probably move the trimming from this part, to the part where we convert the models
            //       to user facing instances, as we can provide different whitespace strategies then
            // TODO: "Every meta element MUST express a value that is at least one character in length after white
            //        space normalization."
            value = meta.ownText().bind().trim(),
            property = meta
                .rawAttr("property")
                .flatMap(::parseProperty)
                .bind(),
            scheme = meta
                .rawOptionalAttr("scheme")
                ?.let(::parseProperty)
                ?.bind(),
            refines = meta.optionalAttr("refines"),
            identifier = meta.optionalAttr("id"),
            direction = meta
                .optionalAttr("dir")
                ?.let(::parseReadingDirection)
                ?.bind(::UnknownReadingDirection),
            language = meta.optionalAttr("lang", XML_NAMESPACE),
        )
    }

    private fun readLink(link: Element) = effect {
        LinkModel(
            href = link.attr("href").bind(),
            relation = link
                .rawOptionalAttr("rel")
                ?.let(::parseProperty)
                ?.bind(),
            mediaType = link.optionalAttr("media-type"), // conditionally required
            identifier = link.optionalAttr("id"),
            properties = link
                .rawOptionalAttr("properties")
                ?.let(::parseProperties)
                ?.bind(),
            refines = link.optionalAttr("refines"),
        )
    }

    fun write(metadata: MetadataModel, data: WriterData): Element = buildElement("metadata", NAMESPACE) {
        addNamespaceDeclaration(DUBLIN_CORE)
        addNamespaceDeclaration(OPF)

        val (refinements, meta3) = groupMeta3(metadata.metaElements)

        // dublin-core
        addDublinCoreElements(metadata.identifiers, data, refinements)
        addDublinCoreElements(metadata.titles, data, refinements)
        addDublinCoreElements(metadata.languages, data, refinements)
        addDublinCoreElements(metadata.dublinCoreElements, data, refinements)

        if (data.supportsEpub3Features()) {
            // link
            addChildrenWithPossibleRefinements(metadata.links, LinkModel::identifier, ::writeLink, refinements, data)

            // meta3
            addChildrenWithPossibleRefinements(meta3, Opf3MetaModel::identifier, ::writeOpf3Meta, refinements, data)

            // leftover refinements
            if (refinements.isNotEmpty()) {
                val leftovers = refinements.values.flatten()
                addChildrenWithPossibleRefinements(
                    leftovers,
                    Opf3MetaModel::identifier,
                    ::writeOpf3Meta,
                    refinements,
                    data
                )
            }
        }

        // meta2
        // TODO: limit this to writing on only certain versions? like epub2..epub3
        metadata
            .metaElements
            .asSequence()
            .filterIsInstance<Opf2MetaModel>()
            .forEach { addContent(writeOpf2Meta(it)) }
    }

    private fun writeOpfMeta(meta: OpfMeta): Element = when (meta) {
        is Opf2MetaModel -> writeOpf2Meta(meta)
        is Opf3MetaModel -> writeOpf3Meta(meta)
    }

    private fun writeOpf2Meta(meta: Opf2MetaModel): Element = buildElement("meta", NAMESPACE) {
        this["charset"] = meta.charset
        this["content"] = meta.content
        this["http-equiv"] = meta.httpEquiv
        this["name"] = meta.name
        this["scheme"] = meta.scheme
        for (attribute in meta.extraAttributes) {
            setAttribute(attribute.name, attribute.value, attribute.namespace ?: NO_NAMESPACE)
        }
    }

    private fun writeOpf3Meta(meta: Opf3MetaModel): Element = buildElement("meta", NAMESPACE) {
        this["property"] = meta.property.asString()
        this["scheme"] = meta.scheme?.asString()
        this["refines"] = meta.refines
        this["id"] = meta.identifier
        this["dir"] = meta.direction?.value
        this["lang", XML_NAMESPACE] = meta.language
        text = meta.value
    }

    private fun writeLink(link: LinkModel): Element = buildElement("link", NAMESPACE) {
        this["href"] = link.href
        this["rel"] = link.relation?.asString()
        this["media-type"] = link.mediaType
        this["id"] = link.identifier
        this["properties"] = link.properties?.asString()
        this["refines"] = link.refines
    }

    private fun Element.addDublinCoreElements(
        elements: Iterable<DublinCoreModel>,
        data: WriterData,
        refinements: MutableMap<String, MutableList<Opf3MetaModel>>,
    ) {
        addChildrenWithPossibleRefinements(
            elements,
            DublinCoreModel::identifier,
            { DublinCoreModelXml.write(it, data) },
            refinements,
            data,
        )
    }

    private inline fun <T> Element.addChildrenWithPossibleRefinements(
        children: Iterable<T>,
        identifier: T.() -> String?,
        converter: (T) -> Element,
        refinements: MutableMap<String, MutableList<Opf3MetaModel>>,
        data: WriterData,
    ) {
        for (child in children) {
            addContent(converter(child))
            if (data.supportsEpub3Features()) {
                val key = identifier(child)
                val foundRefinements = key?.let(refinements::remove) ?: continue
                addRefinement(foundRefinements, refinements)
            }
        }
    }

    private fun Element.addRefinement(
        metaElements: List<Opf3MetaModel>,
        refinements: MutableMap<String, MutableList<Opf3MetaModel>>,
    ) {
        for (meta in metaElements) {
            addContent(writeOpf3Meta(meta))
            // refinements can be applied to any type of element, meaning that an element may refine an element that
            // itself is refining another element
            val foundRefinements = meta.identifier?.let(refinements::remove) ?: continue
            addRefinement(foundRefinements, refinements)
        }
    }

    private fun groupMeta3(models: List<OpfMeta>): Meta3Grouping {
        val rest = mutableListOf<Opf3MetaModel>()
        val refinements = hashMapOf<String, MutableList<Opf3MetaModel>>().apply {
            for (meta in models) {
                if (meta !is Opf3MetaModel) continue
                if (meta.refines == null) {
                    rest.add(meta)
                    continue
                }
                // 'refines' need to be a relative IRI, so this should generally speaking always contain a '#'
                // this is of course ignoring the query parameter that relative IRIs can contain
                val key = meta.refines.substringAfter('#')
                getOrPut(key) { mutableListOf() }.add(meta)
            }
        }
        return Meta3Grouping(refinements, rest)
    }

    private data class Meta3Grouping(
        val refinements: MutableMap<String, MutableList<Opf3MetaModel>>,
        val rest: List<Opf3MetaModel>,
    )

    override fun missingAttribute(name: String, path: String): OpfReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): OpfReadError = MissingElement(name, path)

    override fun missingText(path: String): OpfReadError = MissingText(path)

    override fun invalidProperty(value: String, cause: ParserResult.Error, path: String): OpfReadError =
        InvalidProperty(value, cause, path)

    // because the geniuses decided to make the new meta element the same name as the old one, while STILL
    // supporting the use of the old meta element, we have to try and do our best to guess if a meta element is
    // actually an opf3 variant or not, how fun
    private fun Element.isOpf3(): Boolean = (getAttribute("property") != null) && (getOwnText() != null)
}