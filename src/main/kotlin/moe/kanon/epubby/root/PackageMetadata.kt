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

@file:Suppress("DataClassPrivateConstructor")

package moe.kanon.epubby.root

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.EpubLegacy
import moe.kanon.epubby.EpubRemoved
import moe.kanon.epubby.EpubVersion
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.logger
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.epubby.root.PackageMetadata.Companion.OPF_NAMESPACE
import moe.kanon.epubby.utils.Direction
import moe.kanon.epubby.utils.compareTo
import moe.kanon.epubby.utils.getAttributeValueOrNone
import moe.kanon.epubby.utils.localeOf
import moe.kanon.epubby.utils.stringify
import moe.kanon.kommons.checkThat
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.xml.Namespace
import org.jdom2.Attribute
import org.jdom2.Element
import org.jdom2.output.Format
import java.nio.charset.Charset
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
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
    val dublinCoreElements: MutableList<DublinCore<*>>,
    val metaElements: MutableList<MetaElement>,
    val links: MutableList<Link>
) : ElementSerializer {
    companion object {
        internal val DC_NAMESPACE = Namespace("dc", "http://purl.org/dc/elements/1.1/")
        internal val OPF_NAMESPACE = Namespace("opf", "http://www.idpf.org/2007/opf")

        internal fun parse(book: Book, packageDocument: Path, element: Element): PackageMetadata = with(element) {
            fun malformed(reason: String): Nothing = raiseMalformedError(book.originFile, packageDocument, reason)
            fun logFaultyMetaElement(element: Element, reason: String) =
                logger.warn { "Encountered a faulty 'meta' element: [${element.stringify(Format.getCompactFormat())}], $reason" }

            val identifiers = getChildren("identifier", DC_NAMESPACE)
                .asSequence()
                .map { DublinCore.Identifier(it.value.trim(), it.getAttributeValue("id")) }
                .toMutableList()
                .ifEmpty { malformed("missing required 'dc:identifier' element in 'metadata'") }
            val titles = getChildren("title", DC_NAMESPACE)
                .asSequence()
                .map {
                    DublinCore.Title(
                        it.value.trim(),
                        it.getAttributeValue("id"),
                        it.getAttributeValue("dir")?.let(Direction.Companion::of),
                        it.getAttributeValue("lang", JNamespace.XML_NAMESPACE)?.let(::localeOf)
                    )
                }
                .toMutableList()
                .ifEmpty { malformed("missing required 'dc:title' element in 'metadata'") }
            val languages = getChildren("language", DC_NAMESPACE)
                .asSequence()
                .map { DublinCore.Language(localeOf(it.value.trim()), it.getAttributeValue("id")) }
                .toMutableList()
                .ifEmpty { malformed("missing required 'dc:language' element in 'metadata'") }
            val dublinCoreElements: MutableList<DublinCore<*>> = children
                .asSequence()
                .filter { it.namespace == DC_NAMESPACE }
                .filterNot { it.name == "identifier" || it.name == "title" || it.name == "language" }
                .filterNot { it.text.isEmpty() && it.attributes.isEmpty() }
                .map {
                    // hooh boy
                    val value = it.text.trim()
                    val dir = it.getAttributeValue("dir")?.let(Direction.Companion::of)
                    val id = it.getAttributeValue("id")
                    val language = it.getAttributeValue("lang")?.let(::localeOf)
                    val dublinCore = when (it.name.toLowerCase()) {
                        "contributor" -> DublinCore.Contributor(value, id, dir, language)
                        "coverage" -> DublinCore.Coverage(value, id, dir, language)
                        "creator" -> DublinCore.Creator(value, id, dir, language)
                        "date" -> DublinCore.Date(value, id)
                        "description" -> DublinCore.Description(value, id, dir, language)
                        "format" -> DublinCore.Format(value, id)
                        "identifier" -> DublinCore.Identifier(value, id)
                        "language" -> DublinCore.Language(localeOf(value), id)
                        "publisher" -> DublinCore.Publisher(value, id, dir, language)
                        "relation" -> DublinCore.Relation(value, id, dir, language)
                        "rights" -> DublinCore.Rights(value, id, dir, language)
                        "source" -> DublinCore.Source(value, id)
                        "subject" -> DublinCore.Subject(value, id, dir, language)
                        "title" -> DublinCore.Title(value, id, dir, language)
                        "type" -> DublinCore.Type(value, id)
                        else -> throw EpubbyException(
                            book.originFile,
                            "Unknown dublin-core element <$name> in file <${book.originFile}>"
                        )
                    }
                    dublinCore._attributes.addAll(
                        it.attributes.asSequence()
                            .filter { attr -> attr.namespace == OPF_NAMESPACE }
                            .map { attr -> attr.detach() }
                    )
                    return@map dublinCore
                }
                .toMutableList()
            val metaElements = children
                .asSequence()
                .filter { it.name == "meta" }
                .map {
                    return@map if (book.version > Book.Format.EPUB_2_0) {
                        when {
                            // the 'property' attribute is REQUIRED according to the EPUB specification, which means
                            // that any 'meta' elements that are missing it are NOT valid elements
                            it.attributes.none { attr -> attr.name == "property" } -> {
                                logFaultyMetaElement(it, "missing required 'property' attribute")
                                return@map null
                            }
                            // "Every meta element MUST express a value that is at least one character in length after
                            // white space normalization" which means that if the text is blank after being normalized
                            // it's not a valid 'meta' element
                            it.textNormalize.isBlank() -> {
                                logFaultyMetaElement(it, "value/text is blank")
                                return@map null
                            }
                            else -> MetaElement.Modern(
                                it.text,
                                it.getAttributeValue("property"),
                                it.getAttributeValue("id"),
                                it.getAttributeValue("dir")?.let(Direction.Companion::of),
                                it.getAttributeValue("refines"),
                                it.getAttributeValue("scheme"),
                                it.getAttributeValue("lang", JNamespace.XML_NAMESPACE)?.let(::localeOf)
                            )
                        }
                    } else {
                        MetaElement.Legacy.newInstance(
                            it.getAttributeValue("charset"),
                            it.getAttributeValue("content"),
                            it.getAttributeValue("http-equiv"),
                            it.getAttributeValue("name"),
                            it.getAttributeValue("scheme"),
                            it.attributes.filterNot { attr ->
                                attr.name == "charset" || attr.name == "content" || attr.name == "http-equiv"
                                    || attr.name == "name" || attr.name == "scheme"
                            }.toImmutableList()
                        )
                    }
                }
                .filterNotNull()
                .toMutableList()
            val links = children.asSequence()
                .filter { it.name == "link" }
                .map {
                    Link(
                        it.getAttributeValue("href") ?: malformed("'link' element is missing 'href' attribute"),
                        it.getAttributeValue("rel") ?: malformed("'link' element is missing 'rel' attribute"),
                        it.getAttributeValueOrNone("media-type"),
                        it.getAttributeValueOrNone("id"),
                        it.getAttributeValueOrNone("properties"),
                        it.getAttributeValueOrNone("refines")
                    )
                }
                .toMutableList()
            return@with PackageMetadata(book, identifiers, titles, languages, dublinCoreElements, metaElements, links)
        }
    }

    // -- IDENTIFIERS -- \\
    /**
     * Returns a list of all the known [identifiers][DublinCore.Identifier]. The returned list is guaranteed to have
     * *at least* one element in it.
     */
    val identifiers: ImmutableList<DublinCore.Identifier> get() = _identifiers.toImmutableList()

    /**
     * Returns the first [identifier][DublinCore.Identifier] of the known [identifiers].
     */
    var identifier: DublinCore.Identifier
        get() = _identifiers[0]
        set(value) {
            _identifiers[0] = value
        }

    /**
     * Creates a new [Identifier][DublinCore.Identifier] instance from the given [value] and [id] and adds it to the
     * known [identifiers].
     *
     * @param [value] a string containing an unambiguous identifier
     * @param [id] the [id](https://www.w3.org/TR/xml-id/) attribute for the element
     */
    @JvmOverloads fun addIdentifier(value: String, id: String? = null) {
        _identifiers += DublinCore.Identifier(value, id)
    }

    /**
     * Attempts to remove the *first* [identifier][DublinCore.Identifier] that has [value][DublinCore.Identifier.value]
     * that matches the given [id], returning `true` if one was found, or `false` if none was found.
     *
     * @throws [IllegalStateException] If [identifiers] only contains *one* element.
     *
     * This is because there *NEEDS* to always be *AT LEAST* one known identifier at all times, which means that we
     * *CAN NOT* perform any removal operations if `identifiers` only contains one element.
     */
    fun removeIdentifier(id: String): Boolean {
        // there needs to always be AT LEAST one identifier element, so we can't allow any removal operations if there's
        // only one identifier element available
        checkThat(_identifiers.size > 1) { "(identifiers.size <= 1)" }
        return _identifiers.find { it.value == id }?.let { _identifiers.remove(it) } ?: false
    }

    /**
     * Sets the [Identifier][DublinCore.Identifier] stored under the given [index] to the given [identifier].
     *
     * @throws [IndexOutOfBoundsException] if [index] is out of range
     *
     * (`index < 0 || index > identifiers.size`)
     */
    fun setIdentifier(index: Int, identifier: DublinCore.Identifier) {
        _identifiers[index] = identifier
    }

    // -- TITLES -- \\
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
    var title: DublinCore.Title
        get() = _titles[0]
        set(value) {
            _titles[0] = value
        }

    /**
     * Creates a new [Title][DublinCore.Title] instance from the given [value], [id], [dir] and [language] and adds it
     * to the known [titles].
     *
     * @param [value] a string containing a title for the [book]
     * @param [id] the [id](https://www.w3.org/TR/xml-id/) attribute for the element
     * @param [dir] specifies the base text direction of the [value]
     * @param [language] specifies the language used in the [value]
     */
    @JvmOverloads fun addTitle(value: String, id: String? = null, dir: Direction? = null, language: Locale? = null) {
        _titles += DublinCore.Title(value, id, dir, language)
    }

    /**
     * Attempts to remove the *first* [title][DublinCore.Title] that has [value][DublinCore.Title.value] that matches
     * the given [title], returning `true` if one was found, or `false` if none was found.
     *
     * @throws [IllegalStateException] If [titles] only contains *one* element.
     *
     * This is because there *NEEDS* to always be *AT LEAST* one known title at all times, which means that we
     * *CAN NOT* perform any removal operations if `titles` only contains one element.
     */
    fun removeTitle(title: String): Boolean {
        // there needs to always be AT LEAST one title element, so we can't allow any removal operations if there's
        // only one title element available
        checkThat(_titles.size > 1) { "(titles.size <= 1)" }
        return _titles.find { it.value == title }?.let { _titles.remove(it) } ?: false
    }

    /**
     * Sets the [Title][DublinCore.Title] stored under the given [index] to the given [title].
     *
     * @throws [IndexOutOfBoundsException] if [index] is out of range
     *
     * (`index < 0 || index > titles.size`)
     */
    fun setTitle(index: Int, title: DublinCore.Title) {
        _titles[index] = title
    }

    // -- LANGUAGES -- \\
    /**
     * Returns a list of all the known [languages][DublinCore.Language]. The returned list is guaranteed to have
     * *at least* one element in it.
     */
    val languages: ImmutableList<DublinCore.Language> get() = _languages.toImmutableList()

    /**
     * Returns the first language of the known [languages].
     */
    var language: DublinCore.Language
        get() = _languages[0]
        set(value) {
            _languages[0] = value
        }

    /**
     * Creates a new [Language][DublinCore.Language] instance from the given [value] and [id] and adds it to the
     * known [languages].
     *
     * @param [value] a [Locale] instance
     * @param [id] the [id](https://www.w3.org/TR/xml-id/) attribute for the element
     */
    @JvmOverloads fun addLanguage(value: Locale, id: String? = null) {
        _languages += DublinCore.Language(value, id)
    }

    /**
     * Attempts to remove the *first* [language][DublinCore.Language] that has [value][DublinCore.Language.value] that
     * matches the given [locale], returning `true` if one was found, or `false` if none was found.
     *
     * @throws [IllegalStateException] If [languages] only contains *one* element.
     *
     * This is because there *NEEDS* to always be *AT LEAST* one known language at all times, which means that we
     * *CAN NOT* perform any removal operations if `languages` only contains one element.
     */
    fun removeLanguage(locale: Locale): Boolean {
        // there needs to always be AT LEAST one language element, so we can't allow any removal operations if there's
        // only one language element available
        checkThat(_languages.size > 1) { "(titles.size <= 1)" }
        return _languages.find { it.value == locale }?.let { _languages.remove(it) } ?: false
    }

    /**
     * Sets the [Language][DublinCore.Language] stored under the given [index] to the given [language].
     *
     * @throws [IndexOutOfBoundsException] if [index] is out of range
     *
     * (`index < 0 || index > languages.size`)
     */
    fun setLanguage(index: Int, language: DublinCore.Language) {
        _languages[index] = language
    }

    // -- OTHER -- \\
    /**
     * Updates the `last-modified` date of the [book] this `metadata` element is tied to.
     */
    fun updateLastModified() {
        val lastModified = LocalDateTime.now()
        logger.debug { "Updating last-modified date to <$lastModified>" }
        if (book.version < Book.Format.EPUB_3_0) {
            fun predicate(dc: DublinCore<*>) = dc._attributes.any { it.name == "event" && it.value == "modification" }
            val element = DublinCore.Date(lastModified)
                .apply { addAttribute(Attribute("event", "modification", DC_NAMESPACE)) }
            if (dublinCoreElements.any(::predicate)) {
                dublinCoreElements[dublinCoreElements.indexOfFirst(::predicate)] = element
            } else dublinCoreElements += element
        } else {
            fun predicate(it: MetaElement) = (it as MetaElement.Modern).property == "dcterms:modified"
            val element = MetaElement.Modern(
                lastModified.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "dcterms:modified"
            )
            if (metaElements.any(::predicate)) {
                metaElements[metaElements.indexOfFirst(::predicate)] = element
            } else metaElements += element
        }
    }

    override fun toElement(): Element = Element("metadata", PackageDocument.NAMESPACE).apply {
        addNamespaceDeclaration(DC_NAMESPACE)
        if (book.version < Book.Format.EPUB_3_0) addNamespaceDeclaration(OPF_NAMESPACE)
        _identifiers.map { it.toElement() }.forEach { addContent(it) }
        _titles.map { it.toElement() }.forEach { addContent(it) }
        _languages.map { it.toElement() }.forEach { addContent(it) }
        dublinCoreElements.map { it.toElement() }.forEach { addContent(it) }
        metaElements.map(MetaElement::toElement).forEach { addContent(it) }
        links.map(Link::toElement).forEach { addContent(it) }
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageMetadata -> false
        book != other.book -> false
        _identifiers != other._identifiers -> false
        _titles != other._titles -> false
        _languages != other._languages -> false
        dublinCoreElements != other.dublinCoreElements -> false
        metaElements != other.metaElements -> false
        links != other.links -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + _identifiers.hashCode()
        result = 31 * result + _titles.hashCode()
        result = 31 * result + _languages.hashCode()
        result = 31 * result + dublinCoreElements.hashCode()
        result = 31 * result + metaElements.hashCode()
        result = 31 * result + links.hashCode()
        return result
    }

    override fun toString(): String =
        "PackageMetadata(identifiers=$_identifiers, titles=$_titles, languages=$_languages, dublinCoreElements=$dublinCoreElements, metaElements=$metaElements, links=$links)"


    /**
     * Represents the [link](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-opf-link)
     * element.
     *
     * Linked resources are not [Publication Resources](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-publication-resource)
     * and *MUST NOT* be listed in the [manifest][PackageManifest]. A linked resource *MAY* be embedded in a
     * `Publication Resource` that is listed in the `manifest`, however, in which case it *MUST* be a
     * [Core Media Type Resource](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-core-media-types)
     * *(e.g., an EPUB Content Document could contain a metadata record serialized as
     * [RDFA-CORE](https://www.w3.org/TR/rdfa-core/) or [JSON-LD](https://www.w3.org/TR/json-ld/)).*
     *
     * @property [href] TODO
     * @property [mediaType] TODO
     * @property [relation] TODO
     * @property [identifier] TODO
     * @property [properties] TODO
     * @property [refines] TODO
     */
    data class Link @JvmOverloads constructor(
        val href: String,
        @SerializedName("rel") val relation: String,
        val mediaType: Option<String> = None,
        @SerializedName("id") val identifier: Option<String> = None,
        val properties: Option<String> = None,
        val refines: Option<String> = None
    ) : ElementSerializer {
        override fun toElement(): Element = Element("link", PackageDocument.NAMESPACE).apply {
            setAttribute("href", href)
            setAttribute("rel", relation)
            mediaType.ifPresent { setAttribute("media-type", it) }
            identifier.ifPresent { setAttribute("id", it) }
            properties.ifPresent { setAttribute("properties", it) }
            refines.ifPresent { setAttribute("refines", it) }
        }
    }
}

