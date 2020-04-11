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

package moe.kanon.epubby.structs

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.packages.Metadata
import org.jdom2.Attribute
import org.jdom2.Element
import org.jdom2.Namespace
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import moe.kanon.epubby.structs.Identifier as EpubbyIdentifier

// TODO: Move the "attributes" stuff to a wrapper class inside of metadata instead of having them here?
// TODO: Change these more into "value" classes and remove stuff like identifier and all of 'LocalizedDublinCore'
//       this would synergize with the above point too, as those things would be moved to the wrapper class instead?

/**
 * Represents an abstract implementation of a [dublin core element](http://www.dublincore.org/specifications/dublin-core/dces/).
 *
 * @property [book] The book instance that `this` dublin-core element is tied to.
 * @property [label] The string to use when converting this dublin-core back into its XML form.
 */
sealed class DublinCore<T : Any>(val label: String) {
    abstract var identifier: EpubbyIdentifier?

    /**
     * The value of `this` dublin-core element.
     */
    abstract var content: T

    // for EPUB 2.0 compliance, as the 'meta' element wasn't defined back then, so 'opf:property' attributes were used,
    // which means we need to catch them and then just throw them back onto the element during 'toElement' invocation
    @get:JvmSynthetic
    internal val _attributes: MutableList<Attribute> = ArrayList()

    /**
     * Returns a list containing all the `opf:property` attributes of this dublin-core element.
     *
     * The purpose of these attributes is to preserve backwards compatibility with the [EPUB 2.0][BookVersion.EPUB_2_0]
     * spec, as `opf:property` attributes were used to define additional information regarding the dublin-core element.
     * In [EPUB 3.0][BookVersion.EPUB_3_0] this functionality was replaced with the retrofitted
     * [meta][Metadata.Meta.OPF3] element.
     *
     * @see [addAttribute]
     * @see [removeAttributes]
     */
    val attributes: ImmutableList<Attribute>
        get() = _attributes.map { it.clone() }.toImmutableList()

    /**
     * Creates and adds a new `opf:property` attribute based on the given [name] and [value] to `this` dublin-core
     * element.
     *
     * Note that this is only supported for [EPUB 2.0][BookVersion.EPUB_2_0], as in [EPUB 3.0][BookVersion.EPUB_3_0]
     * the [meta][Metadata.Meta.OPF3] element was retrofitted to perform the role that these attributes did in
     * `EPUB 2.0` *(and more)*. This means that any attributes added will only show up if the `book` that serializes
     * this dublin-core element is of version `EPUB 2.0`.
     */
    fun addAttribute(name: String, value: String) = apply {
        val attribute = Attribute(name, value, Namespaces.OPF_WITH_PREFIX)
        _attributes.add(attribute)
    }

    /**
     * Removes all of the given [attributes] that are also contained in `this` dublin-core element.
     */
    fun removeAttributes(attributes: Iterable<Attribute>): Boolean = _attributes.removeAll(attributes)

    /**
     * Removes *all* the attributes of this dublin-core element.
     */
    fun removeAllAttributes() {
        _attributes.clear()
    }

    /**
     * Returns `true` if [attributes] contains an entry with the same `name` and `value` as the given [name] and
     * [value], otherwise `false`.
     *
     * @see [attributes]
     */
    fun hasAttribute(name: String, value: String): Boolean = _attributes.any { it.name == name && it.value == value }

    /**
     * Returns `true` if [attributes] contains an entry with the same `name` as the given [name], otherwise `false`.
     *
     * @see [attributes]
     */
    fun hasAttribute(name: String): Boolean = _attributes.any { it.name == name }

    /**
     * Returns a `String` version of the [content] of `this` dublin-core metadata element.
     */
    protected abstract fun stringify(): String

