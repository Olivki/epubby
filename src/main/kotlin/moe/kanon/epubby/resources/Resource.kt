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

package moe.kanon.epubby.resources

import com.google.common.net.MediaType
import cz.vutbr.web.css.CSSException
import cz.vutbr.web.css.CSSFactory
import cz.vutbr.web.css.StyleSheet
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentSetOf
import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.packages.Manifest
import moe.kanon.epubby.packages.PackageDocument
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.mediaType
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.createDirectories
import moe.kanon.kommons.io.paths.extension
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.paths.renameTo
import moe.kanon.kommons.io.requireFileExistence
import moe.kanon.kommons.requireThat
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.properties.Delegates

// TODO: Figure out what to do with Resources and Manifest items. Because they are deeply connected so it might just be
//       best to only have like a Resource class rather than a Resource class and a Manifest.Item class too?
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
sealed class Resource(file: Path, identifier: Identifier, desiredDirectory: String) {
    abstract val book: Book

    /**
     * The underlying file that `this` resource is created for.
     *
     *  TODO: Explain what setting the value of this does
     */
    var file: Path = file
        @Throws(IOException::class)
        set(value) {
            // we need to update the references BEFORE we actually change the file, otherwise it won't be properly
            // reflected
            requireThat(value.fileSystem == book.fileSystem) { "can't change the file of a resource to one that is located outside of the book it is tied to" }
            // TODO
            //updateReferencesTo(value)
            field = value
            //updateManifest(href = value)
        }

    /**
     * Returns a set of the supported [media-types](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-cmt-supported)
     * of `this` resource.
     */
    abstract val mediaTypes: ImmutableSet<MediaType>

    /**
     * Returns a path that's relative to the [OPF file][PackageDocument.file] of the [book].
     */
    val relativeFile: Path get() = book.packageDocument.file.relativize(file)

    /**
     * Returns a string representation of a path that's relative to the parent of the [OPF file][PackageDocument.file] of the
     * [book].
     */
    // TODO: Find a better name?
    val href: String get() = book.packageDocument.file.parent.relativize(file).toString()

    val relativeHref: String get() = relativeFile.toString()

    /**
     * The "desired" directory where this resource "ideally" wants to reside.
     *
     * This is used for sorting the resource files into more appropriate directories as some EPUB files simply leave
     * all resource files in the same directory as the [package document][PackageDocument].
     */
    // this is 'lazy' as to avoid any issues with calling the abstract value 'book' directly when initializing it
    open val desiredDirectory: Path by lazy { book.packageRoot.resolve(desiredDirectory) }

    /**
     * The identifier of the [manifestItem] that this resource represents.
     *
     * Setting the value of this property will update the `id` of the `manifest item` across the entire system.
     */
    var identifier: Identifier by Delegates.observable(identifier) { _, oldValue, newValue ->
        if (book.manifest.hasItem(oldValue) && oldValue != newValue) {
            logger.info { "Changing 'identifier' of resource <$this> from '$oldValue' to '$newValue'" }
            val newItem = book.manifest.getLocalItem(oldValue).copy(identifier = newValue)
            book.manifest.items -= oldValue
            book.manifest.items[newValue] = newItem

            if (this is PageResource) {
                book.spine.getReferenceOfOrNull(this)?.apply {
                    item = newItem
                } ?: logger.warn { "Page-resource <$this> does not have a spine entry" }
            }
        }
    }

    /**
     * The [manifest item][Manifest.Item] that `this` resource represents.
     */
    val manifestItem: Manifest.Item.Local get() = book.manifest.getLocalItem(identifier)

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
    @JvmSynthetic
    internal fun updateReferencesTo(file: Path) {
        // TODO: This
        for (ref in references) ref.updateReferenceTo(this, file)
    }

