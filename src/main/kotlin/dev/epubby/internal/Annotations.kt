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

package dev.epubby.internal

import dev.epubby.EpubVersion
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.*

/**
 * Used for marking which version the feature was introduced into the EPUB specification in.
 *
 * If a feature has *not* been annotated with `this`, then it's generally safe to assume that the feature has been
 * available since [EPUB 2.0][EpubVersion.EPUB_2_0].
 *
 * @property [version] The version that the feature was introduced into the EPUB specification in.
 */
// TODO: rename to 'IntroducedIn' and 'since' to 'version'?
@MustBeDocumented
@Retention(SOURCE)
@Target(
    TYPEALIAS,
    CLASS,
    FIELD,
    FILE,
    FUNCTION,
    PROPERTY,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    CLASS,
    PROPERTY,
    VALUE_PARAMETER,
    FUNCTION
)
internal annotation class IntroducedIn(val version: EpubVersion)

/**
 * Used for marking that the feature the annotation target represents is considered to be a
 * [legacy feature](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#legacy).
 *
 * - [Authors](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-author) *MAY* include the legacy
 * feature for compatibility purposes.
 * - [Reading Systems](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-epub-reading-system)
 * *MUST NOT* support the legacy feature in content that conforms to this version of EPUB.
 *
 * @property [in] The version that the feature started getting considered as legacy.
 */
@MustBeDocumented
@Retention(SOURCE)
@Target(CLASS, FIELD, FILE, FUNCTION, PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER, VALUE_PARAMETER)
internal annotation class MarkedAsLegacy(val `in`: EpubVersion)

/**
 * Used for marking that the feature the annotation target represents is considered to be a
 * [deprecated feature](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#deprecated).
 *
 * - [Authors](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-author) are strongly
 * *RECOMMENDED* not to use the feature in their EPUB Publications.
 * - [Reading Systems](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-epub-reading-system)
 * *MAY* support the feature.
 *
 * @property [in] The version that the feature started getting considered as deprecated.
 */
@MustBeDocumented
@Retention(SOURCE)
@Target(
    CLASS,
    FIELD,
    FILE,
    FUNCTION,
    PROPERTY,
    PROPERTY_GETTER,
    PROPERTY_SETTER,
    CLASS,
    PROPERTY,
    VALUE_PARAMETER,
    FUNCTION
)
internal annotation class MarkedAsDeprecated(val `in`: EpubVersion)

/**
 * Any "public" elements marked with this are, for all intents an purposes, to be considered *internal* API.
 *
 * Features annotated with this may be changed completely, or straight up removed without a proper deprecation cycle or
 * even notification.
 */
// TODO: remove?
@RequiresOptIn
internal annotation class EpubbyInternal