    // TODO: Do these work properly?
    override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES)

    override fun hashCode(): Int = kotlinHashCode(PROPERTIES)

    override fun toString(): String = buildString {
        append("DublinCore.$label(value='$content'")
        identifier?.also { append(", identifier='$identifier'") }
        if (this@DublinCore is Localizable) {
            direction?.also { append(", direction=$direction") }
            language?.also { append(", language='$language'") }
        }
        append(")")
    }

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun toElement(book: Book, namespace: Namespace = Namespaces.DUBLIN_CORE): Element =
        Element(label.toLowerCase(), namespace).apply {
            identifier?.also { setAttribute("id", it.value) }

            if (this is Localizable) {
                direction?.also { setAttribute("dir", it.toString()) }
                language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }
            }

            if (book.version < BookVersion.EPUB_3_0) _attributes.forEach { setAttribute(it) }

            text = this@DublinCore.stringify()
        }

    /**
     * Represents a [DublinCore] element whose content can be localized to a different language.
     */
    interface Localizable {
        /**
         * The direction in which the contents should be read in, may be `null`.
         */
        var direction: Direction?

        /**
         * The language that the contents is written in, may be `null`.
         */
        var language: Locale?
    }

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     *
     * `Date` may be used to express temporal information at any level of granularity.
     */
    class Date internal constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Date") {
        @JvmOverloads
        constructor(
            value: LocalDateTime,
            identifier: EpubbyIdentifier? = null
        ) : this(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), identifier)

        override fun stringify(): String = content
    }

    /**
     * The file format, physical medium, or dimensions of the resource.
     *
     * Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary
     * such as the list of [Internet Media Types](http://www.iana.org/assignments/media-types/).
     */
    class Format @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Format") {
        override fun stringify(): String = content
    }

    /**
     * An unambiguous reference to the resource within a given context.
     *
     * Recommended best practice is to identify the resource by means of a string conforming to a formal identification
     * system.
     */
    class Identifier @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Identifier") {
        override fun stringify(): String = content
    }

    /**
     * A language of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as [RFC 4646](http://www.ietf.org/rfc/rfc4646.txt).
     */
    class Language @JvmOverloads constructor(
        override var content: Locale,
        override var identifier: EpubbyIdentifier? = null
    ) : DublinCore<Locale>("Language") {
        override fun stringify(): String = content.toLanguageTag()
    }

    /**
     * A related resource from which the described resource is derived.
     *
     * The described resource may be derived from the related resource in whole or in part. Recommended best practice
     * is to identify the related resource by means of a string conforming to a formal identification system.
     */
    class Source @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Source") {
        override fun stringify(): String = content
    }

    /**
     * The nature or genre of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as the [DCMI Type Vocabulary](http://dublincore.org/specifications/dublin-core/dcmi-type-vocabulary/#H7).
     * To describe the file format, physical medium, or dimensions of the resource, use the [Format] element.
     */
    class Type @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Type") {
        override fun stringify(): String = content
    }

    /**
     * A contributor is an entity that is responsible for making contributions to the [Book].
     *
     * Examples of a `Contributor` include a person, an organization, or a service. Typically, the name of a
     * `Contributor` should be used to indicate the entity.
     */
    class Contributor @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Contributor"), Localizable {
        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
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
    class Coverage @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Coverage"), Localizable {
        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
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
    class Creator @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Creator"), Localizable {
        // TODO: Implement stuff like "setRole" in some manner

        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
    }

    /**
     * An account of the [Book].
     *
     * `Description` may include but is not limited to: an abstract, a table of contents, a graphical representation,
     * or a free-text account of the resource.
     */
    class Description @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Description"), Localizable {
        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
    }

    /**
     * An entity responsible for making the resource available.
     *
     * Examples of a `Publisher` include a person, an organization, or a service. Typically, the name of a `Publisher`
     * should be used to indicate the entity.
     */
    class Publisher @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Publisher"), Localizable {
        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
    }

    /**
     * A related resource.
     *
     * Recommended best practice is to identify the related resource by means of a string conforming to a formal
     * identification system.
     */
    class Relation @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Relation"), Localizable {
        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
    }

    /**
     * Information about rights held in and over the resource.
     *
     * Typically, rights information includes a statement about various property rights associated with the resource,
     * including intellectual property rights.
     */
    class Rights @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Rights"), Localizable {
        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
    }

    /**
     * The topic of the resource.
     *
     * Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended
     * best practice is to use a controlled vocabulary.
     */
    class Subject @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Subject"), Localizable {
        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
    }

    /**
     * A name given to the resource.
     */
    class Title @JvmOverloads constructor(
        override var content: String,
        override var identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Title"), Localizable {
        override fun stringify(): String = content

        override fun equals(other: Any?): Boolean = kotlinEquals(other, PROPERTIES) { super.equals(other) }

        override fun hashCode(): Int = kotlinHashCode(PROPERTIES) { super.hashCode() }
    }

    internal companion object {
        private val PROPERTIES = arrayOf(DublinCore<*>::label, DublinCore<*>::identifier, DublinCore<*>::content)

        private val LOCALIZABLE_PROPERTIES = arrayOf(Localizable::direction, Localizable::language)
    }
}