/**
 * Represents the `meta` element, this may be the [meta](https://www.w3.org/TR/2011/WD-html5-author-20110809/the-meta-element.html)
 * element used in EPUB 2.0, or the [meta](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-meta)
 * element used in EPUB 3.0.
 */
// this is so that rather than having to have two different lists that may or may not contain any elements depending
// on the EPUB format used, we have one list containing 'MetaElement' which has implementations that match the current
// EPUB format.
sealed class MetaElement : ElementSerializer {
    // TODO: Come up with better names?

    abstract override fun toElement(): Element

    /**
     * Represents the [meta](https://www.w3.org/TR/2011/WD-html5-author-20110809/the-meta-element.html) used in EPUB 2.0.
     *
     * > **Note:** The content attribute MUST be defined if the name or the http-equiv attribute is defined. If none of
     * these are defined, the content attribute CANNOT be defined.
     *
     * Due to the above there is no guarantee that any of the properties defined in this class will actually carry a
     * value.
     *
     * For more information regarding the `meta` element and what the different parameters do, see the specification
     * linked above.
     */
    @EpubLegacy(Book.Format.EPUB_3_0)
    data class Legacy private constructor(
        val charset: Option<String> = None,
        val content: Option<String> = None,
        @SerializedName("http-equiv") val httpEquivalent: Option<String> = None,
        val name: Option<String> = None,
        val scheme: Option<String> = None,
        val globalAttributes: ImmutableList<Attribute> = immutableListOf()
    ) : MetaElement() {
        companion object {
            /**
             * Returns a new [Legacy] instance for the given [httpEquiv] with the given [content].
             */
            @JvmOverloads @JvmStatic fun withHttpEquiv(
                httpEquiv: String,
                content: String,
                scheme: String? = null
            ): Legacy = Legacy(httpEquivalent = Option(httpEquiv), content = Option(content), scheme = Option(scheme))

            /**
             * Returns a new [Legacy] instance for the given [name] with the given [content].
             */
            @JvmOverloads @JvmStatic fun withName(name: String, content: String, scheme: String? = null): Legacy =
                Legacy(name = Option(name), content = Option(content), scheme = Option(scheme))

            /**
             * Returns a new [Legacy] instance for the given [charset].
             */
            @JvmOverloads @JvmStatic fun withCharset(charset: String, scheme: String? = null): Legacy =
                Legacy(charset = Option(charset), scheme = Option(scheme))

            /**
             * Returns a new [Legacy] instance for the given [charset].
             */
            @JvmOverloads @JvmStatic fun withCharset(charset: Charset, scheme: String? = null): Legacy =
                Legacy(charset = Option(charset.displayName()), scheme = Option(scheme))

            @JvmSynthetic internal fun newInstance(
                charset: String? = null,
                content: String? = null,
                httpEquiv: String? = null,
                name: String? = null,
                scheme: String? = null,
                globalAttributes: ImmutableList<Attribute> = immutableListOf()
            ): Legacy = Legacy(
                Option(charset),
                Option(content),
                Option(httpEquiv),
                Option(name),
                Option(scheme),
                globalAttributes
            )
        }

        override fun toElement(): Element = Element("meta", PackageDocument.NAMESPACE).apply {
            charset.ifPresent { setAttribute("charset", it) }
            this@Legacy.content.ifPresent { setAttribute("content", it) }
            httpEquivalent.ifPresent { setAttribute("http-equiv", it) }
            this@Legacy.name.ifPresent { setAttribute("name", it) }
            scheme.ifPresent { setAttribute("scheme", it) }
            globalAttributes.forEach { setAttribute(it) }
        }
    }

