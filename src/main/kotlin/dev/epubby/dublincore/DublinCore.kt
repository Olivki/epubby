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

package dev.epubby.dublincore

import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.internal.EpubDateFormatters
import dev.epubby.internal.MarkedAsLegacy
import dev.epubby.internal.ifNotNull
import dev.epubby.utils.Direction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

// TODO: rewrite/reword the documentations of each element so that we're not straight up plagiarizing

sealed class DublinCore(protected val name: String) : BookElement {
    abstract override val book: Book

    override val elementName: String
        get() = "DublinCore.$name"

    /**
     * The contents of this dublin-core element.
     *
     * Element implementations may contain functions for converting this value into a more type-safe structure.
     * *(i.e; [DublinCore.Date.toLocalDateTime], [DublinCore.Language.toLocale])*
     */
    abstract var content: String

    /**
     * The identifier of this dublin-core element, or `null` if no identifier has been defined.
     */
    abstract var identifier: String?

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is DublinCore -> false
        name != other.name -> false
        content != other.content -> false
        identifier != other.identifier -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + (identifier?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = buildString {
        append("DublinCore.$name(")
        append("content='$content'")
        identifier.ifNotNull { append(", identifier='$it'") }
        append(")")
    }

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     *
     * `Date` may be used to express temporal information at any level of granularity.
     */
    class Date @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var event: DateEvent? = null
    ) : DublinCore("Date") {
        companion object {
            @JvmStatic
            @JvmOverloads
            fun fromDate(
                book: Book,
                date: LocalDate,
                identifier: String? = null,
                @MarkedAsLegacy(`in` = EPUB_3_0)
                event: DateEvent? = null
            ): Date = Date(book, date.format(EpubDateFormatters.LOCAL_DATE), identifier, event)

            @JvmStatic
            @JvmOverloads
            fun fromDateTime(
                book: Book,
                date: LocalDateTime,
                identifier: String? = null,
                @MarkedAsLegacy(`in` = EPUB_3_0)
                event: DateEvent? = null
            ): Date = Date(book, date.format(EpubDateFormatters.LOCAL_DATE_TIME), identifier, event)
        }

        /**
         * Returns a [LocalDate] instance based on the [content] of this element.
         *
         * @throws [DateTimeParseException] if [content] can't be parsed into a [LocalDate]
         *
         * @see [toLocalDateTime]
         */
        @JvmOverloads
        fun toLocalDate(formatter: DateTimeFormatter = EpubDateFormatters.LOCAL_DATE): LocalDate =
            LocalDate.parse(content, formatter)

        /**
         * Returns a [LocalDateTime] instance based on the [content] of this element.
         *
         * @throws [DateTimeParseException] if [content] can't be parsed into a [LocalDateTime]
         *
         * @see [toLocalDate]
         */
        @JvmOverloads
        fun toLocalDateTime(formatter: DateTimeFormatter = EpubDateFormatters.LOCAL_DATE_TIME): LocalDateTime =
            LocalDateTime.parse(content, formatter)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Date -> false
            !super.equals(other) -> false
            content != other.content -> false
            identifier != other.identifier -> false
            event != other.event -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + content.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + (event?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("DublinCore.Date(")
            append("content='$content'")
            identifier ifNotNull { append(", identifier='$it'") }
            event ifNotNull { append(", event=$it") }
            append(")")
        }
    }

    /**
     * The file format, physical medium, or dimensions of the resource.
     *
     * Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary
     * such as the list of [Internet Media Types](http://www.iana.org/assignments/media-types/).
     */
    class Format @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null
    ) : DublinCore("Format")

    /**
     * An unambiguous reference to the resource within a given context.
     *
     * Recommended best practice is to identify the resource by means of a string conforming to a formal identification
     * system.
     */
    class Identifier @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var scheme: String? = null
    ) : DublinCore("Identifier") {
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Identifier -> false
            !super.equals(other) -> false
            content != other.content -> false
            identifier != other.identifier -> false
            scheme != other.scheme -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + content.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + (scheme?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("DublinCore.Identifier(")
            append("content='$content'")
            identifier ifNotNull { append(", identifier='$it'") }
            scheme ifNotNull { append(", scheme='$it'") }
            append(")")
        }
    }

    /**
     * A language of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as
     * [RFC 4646](http://www.ietf.org/rfc/rfc4646.txt).
     */
    class Language @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null
    ) : DublinCore("Language") {
        companion object {
            @JvmStatic
            @JvmOverloads
            fun fromLocale(
                book: Book,
                locale: Locale,
                identifier: String? = null
            ): Language = Language(book, locale.toLanguageTag(), identifier)
        }

        /**
         * Returns a [Locale] based on the [content] of this element.
         *
         * @see [Locale.forLanguageTag]
         */
        fun toLocale(): Locale = Locale.forLanguageTag(content)
    }

    /**
     * A related resource from which the described resource is derived.
     *
     * The described resource may be derived from the related resource in whole or in part. Recommended best practice
     * is to identify the related resource by means of a string conforming to a formal identification system.
     */
    class Source @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null
    ) : DublinCore("Source")

    /**
     * The nature or genre of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as the
     * [DCMI Type Vocabulary](http://dublincore.org/specifications/dublin-core/dcmi-type-vocabulary/#H7). To describe
     * the file format, physical medium, or dimensions of the resource, use the [Format] element.
     */
    class Type @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null
    ) : DublinCore("Type")
}

