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

package moe.kanon.epubby.resources

import moe.kanon.epubby.Book
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.getValueOrNone
import moe.kanon.kommons.io.paths.newInputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.nio.file.Path

class Page(val book: Book, val resource: PageResource) {
    /**
     * Returns the [file][Resource.file] of the underlying [resource] of this page.
     */
    val file: Path get() = resource.file

    /**
     * Lazily returns a [Document] by [parsing][Jsoup.parse] [file] as a document.
     */
    val document: Document by lazy { Jsoup.parse(file.newInputStream(), "UTF-8", file.toAbsolutePath().toString()) }
}

class PageRepository(val book: Book) : Iterable<Page> {
    private val pages: MutableMap<String, Page> = LinkedHashMap()

    /**
     * Returns how many pages are currently stored in this repository.
     */
    val size: Int get() = pages.size

    /**
     * Returns the [Page] stored under the given [key], or throws a [NoSuchElementException] if none is found.
     */
    fun getPage(key: String): Page = pages[key] ?: throw NoSuchElementException("No page found under key <$key>")

    /**
     * Returns the [Page] stored under the given [key], or [None] if none is found.
     */
    fun getPageOrNone(key: String): Option<Page> = pages.getValueOrNone(key)

    override fun iterator(): Iterator<Page> = pages.values.iterator().asUnmodifiable()

    override fun toString(): String =
        "PageRepository[${pages.entries.joinToString { (key, page) -> "\"$key\" -> $page" }}]"
}

/**
 * Returns the [Page] stored under the given [key], or throws a [NoSuchElementException] if none is found.
 */
operator fun PageRepository.get(key: String): Page = this.getPage(key)