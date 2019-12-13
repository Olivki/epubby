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

import moe.kanon.epubby.packages.Metadata
import moe.kanon.epubby.structs.prefixes.Prefix
import moe.kanon.epubby.structs.props.Property
import moe.kanon.epubby.utils.internal.findProperty
import moe.kanon.epubby.utils.internal.findPropertyOrNull

/**
 * Represents the [metadata link properties vocabulary](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-link-properties).
 *
 * The values defined here can be used in the `properties` of a [metadata link][Metadata.Link] to establish the type of
 * record the reference resource represents.
 */
enum class MetadataLinkVocabulary(override val reference: String) : Property {
    /**
     * Represents the [onix](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-onix) entry.
     *
     * Indicates that the referenced `resource` is an [ONIX record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#bib-onix).
     */
    ONIX("onix"),
    /**
     * Represents the [xmp](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-xmp) entry.
     *
     * Indicates that the referenced `resource` is an [XMP record](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#bib-xmp).
     */
    XMP("xmp");

    override val prefix: Prefix = Prefix.forVocabulary("http://idpf.org/epub/vocab/package/link/#")

    companion object {
        @JvmStatic
        fun fromReference(reference: String): MetadataLinkVocabulary = findProperty(reference)

        @JvmStatic
        fun fromReferenceOrNull(reference: String): MetadataLinkVocabulary? = findPropertyOrNull(reference)
    }
}