sealed class LocalizedDublinCore(name: String) : DublinCore(name) {
    final override val elementName: String
        get() = "LocalizedDublinCore.$name"

    abstract var direction: Direction?
    abstract var language: Locale?

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is LocalizedDublinCore -> false
        !super.equals(other) -> false
        direction != other.direction -> false
        language != other.language -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (direction?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = buildString {
        append("LocalizedDublinCore.$name(")
        append("content='$content'")
        identifier ifNotNull { append(", identifier='$it'") }
        direction ifNotNull { append(", direction=$it") }
        language ifNotNull { append(", language='$it'") }
        append(")")
    }

    /**
     * A contributor is an entity that is responsible for making contributions to the [Book].
     *
     * Examples of a `Contributor` include a person, an organization, or a service. Typically, the name of a
     * `Contributor` should be used to indicate the entity.
     */
    class Contributor @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var role: CreativeRole? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var fileAs: String? = null
    ) : LocalizedDublinCore("Contributor") {
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Contributor -> false
            !super.equals(other) -> false
            content != other.content -> false
            identifier != other.identifier -> false
            direction != other.direction -> false
            language != other.language -> false
            role != other.role -> false
            fileAs != other.fileAs -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + content.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + (direction?.hashCode() ?: 0)
            result = 31 * result + (language?.hashCode() ?: 0)
            result = 31 * result + (role?.hashCode() ?: 0)
            result = 31 * result + (fileAs?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("LocalizedDublinCore.Contributor(")
            append("content='$content'")
            identifier ifNotNull { append(", identifier='$it'") }
            direction ifNotNull { append(", direction=$it") }
            language ifNotNull { append(", language='$it'") }
            role ifNotNull { append(", role=$it") }
            fileAs ifNotNull { append(", fileAs='$it'") }
            append(")")
        }
    }

    /**
     * The spatial or temporal topic of the resource, the spatial applicability of the resource, or the jurisdiction
     * under which the [Book] is relevant.
     *
     * Spatial topic and spatial applicability may be a named place or a location specified by its geographic
     * coordinates. Temporal topic may be a named period, date, or date range. A jurisdiction may be a named
     * administrative entity or a geographic place to which the resource applies. Recommended best practice is to use a
     * controlled vocabulary such as the
     * [Thesaurus of Geographic Names](http://www.getty.edu/research/tools/vocabulary/tgn/index.html). Where
     * appropriate, named places or time periods can be used in preference to numeric identifiers such as sets of
     * coordinates or date ranges.
     */
    class Coverage @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : LocalizedDublinCore("Coverage")

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
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var role: CreativeRole? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var fileAs: String? = null
    ) : LocalizedDublinCore("Creator") {
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Creator -> false
            !super.equals(other) -> false
            content != other.content -> false
            identifier != other.identifier -> false
            direction != other.direction -> false
            language != other.language -> false
            role != other.role -> false
            fileAs != other.fileAs -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + content.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + (direction?.hashCode() ?: 0)
            result = 31 * result + (language?.hashCode() ?: 0)
            result = 31 * result + (role?.hashCode() ?: 0)
            result = 31 * result + (fileAs?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("LocalizedDublinCore.Creator(")
            append("content='$content'")
            identifier ifNotNull { append(", identifier='$it'") }
            direction ifNotNull { append(", direction=$it") }
            language ifNotNull { append(", language='$it'") }
            role ifNotNull { append(", role=$it") }
            fileAs ifNotNull { append(", fileAs='$it'") }
            append(")")
        }
    }

    /**
     * An account of the [Book].
     *
     * `Description` may include but is not limited to: an abstract, a table of contents, a graphical representation,
     * or a free-text account of the resource.
     */
    class Description @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : LocalizedDublinCore("Description")

    /**
     * An entity responsible for making the resource available.
     *
     * Examples of a `Publisher` include a person, an organization, or a service. Typically, the name of a `Publisher`
     * should be used to indicate the entity.
     */
    class Publisher @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : LocalizedDublinCore("Publisher")

    /**
     * A related resource.
     *
     * Recommended best practice is to identify the related resource by means of a string conforming to a formal
     * identification system.
     */
    class Relation @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : LocalizedDublinCore("Relation")

    /**
     * Information about rights held in and over the resource.
     *
     * Typically, rights information includes a statement about various property rights associated with the resource,
     * including intellectual property rights.
     */
    class Rights @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : LocalizedDublinCore("Rights")

    /**
     * The topic of the resource.
     *
     * Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended
     * best practice is to use a controlled vocabulary.
     */
    class Subject @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : LocalizedDublinCore("Subject")

    /**
     * A name given to the resource.
     */
    class Title @JvmOverloads constructor(
        override val book: Book,
        override var content: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null
    ) : LocalizedDublinCore("Title")
}