    // TODO: Make a system for working with property data types and processing?
    // (https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-property-datatype)

    /**
     * Represents the [meta](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-meta)
     * element used in EPUB 3.0.
     *
     * @property [value] The actual value that this metadata carries.
     * @property [property] A string representing a [property data type](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-property-datatype).
     * @property [identifier] TODO
     * @property [direction] TODO
     * @property [refines] Identifies the expression or resource augmented that is being augmented by this `meta`
     * element.
     *
     * This must be a relative [IRI](https://tools.ietf.org/html/rfc3987) referencing the resource or element that is
     * being augmented.
     * @property [scheme] Identifies the system or scheme that the element's [value] is drawn from.
     * @property [language] TODO
     */
    data class Modern private constructor(
        val value: String,
        val property: String,
        @SerializedName("id") val identifier: Option<String>,
        @SerializedName("dir") val direction: Option<Direction>,
        val refines: Option<String>,
        val scheme: Option<String>,
        @SerializedName("xml:lang") val language: Option<Locale>
    ) : MetaElement() {
        @JvmOverloads constructor(
            value: String,
            property: String,
            id: String? = null,
            dir: Direction? = null,
            refines: String? = null,
            scheme: String? = null,
            language: Locale? = null
        ) : this(value, property, Option(id), Option(dir), Option(refines), Option(scheme), Option(language))

        override fun toElement(): Element = Element("meta", PackageDocument.NAMESPACE).apply {
            setAttribute("property", property)
            identifier.ifPresent { setAttribute("id", it) }
            direction.ifPresent { setAttribute("dir", it.toString()) }
            refines.ifPresent { setAttribute("refines", it) }
            scheme.ifPresent { setAttribute("scheme", it) }
            language.ifPresent { setAttribute("lang", it.toLanguageTag(), JNamespace.XML_NAMESPACE) }
            text = this@Modern.value
        }
    }
}

