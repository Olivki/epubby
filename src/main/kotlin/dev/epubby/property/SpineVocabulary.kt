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
 * Represents the [spine properties vocabulary](https://www.w3.org/publishing/epub3/epub-packages.html#app-itemref-properties-vocab).
 */
// TODO: update the documentation links pointing to [Page]
@Epub3Feature
public enum class SpineVocabulary(override val reference: String) : VocabularyProperty {
    /**
     * Indicates that the first page of the associated [Page] represents the left-hand side of a two-page spread.
     */
    PAGE_SPREAD_LEFT("page-spread-left"),

    /**
     * Indicates that the first page of the associated [Page] represents the right-hand side of a two-page spread.
     */
    PAGE_SPREAD_RIGHT("page-spread-right");

    override val prefix: VocabularyPrefix
        get() = VocabularyPrefix.Spine
}