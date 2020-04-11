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

package moe.kanon.epubby.packages

import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.DeprecatedFeature
import moe.kanon.epubby.NewFeature
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.requireMinimumVersion
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.utils.attr
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path
import com.google.common.net.MediaType as MediaTypeModel

// this seems to have been deprecated/completely removed from the spec in EPUB 3.2 as I can see no mention of it in the
// specification..
/**
 * Represents the [bindings](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-opf-bindings)
 * element in the [package-document][PackageDocument] of the book.
 *
 * Note that as the `bindings` element was introduced in 3.0 and then deprecated in 3.2 this means that the only
 * version that this feature is *actually* supported for is 3.0, as the usage of 3.1 is officially discouraged.
 */
@NewFeature(since = BookVersion.EPUB_3_0)
@DeprecatedFeature(since = BookVersion.EPUB_3_2)
class PackageBindings private constructor(val book: Book, val mediaTypes: MutableList<MediaType>) {
    init {
        requireMinimumVersion(book.version, BookVersion.EPUB_3_0, "package-bindings")
        if (book.version.isNewerThan(BookVersion.EPUB_3_0)) {
            logger.warn { "'package-bindings' is deprecated as of EPUB 3.2, usage of it is highly discouraged." }
        }
    }

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("bindings", namespace).apply {
        mediaTypes.forEach { addContent(it.toElement()) }
    }

    /**
     * Represents the [mediaType](http://idpf.org/epub/301/spec/epub-publications.html#sec-mediaType-elem) element.
     */
    class MediaType internal constructor(val mediaType: MediaTypeModel, val handler: Identifier) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("mediaType", namespace).apply {
            setAttribute("media-type", mediaType.toString())
            setAttribute("handler", handler.value)
        }
    }

    override fun toString(): String = "Bindings(mediaTypes=$mediaTypes)"

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, current: Path): PackageBindings = with(element) {
            val mediaTypes = getChildren("mediaType", namespace)
                .mapTo(mutableListOf()) { createMediaType(element, book.file, current) }
            return PackageBindings(book, mediaTypes).also {
                logger.trace { "Constructed bindings instance <$it>" }
            }
        }

        private fun createMediaType(element: Element, epubFile: Path, current: Path): MediaType {
            val mediaType = element.attr("media-type", epubFile, current).let(MediaTypeModel::parse)
            val handler = element.attr("handler", epubFile, current).let(Identifier.Companion::of)
            return MediaType(mediaType, handler).also {
                logger.trace { "Constructed bindings media-type instance <$it>" }
            }
        }
    }
}