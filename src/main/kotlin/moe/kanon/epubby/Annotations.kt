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

package moe.kanon.epubby

/**
 * Used for marking that a property has a different name in its serialized form than the one used in the code.
 *
 * This is only for marking it in the source code, it is *not* used for any serialization done via reflection. It is
 * merely to reduce confusion where short names like `rel` is used in the XML file, but the representing class uses
 * `relation`.
 *
 * It should however *not* be used in situations where an attribute is called `media-type` and the property is called
 * `mediaType`, as that is merely a difference in which case style is used, and annotating every occurrence of that
 * would result in very cluttered code.
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class SerializedName(val name: String)

/**
 * Used for marking that the feature the annotation target represents is considered to be a [legacy feature](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#legacy).
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
annotation class EpubLegacy(val since: String)

/**
 * Used for marking that the feature the annotation target represents is considered to be a [deprecated feature](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#deprecated).
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
annotation class EpubDeprecated(val since: String)

/**
 * Used for marking that the feature the annotation target represents has been fully removed starting from the given
 * [since] EPUB format.
 *
 * This annotation is rather rare as the EPUB format generally has very good backwards compatibility, so a lot of
 * features are usually just marked as [EpubLegacy] or [EpubDeprecated] at worst, but there are still certain features
 * that have been completely revamped in the way they are done, which means that they no longer are used *at all*.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class EpubRemoved(val `in`: String)

/**
 * Used for marking which version of the EPUB format the feature the annotation target represents is made for.
 *
 * Note that not *all* features should be marked with this, in general, it should be limited to situations where a
 * part of the specification has changed completely/largely but still kept the same name of the feature, in such
 * situations this annotation is helpful in reducing ambiguity.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class EpubVersion(val version: String)