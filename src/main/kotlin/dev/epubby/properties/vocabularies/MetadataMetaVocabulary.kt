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

package dev.epubby.properties.vocabularies

import dev.epubby.prefixes.Prefix
import dev.epubby.properties.Property
import moe.kanon.kommons.collections.getOrThrow
import java.net.URI

// TODO: documentation
/**
 * Represents the [metadata meta properties vocabulary](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-meta-property-values).
 *
 * The following constants define properties for use with the `property` property of the `Metadata.Meta` class.
 */
enum class MetadataMetaVocabulary(reference: String) : Property {
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

    override val reference: URI = URI.create(reference)

    override val prefix: Prefix = VocabularyPrefixes.METADATA_META_PREFIX

    companion object {
        private val referenceToInstance = values().associateByTo(hashMapOf()) { it.reference.toString() }

        @JvmStatic
        fun fromReference(reference: String): MetadataMetaVocabulary =
            referenceToInstance.getOrThrow(reference) { "No vocabulary entry found with the given reference '$reference'." }

        @JvmStatic
        fun fromReferenceOrNull(reference: String): MetadataMetaVocabulary? = referenceToInstance[reference]
    }
}