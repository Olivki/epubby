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

import dev.epubby.Epub2Feature
import dev.epubby.opf.OpfElement

public sealed interface DublinCore : OpfElement {
    /**
     * The identifier of the dublin-core element, or `null` if no identifier has been defined.
     */
    override var identifier: String?

    /**
     * The contents of the dublin-core element.
     */
    public var content: String?

    /**
     * A point or period of time associated with an event in the lifecycle of the resource.
     *
     * `Date` may be used to express temporal information at any level of granularity.
     */
    @OptIn(Epub2Feature::class)
    public data class Date(
        override var identifier: String? = null,
        @property:Epub2Feature
        public var event: DateEvent? = null,
        override var content: String?,
    ) : DublinCore, NonRequiredDublinCore

    /**
     * The file format, physical medium, or dimensions of the resource.
     *
     * Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary
     * such as the list of [Internet Media Types](http://www.iana.org/assignments/media-types/).
     */
    public data class Format(
        override var identifier: String? = null,
        override var content: String?,
    ) : DublinCore, NonRequiredDublinCore

    /**
     * An unambiguous reference to the resource within a given context.
     *
     * Recommended best practice is to identify the resource by means of a string conforming to a formal identification
     * system.
     */
    public data class Identifier(
        override var identifier: String? = null,
        @property:Epub2Feature
        public var scheme: String? = null,
        override var content: String?,
    ) : DublinCore

    /**
     * A language of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as
     * [RFC 4646](http://www.ietf.org/rfc/rfc4646.txt).
     */
    public data class Language(
        override var identifier: String? = null,
        override var content: String?,
    ) : DublinCore

    /**
     * A related resource from which the described resource is derived.
     *
     * The described resource may be derived from the related resource in whole or in part. Recommended best practice
     * is to identify the related resource by means of a string conforming to a formal identification system.
     */
    public data class Source(
        override var identifier: String? = null,
        override var content: String?,
    ) : DublinCore, NonRequiredDublinCore

    /**
     * The nature or genre of the resource.
     *
     * Recommended best practice is to use a controlled vocabulary such as the
     * [DCMI Type Vocabulary](http://dublincore.org/specifications/dublin-core/dcmi-type-vocabulary/#H7). To describe
     * the file format, physical medium, or dimensions of the resource, use the [Format] element.
     */
    public data class Type(
        override var identifier: String? = null,
        override var content: String?,
    ) : DublinCore, NonRequiredDublinCore
}