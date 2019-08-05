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

import moe.kanon.epubby.Book
import org.jsoup.nodes.Document

/**
 * Represents a transformer that works on [Page] instances, changing various parts of its structure.
 */
abstract class PageTransformer {
    /**
     * Gets invoked during the initial load of all page-transformers.
     */
    open fun onInit(book: Book): PageTransformer = this

    /**
     * Gets invoked at the start of the [book] saving process.
     */
    abstract fun transformPage(book: Book, page: Page, document: Document)
}