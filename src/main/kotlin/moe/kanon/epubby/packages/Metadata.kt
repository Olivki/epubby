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

package moe.kanon.epubby.packages

import com.google.common.net.MediaType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.structs.DublinCore
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.prefixes.PackagePrefix
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.structs.props.Property
import moe.kanon.epubby.structs.props.Relationship
import moe.kanon.epubby.structs.props.vocabs.VocabularyParseMode
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.toCompactString
import moe.kanon.kommons.checkThat
import org.jdom2.Attribute
import org.jdom2.Element
import org.jdom2.Namespace
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Represents the [metadata](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-metadata)
 * element belonging to the [package-document][PackageDocument] of the [book].
 */
class Metadata private constructor(
    val book: Book,
    private val _identifiers: MutableList<DublinCore.Identifier>,
    private val _titles: MutableList<DublinCore.Title>,
    private val _languages: MutableList<DublinCore.Language>,
    val dublinCoreElements: MutableList<DublinCore<*>>,
    val metaElements: MutableList<Meta>,
    val links: MutableList<Link>
) {
    val opf2MetaElements: ImmutableList<Meta.OPF2> get() = metaElements.filterIsInstance<Meta.OPF2>().toImmutableList()

    val opf3MetaElements: ImmutableList<Meta.OPF3> get() = metaElements.filterIsInstance<Meta.OPF3>().toImmutableList()

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
     * Creates a new [Identifier][DublinCore.Identifier] instance from the given [value] and [identifier] and adds it to the
     * known [identifiers].
     *
     * @param [value] a string containing an unambiguous identifier
     * @param [identifier] the [id](https://www.w3.org/TR/xml-id/) attribute for the element
     */
    @JvmOverloads
    fun addIdentifier(value: String, identifier: Identifier? = null) {
        _identifiers += DublinCore.Identifier(value, identifier)
    }

    /**
     * Attempts to remove the *first* [identifier][DublinCore.Identifier] that has [value][DublinCore.Identifier.value]
     * that matches the given [value], returning `true` if one was found, or `false` if none was found.
     *
     * @throws [IllegalStateException] If [identifiers] only contains *one* element.
     *
     * This is because there *NEEDS* to always be *AT LEAST* one known identifier at all times, which means that we
     * *CAN NOT* perform any removal operations if `identifiers` only contains one element.
     */
    fun removeIdentifier(value: String): Boolean {
        // there needs to always be AT LEAST one identifier element, so we can't allow any removal operations if there's
        // only one identifier element available
        checkThat(_identifiers.size > 1, "(identifiers.size <= 1)")
        return _identifiers.find { it.value == value }?.let { _identifiers.remove(it) } ?: false
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
     * Creates a new [Title][DublinCore.Title] instance from the given [value], [identifier], [dir] and [language] and
     * adds it to the known [titles].
     *
     * @param [value] a string containing a title for the [book]
     * @param [identifier] the [id](https://www.w3.org/TR/xml-id/) attribute for the element
     * @param [dir] specifies the base text direction of the [value]
     * @param [language] specifies the language used in the [value]
     */
    @JvmOverloads
    fun addTitle(value: String, identifier: Identifier? = null, dir: Direction? = null, language: Locale? = null) {
        _titles += DublinCore.Title(value, identifier, dir, language)
    }

    /**
     * Removes the given [title] from the known [titles] of the book, returning `true` if it was a success, otherwise
     * `false`.
     *
     * @throws [IllegalStateException] If [titles] only contains *one* element.
     *
     * This is because there *NEEDS* to always be *AT LEAST* one known title at all times, which means that we
     * *CAN NOT* perform any removal operations if `titles` only contains one element.
     */
    fun removeTitle(title: DublinCore.Title): Boolean {
        // there needs to always be AT LEAST one title element, so we can't allow any removal operations if there's
        // only one title element available
        checkThat(_titles.size > 1) { "(titles.size <= 1)" }
        return _titles.remove(title)
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
     * Adds the given [language] to the known [languages] of the book.
     */
    fun addLanguage(language: DublinCore.Language) {
        _languages += language
    }

    /**
     * Adds the given [language] to the known [languages] of the book at the given [index].
     */
    fun addLanguage(index: Int, language: DublinCore.Language) {
        _languages.add(index, language)
    }

    /**
     * Removes the given [language] from the known [languages] of the book, returning `true` if it was a success,
     * otherwise `false`.
     *
     * @throws [IllegalStateException] If [languages] only contains *one* element.
     *
     * This is because there *NEEDS* to always be *AT LEAST* one known language at all times, which means that we
     * *CAN NOT* perform any removal operations if `languages` only contains one element.
     */
    fun removeLanguage(language: DublinCore.Language): Boolean {
        // there needs to always be AT LEAST one language element, so we can't allow any removal operations if there's
        // only one language element available
        checkThat(_languages.size > 1) { "(titles.size <= 1)" }
        return _languages.remove(language)
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

    // -- DUBLIN-CORE -- \\
    fun addDublinCore(dublinCore: DublinCore<*>) {
        dublinCoreElements += dublinCore
    }

    // -- META -- \\
    fun addMeta(meta: Meta) {
        metaElements += meta
    }

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
     */
    @JvmOverloads
    fun setLastModifiedDate(date: LocalDateTime = LocalDateTime.now()) {
        val currentDateTime = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        logger.debug { "Updating last-modified date of book <$book> to '$currentDateTime'.." }

        if (book.version > BookVersion.EPUB_2_0) {
            val property = Property.of(PackagePrefix.DC_TERMS, "modified")
            val meta = metaElements
                .filterIsInstance<Meta.OPF3>()
                .firstOrNull { it.property == property }

            if (meta != null) {
                meta.value = currentDateTime
            } else {
                metaElements += Meta.OPF3(currentDateTime, property)
            }
        } else {
            val dublinCore = dublinCoreElements
                .filterIsInstance<DublinCore.Date>()
                .firstOrNull { it.hasAttribute("event", "modification") }

            if (dublinCore != null) {
                dublinCore.value = currentDateTime
            } else {
                addDublinCore(DublinCore.Date(currentDateTime).addAttribute("event", "modification"))
            }
        }
    }

    fun getLastModifiedDateOrNull(): String? = when {
        book.version > BookVersion.EPUB_2_0 -> TODO()
        else -> dublinCoreElements
            .filterIsInstance<DublinCore.Date>()
            .firstOrNull { it.hasAttribute("event", "modification") }?.value
    }

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("metadata", namespace).apply {
        addNamespaceDeclaration(Namespaces.DUBLIN_CORE)

        if (book.version < BookVersion.EPUB_3_0) {
            addNamespaceDeclaration(Namespaces.OPF_WITH_PREFIX)
        }

        _identifiers.forEach { addContent(it.toElement(book)) }
        _titles.forEach { addContent(it.toElement(book)) }
        _languages.forEach { addContent(it.toElement(book)) }
        dublinCoreElements.forEach { addContent(it.toElement(book)) }
        metaElements.forEach { addContent(it.toElement()) }
        links.forEach { addContent(it.toElement()) }
    }

    // this is so that rather than having to have two different lists that may or may not contain any elements depending
    // on the EPUB format used, we have one list containing 'MetaElement' which has implementations that match the current
    // EPUB format.
    /**
     * Represents the `meta` element, this may be the [meta](https://www.w3.org/TR/2011/WD-html5-author-20110809/the-meta-element.html)
     * element used in EPUB 2.0, or the [meta](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-meta)
     * element used in EPUB 3.0.
     */
    sealed class Meta {
        @JvmSynthetic
        internal abstract fun toElement(namespace: Namespace = Namespaces.OPF): Element

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
        // TODO: Change 'refines' from a String? to a DublinCore reference? as the refines part actually refers to the
        //       id of a dublin-core element
        data class OPF3 internal constructor(
            var value: String,
            var property: Property,
            var identifier: Identifier? = null,
            var direction: Direction? = null,
            var refines: String? = null,
            var scheme: String? = null,
            var language: Locale? = null
        ) : Meta() {
            @JvmSynthetic
            override fun toElement(namespace: Namespace): Element = Element("meta", namespace).apply {
                setAttribute("property", property.toStringForm())
                identifier?.also { setAttribute("id", it.value) }
                direction?.also { setAttribute("dir", it.toString()) }
                refines?.also { setAttribute("refines", it) }
                scheme?.also { setAttribute("scheme", it) }
                language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }
                text = this@OPF3.value
            }
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
        class OPF2 private constructor(
            val charset: Charset? = null,
            val content: String? = null,
            val httpEquivalent: String? = null,
            val name: String? = null,
            val scheme: String? = null,
            val globalAttributes: ImmutableList<Attribute> = persistentListOf()
        ) : Meta() {
            @JvmSynthetic
            override fun toElement(namespace: Namespace): Element = Element("meta", namespace).apply {
                charset?.also { setAttribute("charset", it.name()) }
                this@OPF2.content?.also { setAttribute("content", it) }
                httpEquivalent?.also { setAttribute("http-equiv", it) }
                this@OPF2.name?.also { setAttribute("name", it) }
                scheme?.also { setAttribute("scheme", it) }
                globalAttributes.forEach { setAttribute(it) }
            }

            override fun equals(other: Any?): Boolean = when {
                this === other -> true
                other !is OPF2 -> false
                charset != other.charset -> false
                content != other.content -> false
                httpEquivalent != other.httpEquivalent -> false
                name != other.name -> false
                scheme != other.scheme -> false
                globalAttributes != other.globalAttributes -> false
                else -> true
            }

            override fun hashCode(): Int {
                var result = charset?.hashCode() ?: 0
                result = 31 * result + (content?.hashCode() ?: 0)
                result = 31 * result + (httpEquivalent?.hashCode() ?: 0)
                result = 31 * result + (name?.hashCode() ?: 0)
                result = 31 * result + (scheme?.hashCode() ?: 0)
                result = 31 * result + globalAttributes.hashCode()
                return result
            }

            override fun toString(): String =
                "Legacy(charset=$charset, content=$content, httpEquivalent=$httpEquivalent, name=$name, scheme=$scheme)"

            companion object {
                /**
                 * Returns a new [OPF2] instance for the given [httpEquiv] with the given [content].
                 */
                @JvmStatic
                @JvmOverloads
                fun withHttpEquiv(
                    httpEquiv: String,
                    content: String,
                    scheme: String? = null
                ): OPF2 = OPF2(httpEquivalent = httpEquiv, content = content, scheme = scheme)

                /**
                 * Returns a new [OPF2] instance for the given [name] with the given [content].
                 */
                @JvmStatic
                @JvmOverloads
                fun withName(name: String, content: String, scheme: String? = null): OPF2 =
                    OPF2(name = name, content = content, scheme = scheme)

                /**
                 * Returns a new [OPF2] instance for the given [charset].
                 */
                @JvmStatic
                @JvmOverloads
                fun withCharset(charset: Charset, scheme: String? = null): OPF2 =
                    OPF2(charset = charset, scheme = scheme)

                @JvmSynthetic
                internal fun newInstance(
                    charset: Charset? = null,
                    content: String? = null,
                    httpEquiv: String? = null,
                    name: String? = null,
                    scheme: String? = null,
                    globalAttributes: ImmutableList<Attribute> = persistentListOf()
                ): OPF2 = OPF2(charset, content, httpEquiv, name, scheme, globalAttributes)
            }
        }
    }

    // TODO: Update the 'href' value of Link instances when updating a resource path

    /**
     * Represents the [link](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-opf-link)
     * element.
     *
     * Linked resources are not [Publication Resources](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-publication-resource)
     * and *MUST NOT* be listed in the [manifest][Manifest]. A linked resource *MAY* be embedded in a
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
    data class Link internal constructor(
        var href: URI,
        var relation: Relationship,
        var mediaType: MediaType? = null,
        var identifier: Identifier? = null,
        var properties: Properties = Properties.empty(),
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
    }

    // TODO: Make the class use this wrapper rather than the direct 'DublinCore' so we can remove useless crud from the
    //       'dublinCore' class
    internal sealed class DCWrapper(val book: Book) {
        abstract val value: DublinCore<*>
        abstract val identifier: Identifier?

        // for EPUB 2.0 compliance, as the 'meta' element wasn't defined back then, so 'opf:property' attributes were used,
        // which means we need to catch them and then just throw them back onto the element during 'toElement' invocation
        @get:JvmSynthetic
        internal var _attributes: MutableList<Attribute> = ArrayList()

        class Basic(book: Book, override val value: DublinCore<*>, override val identifier: Identifier?) :
            DCWrapper(book) {
            override fun toString(): String = "Basic(value=$value, identifier=$identifier)"

            override fun equals(other: Any?): Boolean = when {
                this === other -> true
                other !is Basic -> false
                value != other.value -> false
                identifier != other.identifier -> false
                else -> true
            }

            override fun hashCode(): Int {
                var result = value.hashCode()
                result = 31 * result + (identifier?.hashCode() ?: 0)
                return result
            }
        }

        class Localized(
            book: Book,
            override val value: DublinCore<*>,
            override val identifier: Identifier?,
            val direction: Direction?,
            val locale: Locale?
        ) : DCWrapper(book) {
            override fun toString(): String =
                "Localized(value=$value, identifier=$identifier, direction=$direction, locale=$locale)"

            override fun equals(other: Any?): Boolean = when {
                this === other -> true
                other !is Localized -> false
                value != other.value -> false
                identifier != other.identifier -> false
                direction != other.direction -> false
                locale != other.locale -> false
                else -> true
            }

            override fun hashCode(): Int {
                var result = value.hashCode()
                result = 31 * result + (identifier?.hashCode() ?: 0)
                result = 31 * result + (direction?.hashCode() ?: 0)
                result = 31 * result + (locale?.hashCode() ?: 0)
                return result
            }
        }
    }

    internal companion object {
        private val DC_NAMES = persistentHashSetOf("identifier", "title", "language")
        private val META_OPF2_ATTRIBUTES = persistentHashSetOf("charset", "content", "http-equiv", "name", "scheme")
        private val META_OPF3_ATTRIBUTES = persistentHashSetOf("property", "id", "dir", "refines", "scheme", "lang")

        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, file: Path): Metadata = with(element) {
            val identifiers = getChildren("identifier", Namespaces.DUBLIN_CORE)
                .mapTo(ArrayList()) { createIdentifier(it) }
                .ifEmpty { malformed(book.file, file, "missing required 'dc:identifier' element in 'metadata'") }
            val titles = getChildren("title", Namespaces.DUBLIN_CORE)
                .mapTo(ArrayList()) { createTitle(it) }
                .ifEmpty { malformed(book.file, file, "missing required 'dc:title' element in 'metadata'") }
            val languages = getChildren("language", Namespaces.DUBLIN_CORE)
                .mapTo(ArrayList()) { createLanguage(it) }
                .ifEmpty { malformed(book.file, file, "missing required 'dc:language' element in 'metadata'") }
            val dublinCoreElements: MutableList<DublinCore<*>> = children
                .asSequence()
                .filter { it.namespace == Namespaces.DUBLIN_CORE }
                .filter { it.name !in DC_NAMES && it.text.isNotBlank() && it.attributes.isNotEmpty() }
                .mapTo(ArrayList()) { createDublinCore(it, book.file, file) }
            val metaElements = getChildren("meta", namespace)
                .asSequence()
                .map { createMetaOrNull(book, it, book.file, file) }
                // remove any invalid meta-elements, this is because we don't want to raise an exception and stop the
                // program just because of a faulty meta-element
                .filterNotNullTo(ArrayList())
            val links = getChildren("link", namespace).mapTo(ArrayList()) { createLink(it, book.file, file) }
            return@with Metadata(book, identifiers, titles, languages, dublinCoreElements, metaElements, links).also {
                logger.trace { "Constructed metadata instance <$it> from file '$file'" }
            }
        }

        private fun Element.hasMeta2Attributes(): Boolean = this.attributes.any { it.name in META_OPF2_ATTRIBUTES }

        private fun Element.hasNoMeta3Attributes(): Boolean = this.attributes.none { it.name in META_OPF3_ATTRIBUTES }

        private fun createMetaOrNull(book: Book, element: Element, container: Path, current: Path): Meta? = when {
            book.version > BookVersion.EPUB_2_0 -> when {
                element.hasMeta2Attributes() && element.hasNoMeta3Attributes() -> {
                    logger.debug { "Encountered a legacy 'meta' element: ${element.toCompactString()}" }
                    createOPF2MetaElement(element)
                }
                else -> createOPF3MetaElement(element, container, current)
            }
            else -> createOPF2MetaElement(element)
        }


        // TODO: As I figured out that the "faulty" element we encountered was just a legacy 'meta' element, we can
        //       probably just have this fail loudly instead?
        private fun createOPF3MetaElement(element: Element, container: Path, current: Path): Meta.OPF3? {
            fun faultyElement(reason: String): Meta.OPF3? {
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
                    val value = element.text
                    val property =
                        element.attr("property", container, current).let { Property.parse(Meta::class, it) }
                    val identifier = element.getAttributeValue("id")?.let { Identifier.of(it) }
                    val direction = element.getAttributeValue("dir")?.let(Direction.Companion::of)
                    val refines = element.getAttributeValue("refines")
                    val scheme = element.getAttributeValue("scheme")
                    val language =
                        element.getAttributeValue("lang", Namespace.XML_NAMESPACE)?.let(Locale::forLanguageTag)
                    Meta.OPF3(value, property, identifier, direction, refines, scheme, language).also {
                        logger.trace { "Constructed metadata meta instance (3.x) <$it>" }
                    }
                }
            }
        }

        private fun createOPF2MetaElement(element: Element): Meta.OPF2 {
            val charset = element.getAttributeValue("charset")?.let(Charset::forName)
            val content = element.getAttributeValue("content")
            val httpEquiv = element.getAttributeValue("http-equiv")
            val name = element.getAttributeValue("name")
            val scheme = element.getAttributeValue("scheme")
            val globalAttributes =
                element.attributes.filterNot { it.name in META_OPF2_ATTRIBUTES }.toImmutableList()
            return Meta.OPF2.newInstance(charset, content, httpEquiv, name, scheme, globalAttributes).also {
                logger.trace { "Constructed metadata meta instance (2.x) <$it>" }
            }
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
                logger.trace { "Constructed metadata dublin-core identifier instance <$it>" }
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
                logger.trace { "Constructed metadata dublin-core title instance <$it>" }
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
                logger.trace { "Constructed metadata dublin-core language instance <$it>" }
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
            return dublinCore.also {
                logger.trace { "Constructed metadata dublin-core instance <$it>" }
            }
        }

        private fun createLink(element: Element, container: Path, current: Path): Link = with(element) {
            val href = URI(attr("href", container, current))
            val relation =
                attr("rel", container, current).let { Properties.parse(Link::class, it, VocabularyParseMode.RELATION) }
            val mediaType = getAttributeValue("media-type")?.let(MediaType::parse)
            val identifier = getAttributeValue("id")?.let { Identifier.of(it) }
            val properties = getAttributeValue("properties")?.let {
                Properties.parse(Link::class, it)
            } ?: Properties.empty()
            val refines = getAttributeValue("refines")
            return@with Link(href, relation, mediaType, identifier, properties, refines).also {
                logger.trace { "Constructed metadata link instance <$it>" }
            }
        }
    }
}