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

package dev.epubby.property

import dev.epubby.Epub3Feature
import dev.epubby.RefinesRelation
import dev.epubby.RefinesRelation.*
import dev.epubby.prefix.VocabularyPrefix

/**
 * Represents the [metadata link rel vocabulary](https://www.w3.org/publishing/epub3/epub-packages.html#sec-link-rel).
 *
 * The values defined here can be used as the `rel` of a [metadata link][PackageMetadata.Link].
 *
 * @property [refinesRelation] Defines whether the constant can be used when the `refines` attribute is
 * present.
 */
@Epub3Feature
public enum class MetadataLinkRelVocabulary(
    override val reference: String,
    public val refinesRelation: RefinesRelation,
) : VocabularyProperty {
    /**
     * Identifies where the full version of the epub can be acquired.
     */
    ACQUIRE("acquire", MUST_NOT_BE_PRESENT),

    /**
     * The `alternate` property is a subset of the HTML `alternate` keyword for `link` elements, it differs as follows:
     *
     * - It cannot be paired with other keywords.
     * - If an alternate `link` is included in the [package-document][PackageDocument] [metadata][PackageMetadata], it
     * identifies an alternate representation of the `package-document` in the format specified by the
     * [mediaType][PackageMetadata.Link.mediaType] of the `link`.
     * - If an alternate `link` is included in a [collection][PackageCollection]'s [metadata][PackageCollection.Metadata], it
     * identifies an alternate representation of the `collection` in the format specified in by the
     * [mediaType][PackageCollection.Metadata.Link.mediaType] of the `link`.
     */
    // TODO: update where the classes point to in the documentation
    ALTERNATE("alternate", MUST_NOT_BE_PRESENT),

    /**
     * Represents the [marc21xml-record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-marc21xml-record)
     * entry.
     */
    MARC_21_XML_RECORD("marc21xml-record", MUST_NOT_BE_PRESENT),

    /**
     * Represents the [mods-record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-mods-record)
     * entry.
     */
    MODS_RECORD("mods-record", MUST_NOT_BE_PRESENT),

    /**
     * Represents the [onix-record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-onix-record)
     * entry.
     */
    ONIX_RECORD("onix-record", MUST_NOT_BE_PRESENT),

    /**
     * Represents the [record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-record)
     * entry.
     *
     * Indicates that the referenced resource is a metadata record.
     */
    RECORD("record", MUST_NOT_BE_PRESENT),

    /**
     * Represents the [voicing](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-voicing)
     * entry.
     */
    VOICING("voicing", MUST_BE_PRESENT),

    /**
     * Represents the [xml-signature](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-xml-signature)
     * entry.
     */
    XML_SIGNATURE("xml-signature", INDIFFERENT),

    /**
     * Represents the [xmp-record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-xmp-record)
     * entry.
     */
    XMP_RECORD("xmp-record", MUST_NOT_BE_PRESENT);

    override val prefix: VocabularyPrefix
        get() = VocabularyPrefix.MetadataLink
}