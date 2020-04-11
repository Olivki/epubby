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

package moe.kanon.epubby.resources.transformers

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import moe.kanon.epubby.Book
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.resources.StyleSheetResource
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.kommons.collections.asUnmodifiable

class Transformers(val book: Book) : Iterable<Transformer> {
    // TODO: Change to list? Currently it's a set because we do not need to be able to keep multiple of the same
    //       instances of a transformer, the underlying set is also a LinkedHashSet so the order is kept
    val entries: MutableSet<Transformer> = mutableSetOf()

    val pageTransformers: ImmutableSet<PageTransformer>
        get() = entries.filterIsInstance<PageTransformer>().toImmutableSet()

    val styleSheetTransformers: ImmutableSet<StyleSheetTransformer>
        get() = entries.filterIsInstance<StyleSheetTransformer>().toImmutableSet()

    fun registerTransformer(transformer: Transformer) {
        entries += transformer
    }

    fun unregisterTransformer(transformer: Transformer): Boolean = entries.remove(transformer)

    fun transformPages() {
        logger.debug { "Starting the transformation process for all registered page transformers.." }
        book.pages.forEach(this::transformPage)
    }

    fun transformPage(page: Page) {
        for (transformer in pageTransformers) {
            logger.trace { "Transforming page <$page> with transformer <$transformer>.." }
            transformer.transformPage(page, page.document, page.body)
        }
    }

    fun transformStyleSheets() {
        logger.debug { "Starting the transformation process for all registered style-sheet transformers.." }
        book.resources.styleSheets.values.forEach(this::transformStyleSheet)
    }

    fun transformStyleSheet(resource: StyleSheetResource) {
        for (transformer in styleSheetTransformers) {
            logger.trace { "Transforming style-sheet <$resource> with transformer <$transformer>.." }
            transformer.transformStyleSheet(resource.styleSheet, resource)
        }
    }

    override fun iterator(): Iterator<Transformer> = entries.iterator().asUnmodifiable()
}