    /**
     * Handles the updating the of the [manifestItem] that this resource represents.
     *
     * This function makes sure that all the appropriate systems get updated/notified accordingly when the manifest
     * of this resource has been changed in some way.
     *
     * This function does *not* allow the [mediaType][Manifest.Item.mediaType] of the `item` to be changed, as that
     * would require `this` resource to *dynamically* change its entire type, which is beyond the scope of this
     * framework.
     */
    @Suppress("CopyWithoutNamedArguments")
    @JvmSynthetic
    internal fun updateManifest(
        // this is simply here to avoid multiple invocations of 'getLocalItem' for every parameter, it should never be
        // anything but 'manifestItem'
        item: Manifest.Item.Local = manifestItem,
        identifier: Identifier = item.identifier,
        href: Path = item.href,
        fallback: String? = item.fallback,
        mediaOverlay: String? = item.mediaOverlay,
        properties: Properties = item.properties
    ) {
        // TODO: This currently does essentially the same operation twice, maybe bake everything into this function
        //      to improve performance?
        val newItem = item.copy(
            identifier = identifier,
            href = href,
            fallback = fallback,
            mediaOverlay = mediaOverlay,
            properties = properties
        )

        // if the given 'identifier' is different than the currently known 'identifier' of this resource, we want to
        // update all the references properly, which the 'identifier' property is set to do, so we just invoke that
        if (identifier != this.identifier) {
            this.identifier = identifier
        }

        if (href != this.file) {
            this.file = href
        }

        book.manifest.items[identifier] = newItem

        if (this is PageResource) {
            book.spine.getReferenceOfOrNull(this)?.apply {
                this.item = newItem
            } ?: logger.warn { "Page-resource <$this> has no spine entry" }
        }
    }

    /**
     * Invoked when `this` resource is first created by the system.
     */
    @Throws(ResourceException::class)
    open fun onCreation() {
    }

    /**
     * Invoked when `this` resource has been marked for removal by the system.
     */
    @Throws(ResourceException::class)
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

    protected fun raiseException(message: String, cause: Throwable? = null): Nothing =
        throw ResourceException(file, message, cause)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Resource -> false
        book != other.book -> false
        file != other.file -> false
        mediaTypes != other.mediaTypes -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + mediaTypes.hashCode()
        return result
    }

    companion object {
        @JvmSynthetic
        internal fun fromMediaType(
            mediaType: MediaType,
            file: Path,
            book: Book,
            identifier: Identifier = Identifier.fromFile(file)
        ): Resource {
            requireThat(file.fileSystem == book.fileSystem) { "file should have the same file-system as the given book [book=${book.fileSystem}, file=${file.fileSystem}]" }
            requireFileExistence(file)
            return when (mediaType) {
                in TableOfContentsResource.MEDIA_TYPES -> TableOfContentsResource(book, file, identifier)
                in PageResource.MEDIA_TYPES -> PageResource(book, file, identifier)
                in StyleSheetResource.MEDIA_TYPES -> StyleSheetResource(book, file, identifier)
                in ImageResource.MEDIA_TYPES -> ImageResource(book, file, identifier)
                in FontResource.MEDIA_TYPES -> FontResource(book, file, identifier)
                in AudioResource.MEDIA_TYPES -> AudioResource(book, file, identifier)
                in ScriptResource.MEDIA_TYPES -> ScriptResource(book, file, identifier)
                in VideoResource.MEDIA_TYPES -> VideoResource(book, file, identifier)
                else -> {
                    logger.info { "No resource implementations found for the given media-type <$mediaType>, marking as misc-resource.." }
                    MiscResource(book, file, identifier)
                }
            }.also {
                if (it !is MiscResource) {
                    logger.debug { "Created resource <$it> for media-type <$mediaType>" }
                }
            }
        }

        /**
         * Returns a new [Resource] implementation that is appropriate for the [contentType][Path.contentType] of the
         * given [file], or [MiscResource] if none could be found.
         *
         * @param [file] the [path][Path] pointing towards the file that the returned [Resource] should be wrapping
         * around, note that this **NEEDS** to point towards an [existing][Path.exists] file
         * @param [book] the [Book] instance that the returned [Resource] should be tied to
         * @param [identifier] the unique identifier that the returned [Resource] should be using, this is used for
         * storing the resource inside the [manifest][Manifest] of the [book]
         *
         * @throws [IllegalArgumentException] if the [file-system][Path.getFileSystem] of the given [file] is not the same as the
         * [file-system][Book.fileSystem] of the given [book]
         * @throws [IOException] if an i/o error occurred
         * @throws [EpubbyException] if something went wrong with the creation of the resource
         */
        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class, EpubbyException::class)
        fun fromFile(file: Path, book: Book, identifier: Identifier = Identifier.fromFile(file)): Resource {
            requireThat(file.fileSystem == book.fileSystem) { "file should have the same file-system as the given book [book=${book.fileSystem}, file=${file.fileSystem}]" }
            requireFileExistence(file)
            val resource = file.mediaType?.let { fromMediaType(it, file, book, identifier) }
            if (file.mediaType == null) {
                logger.info { "Resource file <$file> does not have a known media-type, marking as a misc-resource.." }
            }
            return resource ?: MiscResource(book, file, identifier)
        }

        /**
         * Returns a new [Resource] implementation that is appropriate for the [contentType][Path.contentType] of the
         * given [file], or [MiscResource] if none could be found.
         *
         * This function will copy the given [file] to the given [targetDirectory] before it is turned into a resource.
         *
         * @param [file] TODO
         * @param [targetDirectory] the directory in the [book] to copy the [file] to, if it does not exist then it
         * will be created
         * @param [book] TODO
         * @param [identifier] TODO
         *
         * @throws [IOException] if an i/o error occurred
         * @throws [EpubbyException] if something went wrong with the creation of the resource
         */
        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class, EpubbyException::class)
        fun fromExternalFile(
            file: Path,
            targetDirectory: Path,
            book: Book,
            identifier: Identifier = Identifier.fromFile(file)
        ): Resource {
            requireFileExistence(file)
            val directory = book.root.resolve(targetDirectory).createDirectories()
            logger.debug { "Copying external file '$file' to '$directory' in book <$book>" }
            val resourceFile = file.copyTo(directory)
            return fromFile(resourceFile, book, identifier)
        }
    }
}

class TableOfContentsResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    // TODO: Change this if it's using a XHTML ToC, or maybe just create both somehow?
    //override val desiredDirectory: Path by lazy { book.packageDocument.file.parent }

    //override fun toString(): String = "TableOfContentsResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        // TODO: Make this also recognize the 'nav xhtml document'
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(
            MediaType.create("application", "x-dtbncx+xml"),
            MediaType.create("application", "oebps-package+xml")
        )
    }
}

class PageResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Text/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "PageResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.XHTML_UTF_8)
    }
}

class StyleSheetResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Styles/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    /**
     * Lazily reads and returns a [StyleSheet] instance based on the [file] of `this` resource.
     */
    @get:Throws(ResourceException::class)
    val styleSheet: StyleSheet by lazy {
        try {
            CSSFactory.parse(file.toUri().toURL(), "UTF-8")
        } catch (e: IOException) {
            raiseException("Could not read file '${file.name}' into a style-sheet", e)
        } catch (e: CSSException) {
            raiseException("Could not read file '${file.name}' into a style-sheet", e)
        }
    }

    override fun onDeletion() {
        //for (page in book.pages) page.removeStyleSheet(styleSheet)
    }

    //override fun toString(): String = "StyleSheetResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.CSS_UTF_8)
    }
}

class ImageResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Images/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    /**
     * Lazily reads and returns a [BufferedImage] instance based on the [file] of `this` resource.
     */
    @get:Throws(ResourceException::class)
    val image: BufferedImage by lazy {
        try {
            file.newInputStream().use(ImageIO::read)
        } catch (e: IOException) {
            raiseException("Could not read file '${file.name}' into a buffered-image", e)
        }
    }

    //override fun toString(): String = "ImageResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> =
            persistentHashSetOf(MediaType.GIF, MediaType.JPEG, MediaType.PNG, MediaType.SVG_UTF_8)
    }
}

class FontResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Fonts/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "FontResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> =
            persistentHashSetOf(MediaType.create("application", "vnd.ms-opentype"), MediaType.WOFF)
    }
}

class AudioResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Audio/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "AudioResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.MPEG_AUDIO)
    }
}

class ScriptResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Scripts/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "ScriptResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.TEXT_JAVASCRIPT_UTF_8)
    }
}

class VideoResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Video/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "VideoResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.MP4_VIDEO)
    }
}

class MiscResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Misc/") {
    override val mediaTypes: ImmutableSet<MediaType> = persistentSetOf()
    //override fun toString(): String = "MiscResource(identifier='$identifier', href='$href', book=$book)"
}