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

package moe.kanon.epubby.structs.props.vocabs

import moe.kanon.epubby.internal.findProperty
import moe.kanon.epubby.internal.findPropertyOrNull
import moe.kanon.epubby.packages.PackageCollection
import moe.kanon.epubby.packages.PackageDocument
import moe.kanon.epubby.packages.PackageMetadata
import moe.kanon.epubby.structs.prefixes.Prefix
import moe.kanon.epubby.structs.props.Property

/**
 * Represents the [metadata link rel vocabulary](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-link-rel).
 *
 * The values defined here can be used as the `rel` of a [metadata link][PackageMetadata.Link].
 *
 * @property [refinesRelation] Defines whether or not the constant can be used when the `refines` attribute is
 * present.
 */
enum class MetadataLinkRelVocabulary(
    override val reference: String,
    private val refinesRelation: Refines
) : Property {
    // TODO: Change the documentation for these to not just be pointing towards the specification and instead actually
    //       explain what they do
    // TODO: Documentation
    /**
     * Identifies where the full version of the book can be acquired.
     */
    ACQUIRE("acquire", Refines.MUST_NOT_BE_PRESENT),
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
    ALTERNATE("alternate", Refines.MUST_NOT_BE_PRESENT),
    /**
     * Represents the [marc21xml-record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-marc21xml-record)
     * entry.
     */
    MARC_21_XML_RECORD("marc21xml-record", Refines.MUST_NOT_BE_PRESENT),
    /**
     * Represents the [mods-record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-mods-record)
     * entry.
     */
    MODS_RECORD("mods-record", Refines.MUST_NOT_BE_PRESENT),
    /**
     * Represents the [onix-record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-onix-record)
     * entry.
     */
    ONIX_RECORD("onix-record", Refines.MUST_NOT_BE_PRESENT),
    /**
     * Represents the [record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-record)
     * entry.
     *
     * Indicates that the referenced resource is a metadata record.
     */
    RECORD("record", Refines.MUST_NOT_BE_PRESENT),
    /**
     * Represents the [voicing](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-voicing)
     * entry.
     */
    VOICING("voicing", Refines.MUST_BE_PRESENT),
    /**
     * Represents the [xml-signature](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-xml-signature)
     * entry.
     */
    XML_SIGNATURE("xml-signature", Refines.INDIFFERENT),
    /**
     * Represents the [xmp-record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-xmp-record)
     * entry.
     */
    XMP_RECORD("xmp-record", Refines.MUST_NOT_BE_PRESENT);

    // TODO: Implement the deprecation tags?

    private enum class Refines { MUST_BE_PRESENT, MUST_NOT_BE_PRESENT, INDIFFERENT }

    override val prefix: Prefix = Prefix.forVocabulary("http://idpf.org/epub/vocab/package/link/#")

    companion object {
        /**
         * The prefix used by this vocabulary.
         */
        @JvmField val PREFIX: Prefix = Prefix.forVocabulary("http://idpf.org/epub/vocab/package/link/#")

        @JvmStatic
        fun fromReference(reference: String): MetadataLinkRelVocabulary = findProperty(reference)

        @JvmStatic
        fun fromReferenceOrNull(reference: String): MetadataLinkRelVocabulary? = findPropertyOrNull(reference)
    }
}