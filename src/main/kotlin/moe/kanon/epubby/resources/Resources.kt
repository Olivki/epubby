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

package moe.kanon.epubby.resources

import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.utils.combineWith
import moe.kanon.kommons.io.paths.newInputStream
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO

sealed class Resource {
    /**
     * The [Book] instance that `this` resource belongs to.
     */
    abstract val book: Book

    /**
     * The underlying file that `this` resource is created for.
     */
    abstract var file: Path
        protected set

    /**
     * Returns a string representing the full path to `this` resource.
     *
     * Suppose our [epub file][Book.file] has the following path: `H:\Books\Pride_and_Prejudice.epub`, and we have a
     * `PageResource` with a [file] with the following path `/OEBPS/Text/Cover.xhtml`, then this would return
     * `"H:\Books\Pride_and_Prejudice.epub\OEBPS\Text\Cover.xhtml"`.
     */
    val fullPath: String get() = book.file.combineWith(file)

    /**
     * Gets invoked when `this` resource is first created by the system.
     */
    @Throws(ResourceCreationException::class)
    open fun onCreation() {}

    /**
     * Gets invoked when `this` resource has been marked for removal by the system.
     */
    @Throws(ResourceDeletionException::class)
    open fun onDeletion() {}

    @Throws(EpubbyException::class)
    fun renameTo(name: String) {
        TODO("not implemented")
    }

    /**
     * Throws a [ResourceCreationException] using the given [message] and [cause] and the data stored in this resource.
     */
    protected inline fun raiseCreationError(message: String? = null, cause: Throwable? = null): Nothing =
        throw ResourceCreationException(file, book.file, message, cause)

    /**
     * Throws a [ResourceDeletionException] using the given [message] and [cause] and the data stored in this resource.
     */
    protected inline fun raiseDeletionError(message: String? = null, cause: Throwable? = null): Nothing =
        throw ResourceDeletionException(file, book.file, message, cause)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Resource -> false
        book != other.book -> false
        file != other.file -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + file.hashCode()
        return result
    }
}

class PageResource(override val book: Book, override var file: Path) : Resource() {

}

class StyleSheetResource(override val book: Book, override var file: Path) : Resource() {

}

class ImageResource(override val book: Book, override var file: Path) : Resource() {
    /**
     * Lazily returns a [BufferedImage] read from the underlying [file] of `this` resource.
     *
     * This operation may be rather costly depending on the size of the image.
     */
    @get:Throws(ResourceCreationException::class)
    val image: BufferedImage by lazy {
        try {
            ImageIO.read(file.newInputStream())
        } catch (e: IOException) {
            raiseCreationError("Failed to read image <$fullPath>, ${e.message}", e)
        }
    }
}

class FontResource(override val book: Book, override var file: Path) : Resource() {
    // TODO: Change this? It only supports True type fonts (TTF) atm
    val font: Font by lazy { Font.createFont(Font.TRUETYPE_FONT, file.newInputStream()) }
}

data class OpfResource(override val book: Book, override var file: Path) : Resource() {

}

data class NcxResource(override val book: Book, override var file: Path) : Resource() {

}