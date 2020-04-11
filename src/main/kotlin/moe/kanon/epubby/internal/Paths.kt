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

package moe.kanon.epubby.internal

import com.google.common.net.MediaType
import moe.kanon.epubby.Book
import moe.kanon.kommons.io.paths.contentType
import java.nio.file.Path

@get:JvmSynthetic
internal val Path.mediaType: MediaType? get() = this.contentType?.let(MediaType::parse)

@JvmSynthetic
internal fun getBookPathFromHref(book: Book, href: String, documentFile: Path): Path =
    book.getPath(href).let { if (it.isAbsolute) it else documentFile.parent.resolve(it) }