/**
 * Represents an abstract implementation of a [dublin core element](http://www.dublincore.org/specifications/dublin-core/dces/).
 */
sealed class DublinCore<T>(val label: String) : ElementSerializer {
    @SerializedName("id") abstract val identifier: Option<String>
    abstract val value: T

    // for EPUB 2.0 compliance, as the 'meta' element wasn't defined back then, so 'opf:property' attributes were used,
    // which means we need to catch them and then just throw them back onto the element during 'toElement' invocation
    @EpubRemoved(Book.Format.EPUB_3_0)
    @JvmSynthetic internal var _attributes: MutableList<Attribute> = ArrayList()

    /**
     * Returns a list containing any attributes that "overflowed".
     *
     * This is for backwards compatibility with [EPUB_2.0][Book.Format.EPUB_2_0] versions, as they stored role info
     * differently.
     */
    @EpubRemoved(Book.Format.EPUB_3_0)
    @EpubVersion(Book.Format.EPUB_2_0)
    val attributes: ImmutableList<Attribute>
        get() = _attributes.toImmutableList()

    /**
     * Returns a `String` version of the [value] of `this` dublin-core metadata element.
     */
    protected abstract fun stringify(): String

    override fun toElement(): Element = Element(label.toLowerCase(), PackageMetadata.DC_NAMESPACE).apply {
        identifier.ifPresent { setAttribute("id", it) }
        if (this is FullElement) {
            direction.ifPresent { setAttribute("dir", it.toString()) }
            language.ifPresent { setAttribute("lang", it.toLanguageTag(), JNamespace.XML_NAMESPACE) }
        }
        appendExtraAttributes(this)
        text = this@DublinCore.stringify()
    }

