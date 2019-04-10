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

package moe.kanon.epubby.resources.opf

import arrow.core.Option
import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubFormat

/**
 * Implementation of the [spine](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.4) specification.
 *
 * @property [container] The [OPF document][PackageDocument] that `this` spine belongs to.
 * @property [book] The [Book] instance that `this` spine is bound to.
 * @property [ncxItem] The [ManifestItem] that is marked as the `NCX` `item`.
 *
 * `NCX` is no longer in use starting from [EPUB 3.0][EpubFormat.EPUB_3_0], so there's no guarantee that this will
 * exist unless the current format is [EPUB 2.0][EpubFormat.EPUB_2_0]
 */
class PackageSpine internal constructor(
    val container: PackageDocument,
    val book: Book = container.book,
    val ncxItem: Option<ManifestItem> = Option.empty()
) {
    
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageSpine -> false
        container != other.container -> false
        book != other.book -> false
        ncxItem != other.ncxItem -> false
        else -> true
    }
    
    override fun hashCode(): Int {
        var result = container.hashCode()
        result = 31 * result + book.hashCode()
        result = 31 * result + ncxItem.hashCode()
        return result
    }
    
    override fun toString(): String = "PackageSpine(container=$container, book=$book, ncxItem=$ncxItem)"
    
    data class ItemRef(val ref: ManifestItem, val linear: Option<Boolean>)
}