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
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.immutableSetOf
import moe.kanon.epubby.Book
import moe.kanon.epubby.logger
import moe.kanon.epubby.resources.styles.StyleSheet
import moe.kanon.epubby.root.ManifestItem
import moe.kanon.epubby.root.PackageDocument
import moe.kanon.epubby.root.PackageManifest
import moe.kanon.epubby.root.get
import moe.kanon.epubby.utils.combineWith
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.io.paths.contentType
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.extension
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.paths.renameTo
import moe.kanon.kommons.requireThat
import org.jsoup.nodes.Element
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.properties.Delegates

// TODO: Implement something for handling identifier fragments for href

/**
 * Represents a [Publication Resource](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-publication-resource).
 *
 * A resource is an object that contains  content or instructions that contribute to the logic and rendering of at
 * least one [Rendition](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-rendition) of an
 * [EPUB Publication][Book].
 *
 *  Examples of Publication Resources include a Rendition's Package Document, EPUB Content Document, CSS Style Sheets, audio, video, images, embedded fonts and scripts.
 *
 * @param [identifier] the initial `identifier` of the [manifestItem] that this resource represents
 * @param [desiredDirectory] the [name][Path.simpleName] of the [desiredDirectory]
 */
sealed class Resource(file: Path, identifier: String, desiredDirectory: String) {
    companion object {
        /**
         * Returns a new [Resource] instance that matches the given [mediaType], or [MiscResource] if no appropriate
         * `Resource` implementation could be found.
         *
         * @param [mediaType] a string conforming to the [RFC-2046](https://tools.ietf.org/html/rfc2046) standard,
         * this string is *not* checked for any sort of validity, and is consumed *as is*
         * @param [file] the [path][Path] pointing towards the file that the returned [Resource] should be wrapping
         * around, note that this **NEEDS** to point towards an [existing][Path.exists] file
         * @param [book] the [Book] instance that the returned [Resource] should be tied to
         * @param [id] the unique identifier that the returned [Resource] should be using, this is used for storing
         * the resource inside the [manifest][PackageManifest] of the [book]
         *
         * ([href.name][Path.name] by default)
         *
         * @throws [IllegalArgumentException]
         * - If the [file-system][Path.getFileSystem] of the given [file] is not the same as the
         * [file-system][Book.fileSystem] of the given [book].
         * - If the given [file] points towards a file that [does not exist][Path.notExists]
         */
        @JvmOverloads
        @JvmStatic fun fromMediaType(mediaType: String, file: Path, book: Book, id: String = file.name): Resource {
            requireThat(file.fileSystem == book.fileSystem) { "File-system of 'file' should be <${book.fileSystem}> and not <${file.fileSystem}>" }
            requireThat(file.exists) { "'file' does not point towards an existing file <$file>" }
            return when (mediaType) {
                in TableOfContentsResource.MEDIA_TYPES -> TableOfContentsResource(book, file, id)
                in PageResource.MEDIA_TYPES -> PageResource(book, file, id)
                in StyleSheetResource.MEDIA_TYPES -> StyleSheetResource(book, file, id)
                in ImageResource.MEDIA_TYPES -> ImageResource(book, file, id)
                in FontResource.MEDIA_TYPES -> FontResource(book, file, id)
                in AudioResource.MEDIA_TYPES -> AudioResource(book, file, id)
                in ScriptResource.MEDIA_TYPES -> ScriptResource(book, file, id)
                in VideoResource.MEDIA_TYPES -> VideoResource(book, file, id)
                else -> {
                    logger.debug { "File <$file> with media-type <$mediaType> did not match any 'Resource' implementations, marking as 'MiscResource'.." }
                    MiscResource(book, file, id)
                }
            }
        }

        /**
         * Invokes the [fromMediaType] function with `mediaType` as the [contentType][Path.contentType] of the given
         * [file].
         *
         * @return an appropriate [Resource] instance for the given [file], or [MiscResource] if no `media-type` could
         * be found for `file`
         *
         * @see [fromMediaType]
         */
        @JvmOverloads
        @JvmStatic fun fromFile(file: Path, book: Book, id: String = file.name): Resource =
            file.contentType?.let { fromMediaType(it, file, book, id) } ?: MiscResource(book, file, id).also {
                logger.debug { "File <$file> has an unknown media-type, marking as 'MiscResource'" }
            }

        /**
         * Returns a set of the [media-types](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-cmt-supported)
         * that the given [resource] is made to wrap around, or an empty set if `resource` is a [MiscResource].
         *
         * @see [Resource.mediaTypes]
         */
        @JvmStatic fun getMediaTypesOf(resource: Resource): ImmutableSet<String> = when (resource) {
            is TableOfContentsResource -> TableOfContentsResource.MEDIA_TYPES
            is PageResource -> PageResource.MEDIA_TYPES
            is StyleSheetResource -> StyleSheetResource.MEDIA_TYPES
            is ImageResource -> ImageResource.MEDIA_TYPES
            is FontResource -> FontResource.MEDIA_TYPES
            is AudioResource -> AudioResource.MEDIA_TYPES
            is ScriptResource -> ScriptResource.MEDIA_TYPES
            is VideoResource -> VideoResource.MEDIA_TYPES
            is MiscResource -> immutableSetOf()
        }
    }