    /**
     * Adds the given [attribute] to the [attributes] of `this` element.
     *
     * @throws [IllegalArgumentException] if [attribute] does not belong to the [OPF][OPF_NAMESPACE] namespace
     */
    fun addAttribute(attribute: Attribute) {
        attribute.namespace = OPF_NAMESPACE
        _attributes.add(attribute)
    }

    /**
     * Attempts to remove the first [Attribute] that has a [name][Attribute.name] that matches with the given [name],
     * returning `true` if one is found and removed, `false` if it is not removed/not found.
     */
    fun removeAttribute(name: String): Boolean =
        _attributes.find { it.name == name }?.let { _attributes.remove(it) } ?: false

    interface FullElement {
        @SerializedName("dir") val direction: Option<Direction>
        @SerializedName("xml:lang") val language: Option<Locale>
    }

    private fun appendExtraAttributes(to: Element) {
        if (_attributes.isNotEmpty()) for (attr in _attributes) to.setAttribute(attr)
    }

    /**
     * A contributor is an entity that is responsible for making contributions to the [Book].
     *
     * Examples of a `Contributor` include a person, an organization, or a service. Typically, the name of a
     * `Contributor` should be used to indicate the entity.
     */
    data class Contributor private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Contributor"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
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
    data class Coverage private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Coverage"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
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
    data class Creator private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Creator"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
    }

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     *
     * `Date` may be used to express temporal information at any level of granularity.
     */
    data class Date private constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String>
    ) : DublinCore<String>("Date") {
        @JvmOverloads constructor(
            value: LocalDateTime,
            id: String? = null
        ) : this(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), Option(id))

        @JvmOverloads constructor(
            value: String,
            id: String? = null
        ) : this(value, Option(id))

        override fun stringify(): String = value
    }

    /**
     * An account of the [Book].
     *
     * `Description` may include but is not limited to: an abstract, a table of contents, a graphical representation,
     * or a free-text account of the resource.
     */
    data class Description private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Description"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
    }

    /**
     * The file format, physical medium, or dimensions of the resource.
     *
     * Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary
     * such as the list of [Internet Media Types](http://www.iana.org/assignments/media-types/).
     */
    data class Format private constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String>
    ) : DublinCore<String>("Format") {
        @JvmOverloads constructor(value: String, id: String? = null) : this(value, Option(id))

        override fun stringify(): String = value
    }

    /**
     * An unambiguous reference to the resource within a given context.
     *
     * Recommended best practice is to identify the resource by means of a string conforming to a formal identification
     * system.
     */
    data class Identifier private constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String>
    ) : DublinCore<String>("Identifier") {
        @JvmOverloads constructor(value: String, id: String? = null) : this(value, Option(id))

        override fun stringify(): String = value
    }

    /**
     * A language of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as [RFC 4646](http://www.ietf.org/rfc/rfc4646.txt).
     */
    data class Language private constructor(
        override val value: Locale,
        @SerializedName("id") override val identifier: Option<String>
    ) : DublinCore<Locale>("Language") {
        @JvmOverloads constructor(value: Locale, id: String? = null) : this(value, Option(id))

        override fun stringify(): String = value.toLanguageTag()
    }

    /**
     * An entity responsible for making the resource available.
     *
     * Examples of a `Publisher` include a person, an organization, or a service. Typically, the name of a `Publisher`
     * should be used to indicate the entity.
     */
    data class Publisher private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Publisher"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
    }

    /**
     * A related resource.
     *
     * Recommended best practice is to identify the related resource by means of a string conforming to a formal
     * identification system.
     */
    data class Relation private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Relation"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
    }

    /**
     * Information about rights held in and over the resource.
     *
     * Typically, rights information includes a statement about various property rights associated with the resource,
     * including intellectual property rights.
     */
    data class Rights private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Rights"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
    }

    /**
     * A related resource from which the described resource is derived.
     *
     * The described resource may be derived from the related resource in whole or in part. Recommended best practice
     * is to identify the related resource by means of a string conforming to a formal identification system.
     */
    data class Source private constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String>
    ) : DublinCore<String>("Source") {
        @JvmOverloads constructor(value: String, id: String? = null) : this(value, Option(id))

        override fun stringify(): String = value
    }

    /**
     * The topic of the resource.
     *
     * Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended
     * best practice is to use a controlled vocabulary.
     */
    data class Subject private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Subject"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
    }

    /**
     * A name given to the resource.
     */
    data class Title private constructor(
        override val value: String,
        @SerializedName("dir") override val direction: Option<Direction>,
        @SerializedName("id") override val identifier: Option<String>,
        @SerializedName("xml:lang") override val language: Option<Locale>
    ) : DublinCore<String>("Title"), FullElement {
        @JvmOverloads constructor(
            value: String,
            id: String? = null,
            dir: Direction? = null,
            language: Locale? = null
        ) : this(value, Option(dir), Option(id), Option(language))

        override fun stringify(): String = value
    }

    /**
     * The nature or genre of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as the [DCMI Type Vocabulary](http://dublincore.org/specifications/dublin-core/dcmi-type-vocabulary/#H7).
     * To describe the file format, physical medium, or dimensions of the resource, use the [Format] element.
     */
    data class Type private constructor(
        override val value: String,
        @SerializedName("id") override val identifier: Option<String>
    ) : DublinCore<String>("Type") {
        @JvmOverloads constructor(value: String, id: String? = null) : this(value, Option(id))

        override fun stringify(): String = value
    }
}