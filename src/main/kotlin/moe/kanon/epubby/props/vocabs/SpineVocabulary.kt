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

package moe.kanon.epubby.props.vocabs

import moe.kanon.epubby.prefixes.Prefix
import moe.kanon.epubby.prefixes.VocabularyPrefix
import moe.kanon.epubby.props.Property
import moe.kanon.kommons.collections.getOrThrow
import java.net.URI

// TODO: documentation
/**
 * Represents the [spine properties vocabulary](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#app-itemref-properties-vocab).
 *
 * TODO
 */
enum class SpineVocabulary(override val reference: String) : Property {
    /**
     * Indicates that the first page of the associated [item][PackageSpine.ItemReference.item] represents the left-hand
     * side of a two-page spread.
     */
    PAGE_SPREAD_LEFT("page-spread-left"),

    /**
     * Indicates that the first page of the associated [item][PackageSpine.ItemReference.item] represents the right-hand
     * side of a two-page spread.
     */
    PAGE_SPREAD_RIGHT("page-spread-right");

    override val prefix: Prefix = VocabularyPrefix(URI("http://idpf.org/epub/vocab/package/itemref/#"))

    companion object {
        @JvmField val PREFIX: Prefix = VocabularyPrefix(URI("http://idpf.org/epub/vocab/package/itemref/#"))

        private val referenceToInstance by lazy {
            values().associateByTo(hashMapOf(), SpineVocabulary::reference)
        }

        @JvmStatic
        fun fromReference(reference: String): SpineVocabulary =
            referenceToInstance.getOrThrow(reference) { "No vocabulary entry found with the given reference '$reference'." }

        @JvmStatic
        fun fromReferenceOrNull(reference: String): SpineVocabulary? = referenceToInstance[reference]
    }
}