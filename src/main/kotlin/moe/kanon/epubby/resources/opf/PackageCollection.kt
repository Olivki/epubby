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

@file:Suppress("MemberVisibilityCanBePrivate")

package moe.kanon.epubby.resources.opf

import arrow.core.Option
import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubFormat
import moe.kanon.epubby.utils.requireMinimumFormat

/**
 * Implementation of the [collections](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-collection-elem)
 * specification.
 *
 * **NOTE:** This class is only used if the `format` of the specified [book] is [EPUB 3.0][EpubFormat.EPUB_3_0] or
 * greater.
 *
 * @property [container] The [OPF document][PackageDocument] that `this` manifest belongs to.
 * @property [book] The [Book] instance that `this` manifest is bound to.
 * @property [parent] The parent of `this` collection.
 *
 * As there is no guarantee that `this` collection is a *sub-collection*, this is wrapped in a [Option].
 */
// Oh boy, this one will fun
class PackageCollection internal constructor(
    val container: PackageDocument,
    val role: String,
    val book: Book = container.book,
    val parent: Option<PackageCollection> = Option.empty()
) {
    init {
        book.requireMinimumFormat(EpubFormat.EPUB_3_0) {
            "Collections are only supported starting from EPUB 3.0, current format is ${book.format}"
        }
    }
}