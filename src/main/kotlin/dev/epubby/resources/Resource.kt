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

package dev.epubby.resources

import dev.epubby.Book

sealed class Resource(val book: Book) {
    val relativeHref: String
        get() = TODO()
}

class PageResource(book: Book) : Resource(book)

// https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#cmt-woff2
class FontResource(book: Book) : Resource(book)