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

package moe.kanon.epubby

import moe.kanon.epubby.resources.toc.TableOfContents

class BookSettings private constructor(
    /**
     * Whether or not a `.xhtml` file should be generated containing the entries of the [TableOfContents] of the book.
     *
     * Note that this option is only used if the format of the `book` is [EPUB 2.0][BookVersion.EPUB_2_0]. This is
     * because starting from [EPUB 3.0][BookVersion.EPUB_3_0] a `.xhtml` ToC page is *required* to be a valid epub file.
     *
     * By default this is set to `false`.
     */
    val generateTableOfContentsPage: Boolean
) {
    class Builder internal constructor(
        /**
         * Whether or not a `.xhtml` file should be generated containing the entries of the [TableOfContents] of the book.
         *
         * Note that this option is only used if the format of the `book` is [EPUB 2.0][BookVersion.EPUB_2_0]. This is
         * because starting from [EPUB 3.0][BookVersion.EPUB_3_0] a `.xhtml` ToC page is *required* to be a valid epub file.
         */
        @set:JvmSynthetic
        @get:JvmName("generateTableOfContentsPage")
        var generateTableOfContentsPage: Boolean = false
    ) {
        /**
         * Whether or not a `.xhtml` file should be generated containing the entries of the [TableOfContents] of the book.
         *
         * Note that this option is only used if the format of the `book` is [EPUB 2.0][BookVersion.EPUB_2_0]. This is
         * because starting from [EPUB 3.0][BookVersion.EPUB_3_0] a `.xhtml` ToC page is *required* to be a valid epub file.
         *
         * By default this is set to `false`.
         */
        fun generateTableOfContentsPage(shouldGenerateTableOfContentsPage: Boolean): Builder = apply {
            generateTableOfContentsPage = shouldGenerateTableOfContentsPage
        }

        /**
         * Returns a new [BookSettings] instance containing the values set in `this` builder.
         */
        fun build(): BookSettings = BookSettings(
            generateTableOfContentsPage = generateTableOfContentsPage
        )
    }

    companion object {
        /**
         * Returns the default [book settings][BookSettings].
         */
        @get:[JvmStatic JvmName("getDefault")]
        val DEFAULT: BookSettings = builder().build()

        @JvmStatic
        fun builder(): Builder = Builder()

        @JvmSynthetic
        inline operator fun invoke(scope: Builder.() -> Unit): BookSettings = builder().apply(scope).build()
    }
}