    /**
     * Returns a set of the supported [media-types](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-cmt-supported)
     * of `this` resource.
     *
     * @see [getMediaTypesOf]
     */
    val mediaTypes: ImmutableSet<String> by lazy { getMediaTypesOf(this) }

    /**
     * The [Book] instance that `this` resource belongs to.
     */
    abstract val book: Book

    /**
     * The underlying file that `this` resource is created for.
     */
    var file: Path = file
        @Throws(IOException::class)
        set(value) {
            // we need to update the references BEFORE we actually change the file, otherwise it won't be properly
            // reflected
            requireThat(value.fileSystem == book.fileSystem) { "The file-system of 'value' is not the same as the books file-system" }
            updateReferencesTo(value)
            field = value
            updateManifest(href = value)
        }

    /**
     * Returns a relative [Path] to the [file] of this resource.
     */
    val relativeFile: Path get() = book.packageDocument.file.relativize(file)

    /**
     * Returns a string representing the full path to `this` resource.
     *
     * Suppose our [epub file][Book.file] has the following path: `H:\Books\Pride_and_Prejudice.epub`, and we have a
     * `PageResource` with a [file] with the following path `/OEBPS/Text/Cover.xhtml`, then this would return
     * `"H:\Books\Pride_and_Prejudice.epub\OEBPS\Text\Cover.xhtml"`.
     */
    val fullPath: String get() = book.file.combineWith(file)

    /**
     * Returns a string representation of [file] that can be used within the EPUB.
     * TODO: Explanation
     */
    val href: String get() = book.packageDocument.file.parent.relativize(file).toString()

    val relativeHref: String get() = relativeFile.toString()

    /**
     * The "desired" directory where this resource "ideally" wants to reside.
     *
     * This is used for sorting the resource files into more appropriate directories as some EPUB files simply leave
     * all resource files in the same directory as the [package document][PackageDocument].
     */
    // this is 'lazy' as to avoid any issues with calling the abstract value 'book' directly when initializing it
    open val desiredDirectory: Path by lazy { book.packageDocument.file.parent.resolve(desiredDirectory) }

    /**
     * The identifier of the [manifestItem] that this resource represents.
     *
     * Setting the value of this property will update the `id` of the `manifest item` across the entire system.
     */
    var identifier: String by Delegates.observable(identifier) { _, oldValue, newValue ->
        if (oldValue in book.manifest && oldValue != newValue) {
            logger.debug { "Updating 'identifier' of resource <$this> from <$oldValue> to <$newValue>" }
            val newItem = book.manifest.getLocalItem(oldValue).copy(identifier = newValue)
            book.manifest.items -= oldValue
            book.manifest.items[newValue] = newItem

            if (this is PageResource) {
                val ref = book.spine[oldValue]
                book.spine.references.apply {
                    this[this.indexOf(ref)] = ref.copy(reference = newItem)
                }
            }
        }
    }

    /**
     * The [manifest item][ManifestItem] that this resource represents.
     */
    val manifestItem: ManifestItem.Local get() = book.manifest.getLocalItem(identifier)

    /**
     * Returns a list containing all [Element]s that have an `href`/`src` reference to this resource, or an  empty list
     * if no such elements are found.
     *
     * Note that invoking this property may cause serious overhead, as this will traverse *all* the elements in *all*
     * [pages][Book.pages] of the [book].
     */
    val references: ImmutableList<ResourceReference> get() = book.pages.getReferencesOf(this)

