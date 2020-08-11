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

package dev.epubby.internal.verifiers

import dev.epubby.Book
import dev.epubby.BookVersion
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.InvalidBookVersionException
import dev.epubby.MalformedBookException
import dev.epubby.metainf.MetaInfContainer

internal object MetaInfContainerVerifier {
    @Throws(MalformedBookException::class)
    internal fun verify(book: Book, container: MetaInfContainer) {
        for (link in container.links) {
            checkLinkFeaturesAlignsWithVersion(book.version, link)
        }
    }

    @Throws(MalformedBookException::class)
    internal fun checkLinkFeaturesAlignsWithVersion(
        version: BookVersion,
        link: MetaInfContainer.Link
    ) = checkVersion("MetaInfContainer.Link", version) {
        verify(EPUB_3_0, link::relation)
    }
}