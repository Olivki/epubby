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

package moe.kanon.epubby.resources.root

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.epubby.resources.Direction
import moe.kanon.epubby.utils.getAttributeValueOrNone
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.writeOut
import moe.kanon.xml.Namespace
import org.jdom2.Attribute
import org.jdom2.Element
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jdom2.Namespace as JNamespace

/**
 * Represents the [metadata](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-opf-metadata)
 * element in a [package-document][PackageDocument].
 */
class PackageMetadata private constructor(
    val book: Book,
    private val _identifiers: MutableList<DublinCore.Identifier>,
    private val _titles: MutableList<DublinCore.Title>,
    private val _languages: MutableList<DublinCore.Language>,
    private val _elements: MutableList<DublinCore<*>>,
    private val _metas: MutableList<Meta>
) : ElementSerializer {
    companion object {
        private const val NAMESPACE_URI = "http://purl.org/dc/elements/1.1/"

        // TODO: Give back 'book: Book' param?
        internal fun parse(origin: Path, packageDocument: Path, element: Element): PackageMetadata = with(element) {
            fun malformed(reason: String): Nothing = raiseMalformedError(origin, packageDocument, reason)

            val namespace = Namespace("dc", NAMESPACE_URI)

            writeOut(children)

            val identifiers = getChildren("identifier", namespace).asSequence()
                .map { DublinCore.Identifier(it.value.trim(), it.getAttributeValueOrNone("id")) }
                .toMutableList()
                .ifEmpty { malformed("no 'dc:identifier' element in 'metadata' element") }
            writeOut(identifiers)
            TODO()
        }
    }

    /**
     * Returns a list of all the known [identifiers][DublinCore.Identifier]. The returned list is guaranteed to have
     * *at least* one element in it.
     */
    val identifiers: ImmutableList<DublinCore.Identifier> get() = _identifiers.toImmutableList()

    /**
     * Returns the first [identifier][DublinCore.Identifier] of the known [identifiers].
     */
    val identifier: DublinCore.Identifier get() = _identifiers[0]

    /**
     * Returns a list of all the known [titles][DublinCore.Title]. The returned list is guaranteed to have *at least*
     * one element in it.
     */
    val titles: ImmutableList<DublinCore.Title> get() = _titles.toImmutableList()

    /**
     * Returns the first [title][DublinCore.Title] element of the known [titles].
     *
     * This is in compliance with the following excerpt from the EPUB specification;
     * > [Reading Systems](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-epub-reading-system)
     * *MUST* recognize the first title element in document order as the main title of the EPUB Publication.
     */
    val title: DublinCore.Title get() = _titles[0]

    /**
     * Returns a list of all the known [languages][DublinCore.Language]. The returned list is guaranteed to have
     * *at least* one element in it.
     */
    val languages: ImmutableList<DublinCore.Language> get() = _languages.toImmutableList()

    /**
     * Returns the first language of the known [languages].
     */
    val language: DublinCore.Language get() = _languages[0]

    /**
     * Returns a list containing any extra [DublinCore] elements outside of the required [identifiers], [titles] and
     * [languages].
     */
    val extraElements: ImmutableList<DublinCore<*>> get() = _elements.toImmutableList()

    /**
     * Returns a list containing all the [meta][Meta] elements defined in `this` meta-data element.
     */
    val metaElements: ImmutableList<Meta> get() = _metas.toImmutableList()

    override fun toElement(): Element = Element("metadata", Namespace("dc", NAMESPACE_URI)).apply {
        _identifiers.map { it.toElement().setNamespace(namespace) }.forEach { addContent(it) }
        _titles.map { it.toElement().setNamespace(namespace) }.forEach { addContent(it) }
        _languages.map { it.toElement().setNamespace(namespace) }.forEach { addContent(it) }
        _elements.map { it.toElement().setNamespace(namespace) }.forEach { addContent(it) }
        _metas.map(Meta::toElement).forEach { addContent(it) }
    }

    /**
     * Represents a [meta element](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-meta).
     */
    data class Meta @JvmOverloads constructor(
        val property: String,
        val value: String,
        @SerializedName("id") val identifier: Option<String> = None,
        @SerializedName("dir") val direction: Option<Direction> = None,
        val refines: Option<String> = None,
        val scheme: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : ElementSerializer {
        override fun toElement(): Element = Element("meta").apply {
            setAttribute("property", property)
            identifier.ifPresent { setAttribute("id", it) }
            direction.ifPresent { setAttribute("dir", it.toString()) }
            refines.ifPresent { setAttribute("refines", it) }
            scheme.ifPresent { setAttribute("scheme", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            text = this@Meta.value
        }
    }

    data class Link(val href: String) // TODO
}

/**
 * Represents an abstract implementation of a [dublin core element](http://www.dublincore.org/specifications/dublin-core/dces/).
 */
sealed class DublinCore<T>(val label: String) : ElementSerializer {
    @SerializedName("id") abstract val identifier: Option<String>
    abstract val value: T

    // for EPUB 2.0 compliance, as the 'meta' element wasn't defined back then, so 'opf:property' attributes were used,
    // which means we need to catch them and then just throw them back onto the element during 'toElement' invocation
    protected val _attributes: MutableList<Attribute> = ArrayList()

    /**
     * Returns a list containing any attributes that "overflowed".
     *
     * This is for backwards compatibility with [EPUB_2.0][Book.Format.EPUB_2_0] versions, as they stored role info
     * differently.
     */
    val attributes: ImmutableList<Attribute> get() = _attributes.toImmutableList()

    abstract override fun toElement(): Element

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is DublinCore<*> -> false
        label != other.label -> false
        identifier != other.identifier -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + identifier.hashCode()
        return result
    }

    /**
     * A contributor is an entity that is responsible for making contributions to the [Book].
     *
     * Examples of a `Contributor` include a person, an organization, or a service. Typically, the name of a
     * `Contributor` should be used to indicate the entity.
     */
    data class Contributor @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Contributor") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Contributor.value
        }
    }

    /**
     * The spatial or temporal topic of the resource, the spatial applicability of the resource, or the jurisdiction
     * under which the [Book] is relevant.
     *
     * Spatial topic and spatial applicability may be a named place or a location specified by its geographic
     * coordinates. Temporal topic may be a named period, date, or date range. A jurisdiction may be a named
     * administrative entity or a geographic place to which the resource applies. Recommended best practice is to use a
     * controlled vocabulary such as the [Thesaurus of Geographic Names](http://www.getty.edu/research/tools/vocabulary/tgn/index.html).
     * Where appropriate, named places or time periods can be used in preference to numeric identifiers such as sets of
     * coordinates or date ranges.
     */
    data class Coverage @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Coverage") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Coverage.value
        }
    }

    /**
     * The entity primarily responsible for making the [Book].
     *
     * Do note that by "primarily responsible" it means the one who *originally* wrote the *contents* of the `Book`,
     * not the person who made the epub.
     *
     * Examples of a `Creator` include a person, an organization, or a service. Typically, the name of a `Creator`
     * should be used to indicate the entity.
     */
    data class Creator @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Creator") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Creator.value
        }
    }

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     *
     * `Date` may be used to express temporal information at any level of granularity.
     */
    data class Date @JvmOverloads constructor(
        override val value: LocalDateTime,
        @SerializedName("id") override val identifier: Option<String> = None
    ) : DublinCore<LocalDateTime>("Date") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            identifier.ifPresent { setAttribute("id", it) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Date.value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }

    /**
     * An account of the [Book].
     *
     * `Description` may include but is not limited to: an abstract, a table of contents, a graphical representation,
     * or a free-text account of the resource.
     */
    data class Description @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Description") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Description.value
        }
    }

    /**
     * The file format, physical medium, or dimensions of the resource.
     *
     * Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary
     * such as the list of [Internet Media Types](http://www.iana.org/assignments/media-types/).
     */
    data class Format @JvmOverloads constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String> = None
    ) : DublinCore<String>("Format") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            identifier.ifPresent { setAttribute("id", it) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Format.value
        }
    }

    /**
     * An unambiguous reference to the resource within a given context.
     *
     * Recommended best practice is to identify the resource by means of a string conforming to a formal identification
     * system.
     */
    data class Identifier @JvmOverloads constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String> = None
    ) : DublinCore<String>("Identifier") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            identifier.ifPresent { setAttribute("id", it) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Identifier.value
        }
    }

    /**
     * A language of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as [RFC 4646](http://www.ietf.org/rfc/rfc4646.txt).
     */
    data class Language @JvmOverloads constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String> = None
    ) : DublinCore<String>("Language") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            identifier.ifPresent { setAttribute("id", it) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Language.value
        }
    }

    /**
     * An entity responsible for making the resource available.
     *
     * Examples of a `Publisher` include a person, an organization, or a service. Typically, the name of a `Publisher`
     * should be used to indicate the entity.
     */
    data class Publisher @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Publisher") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Publisher.value
        }
    }

    /**
     * A related resource.
     *
     * Recommended best practice is to identify the related resource by means of a string conforming to a formal
     * identification system.
     */
    data class Relation @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Relation") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Relation.value
        }
    }

    /**
     * Information about rights held in and over the resource.
     *
     * Typically, rights information includes a statement about various property rights associated with the resource,
     * including intellectual property rights.
     */
    data class Rights @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Rights") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Rights.value
        }
    }

    /**
     * A related resource from which the described resource is derived.
     *
     * The described resource may be derived from the related resource in whole or in part. Recommended best practice
     * is to identify the related resource by means of a string conforming to a formal identification system.
     */
    data class Source @JvmOverloads constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String> = None
    ) : DublinCore<String>("Source") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            identifier.ifPresent { setAttribute("id", it) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Source.value
        }
    }

    /**
     * The topic of the resource.
     *
     * Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended
     * best practice is to use a controlled vocabulary.
     */
    data class Subject @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Subject") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Subject.value
        }
    }

    /**
     * A name given to the resource.
     */
    data class Title @JvmOverloads constructor(
        override val value: String,
        @SerializedName("dir") val direction: Option<Direction> = None,
        @SerializedName("id") override val identifier: Option<String> = None,
        @SerializedName("xml:lang") val language: Option<String> = None
    ) : DublinCore<String>("Title") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            identifier.ifPresent { setAttribute("id", it) }
            language.ifPresent { setAttribute("lang", it, JNamespace.XML_NAMESPACE) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Title.value
        }
    }

    /**
     * The nature or genre of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as the [DCMI Type Vocabulary](http://dublincore.org/specifications/dublin-core/dcmi-type-vocabulary/#H7).
     * To describe the file format, physical medium, or dimensions of the resource, use the [Format] element.
     */
    data class Type @JvmOverloads constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String> = None
    ) : DublinCore<String>("Type") {
        override fun toElement(): Element = Element(label.toLowerCase()).apply {
            identifier.ifPresent { setAttribute("id", it) }
            if (_attributes.isNotEmpty()) for (attr in attributes) setAttribute(attr)
            text = this@Type.value
        }
    }
}