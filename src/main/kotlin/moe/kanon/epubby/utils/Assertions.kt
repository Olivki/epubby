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

@file:Suppress("NOTHING_TO_INLINE")

package moe.kanon.epubby.utils

import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubbyException

@DslMarker internal annotation class Assertion

@Assertion inline fun requireMinFormat(book: Book, format: Book.Format, lazyMsg: () -> Any) {
    if (book.format < format) throw EpubbyException(book.file, lazyMsg().toString())
}

@Assertion inline fun requireMinFormat(book: Book, format: Book.Format) = requireMinFormat(book, format) {
    "This feature is only for format <$format> and up, current format is <${book.format}>"
}

@Assertion inline fun requireMaxFormat(book: Book, format: Book.Format, lazyMsg: () -> Any) {
    if (book.format > format) throw EpubbyException(book.file, lazyMsg().toString())
}

@Assertion inline fun requireMaxFormat(book: Book, format: Book.Format) = requireMaxFormat(book, format) {
    "This feature is no longer supported in versions beyond <$format>, current format is <${book.format}>"
}