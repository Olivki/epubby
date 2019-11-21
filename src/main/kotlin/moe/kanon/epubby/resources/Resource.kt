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
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentSetOf
import moe.kanon.epubby.Book
import moe.kanon.epubby.packages.Manifest
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.mediaType
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.requireThat
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO

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

    val identifier: Identifier = TODO()

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
        private fun fromMediaType(
            mediaType: MediaType,
            file: Path,
            book: Book,
            identifier: Identifier = Identifier.fromFile(file)
        ): Resource {
            requireThat(file.fileSystem == book.fileSystem) { "file should have the same file-system as the given book [book=${book.fileSystem}, file=${file.fileSystem}]" }
            requireThat(file.exists) { "file should exist: $file" }
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
         * @throws [IllegalArgumentException]
         * - If the [file-system][Path.getFileSystem] of the given [file] is not the same as the
         * [file-system][Book.fileSystem] of the given [book].
         * - If the given [file] points towards a file that [does not exist][Path.notExists]
         */
        @JvmStatic
        @JvmOverloads
        fun fromFile(file: Path, book: Book, identifier: Identifier = Identifier.fromFile(file)): Resource {
            requireThat(file.fileSystem == book.fileSystem) { "file should have the same file-system as the given book [book=${book.fileSystem}, file=${file.fileSystem}]" }
            requireThat(file.exists) { "file should exist: $file" }
            val resource = file.mediaType?.let { fromMediaType(it, file, book, identifier) }
            if (resource == null) {
                logger.info { "Resource file <$file> does not have a known media-type, marking as a misc-resource.." }
            }
            return resource ?: MiscResource(book, file, identifier)
        }
    }
}

class TableOfContentsResource(override val book: Book, file: Path, identifier: Identifier) :
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

class PageResource(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Text/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "PageResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.XHTML_UTF_8)
    }
}

class StyleSheetResource(override val book: Book, file: Path, identifier: Identifier) :
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

class ImageResource(override val book: Book, file: Path, identifier: Identifier) :
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

class FontResource(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Fonts/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "FontResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> =
            persistentHashSetOf(MediaType.create("application", "vnd.ms-opentype"), MediaType.WOFF)
    }
}

class AudioResource(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Audio/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "AudioResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.MPEG_AUDIO)
    }
}

class ScriptResource(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Scripts/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "ScriptResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.TEXT_JAVASCRIPT_UTF_8)
    }
}

class VideoResource(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Video/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    //override fun toString(): String = "VideoResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.MP4_VIDEO)
    }
}

class MiscResource(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Misc/") {
    override val mediaTypes: ImmutableSet<MediaType> = persistentSetOf()
    //override fun toString(): String = "MiscResource(identifier='$identifier', href='$href', book=$book)"
}