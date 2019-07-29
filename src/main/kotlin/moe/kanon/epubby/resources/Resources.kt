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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.root.ManifestItem
import moe.kanon.epubby.root.PackageDocument
import moe.kanon.epubby.root.get
import moe.kanon.epubby.utils.combineWith
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.io.paths.newInputStream
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.properties.Delegates

// TODO: Implement something for handling identifier fragments for href

/**
 * Represents a [SOMETHING](...).
 *
 * @param [identifier] the initial `identifier` of the [manifestItem] that this resource represents
 * @param [desiredDirectory] the [name][Path.simpleName] of the [desiredDirectory]
 * @param [mediaTypes] the [media-types](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#attrdef-item-media-type)
 * that represent what type of files this resource can work on, 1 or more
 *
 * @property [mediaTypes] The [media-types](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#attrdef-item-media-type)
 * that represent what type of files this resource can work on.
 */
sealed class Resource(identifier: String, desiredDirectory: String, vararg val mediaTypes: String) {
    /**
     * The [Book] instance that `this` resource belongs to.
     */
    abstract val book: Book

    /**
     * The underlying file that `this` resource is created for.
     */
    abstract var href: Path
        protected set

    /**
     * Returns a string representing the full path to `this` resource.
     *
     * Suppose our [epub file][Book.file] has the following path: `H:\Books\Pride_and_Prejudice.epub`, and we have a
     * `PageResource` with a [href] with the following path `/OEBPS/Text/Cover.xhtml`, then this would return
     * `"H:\Books\Pride_and_Prejudice.epub\OEBPS\Text\Cover.xhtml"`.
     */
    val fullPath: String get() = book.file.combineWith(href)

    /**
     * The "desired" directory where this resource "ideally" wants to reside.
     *
     * This is used for sorting the resource files into more appropriate directories as some EPUB files simply leave
     * all resource files in the same directory as the [package document][PackageDocument].
     */
    // this is 'lazy' as to avoid any issues with calling the abstract value 'book' directly when initializing it
    val desiredDirectory: Path by lazy { book.packageDocument.file.resolve(desiredDirectory) }

    /**
     * The identifier of the [manifestItem] that this resource represents.
     *
     * Setting the value of this property will update the `id` of the `manifest item` across the entire system.
     */
    var identifier: String by Delegates.observable(identifier) { _, oldValue, newValue ->
        val newItem = book.packageManifest.getLocalItem(oldValue).copy(identifier = newValue)
        book.packageManifest.items -= oldValue
        book.packageManifest.items[newValue] = newItem

        if (this is PageResource) {
            val ref = book.packageSpine[oldValue]
            book.packageSpine.references.apply {
                this[this.indexOf(ref)] = ref.copy(reference = newItem)
            }
        }
    }

    /**
     * The [manifest item][ManifestItem] that this resource represents.
     */
    val manifestItem: ManifestItem.Local get() = book.packageManifest.getLocalItem(identifier)

    /**
     * Handles the updating the of the [manifestItem] that this resource represents.
     *
     * This function makes sure that all the appropriate systems get updated/notified accordingly when the manifest
     * of this resource has been changed in some way.
     *
     * This function does *not* allow the [mediaType][ManifestItem.mediaType] of the `item` to be changed, as that
     * would require `this` resource to *dynamically* change its entire type, which is beyond the scope of this
     * framework.
     */
    @Suppress("CopyWithoutNamedArguments")
    @JvmSynthetic internal fun updateManifest(
        // this is simply here to avoid multiple invocations of 'getLocalItem' for every parameter, it should never be
        // anything but 'manifestItem'
        item: ManifestItem.Local = manifestItem,
        identifier: String = item.identifier,
        href: Path = item.href,
        fallback: Option<String> = item.fallback,
        mediaOverlay: Option<String> = item.mediaOverlay,
        properties: Option<String> = item.properties
    ) {
        // TODO: This currently does essentially the same operation twice, maybe bake everything into this function
        //      to improve performance?
        val newItem = item.copy(identifier, href, fallback, mediaOverlay = mediaOverlay, properties = properties)
        // if the given 'identifier' is different than the currently known 'identifier' of this resource, we want to
        // update all the references properly, which the 'identifier' property is set to do, so we just invoke that
        if (identifier != this.identifier) this.identifier = identifier
        if (href != this.href) this.href = href
        book.packageManifest.items[identifier] = newItem
        if (this is PageResource) {
            val ref = book.packageSpine[identifier]
            book.packageSpine.references.apply {
                this[this.indexOf(ref)] = ref.copy(reference = newItem)
            }
        }
    }

