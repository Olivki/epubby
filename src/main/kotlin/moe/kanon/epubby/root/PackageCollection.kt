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

package moe.kanon.epubby.root

import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.EpubVersion
import moe.kanon.epubby.utils.requireMinFormat
import org.jdom2.Element

@EpubVersion(Book.Format.EPUB_3_0)
class PackageCollection(val book: Book) : ElementSerializer {
    init {
        requireMinFormat(book, Book.Format.EPUB_3_0) { "'collection' element is only supported from EPUB 3.0 and up" }
    }

    override fun toElement(): Element {
        TODO("not implemented")
    }
}