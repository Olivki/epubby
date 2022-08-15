/*
 * Copyright 2019-2022 Oliver Berg
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

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.internal.EpubDateFormatters
import dev.epubby.internal.MarkedAsLegacy
import dev.epubby.internal.utils.ifNotNull
import dev.epubby.packages.metadata.Opf3Meta
import dev.epubby.utils.Direction
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

// TODO: rewrite/reword the documentations of each element so that we're not straight up plagiarizing
// TODO: automatically convert usage of 'role', 'event', 'scheme' and any other things marked as legacy in 3.0 to the
//       correct equivalenet? or at least provide a function for doing that?

sealed class DublinCore(val name: String) : EpubElement {
    abstract override val epub: Epub

    override val elementName: String
        get() = "DublinCore.$name"

    /**
     * The contents of this dublin-core element.
     *
     * Element implementations may contain functions for converting this value into a more type-safe structure.
     * *(i.e; [DublinCore.Date.toLocalDateTime], [DublinCore.Language.toLocale])*
     */
    abstract var value: String

    /**
     * The identifier of this dublin-core element, or `null` if no identifier has been defined.
     */
    abstract var identifier: String?

    /**
     * Returns a list of all the meta elements that are refining this dublin-core element.
     *
     * The returned list will become stale the moment the [refines][Opf3Meta.refines] property of any
     * of the collected elements is changed, or when a new `Opf3Meta` element gets added to the `metadata` package,
     * therefore it is not recommended to cache the returned list, instead one should retrieve a new one whenever needed.
     */
    val refinements: PersistentList<Opf3Meta>
        get() = epub.metadata.opf3MetaEntries.filter { it.refines == this }.toPersistentList()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is DublinCore -> false
        name != other.name -> false
        value != other.value -> false
        identifier != other.identifier -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + (identifier?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = buildString {
        append("DublinCore.$name(")
        append("content='$value'")
        identifier.ifNotNull { append(", identifier='$it'") }
        append(")")
    }

    /**
     * Accepts the given [visitor] and visits the appropriate function for this dublin-core implementation.
     */
    abstract fun <R> accept(visitor: DublinCoreVisitor<R>): R

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     *
     * `Date` may be used to express temporal information at any level of granularity.
     */
    class Date @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var event: DateEvent? = null,
    ) : DublinCore("Date") {
        companion object {
            @JvmStatic
            @JvmOverloads
            fun fromDate(
                epub: Epub,
                date: LocalDate,
                identifier: String? = null,
                @MarkedAsLegacy(`in` = EPUB_3_0)
                event: DateEvent? = null,
            ): Date = Date(epub, date.format(EpubDateFormatters.LOCAL_DATE), identifier, event)

            @JvmStatic
            @JvmOverloads
            fun fromDateTime(
                epub: Epub,
                date: LocalDateTime,
                identifier: String? = null,
                @MarkedAsLegacy(`in` = EPUB_3_0)
                event: DateEvent? = null,
            ): Date = Date(epub, date.format(EpubDateFormatters.LOCAL_DATE_TIME), identifier, event)
        }

        /**
         * Returns a [LocalDate] instance based on the [value] of this element.
         *
         * @throws [DateTimeParseException] if [value] can't be parsed into a [LocalDate]
         *
         * @see [toLocalDateTime]
         */
        @JvmOverloads
        fun toLocalDate(formatter: DateTimeFormatter = EpubDateFormatters.LOCAL_DATE): LocalDate =
            LocalDate.parse(value, formatter)

        /**
         * Returns a [LocalDateTime] instance based on the [value] of this element.
         *
         * @throws [DateTimeParseException] if [value] can't be parsed into a [LocalDateTime]
         *
         * @see [toLocalDate]
         */
        @JvmOverloads
        fun toLocalDateTime(formatter: DateTimeFormatter = EpubDateFormatters.LOCAL_DATE_TIME): LocalDateTime =
            LocalDateTime.parse(value, formatter)

        /**
         * Returns the result of invoking the [visitDate][DublinCoreVisitor.visitDate] function of the given [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitDate(this)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Date -> false
            !super.equals(other) -> false
            value != other.value -> false
            identifier != other.identifier -> false
            event != other.event -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + value.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + (event?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("DublinCore.Date(")
            append("content='$value'")
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
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
    ) : DublinCore("Format") {
        /**
         * Returns the result of invoking the [visitFormat][DublinCoreVisitor.visitFormat] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitFormat(this)
    }

    /**
     * An unambiguous reference to the resource within a given context.
     *
     * Recommended best practice is to identify the resource by means of a string conforming to a formal identification
     * system.
     */
    class Identifier @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var scheme: String? = null,
    ) : DublinCore("Identifier") {
        companion object {
            /**
             * Returns a new [Identifier] based on a [random uuid][UUID.randomUUID].
             */
            // TODO: find a better name
            // TODO: handle this differently if epub version is 3.x as 'scheme' is marked as legacy starting from 3.0
            //       so we probably want to use a op3 meta element to refine it or something like that?
            @JvmStatic
            @JvmOverloads
            fun generateRandom(epub: Epub, identifier: String? = null): Identifier {
                val content = UUID.randomUUID().toString()
                return Identifier(epub, content, identifier, "UUID")
            }
        }

        /**
         * Returns the result of invoking the [visitIdentifier][DublinCoreVisitor.visitIdentifier] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitIdentifier(this)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Identifier -> false
            !super.equals(other) -> false
            value != other.value -> false
            identifier != other.identifier -> false
            scheme != other.scheme -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + value.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + (scheme?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("DublinCore.Identifier(")
            append("content='$value'")
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
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
    ) : DublinCore("Language") {
        companion object {
            @JvmStatic
            @JvmOverloads
            fun fromLocale(
                epub: Epub,
                locale: Locale,
                identifier: String? = null,
            ): Language = Language(epub, locale.toLanguageTag(), identifier)
        }

        /**
         * Returns a [Locale] based on the [value] of this element.
         *
         * @see [Locale.forLanguageTag]
         */
        fun toLocale(): Locale = Locale.forLanguageTag(value)

        /**
         * Returns the result of invoking the [visitLanguage][DublinCoreVisitor.visitLanguage] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitLanguage(this)
    }

    /**
     * A related resource from which the described resource is derived.
     *
     * The described resource may be derived from the related resource in whole or in part. Recommended best practice
     * is to identify the related resource by means of a string conforming to a formal identification system.
     */
    class Source @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
    ) : DublinCore("Source") {
        /**
         * Returns the result of invoking the [visitSource][DublinCoreVisitor.visitSource] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitSource(this)
    }

    /**
     * The nature or genre of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as the
     * [DCMI Type Vocabulary](http://dublincore.org/specifications/dublin-core/dcmi-type-vocabulary/#H7). To describe
     * the file format, physical medium, or dimensions of the resource, use the [Format] element.
     */
    class Type @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
    ) : DublinCore("Type") {
        /**
         * Returns the result of invoking the [visitType][DublinCoreVisitor.visitType] function of the given [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitType(this)
    }
}

// TODO: rename to 'LocalizableDublinCore'?
sealed class LocalizedDublinCore(name: String) : DublinCore(name) {
    final override val elementName: String
        get() = "LocalizedDublinCore.$name"

    abstract var direction: Direction?

    // TODO: change this to just 'String'?
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
        append("content='$value'")
        identifier ifNotNull { append(", identifier='$it'") }
        direction ifNotNull { append(", direction=$it") }
        language ifNotNull { append(", language='$it'") }
        append(")")
    }

    /**
     * A contributor is an entity that is responsible for making contributions to the [Epub].
     *
     * Examples of a `Contributor` include a person, an organization, or a service. Typically, the name of a
     * `Contributor` should be used to indicate the entity.
     */
    class Contributor @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var role: CreativeRole? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var fileAs: String? = null,
    ) : LocalizedDublinCore("Contributor") {
        /**
         * Returns the result of invoking the [visitContributor][DublinCoreVisitor.visitContributor] function of the
         * given [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitContributor(this)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Contributor -> false
            !super.equals(other) -> false
            value != other.value -> false
            identifier != other.identifier -> false
            direction != other.direction -> false
            language != other.language -> false
            role != other.role -> false
            fileAs != other.fileAs -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + value.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + (direction?.hashCode() ?: 0)
            result = 31 * result + (language?.hashCode() ?: 0)
            result = 31 * result + (role?.hashCode() ?: 0)
            result = 31 * result + (fileAs?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("LocalizedDublinCore.Contributor(")
            append("content='$value'")
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
     * under which the [Epub] is relevant.
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
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
    ) : LocalizedDublinCore("Coverage") {
        /**
         * Returns the result of invoking the [visitCoverage][DublinCoreVisitor.visitCoverage] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitCoverage(this)
    }

    /**
     * The entity primarily responsible for making the [Epub].
     *
     * Do note that by "primarily responsible" it means the one who *originally* wrote the *contents* of the `Epub`,
     * not the person who made the epub.
     *
     * Examples of a `Creator` include a person, an organization, or a service. Typically, the name of a `Creator`
     * should be used to indicate the entity.
     */
    class Creator @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var role: CreativeRole? = null,
        @MarkedAsLegacy(`in` = EPUB_3_0)
        var fileAs: String? = null,
    ) : LocalizedDublinCore("Creator") {
        /**
         * Returns `true` if this `creator` element defines the author of the [epub], otherwise `false`.
         */
        val isAuthor: Boolean
            get() = when (role) {
                null -> false // TODO: check for enhancements belonging to this creator element in 'epub'
                else -> role == CreativeRole.AUTHOR
            }

        /**
         * Returns the result of invoking the [visitCreator][DublinCoreVisitor.visitCreator] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitCreator(this)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Creator -> false
            !super.equals(other) -> false
            value != other.value -> false
            identifier != other.identifier -> false
            direction != other.direction -> false
            language != other.language -> false
            role != other.role -> false
            fileAs != other.fileAs -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + value.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + (direction?.hashCode() ?: 0)
            result = 31 * result + (language?.hashCode() ?: 0)
            result = 31 * result + (role?.hashCode() ?: 0)
            result = 31 * result + (fileAs?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("LocalizedDublinCore.Creator(")
            append("content='$value'")
            identifier ifNotNull { append(", identifier='$it'") }
            direction ifNotNull { append(", direction=$it") }
            language ifNotNull { append(", language='$it'") }
            role ifNotNull { append(", role=$it") }
            fileAs ifNotNull { append(", fileAs='$it'") }
            append(")")
        }
    }

    /**
     * An account of the [Epub].
     *
     * `Description` may include but is not limited to: an abstract, a table of contents, a graphical representation,
     * or a free-text account of the resource.
     */
    class Description @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
    ) : LocalizedDublinCore("Description") {
        /**
         * Returns the result of invoking the [visitDescription][DublinCoreVisitor.visitDescription] function of the
         * given [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitDescription(this)
    }

    /**
     * An entity responsible for making the resource available.
     *
     * Examples of a `Publisher` include a person, an organization, or a service. Typically, the name of a `Publisher`
     * should be used to indicate the entity.
     */
    class Publisher @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
    ) : LocalizedDublinCore("Publisher") {
        /**
         * Returns the result of invoking the [visitPublisher][DublinCoreVisitor.visitPublisher] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitPublisher(this)
    }

    /**
     * A related resource.
     *
     * Recommended best practice is to identify the related resource by means of a string conforming to a formal
     * identification system.
     */
    class Relation @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
    ) : LocalizedDublinCore("Relation") {
        /**
         * Returns the result of invoking the [visitRelation][DublinCoreVisitor.visitRelation] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitRelation(this)
    }

    /**
     * Information about rights held in and over the resource.
     *
     * Typically, rights information includes a statement about various property rights associated with the resource,
     * including intellectual property rights.
     */
    class Rights @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
    ) : LocalizedDublinCore("Rights") {
        /**
         * Returns the result of invoking the [visitRights][DublinCoreVisitor.visitRights] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitRights(this)
    }

    /**
     * The topic of the resource.
     *
     * Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended
     * best practice is to use a controlled vocabulary.
     */
    class Subject @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
    ) : LocalizedDublinCore("Subject") {
        /**
         * Returns the result of invoking the [visitSubject][DublinCoreVisitor.visitSubject] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitSubject(this)
    }

    /**
     * A name given to the resource.
     */
    class Title @JvmOverloads constructor(
        override val epub: Epub,
        override var value: String,
        override var identifier: String? = null,
        override var direction: Direction? = null,
        override var language: Locale? = null,
    ) : LocalizedDublinCore("Title") {
        /**
         * Returns the result of invoking the [visitTitle][DublinCoreVisitor.visitTitle] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: DublinCoreVisitor<R>): R = visitor.visitTitle(this)
    }
}