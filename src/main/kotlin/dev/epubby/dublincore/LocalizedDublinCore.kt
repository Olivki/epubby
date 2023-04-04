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

package dev.epubby.dublincore

import dev.epubby.Epub
import dev.epubby.Epub2Feature
import dev.epubby.ReadingDirection

public sealed class LocalizedDublinCore : DublinCore() {
    /**
     * The reading direction of the [content] of the dublin-core element.
     */
    public abstract var direction: ReadingDirection?

    /**
     * The language that the [content] of the dublin-core element is written in.
     */
    public abstract var language: String?

    /**
     * A contributor is an entity that is responsible for making contributions to the [Epub].
     *
     * Examples of a `Contributor` include a person, an organization, or a service. Typically, the name of a
     * `Contributor` should be used to indicate the entity.
     */
    @OptIn(Epub2Feature::class)
    public data class Contributor(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        @property:Epub2Feature
        public var role: CreativeRole? = null,
        @property:Epub2Feature
        public var fileAs: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()

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
    public data class Coverage(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()

    /**
     * The entity primarily responsible for making the [Epub].
     *
     * Do note that by "primarily responsible" it means the one who *originally* wrote the *contents* of the `Epub`,
     * not the person who made the epub.
     *
     * Examples of a `Creator` include a person, an organization, or a service. Typically, the name of a `Creator`
     * should be used to indicate the entity.
     */
    @OptIn(Epub2Feature::class)
    public data class Creator(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        @property:Epub2Feature
        public var role: CreativeRole? = null,
        @property:Epub2Feature
        public var fileAs: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()

    /**
     * An account of the [Epub].
     *
     * `Description` may include but is not limited to: an abstract, a table of contents, a graphical representation,
     * or a free-text account of the resource.
     */
    public data class Description(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()

    /**
     * An entity responsible for making the resource available.
     *
     * Examples of a `Publisher` include a person, an organization, or a service. Typically, the name of a `Publisher`
     * should be used to indicate the entity.
     */
    public data class Publisher(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()

    /**
     * A related resource.
     *
     * Recommended best practice is to identify the related resource by means of a string conforming to a formal
     * identification system.
     */
    public data class Relation(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()

    /**
     * Information about rights held in and over the resource.
     *
     * Typically, rights information includes a statement about various property rights associated with the resource,
     * including intellectual property rights.
     */
    public data class Rights(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()

    /**
     * The topic of the resource.
     *
     * Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended
     * best practice is to use a controlled vocabulary.
     */
    public data class Subject(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()

    /**
     * A name given to the resource.
     */
    public data class Title(
        override var identifier: String? = null,
        override var direction: ReadingDirection? = null,
        override var language: String? = null,
        override var content: String?,
    ) : LocalizedDublinCore()
}