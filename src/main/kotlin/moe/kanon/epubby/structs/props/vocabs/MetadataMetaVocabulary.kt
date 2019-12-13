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

import moe.kanon.epubby.structs.prefixes.Prefix
import moe.kanon.epubby.structs.props.Property
import moe.kanon.epubby.utils.internal.findProperty
import moe.kanon.epubby.utils.internal.findPropertyOrNull

/**
 * Represents the [metadata meta properties vocabulary](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-meta-property-values).
 *
 * The following constants define properties for use with the `property` property of the `Metadata.Meta` class.
 */
enum class MetadataMetaVocabulary(override val reference: String) : Property {
    ALTERNATE_SCRIPT("alternate-script"),
    AUTHORITY("authority"),
    BELONGS_TO_COLLECTION("belongs-to-collection"),
    COLLECTION_TYPE("collection-type"),
    DISPLAY_SEQUENCE("display-seq"),
    FILE_AS("file-as"),
    GROUP_POSITION("group-position"),
    IDENTIFIER_TYPE("identifier-type"),
    META_AUTH("meta-auth"),
    ROLE("role"),
    SOURCE_OF("source-of"),
    TERM("term"),
    TITLE_TYPE("title-type");

    // TODO: Documentation

    override val prefix: Prefix = Prefix.forVocabulary("http://idpf.org/epub/vocab/package/meta/#")

    companion object {
        @JvmStatic
        fun fromReference(reference: String): MetadataMetaVocabulary = findProperty(reference)

        @JvmStatic
        fun fromReferenceOrNull(reference: String): MetadataMetaVocabulary? = findPropertyOrNull(reference)
    }
}