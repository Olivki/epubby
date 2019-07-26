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

data class BookSettings private constructor(
    /**
     * Whether or not a `.xhtml` file should be generated containing the entries of the [TableOfContents] of the `book`.
     *
     * Note that this option is only used if the format of the `book` is [EPUB_2_0][Book.Format.EPUB_2_0]. This is
     * because starting from [v3.0.0][Book.Format.EPUB_3_0] a `.xhtml` ToC page is *required* to be a valid epub file.
     *
     * (`false` by default)
     */
    val generateTableOfContentsPage: Boolean = false
) {
    companion object {
        /**
         * Returns a [BookSettings] instance with all the values set to their defaults.
         */
        @JvmStatic val default: BookSettings get() = BookSettings()
    }

    /**
     * Whether or not a `.xhtml` file should be generated containing the entries of the [TableOfContents] of the `book`.
     *
     * Note that this option is only used if the format of the `book` is [EPUB_2_0][Book.Format.EPUB_2_0]. This is
     * because starting from [v3.0.0][Book.Format.EPUB_3_0] a `.xhtml` ToC page is *required* to be a valid epub file.
     *
     * (`false` by default)
     */
    fun generateTableOfContentsPage(shouldGenerate: Boolean) = copy(generateTableOfContentsPage = shouldGenerate)
}