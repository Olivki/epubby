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

@file:Suppress("DataClassPrivateConstructor")

package moe.kanon.epubby.settings

import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubFormat
import moe.kanon.epubby.resources.toc.TableOfContents

/**
 * An immutable builder class for setting on how a [Book] instance should behave.
 */
data class BookSettings private constructor(
    /**
     * Whether or not a `.xhtml` file should be generated containing the entries of the [TableOfContents] of the `book`.
     *
     * Note that this option is only used if the format of the `book` is [EPUB_2_0][EpubFormat.EPUB_2_0]. This is
     * because starting from [v3.0.0][EpubFormat.EPUB_3_0] a `.xhtml` ToC page is *required* to be a valid epub file.
     *
     * (`false` by default)
     */
    val generateTableOfContentsPage: Boolean = false
) {
    /**
     * Whether or not a `.xhtml` file should be generated containing the entries of the [TableOfContents] of the `book`.
     *
     * Note that this option is only used if the format of the `book` is [EPUB_2_0][EpubFormat.EPUB_2_0]. This is
     * because starting from [v3.0.0][EpubFormat.EPUB_3_0] a `.xhtml` ToC page is *required* to be a valid epub file.
     *
     * (`false` by default)
     */
    fun generateTableOfContentsPage(shouldGenerate: Boolean): BookSettings =
        copy(generateTableOfContentsPage = shouldGenerate)
    
    companion object {
        /**
         * Creates and returns a [BookSettings] instance with all values set to their defaults.
         */
        @JvmStatic
        fun newInstance(): BookSettings = BookSettings()
        
        /**
         * Creates and returns a [BookSettings] instance with all values set to their defaults.
         */
        @JvmSynthetic
        operator fun invoke(): BookSettings = newInstance()
    }
}