    /**
     * Handles the updating of all page references *(`href`, `src`, etc..)* of this resource.
     */
    @JvmSynthetic internal fun updateReferencesTo(file: Path) {
        for (ref in references) ref.updateReferenceTo(this, file)
    }

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
        if (href != this.file) this.file = href
        book.manifest.items[identifier] = newItem
        if (this is PageResource) {
            val ref = book.spine[identifier]
            book.spine.references.apply {
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

    /**
     * Renames the [file] this resource is wrapping around to the given [name].
     *
     * Note that this function *only* changes the [simple name][Path.simpleName] of the file, meaning that the
     * [extension][Path.extension] of it is left as-is. This function is merely intended for *simple* renaming of file
     * names, for more advanced operations it is recommended to set the `file` property directly.
     *
     * @throws [IOException] if an i/o error occurs
     */
    @Throws(IOException::class)
    fun renameTo(name: String) {
        // TODO: Check if this works properly
        file = file.renameTo(name + file.extension)
    }

    /**
     * Returns whether or not the given [href] is equal to this resources [href][Resource.href]. This function also
     * takes care of cases where an `href` attribute might contain a fragment-identifier *(`#`)*.
     */
    @JvmOverloads
    fun isHrefEqual(href: String, ignoreCase: Boolean = false): Boolean = when {
        '#' in href -> href.split('#')[0].equals(this.href, ignoreCase)
            || href.split('#')[0].equals(this.relativeFile.toString(), ignoreCase)
        else -> href.equals(this.href, ignoreCase) || href.equals(this.relativeFile.toString(), ignoreCase)
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

// TODO: Make it work for the EPUB 2.0 'ncx' file and the EPUB 3.0 navigation document
class TableOfContentsResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "/") {
    companion object {
        // TODO: Make this also recognize the 'nav xhtml document'
        @JvmField val MEDIA_TYPES: ImmutableSet<String> =
            immutableSetOf("application/x-dtbncx+xml", "application/oebps-package+xml")
    }

    // TODO: Change this if it's using a XHTML ToC, or maybe just create both somehow?
    override val desiredDirectory: Path by lazy { book.packageDocument.file.parent }

    override fun toString(): String = "TableOfContentsResource(identifier='$identifier', href='$href', book=$book)"
}

class PageResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "Text/") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<String> = immutableSetOf("application/xhtml+xml")
    }

    override fun toString(): String = "PageResource(identifier='$identifier', href='$href', book=$book)"
}

class StyleSheetResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "Styles/") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<String> = immutableSetOf("text/css")
    }

    val styleSheet: StyleSheet get() = book.styles.styleSheets.first { it.resource == this }

    override fun onDeletion() {
        for (page in book.pages) page.removeStyleSheet(styleSheet)
    }

    override fun toString(): String = "StyleSheetResource(identifier='$identifier', href='$href', book=$book)"
}

class ImageResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "Images/") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<String> =
            immutableSetOf("image/gif", "image/jpeg", "image/png", "image/svg+xml")
    }

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

    override fun toString(): String = "ImageResource(identifier='$identifier', href='$href', book=$book)"
}

class FontResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "Fonts/") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<String> =
            immutableSetOf("application/vnd.ms-opentype", "application/font-woff")
    }

    override fun toString(): String = "FontResource(identifier='$identifier', href='$href', book=$book)"
}

class AudioResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "Audio/") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<String> = immutableSetOf("audio/mpeg")
    }

    override fun toString(): String = "AudioResource(identifier='$identifier', href='$href', book=$book)"
}

class ScriptResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "Scripts/") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<String> = immutableSetOf("text/javascript")
    }

    override fun toString(): String = "ScriptResource(identifier='$identifier', href='$href', book=$book)"
}

class VideoResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "Video/") {
    companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<String> = immutableSetOf("audio/mp4")
    }

    override fun toString(): String = "VideoResource(identifier='$identifier', href='$href', book=$book)"
}

class MiscResource(override val book: Book, file: Path, identifier: String) :
    Resource(file, identifier, "Misc/") {
    override fun toString(): String = "MiscResource(identifier='$identifier', href='$href', book=$book)"
}