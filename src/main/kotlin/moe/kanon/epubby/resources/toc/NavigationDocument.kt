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

package moe.kanon.epubby.resources.toc

import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.utils.parseHtmlFile
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.nio.file.Path

/**
 * Represents the [navigation document](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-package-nav)
 * introduced in [EPUB 3.0][BookVersion.EPUB_3_0].
 */
internal class NavigationDocument private constructor(
    val book: Book,
    val document: Document,
    val tocNav: Navigation,
    val pageListNav: Navigation?,
    val landmarksNav: Navigation?
) {
    // Note that there are no restrictions on the attributes allowed on these elements.

    data class Navigation(val title: String?, val orderedList: OrderedList, val type: String)

    data class OrderedList(val entryList: EntryList)

    data class EntryList(val content: Element, val child: EntryList?)

    internal companion object {
        @JvmSynthetic
        fun fromFile(book: Book, htmlFile: Path): NavigationDocument = parseHtmlFile(htmlFile) {
            TODO()
        }
    }
}