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

import com.google.common.net.MediaType
import dev.epubby.Book
import java.nio.file.Path

interface LocalResourceLocator {
    /**
     * Returns a [LocalResource] implementation if the given [mediaType] corresponds to `this` locator, otherwise
     * `null` if it doesn't.
     */
    fun findFactory(mediaType: MediaType): LocalResourceFactory?
}

// TODO: replace this with a 'fun interface' whenever Kotlins type inference engine can properly convert between
//       function literals and SAM interfaces
typealias LocalResourceFactory = (book: Book, identifier: String, file: Path, mediaType: MediaType) -> LocalResource