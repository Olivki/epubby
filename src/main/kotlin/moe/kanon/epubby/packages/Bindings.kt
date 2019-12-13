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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.DeprecatedFeature
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.internal.Namespaces
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.kommons.requireThat
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
@DeprecatedFeature(since = "3.2")
class Bindings private constructor(val book: Book, private val _mediaTypes: MutableList<MediaType>) {
    val mediaTypes: ImmutableList<MediaType> get() = _mediaTypes.toImmutableList()

    // TODO: Maybe just completely disallow the user to create a bindings instance if the version is not 3.0?
    init {
        requireThat(book.version > BookVersion.EPUB_2_0) { "expected version of 'book' to be 3.0 or greater, was ${book.version}" }
        if (book.version > BookVersion.EPUB_3_0) {
            logger.warn { "'bindings' are deprecated as of EPUB 3.2, usage of it is highly discouraged." }
        }
    }

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("bindings", namespace).apply {
        _mediaTypes.forEach { addContent(it.toElement()) }
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

    override fun toString(): String = "Bindings(mediaTypes=$_mediaTypes)"

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, current: Path): Bindings = with(element) {
            val mediaTypes = getChildren("mediaType", namespace)
                .mapTo(mutableListOf()) { createMediaType(element, book.file, current) }
            return Bindings(book, mediaTypes).also {
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