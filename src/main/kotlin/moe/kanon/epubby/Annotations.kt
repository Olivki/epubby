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

package moe.kanon.epubby

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FILE
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.PROPERTY_SETTER
import kotlin.annotation.AnnotationTarget.TYPEALIAS

/**
 * Used for marking which version the feature was introduced into the EPUB specification in.
 *
 * If a feature has *not* been annotated with `this`, then it's generally safe to assume that the feature has been
 * available since [EPUB 2.0][BookVersion.EPUB_2_0].
 *
 * @property [since] The version that the feature was introduced into the EPUB specification in.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(TYPEALIAS, CLASS, FIELD, FILE, FUNCTION, PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class NewFeature(val since: BookVersion)

/**
 * Used for marking that the feature the annotation target represents is considered to be a
 * [legacy feature](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#legacy).
 *
 * - [Authors](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-author) *MAY* include the legacy
 * feature for compatibility purposes.
 * - [Reading Systems](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-epub-reading-system)
 * *MUST NOT* support the legacy feature in content that conforms to this version of EPUB.
 *
 * @property [since] The version that the feature started getting considered as legacy.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, FIELD, FILE, FUNCTION, PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class LegacyFeature(val since: BookVersion)

/**
 * Used for marking that the feature the annotation target represents is considered to be a
 * [deprecated feature](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#deprecated).
 *
 * - [Authors](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-author) are strongly
 * *RECOMMENDED* not to use the feature in their EPUB Publications.
 * - [Reading Systems](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-epub-reading-system)
 * *MAY* support the feature.
 *
 * @property [since] The version that the feature started getting considered as deprecated.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, FIELD, FILE, FUNCTION, PROPERTY, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class DeprecatedFeature(val since: BookVersion)