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

import dev.epubby.page.Page
import dev.epubby.prefixes.Prefix
import dev.epubby.properties.Property
import kotlinx.collections.immutable.toPersistentHashMap
import moe.kanon.kommons.collections.getOrThrow
import java.net.URI

// TODO: documentation
/**
 * Represents the [spine properties vocabulary](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#app-itemref-properties-vocab).
 *
 * TODO
 */
enum class SpineVocabulary(reference: String) : Property {
    /**
     * Indicates that the first page of the associated [Page] represents the left-hand side of a two-page spread.
     */
    PAGE_SPREAD_LEFT("page-spread-left"),

    /**
     * Indicates that the first page of the associated [Page] represents the right-hand side of a two-page spread.
     */
    PAGE_SPREAD_RIGHT("page-spread-right");

    override val reference: URI = URI.create(reference)

    override val prefix: Prefix = VocabularyPrefixes.SPINE

    companion object {
        private val REFERENCES = values().associateBy { it.reference.toString() }.toPersistentHashMap()

        @JvmStatic
        fun fromReference(reference: String): SpineVocabulary =
            REFERENCES.getOrThrow(reference) { "No vocabulary entry found with the given reference '$reference'." }

        @JvmStatic
        fun fromReferenceOrNull(reference: String): SpineVocabulary? = REFERENCES[reference]
    }
}