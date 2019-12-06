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

import moe.kanon.epubby.packages.Spine
import moe.kanon.epubby.structs.props.BasicPropertyPrefix
import moe.kanon.epubby.structs.props.Property
import moe.kanon.epubby.structs.props.PropertyPrefix
import moe.kanon.epubby.utils.internal.findProperty

/**
 * Represents the [spine properties vocabulary](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#app-itemref-properties-vocab).
 *
 * TODO
 */
enum class SpineVocabulary(override val reference: String) : Property {
    /**
     * Represents the [page-spread-left](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-page-spread-left)
     * entry.
     *
     * Indicates that the first page of the associated [item][Spine.ItemReference.item] page represents the left-hand
     * side of a two-page spread.
     */
    PAGE_SPREAD_LEFT("page-spread-left"),
    /**
     * Represents the [page-spread-right](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-page-spread-right)
     * entry.
     *
     * Indicates that the first page of the associated [item][Spine.ItemReference.item] page represents the right-hand
     * side of a two-page spread.
     */
    PAGE_SPREAD_RIGHT("page-spread-right");

    // TODO: Update documentation with reference to our implementations

    override val prefix: PropertyPrefix = BasicPropertyPrefix(null, "http://idpf.org/epub/vocab/package/itemref/#")

    companion object {
        @JvmStatic
        fun fromReference(reference: String): SpineVocabulary = findProperty(reference)
    }
}