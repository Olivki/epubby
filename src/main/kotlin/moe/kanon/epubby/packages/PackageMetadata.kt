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

package moe.kanon.epubby.packages

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import com.google.common.net.MediaType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.NewFeature
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.structs.DublinCore
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.NonEmptyList
import moe.kanon.epubby.structs.prefixes.PackagePrefix
import moe.kanon.epubby.structs.prefixes.Prefixes
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.structs.props.Property
import moe.kanon.epubby.structs.props.Relationship
import moe.kanon.epubby.structs.props.toStringForm
import moe.kanon.epubby.structs.props.vocabs.VocabularyParseMode
import moe.kanon.epubby.structs.toNonEmptyList
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.toCompactString
import org.jdom2.Attribute
import org.jdom2.Comment
import org.jdom2.Element
import org.jdom2.Namespace
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Represents the [metadata](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata)
 * element belonging to the [package-document][PackageDocument] of the [book].
 */
class PackageMetadata private constructor(
    val book: Book,
    val identifiers: NonEmptyList<DublinCore.Identifier>,
    val titles: NonEmptyList<DublinCore.Title>,
    val languages: NonEmptyList<DublinCore.Language>,
    val dublinCoreElements: MutableList<DublinCore<*>>,
    // TODO: meta-elements is not actually allowed to be empty
    val opf2MetaElements: MutableList<OPF2Meta>,
    val opf3MetaElements: MutableList<OPF3Meta>,
    val links: MutableList<Link>
) {
    /**
     * Returns the first [identifier][DublinCore.Identifier] of the known [identifiers].
     */
    var primaryIdentifier: DublinCore.Identifier
        get() = identifiers[0]
        set(value) {
            identifiers[0] = value
        }

    /**
     * Returns the first [title][DublinCore.Title] element of the known [titles].
     *
     * This is in compliance with the following excerpt from the EPUB specification;
     * > [Reading Systems](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-epub-reading-system)
     * *MUST* recognize the first title element in document order as the main title of the EPUB Publication.
     */
    var primaryTitle: DublinCore.Title
        get() = titles[0]
        set(value) {
            titles[0] = value
        }

    /**
     * Returns the first language of the known [languages].
     */
    var primaryLanguage: DublinCore.Language
        get() = languages[0]
        set(value) {
            languages[0] = value
        }

    // -- DUBLIN-CORE -- \\
    fun addDublinCore(dublinCore: DublinCore<*>) {
        dublinCoreElements += dublinCore
    }

    // TODO: add more things like this
    val authors: ImmutableList<DublinCore.Creator>
        get() = dublinCoreElements.filterIsInstance<DublinCore.Creator>().toImmutableList()

    // -- OTHER -- \\
    // TODO: Add functions for working with the other collections

    // TODO: Add some functions to make this function look cleaner, and just to make stuff like easier to accomplish
    //       for the actual person using this library

    // -- META -- \\
    /**
     * Updates the `last-modified` date of the [book] to the given [date], formatted according to
     * [ISO_LOCAL_DATE_TIME][DateTimeFormatter.ISO_LOCAL_DATE_TIME].
     *
     * This will create a new `last-modified` meta entry if none can be found.
     *
     * The resulting text will use the given [date], with its offset set to [UTC][ZoneOffset.UTC], and formatted with
     * the pattern `"CCYY-MM-DDThh:mm:ssZ"`, as per the EPUB specification.
     */
    @JvmOverloads
    fun setLastModifiedDate(date: LocalDateTime = LocalDateTime.now()) {
        val currentDateTime = date.atOffset(ZoneOffset.UTC).format(LAST_MODIFIED_FORMAT)
        logger.info { "Updating last-modified date of book <$book> to '$currentDateTime'.." }

        when {
            book.version.isOlderThan(BookVersion.EPUB_3_0) -> {
                val dublinCore = dublinCoreElements
                    .filterIsInstance<DublinCore.Date>()
                    .firstOrNull { it.hasAttribute("event", "modification") }

                if (dublinCore != null) {
                    dublinCore.content = currentDateTime
                } else {
                    addDublinCore(DublinCore.Date(currentDateTime).addAttribute("event", "modification"))
                }
            }
            book.version.isNewerThan(BookVersion.EPUB_2_0) -> {
                val property = Property.of(PackagePrefix.DC_TERMS, "modified")
                val meta = opf3MetaElements.firstOrNull { it.property == property }

                if (meta != null) {
                    meta.value = currentDateTime
                } else {
                    opf3MetaElements += OPF3Meta(currentDateTime, property)
                }
            }
            else -> throw IllegalStateException("Unknown version '${book.version}'")
        }
    }

    /**
     * @throws [DateTimeParseException] if the found meta elements value can not be parsed into a date properly
     */
    // TODO: Change return value to 'OffsetDateTime?'
    fun getLastModifiedDate(): LocalDateTime = when {
        book.version.isOlderThan(BookVersion.EPUB_3_0) -> dublinCoreElements
            .filterIsInstance<DublinCore.Date>()
            .firstOrNull { it.hasAttribute("event", "modification") }
            ?.content
            ?.let { LocalDateTime.parse(it, LAST_MODIFIED_FORMAT)/*.atOffset(ZoneOffset.UTC)*/ }
            ?: throw NoSuchElementException("Could not find an element describing the last-modified time.") // this shouldn't happen
        book.version.isNewerThan(BookVersion.EPUB_2_0) -> {
            val property = Property.of(PackagePrefix.DC_TERMS, "modified")
            opf3MetaElements
                .firstOrNull { it.property == property }
                ?.value
                ?.let { LocalDateTime.parse(it, LAST_MODIFIED_FORMAT)/*.atOffset(ZoneOffset.UTC)*/ }
                ?: throw NoSuchElementException("Could not find an element describing the last-modified time.")
        }
        else -> throw IllegalStateException("Unknown version '${book.version}'")
    }

    fun getLastModifiedDateAsString(): String? = when {
        book.version.isOlderThan(BookVersion.EPUB_3_0) -> dublinCoreElements
            .filterIsInstance<DublinCore.Date>()
            .firstOrNull { it.hasAttribute("event", "modification") }?.content
        book.version.isNewerThan(BookVersion.EPUB_2_0) -> opf3MetaElements
            .firstOrNull { it.property == Property.of(PackagePrefix.DC_TERMS, "modified") }
            ?.value
        else -> throw IllegalStateException("Unknown version '${book.version}'")
    }

    // -- INTERNAL -- \\
    // TODO: group dublin-core elements with any opf3meta elements that refine it
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("metadata", namespace).apply {
        addNamespaceDeclaration(Namespaces.DUBLIN_CORE)

        if (book.version.isOlderThan(BookVersion.EPUB_3_0)) {
            addNamespaceDeclaration(Namespaces.OPF_WITH_PREFIX)
        }

        identifiers.forEach { addContent(it.toElement(book)) }
        titles.forEach { addContent(it.toElement(book)) }
        languages.forEach { addContent(it.toElement(book)) }
        dublinCoreElements.forEach { addContent(it.toElement(book)) }
        opf3MetaElements.forEach { addContent(it.toElement()) }
        opf2MetaElements.also { opf2Meta ->
            if (opf2Meta.isNotEmpty() && book.version.isNewerThan(BookVersion.EPUB_2_0)) {
                addContent(Comment("LEGACY META ELEMENTS"))
            }

            opf2Meta.forEach { addContent(it.toElement()) }
        }
        links.forEach { addContent(it.toElement()) }
    }

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
    class OPF2Meta private constructor(
        val charset: Charset? = null,
        val content: String? = null,
        val httpEquivalent: String? = null,
        val name: String? = null,
        val scheme: String? = null,
        val globalAttributes: ImmutableList<Attribute> = persistentListOf()
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("meta", namespace).apply {
            charset?.also { setAttribute("charset", it.name()) }
            this@OPF2Meta.content?.also { setAttribute("content", it) }
            httpEquivalent?.also { setAttribute("http-equiv", it) }
            this@OPF2Meta.name?.also { setAttribute("name", it) }
            scheme?.also { setAttribute("scheme", it) }
            globalAttributes.forEach { setAttribute(it) }
        }

        override fun equals(other: Any?): Boolean = kotlinEquals(other, OPF2_META_PROPS)

        override fun hashCode(): Int = kotlinHashCode(OPF2_META_PROPS)

        override fun toString(): String = kotlinToString(OPF2_META_PROPS, omitNulls = true)

        companion object {
            /**
             * Returns a new [OPF2Meta] instance for the given [httpEquiv] with the given [content].
             */
            @JvmStatic
            @JvmOverloads
            fun withHttpEquiv(
                httpEquiv: String,
                content: String,
                scheme: String? = null
            ): OPF2Meta = OPF2Meta(httpEquivalent = httpEquiv, content = content, scheme = scheme)

            /**
             * Returns a new [OPF2Meta] instance for the given [name] with the given [content].
             */
            @JvmStatic
            @JvmOverloads
            fun withName(name: String, content: String, scheme: String? = null): OPF2Meta =
                OPF2Meta(name = name, content = content, scheme = scheme)

            /**
             * Returns a new [OPF2Meta] instance for the given [charset].
             */
            @JvmStatic
            @JvmOverloads
            fun withCharset(charset: Charset, scheme: String? = null): OPF2Meta =
                OPF2Meta(charset = charset, scheme = scheme)

            @JvmSynthetic
            internal fun newInstance(
                charset: Charset? = null,
                content: String? = null,
                httpEquiv: String? = null,
                name: String? = null,
                scheme: String? = null,
                globalAttributes: ImmutableList<Attribute> = persistentListOf()
            ): OPF2Meta = OPF2Meta(charset, content, httpEquiv, name, scheme, globalAttributes)
        }
    }

    /**
     * Represents the [meta](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-meta)
     * introduced in EPUB 3.0.
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
    class OPF3Meta internal constructor(
        var value: String,
        var property: Property,
        var identifier: Identifier? = null,
        var direction: Direction? = null,
        var refines: DublinCore<*>? = null,
        var scheme: String? = null,
        var language: Locale? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("meta", namespace).apply {
            setAttribute("property", property.toStringForm())
            identifier?.also { setAttribute("id", it.value) }
            direction?.also { setAttribute("dir", it.toString()) }
            refines?.also { setAttribute("refines", "#${it.identifier!!}") }
            scheme?.also { setAttribute("scheme", it) }
            language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }
            text = this@OPF3Meta.value
        }

        override fun equals(other: Any?): Boolean = kotlinEquals(other, OPF3_META_PROPS)

        override fun hashCode(): Int = kotlinHashCode(OPF3_META_PROPS)

        override fun toString(): String = kotlinToString(OPF3_META_PROPS, omitNulls = true)
    }

    // TODO: Update the 'href' value of Link instances when updating a resource path
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
    class Link internal constructor(
        var href: URI,
        var relation: Relationship,
        var mediaType: MediaType? = null,
        var identifier: Identifier? = null,
        // TODO: don't serialize these if version is 2.0
        @NewFeature(since = BookVersion.EPUB_3_0)
        var properties: Properties = Properties.empty(),
        @NewFeature(since = BookVersion.EPUB_3_0)
        var refines: String? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("link", namespace).apply {
            setAttribute("href", href.toString())

            // TODO: Can relation be empty?
            //checkThat(relation.isNotEmpty()) { "relation should not be empty" }
            setAttribute(relation.toAttribute("rel", namespace))
            mediaType?.also { setAttribute("media-type", it.toString()) }
            identifier?.also { setAttribute(it.toAttribute(namespace = namespace)) }

            if (properties.isNotEmpty()) {
                setAttribute(properties.toAttribute(namespace = namespace))
            }

            refines?.also { setAttribute("refines", it) }
        }

        override fun equals(other: Any?): Boolean = kotlinEquals(other, LINK_PROPS)

        override fun hashCode(): Int = kotlinHashCode(LINK_PROPS)

        override fun toString(): String = kotlinToString(LINK_PROPS, omitNulls = true)
    }

    internal companion object {
        private val OPF2_META_PROPS = arrayOf(
            OPF2Meta::charset,
            OPF2Meta::content,
            OPF2Meta::httpEquivalent,
            OPF2Meta::name,
            OPF2Meta::scheme,
            OPF2Meta::globalAttributes
        )

        private val OPF3_META_PROPS = arrayOf(
            OPF3Meta::value,
            OPF3Meta::property,
            OPF3Meta::identifier,
            OPF3Meta::direction,
            OPF3Meta::refines,
            OPF3Meta::scheme,
            OPF3Meta::language
        )

        private val LINK_PROPS =
            arrayOf(Link::href, Link::relation, Link::mediaType, Link::identifier, Link::properties, Link::refines)

        @JvmField
        // TODO: 'CC' should be able to just be replaced with 'YY' as it just stands for Era
        // TODO: 'DateTimeFormatter.ofPattern("CCYY-MM-DDThh:mm:ssZ")' the format that's given in the EPUB spec seems
        //       can not just be directly thrown into a 'ofPattern' function, as it fails on the 'CC' characters,
        //       but when looking at the spec for the 'dateTime' object in XML-SCHEMA there is no mention of the 'CC'
        //       characters either, so I don't know what's actually wrong here? This will just use standard ISO time
        //       until I figure out the correct format here.
        internal val LAST_MODIFIED_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        private val DC_NAMES = persistentHashSetOf("identifier", "title", "language")
        private val META_OPF2_ATTRIBUTES = persistentHashSetOf("charset", "content", "http-equiv", "name", "scheme")
        private val META_OPF3_ATTRIBUTES = persistentHashSetOf("property", "id", "dir", "refines", "scheme", "lang")

        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, file: Path, prefixes: Prefixes): PackageMetadata =
            with(element) {
                val identifiers = getChildren("identifier", Namespaces.DUBLIN_CORE)
                    .mapTo(ArrayList()) { createIdentifier(it) }
                    .ifEmpty { malformed(book.file, file, "missing required 'dc:identifier' element in 'metadata'") }
                    .toNonEmptyList()
                val titles = getChildren("title", Namespaces.DUBLIN_CORE)
                    .mapTo(ArrayList()) { createTitle(it) }
                    .ifEmpty { malformed(book.file, file, "missing required 'dc:title' element in 'metadata'") }
                    .toNonEmptyList()
                val languages = getChildren("language", Namespaces.DUBLIN_CORE)
                    .mapTo(ArrayList()) { createLanguage(it) }
                    .ifEmpty { malformed(book.file, file, "missing required 'dc:language' element in 'metadata'") }
                    .toNonEmptyList()
                val dublinCoreElements: MutableList<DublinCore<*>> = children
                    .asSequence()
                    .filter { it.namespace == Namespaces.DUBLIN_CORE }
                    .filter { it.name !in DC_NAMES && it.text.isNotBlank() && it.attributes.isNotEmpty() }
                    .mapTo(ArrayList()) { createDublinCore(it, book.file, file) }
                val allDublinCoreElements = mutableListOf<DublinCore<*>>().apply {
                    addAll(identifiers)
                    addAll(titles)
                    addAll(languages)
                    addAll(dublinCoreElements)
                }
                val metaElements = getChildren("meta", namespace).asSequence()
                val opf2Metas = metaElements
                    .filter { it.isOPF2MetaElement() }
                    .mapNotNullTo(mutableListOf()) { createOPF2Meta(book, it) }
                val opf3Metas = metaElements
                    .filter { it.isOPF3MetaElement() }
                    .mapNotNullTo(mutableListOf()) {
                        createOPF3Meta(
                            it,
                            book.file,
                            file,
                            prefixes,
                            allDublinCoreElements
                        )
                    }
                val links = getChildren("link", namespace)
                    .mapTo(ArrayList()) { createLink(it, book.file, file, prefixes) }
                PackageMetadata(book, identifiers, titles, languages, dublinCoreElements, opf2Metas, opf3Metas, links)
            }

        private fun Element.isOPF2MetaElement(): Boolean = this.attributes.none { it.name in META_OPF3_ATTRIBUTES } &&
            this.attributes.any { it.name in META_OPF2_ATTRIBUTES }

        private fun Element.isOPF3MetaElement(): Boolean = this.attributes.any { it.name in META_OPF3_ATTRIBUTES } &&
            this.attributes.none { it.name in META_OPF2_ATTRIBUTES }

        // TODO: As I figured out that the "faulty" element we encountered was just a legacy 'meta' element, we can
        //       probably just have this fail loudly instead?
        private fun createOPF3Meta(
            element: Element,
            container: Path,
            current: Path,
            prefixes: Prefixes,
            dublinCoreElements: List<DublinCore<*>>
        ): OPF3Meta? {
            fun faultyElement(reason: String): OPF3Meta? {
                logger.warn { "Discarding a faulty 'meta' element: [${element.toCompactString()}]: $reason." }
                return null
            }

            return when {
                // the 'property' attribute is REQUIRED according to the EPUB specification, which means
                // that any 'meta' elements that are missing it are NOT valid elements, and should therefore
                // be discarded by the system, as we have no way of salvaging a faulty meta element, and we do
                // not want to be serializing faulty elements during the writing process
                element.attributes.none { it.name == "property" } -> faultyElement("missing required 'property' attribute")
                // "Every meta element MUST express a value that is at least one character in length after
                // white space normalization" which means that if the text is blank after being normalized
                // it's not a valid 'meta' element
                element.textNormalize.isBlank() -> faultyElement("value/text is blank")
                else -> {
                    val value = element.textNormalize
                    val property = element.attr("property", container, current)
                        .let { Property.parse(OPF3Meta::class, it, prefixes) }
                    val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
                    val direction = element.getAttributeValue("dir")?.let(Direction.Companion::of)
                    val refines = element.getAttributeValue("refines")?.let { rawId ->
                        val id = Identifier.of(rawId.substringAfter('#'))
                        dublinCoreElements.find { it.identifier == id } ?: malformed(
                            container,
                            current,
                            "could not find a dublin-core element with the id '$id' to refine"
                        )
                    }
                    val scheme = element.getAttributeValue("scheme")
                    val language =
                        element.getAttributeValue("lang", Namespace.XML_NAMESPACE)?.let(Locale::forLanguageTag)
                    OPF3Meta(value, property, identifier, direction, refines, scheme, language)
                }
            }
        }

        private fun createOPF2Meta(book: Book, element: Element): OPF2Meta {
            if (book.version.isNewerThan(BookVersion.EPUB_2_0)) {
                logger.debug { "Encountered a legacy 'meta' element: ${element.toCompactString()}" }
            }

            val charset = element.getAttributeValue("charset")?.let(Charset::forName)
            val content = element.getAttributeValue("content")
            val httpEquiv = element.getAttributeValue("http-equiv")
            val name = element.getAttributeValue("name")
            val scheme = element.getAttributeValue("scheme")
            val globalAttributes =
                element.attributes.filterNot { it.name in META_OPF2_ATTRIBUTES }.toImmutableList()
            return OPF2Meta.newInstance(charset, content, httpEquiv, name, scheme, globalAttributes)
        }

        private fun createIdentifier(element: Element): DublinCore.Identifier {
            val value = element.textNormalize
            val identifier = element.getAttributeValue("id")?.let { Identifier.of(it) }
            val attributes = element
                .attributes
                .asSequence()
                .filter { it.namespace == Namespaces.OPF_WITH_PREFIX }
                .map(Attribute::detach)
            return DublinCore.Identifier(value, identifier).also {
                it._attributes.addAll(attributes)
            }
        }

        private fun createTitle(element: Element): DublinCore.Title {
            val value = element.textNormalize
            val identifier = element.getAttributeValue("id")?.let { Identifier.of(it) }
            val direction = element.getAttributeValue("dir")?.let(Direction.Companion::of)
            val language = element.getAttributeValue("lang", Namespace.XML_NAMESPACE)?.let(Locale::forLanguageTag)
            val attributes = element
                .attributes
                .asSequence()
                .filter { it.namespace == Namespaces.OPF_WITH_PREFIX }
                .map(Attribute::detach)
            return DublinCore.Title(value, identifier, direction, language).also {
                it._attributes.addAll(attributes)
            }
        }

        private fun createLanguage(element: Element): DublinCore.Language {
            val value = Locale.forLanguageTag(element.textNormalize)
            val identifier = element.getAttributeValue("id")?.let { Identifier.of(it) }
            val attributes = element
                .attributes
                .asSequence()
                .filter { it.namespace == Namespaces.OPF_WITH_PREFIX }
                .map(Attribute::detach)
            return DublinCore.Language(value, identifier).also {
                it._attributes.addAll(attributes)
            }
        }

        private fun createDublinCore(element: Element, container: Path, current: Path): DublinCore<*> {
            val value = element.textNormalize
            val direction = element.getAttributeValue("dir")?.let(Direction.Companion::of)
            val identifier = element.getAttributeValue("id")?.let { Identifier.of(it) }
            val language = element.getAttributeValue("lang")?.let(Locale::forLanguageTag)
            // this is missing the 'identifier', 'title' and 'language' elements as those should be dealt with separately
            val dublinCore: DublinCore<*> = when (element.name.toLowerCase()) {
                "contributor" -> DublinCore.Contributor(value, identifier, direction, language)
                "coverage" -> DublinCore.Coverage(value, identifier, direction, language)
                "creator" -> DublinCore.Creator(value, identifier, direction, language)
                "date" -> DublinCore.Date(value, identifier)
                "description" -> DublinCore.Description(value, identifier, direction, language)
                "format" -> DublinCore.Format(value, identifier)
                "publisher" -> DublinCore.Publisher(value, identifier, direction, language)
                "relation" -> DublinCore.Relation(value, identifier, direction, language)
                "rights" -> DublinCore.Rights(value, identifier, direction, language)
                "source" -> DublinCore.Source(value, identifier)
                "subject" -> DublinCore.Subject(value, identifier, direction, language)
                "type" -> DublinCore.Type(value, identifier)
                else -> malformed(container, current, "unknown dublin-core element <${element.name}>")
            }
            val attributes = element
                .attributes
                .asSequence()
                .filter { it.namespace == Namespaces.OPF_WITH_PREFIX }
                .map(Attribute::detach)
            dublinCore._attributes.addAll(attributes)
            return dublinCore
        }

        private fun createLink(element: Element, container: Path, current: Path, prefixes: Prefixes): Link =
            with(element) {
                val href = URI(attr("href", container, current))
                val relation = attr("rel", container, current).let {
                    Properties.parse(Link::class, it, prefixes, VocabularyParseMode.RELATION)
                }
                val mediaType = getAttributeValue("media-type")?.let(MediaType::parse)
                val identifier = getAttributeValue("id")?.let { Identifier.of(it) }
                val properties = getAttributeValue("properties")?.let {
                    Properties.parse(Link::class, it, prefixes)
                } ?: Properties.empty()
                val refines = getAttributeValue("refines")
                return@with Link(href, relation, mediaType, identifier, properties, refines)
            }
    }
}