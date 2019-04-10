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

@file:JvmName("BookUtils")
@file:Suppress("NOTHING_TO_INLINE")

package moe.kanon.epubby.utils

import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubFormat

// TODO: Documentation

inline fun Book.requireMinimumFormat(format: EpubFormat, lazyMessage: () -> String) {
    if (this.format < format) throw UnsupportedOperationException(lazyMessage())
}

inline infix fun Book.requireMinimumFormat(format: EpubFormat) = requireMinimumFormat(format) {
    "This feature requires EPUB format $format and up, current format is ${this.format}"
}

inline fun Book.requireMaxmimumFormat(format: EpubFormat, lazyMessage: () -> String) {
    if (this.format < format) throw UnsupportedOperationException(lazyMessage())
}

inline infix fun Book.requireMaxmimumFormat(format: EpubFormat) = requireMinimumFormat(format) {
    "This feature is no longer supported in EPUB format versions beyond $format, current format is ${this.format}"
}