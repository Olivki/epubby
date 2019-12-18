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
import kotlinx.collections.immutable.toPersistentHashSet
import moe.kanon.epubby.Book
import moe.kanon.epubby.internal.logger
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.reflection.loadServices

// TODO: This
class PageTransformers internal constructor(val book: Book) : Iterable<PageTransformer> {
    private val transformers: MutableSet<PageTransformer> = hashSetOf()

    val entries: ImmutableSet<PageTransformer> get() = transformers.toPersistentHashSet()

    /**
     * Invokes [transformPage] on all the [pages][Book.pages] of the [book].
     */
    fun transformPages() {
        logger.debug { "Starting the transformation process for all registered page transformers.." }
        for (page in book.pages) {
            transformPage(page)
        }
    }

    fun transformPage(page: Page) {
        for (transformer in transformers) {
            logger.trace { "Transforming page <$page> with transformer <$transformer>.." }
            transformer.transformPage(page, page.document, page.body)
        }
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

    override fun iterator(): Iterator<PageTransformer> = transformers.iterator().asUnmodifiable()
}