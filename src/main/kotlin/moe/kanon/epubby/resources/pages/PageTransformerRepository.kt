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

package moe.kanon.epubby.resources.pages

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableHashSet
import moe.kanon.epubby.Book
import moe.kanon.epubby.logger
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.reflection.loadServices
import java.util.*
import kotlin.collections.HashSet

class PageTransformerRepository(val book: Book) : Iterable<PageTransformer> {
    private val transformers: MutableSet<PageTransformer> = HashSet()

    /**
     * Returns a set of all the currently available [page-transformers][PageTransformer].
     */
    val entries: ImmutableSet<PageTransformer> get() = transformers.toImmutableHashSet()

    fun clearTransformers() {
        transformers.clear()
    }

    /**
     * Invokes [transformPage] on all the [pages][Book.pages] of the [book].
     */
    fun transformAllPages() {
        for (page in book.pages) transformPage(page)
    }

    /**
     * Invokes all the currently registered transformers on the given [page].
     */
    fun transformPage(page: Page) {
        if (transformers.isNotEmpty()) logger.debug { "Transforming page <$page>.." }
        for (transformer in transformers) transformer.transformPage(book, page, page.document, page.body)
    }

    /**
     * Registers all the page-transformers that are available using the [ServiceLoader] utility.
     */
    fun registerInstalled() {
        for (service in loadServices<PageTransformer>()) {
            registerTransformer(service)
        }
    }

    fun registerTransformer(transformer: PageTransformer) {
        transformer.onInit(book)
        transformers += transformer
    }

    fun unregisterTransformer(transformer: PageTransformer) {
        transformers -= transformer
    }

    /**
     * Removes all currently registered transformers from this repository.
     */
    fun clear() {
        transformers.clear()
    }

    @JvmName("hasTransformer")
    operator fun contains(transformer: PageTransformer): Boolean = transformer in transformers

    override fun iterator(): Iterator<PageTransformer> = transformers.iterator().asUnmodifiable()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PageTransformerRepository -> false
        book != other.book -> false
        transformers != other.transformers -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + transformers.hashCode()
        return result
    }

    override fun toString(): String = "PageTransformerRepository(transformers=$transformers, book=$book)"
}