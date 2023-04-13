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
import dev.epubby.prefix.VocabularyPrefix

/**
 * Represents the [metadata meta properties vocabulary](https://www.w3.org/publishing/epub3/epub-packages.html#app-meta-property-vocab).
 */
// TODO: documentation of constants
@Epub3Feature
public enum class MetadataMetaVocabulary(override val reference: String) : VocabularyProperty {
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

    override val prefix: VocabularyPrefix
        get() = VocabularyPrefix.MetadataMeta
}