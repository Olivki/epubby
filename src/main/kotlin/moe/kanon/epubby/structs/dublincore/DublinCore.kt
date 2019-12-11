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

package moe.kanon.epubby.structs.dublincore

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.utils.internal.Namespaces
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
 */
sealed class DublinCore<T>(val label: String) {
    abstract val identifier: EpubbyIdentifier?
    abstract var value: T

    // for EPUB 2.0 compliance, as the 'meta' element wasn't defined back then, so 'opf:property' attributes were used,
    // which means we need to catch them and then just throw them back onto the element during 'toElement' invocation
    @get:JvmSynthetic
    internal var _attributes: MutableList<Attribute> = ArrayList()

    /**
     * Returns a list containing any attributes that "overflowed".
     *
     * This is for backwards compatibility with [EPUB_2.0][Book.Format.EPUB_2_0] versions, as they stored role info
     * differently.
     */
    val attributes: ImmutableList<Attribute>
        get() = _attributes.toImmutableList()

    /**
     * Returns a `String` version of the [value] of `this` dublin-core metadata element.
     */
    protected abstract fun stringify(): String

    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.DUBLIN_CORE): Element =
        Element(label.toLowerCase(), namespace).apply {
            identifier?.also { setAttribute("id", it.value) }
            if (this is DublinCoreFull) {
                direction?.also { setAttribute("dir", it.toString()) }
                language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }
            }
            appendExtraAttributes(this)
            text = this@DublinCore.stringify()
        }

    /**
     * Adds the given [attribute] to the [attributes] of `this` element.
     */
    fun addAttribute(attribute: Attribute) {
        attribute.namespace = Namespaces.OPF_WITH_PREFIX
        _attributes.add(attribute)
    }

    /**
     * Attempts to remove the first [Attribute] that has a [name][Attribute.name] that matches with the given [name],
     * returning `true` if one is found and removed, `false` if it is not removed/not found.
     */
    fun removeAttribute(name: String): Boolean =
        _attributes.find { it.name == name }?.let { _attributes.remove(it) } ?: false

    private fun appendExtraAttributes(target: Element) {
        if (_attributes.isNotEmpty()) {
            for (attr in _attributes) {
                target.setAttribute(attr)
            }
        }
    }

    interface DublinCoreFull {
        var direction: Direction?
        var language: Locale?
    }

    /**
     * A contributor is an entity that is responsible for making contributions to the [Book].
     *
     * Examples of a `Contributor` include a person, an organization, or a service. Typically, the name of a
     * `Contributor` should be used to indicate the entity.
     */
    data class Contributor @JvmOverloads constructor(
        override var value: String,
        override val identifier: moe.kanon.epubby.structs.Identifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Contributor"), DublinCoreFull {
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
    data class Coverage @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Coverage"),
        DublinCoreFull {
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
    data class Creator @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Creator"),
        DublinCoreFull {
        override fun stringify(): String = value
    }

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     *
     * `Date` may be used to express temporal information at any level of granularity.
     */
    data class Date internal constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Date") {
        override fun stringify(): String = value

        companion object {
            @JvmStatic
            @JvmOverloads
            fun of(date: LocalDateTime, identifier: EpubbyIdentifier? = null): Date =
                Date(
                    date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    identifier
                )
        }
    }

    /**
     * An account of the [Book].
     *
     * `Description` may include but is not limited to: an abstract, a table of contents, a graphical representation,
     * or a free-text account of the resource.
     */
    data class Description @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Description"),
        DublinCoreFull {
        override fun stringify(): String = value
    }

    /**
     * The file format, physical medium, or dimensions of the resource.
     *
     * Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary
     * such as the list of [Internet Media Types](http://www.iana.org/assignments/media-types/).
     */
    data class Format @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Format") {
        override fun stringify(): String = value
    }

    /**
     * An unambiguous reference to the resource within a given context.
     *
     * Recommended best practice is to identify the resource by means of a string conforming to a formal identification
     * system.
     */
    data class Identifier @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Identifier") {
        override fun stringify(): String = value
    }

    /**
     * A language of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as [RFC 4646](http://www.ietf.org/rfc/rfc4646.txt).
     */
    data class Language @JvmOverloads constructor(
        override var value: Locale,
        override val identifier: EpubbyIdentifier? = null
    ) : DublinCore<Locale>("Language") {
        override fun stringify(): String = value.toLanguageTag()
    }

    /**
     * An entity responsible for making the resource available.
     *
     * Examples of a `Publisher` include a person, an organization, or a service. Typically, the name of a `Publisher`
     * should be used to indicate the entity.
     */
    data class Publisher @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Publisher"),
        DublinCoreFull {
        override fun stringify(): String = value
    }

    /**
     * A related resource.
     *
     * Recommended best practice is to identify the related resource by means of a string conforming to a formal
     * identification system.
     */
    data class Relation @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Relation"),
        DublinCoreFull {
        override fun stringify(): String = value
    }

    /**
     * Information about rights held in and over the resource.
     *
     * Typically, rights information includes a statement about various property rights associated with the resource,
     * including intellectual property rights.
     */
    data class Rights @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Rights"),
        DublinCoreFull {
        override fun stringify(): String = value
    }

    /**
     * A related resource from which the described resource is derived.
     *
     * The described resource may be derived from the related resource in whole or in part. Recommended best practice
     * is to identify the related resource by means of a string conforming to a formal identification system.
     */
    data class Source @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Source") {
        override fun stringify(): String = value
    }

    /**
     * The topic of the resource.
     *
     * Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended
     * best practice is to use a controlled vocabulary.
     */
    data class Subject @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Subject"),
        DublinCoreFull {
        override fun stringify(): String = value
    }

    /**
     * A name given to the resource.
     */
    data class Title @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : DublinCore<String>("Title"),
        DublinCoreFull {
        override fun stringify(): String = value
    }

    /**
     * The nature or genre of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as the [DCMI Type Vocabulary](http://dublincore.org/specifications/dublin-core/dcmi-type-vocabulary/#H7).
     * To describe the file format, physical medium, or dimensions of the resource, use the [Format] element.
     */
    data class Type @JvmOverloads constructor(
        override var value: String,
        override val identifier: EpubbyIdentifier? = null
    ) : DublinCore<String>("Type") {
        override fun stringify(): String = value
    }
}