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
import org.jsoup.nodes.Element

/**
 * Represents a transformer that works on [Page] instances, changing various parts of its structure.
 */
interface PageTransformer {
    /**
     * Gets invoked during the initial load of all page-transformers.
     */
    @JvmDefault fun onInit(book: Book) {}

    /**
     * Gets invoked at the start of the [book] saving process, allows this transformer to modify the contents of the
     * given [page].
     */
    fun transformPage(book: Book, page: Page, document: Document, body: Element)
}