    /**
     * Gets invoked when `this` resource is first created by the system.
     */
    @Throws(ResourceCreationException::class)
    open fun onCreation() {
    }

    /**
     * Gets invoked when `this` resource has been marked for removal by the system.
     */
    @Throws(ResourceDeletionException::class)
    open fun onDeletion() {
    }

    @Throws(EpubbyException::class, IOException::class)
    fun renameTo(name: String) {
        TODO("not implemented")
    }

    /**
     * Throws a [ResourceCreationException] using the given [message] and [cause] and the data stored in this resource.
     */
    protected inline fun raiseCreationError(message: String? = null, cause: Throwable? = null): Nothing =
        throw ResourceCreationException(href, book.file, message, cause)

    /**
     * Throws a [ResourceDeletionException] using the given [message] and [cause] and the data stored in this resource.
     */
    protected inline fun raiseDeletionError(message: String? = null, cause: Throwable? = null): Nothing =
        throw ResourceDeletionException(href, book.file, message, cause)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Resource -> false
        !mediaTypes.contentEquals(other.mediaTypes) -> false
        book != other.book -> false
        href != other.href -> false
        identifier != other.identifier -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = mediaTypes.contentHashCode()
        result = 31 * result + book.hashCode()
        result = 31 * result + href.hashCode()
        result = 31 * result + identifier.hashCode()
        return result
    }
}

// TODO: Make it work for the EPUB 2.0 'ncx' file and the EPUB 3.0 navigation document
class TableOfContentsResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "/") {
    companion object {
        // TODO: Make this also recognize the 'nav xhtml document'
        @JvmField val MEDIA_TYPES: ImmutableList<String> = immutableListOf("application/x-dtbncx+xml")
    }

    override fun toString(): String = "TableOfContentsResource(identifier='$identifier', href='$href', book=$book)"
}

class PageResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "Text/", "application/xhtml+xml") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableList<String> = immutableListOf("application/xhtml+xml")
    }

    override fun toString(): String = "PageResource(identifier='$identifier', href='$href', book=$book)"
}

class StyleSheetResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "Styles/", "text/css") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableList<String> = immutableListOf("text/css")
    }

    override fun toString(): String = "StyleSheetResource(identifier='$identifier', href='$href', book=$book)"
}

class ImageResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "Images/", "image/gif", "image/jpeg", "image/png", "image/svg+xml") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableList<String> =
            immutableListOf("image/gif", "image/jpeg", "image/png", "image/svg+xml")
    }

    /**
     * Lazily returns a [BufferedImage] read from the underlying [href] of `this` resource.
     *
     * This operation may be rather costly depending on the size of the image.
     */
    @get:Throws(ResourceCreationException::class)
    val image: BufferedImage by lazy {
        try {
            ImageIO.read(href.newInputStream())
        } catch (e: IOException) {
            raiseCreationError("Failed to read image <$fullPath>, ${e.message}", e)
        }
    }

    override fun toString(): String = "ImageResource(identifier='$identifier', href='$href', book=$book)"
}

class FontResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "Fonts/", "application/vnd.ms-opentype", "application/font-woff") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableList<String> =
            immutableListOf("application/vnd.ms-opentype", "application/font-woff")
    }

    override fun toString(): String = "FontResource(identifier='$identifier', href='$href', book=$book)"
}

class AudioResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "Audio/", "audio/mpeg") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableList<String> = immutableListOf("audio/mpeg")
    }

    override fun toString(): String = "AudioResource(identifier='$identifier', href='$href', book=$book)"
}

class ScriptResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "Scripts/", "text/javascript") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableList<String> = immutableListOf("text/javascript")
    }

    override fun toString(): String = "ScriptResource(identifier='$identifier', href='$href', book=$book)"
}

class VideoResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "Video/", "audio/mp4") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableList<String> = immutableListOf("audio/mp4")
    }

    override fun toString(): String = "VideoResource(identifier='$identifier', href='$href', book=$book)"
}

class MiscResource(override val book: Book, override var href: Path, identifier: String) :
    Resource(identifier, "Misc/") {
    override fun toString(): String = "MiscResource(identifier='$identifier', href='$href', book=$book)"
}