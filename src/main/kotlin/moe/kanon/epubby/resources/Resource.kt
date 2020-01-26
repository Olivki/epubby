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

package moe.kanon.epubby.resources

import com.google.common.net.MediaType
import com.helger.css.ECSSVersion
import com.helger.css.decl.CascadingStyleSheet
import com.helger.css.reader.CSSReader
import com.helger.css.reader.CSSReaderSettings
import com.helger.css.reader.errorhandler.ThrowingCSSParseErrorHandler
import com.helger.css.writer.CSSWriter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentSetOf
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.mediaType
import moe.kanon.epubby.packages.Manifest
import moe.kanon.epubby.packages.OPF2Meta
import moe.kanon.epubby.packages.PackageDocument
import moe.kanon.epubby.packages.Spine
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.epubby.resources.pages.contains
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.structs.props.vocabs.ManifestVocabulary
import moe.kanon.kommons.collections.emptyEnumSet
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.createDirectories
import moe.kanon.kommons.io.paths.extension
import moe.kanon.kommons.io.paths.isSameAs
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.newBufferedReader
import moe.kanon.kommons.io.paths.newBufferedWriter
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.paths.notExists
import moe.kanon.kommons.io.paths.renameTo
import moe.kanon.kommons.io.paths.writeString
import moe.kanon.kommons.io.requireFileExistence
import moe.kanon.kommons.io.writeTo
import moe.kanon.kommons.requireThat
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.EnumSet
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
    /**
     * The book that `this` resource belongs to.
     */
    abstract val book: Book

    /**
     * Returns a set of the supported [media-types](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-cmt-supported)
     * of `this` resource.
     */
    abstract val mediaTypes: ImmutableSet<MediaType>

    /**
     * The underlying file that `this` resource is created for.
     *
     *  TODO: Explain what setting the value of this does
     */
    // TODO: Leaving this mutable might result in some weird stuff?
    var file: Path = file
        @Throws(IOException::class)
        set(value) {
            // we need to update the references BEFORE we actually change the file, otherwise it won't be properly
            // reflected
            requireThat(value.fileSystem == book.fileSystem) { "can't change the file of a resource to one that is located outside of the book it is tied to" }
            requireThat(!(file.parent isSameAs book.root)) { "files are not allowed to be moved to the root of the epub" }
            // TODO
            updateReferencesTo(value)
            field = value
            updateManifest(href = value)
        }

    /**
     * Returns the [media-type][MediaType] of the underlying [file] of this resource, or `null` if `file` represents an
     * unknown `media-type`.
     */
    val mediaType: MediaType? get() = file.mediaType

    /**
     * Returns a path that's relative to the [OPF file][PackageDocument.file] of the [book].
     */
    val relativeFile: Path get() = book.packageFile.relativize(file)

    /**
     * Returns a string representation of a path that's relative to the parent of the [OPF file][PackageDocument.file] of the
     * [book].
     */
    // TODO: Find a better name?
    val href: String get() = book.packageFile.parent.relativize(file).toString()

    val relativeHref: String get() = relativeFile.toString()

    /**
     * The properties specific to `this` resource.
     *
     * See [ManifestVocabulary] for the available ones.
     */
    // TODO: Change this to a general 'Properties'?
    val properties: EnumSet<ManifestVocabulary> = emptyEnumSet()

    /**
     * The `resource` that a reading system should use instead if it can't properly display `this` resource.
     */
    var fallback by Delegates.observable<Resource?>(null) { _, oldFallback, newFallback ->
        if (oldFallback != newFallback) {
            updateManifest(fallback = newFallback?.identifier)
        }
    }

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
    var identifier: Identifier by Delegates.observable(identifier) { _, oldIdentifier, newIdentifier ->
        if (book.manifest.hasItem(oldIdentifier) && oldIdentifier != newIdentifier) {
            logger.debug { "Changing 'identifier' of resource <$this> from '$oldIdentifier' to '$newIdentifier'" }

            book.resources.updateResourceIdentifier(this, oldIdentifier, newIdentifier)

            if (this is PageResource) {
                book.spine.updateReferenceItemFor(this, book.manifest.getLocalItem(newIdentifier))
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

    /**
     * Handles the updating of all page references *(`href`, `src`, etc..)* of this resource.
     */
    @JvmSynthetic
    internal fun updateReferencesTo(file: Path) {
        for (ref in references) ref.updateReferenceTo(this, file)
    }

    @JvmSynthetic
    internal fun updateItemProperties() {
        manifestItem.properties.apply {
            clear()
            addAll(properties)
        }
    }

    /**
     * Handles the updating the of the [manifestItem] that this resource represents.
     *
     * This function makes sure that all the appropriate systems get updated/notified accordingly when the manifest
     * of this resource has been changed in some way.
     */
    @JvmSynthetic
    internal fun updateManifest(
        // this is simply here to avoid multiple invocations of 'getLocalItem' for every parameter, it should never be
        // anything but 'manifestItem'
        item: Manifest.Item.Local = manifestItem,
        identifier: Identifier = item.identifier,
        href: Path = item.href,
        mediaType: MediaType? = item.mediaType,
        fallback: Identifier? = item.fallback,
        mediaOverlay: String? = item.mediaOverlay,
        properties: Properties = item.properties
    ) {
        // TODO: This currently does essentially the same operation twice, maybe bake everything into this function
        //      to improve performance?
        val newItem = item.copy(
            identifier = identifier,
            href = href,
            mediaType = mediaType,
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

        // TODO: This should probably work without ending up in an infinite loop, as 'fallback' is an observable and
        //       will only invoke this function if the 'old' & 'new' value are NOT the same
        if (fallback != this.fallback?.identifier) {
            this.fallback = when (fallback) {
                null -> null
                else -> book.resources.getResourceOrNull(fallback)
            }
        }

        book.manifest.items[identifier] = newItem

        if (this is PageResource) book.spine.updateReferenceItemFor(this, newItem)
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Resource -> false
        book != other.book -> false
        mediaTypes != other.mediaTypes -> false
        file != other.file -> false
        properties != other.properties -> false
        identifier != other.identifier -> false
        fallback != other.fallback -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + mediaTypes.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + (fallback?.hashCode() ?: 0)
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
                in NcxResource.MEDIA_TYPES -> NcxResource(book, file, identifier)
                in PageResource.MEDIA_TYPES -> PageResource(book, file, identifier)
                in StyleSheetResource.MEDIA_TYPES -> StyleSheetResource.fromFile(file, book, identifier)
                in ImageResource.MEDIA_TYPES -> ImageResource(book, file, identifier)
                in FontResource.MEDIA_TYPES -> FontResource(book, file, identifier)
                in AudioResource.MEDIA_TYPES -> AudioResource(book, file, identifier)
                in ScriptResource.MEDIA_TYPES -> ScriptResource(book, file, identifier)
                in VideoResource.MEDIA_TYPES -> VideoResource(book, file, identifier)
                else -> {
                    logger.debug { "No resource implementations found for the given media-type <$mediaType>, marking file <$file> as misc-resource.." }
                    MiscResource(book, file, identifier)
                }
            }.also {
                if (it !is MiscResource) {
                    logger.trace { "Created resource <$it> for media-type <$mediaType>" }
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
         * @throws [IOException] if an i/o error occurred
         * @throws [EpubbyException] if something went wrong with the creation of the resource
         * @throws [IllegalArgumentException]
         * - if [book] already contains a resource with the given [identifier]
         * - if the [file-system][Path.getFileSystem] of the given [file] is not the same as the
         * [file-system][Book.fileSystem] of the given [book]
         */
        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class, EpubbyException::class)
        fun fromFile(file: Path, book: Book, identifier: Identifier = Identifier.fromFile(file)): Resource {
            requireThat(identifier !in book.resources) { "expected 'identifier' to be unique, but there already exists a resource with the same identifier '$identifier'" }
            requireThat(file.fileSystem == book.fileSystem) { "file should have the same file-system as the given book [book=${book.fileSystem}, file=${file.fileSystem}]" }
            requireFileExistence(file)
            val resource = file.mediaType?.let { fromMediaType(it, file, book, identifier) }
            if (file.mediaType == null) {
                logger.debug { "Resource file <$file> does not have a known media-type, marking as a misc-resource.." }
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
         * @throws [IllegalArgumentException] if [book] already contains a resource with the given [identifier]
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
            // TODO: Make this fail if there already exists a file at the target destination?
            requireFileExistence(file)
            requireThat(identifier !in book.resources) { "expected 'identifier' to be unique, but there already exists a resource with the same identifier '$identifier'" }
            val directory = book.root.resolve(targetDirectory).createDirectories()
            logger.debug { "Copying external file '$file' to '$directory' in book <$book>" }
            val resourceFile = file.copyTo(directory)
            return fromFile(resourceFile, book, identifier)
        }
    }
}

class NcxResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    override val desiredDirectory: Path by lazy { book.packageDocument.file.parent }

    override fun toString(): String = "TableOfContentsResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField internal val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(
            MediaType.create("application", "x-dtbncx+xml"),
            MediaType.create("application", "oebps-package+xml")
        )
    }
}

class PageResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Text/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    // TODO: See if one can make this return true even if the book is 2.x but the page is a a generated ToC based on
    //       the NCX file?
    val isNavigationDocument: Boolean get() = TODO()

    /**
     * Returns the page tied to `this` page-resource, or throws a [NoSuchElementException] if `this` page-resource has
     * no page tied to it.
     */
    val page: Page get() = book.pages.getPageByResource(this)

    /**
     * Returns the [page], or creates a new [page][Page], adds it to the [spine][Spine] at the given [index] and
     * returns it.
     *
     * Note that `index` is *not* used if `this` page-resource is already tied to a `page`, as it simply returns `page`
     * then. Invoking this function will also mean that `this` resource will be added to the [manifest][Manifest] if it
     * is not already in there, as a `page` can not exist in the `spine` without a `manifest` entry to accompany it.
     */
    fun getOrCreatePage(index: Int): Page = when (this) {
        in book.pages -> page
        else -> book.pages.addPage(index, Page.fromResource(this))
    }

    /**
     * Returns the [page], or creates a new page, adds it to the end of the [spine][Spine] and returns it.
     *
     * Note that invoking this function will also mean that `this` resource will be added to the [manifest][Manifest]
     * if it is not already in there, as a `page` can not exist in the `spine` without a `manifest` entry to accompany
     * it.
     */
    fun getOrCreatePage(): Page = when (this) {
        in book.pages -> page
        else -> book.pages.addPage(Page.fromResource(this))
    }

    /**
     * Returns `true` if `this` page-resource has an entry in the spine, otherwise `false`.
     */
    fun hasSpineEntry(): Boolean = book.pages.hasPage(this)

    override fun onDeletion() {
        book.pages.removePage(page)
    }

    override fun toString(): String = "PageResource(identifier='$identifier', href='$href', book=$book)"

    companion object {
        @JvmField
        val MEDIA_TYPES: ImmutableSet<MediaType> =
            persistentHashSetOf(MediaType.create("application", "xhtml+xml"), MediaType.XHTML_UTF_8)

        // TODO: Documentation
        /**
         * @throws [IOException] if an i/o error occurred
         * @throws [IllegalArgumentException]
         * - if [book] already contains a resource with the given [identifier]
         * - if the [media-type][Path.mediaType] of [file] does not match any of the [known media-types][MEDIA_TYPES]
         * for page-resources
         */
        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun fromFile(
            file: Path,
            book: Book,
            identifier: Identifier = Identifier.fromFile(file)
        ): PageResource {
            requireThat(identifier !in book.resources) { "expected 'identifier' to be unique, but there already exists a resource with the same identifier '$identifier'" }
            val mediaType = file.mediaType
            requireThat(mediaType in MEDIA_TYPES) { "expected 'file' to have a media-type that matches any of $MEDIA_TYPES, got '$mediaType'" }
            return PageResource(book, file, identifier)
        }

        /**
         * The [resource][Page.resource] that the returned page is tied to will be created from scratch, in the
         * [desired directory][PageResource.desiredDirectory] of the [page-resource][PageResource], with the given
         * [fileName]. The newly created resource will then be added to the given [book] with its identifier set to the
         * given [identifier] *(which by default is the given `fileName` prefixed by an `'x'` and suffixed with
         * `".xhtml"`)*.
         *
         * For example, say we invoke this function in the following manner
         * `Page.fromString(contents = .., book = .., fileName = "interesting_page")` then a fil
         *
         * TODO: The above ddocumentation as it was just copy pasted of what had been written for the function in 'Page'
         *
         * @throws [IOException] if an i/o error occurred
         * @throws [IllegalArgumentException]
         * - if [book] already contains a resource with the given [identifier]
         * - if [contents] [is blank][String.isBlank] or invalid CSS
         * - if [fileName] points to an already existing file
         */
        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun fromString(
            contents: String,
            book: Book,
            fileName: String,
            identifier: Identifier = Identifier.of("x_$fileName.xhtml")
        ): PageResource {
            requireThat(identifier !in book.resources) { "expected 'identifier' to be unique, but there already exists a resource with the same identifier '$identifier'" }
            requireThat(contents.isNotBlank()) { "given 'contents' is blank" }
            // TODO: Some sort of HTML validation done on the 'contents'?
            val file = book
                .resources
                .getDirectoriesUsedBy<PageResource>()
                .first()
                .resolve("$fileName.xhtml")
            requireThat(file.notExists) { "given 'fileName' points to an existing file: '$file'" }
            file.writeString(contents)
            return PageResource(book, file, identifier)
        }

    }
}

/**
 * A resource wrapping around a [CascadingStyleSheet] instance.
 *
 * @property [styleSheet] The [CascadingStyleSheet] instance that `this` resource is wrapping around.
 */
class StyleSheetResource private constructor(
    override val book: Book,
    file: Path,
    identifier: Identifier,
    val styleSheet: CascadingStyleSheet
) : Resource(file, identifier, "Styles/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    override fun onCreation() {
        // TODO: The 'onCreation' function seems to never get ran?
    }

    override fun onDeletion() {
        for (page in book.pages) page.removeStyleSheet(this)
    }

    override fun toString(): String = "StyleSheetResource(identifier='$identifier', href='$href', book=$book)"

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        logger.trace { "Writing style-sheet contents to file <$file>.." }
        fileSystem.getPath(file.toString()).newBufferedWriter().use { CSSWriter().writeCSS(styleSheet, it) }
    }

    companion object {
        @JvmField
        val MEDIA_TYPES: ImmutableSet<MediaType> =
            persistentHashSetOf(MediaType.create("text", "css"), MediaType.CSS_UTF_8)

        // TODO: Documentation
        /**
         * @throws [IllegalArgumentException]
         * - if [book] already contains a resource with the given [identifier]
         * - if the [media-type][Path.mediaType] of [file] does not match any of the [known media-types][MEDIA_TYPES]
         * for stylesheet-resources
         * @throws [IOException] if an i/o error occurred
         */
        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun fromFile(
            file: Path,
            book: Book,
            identifier: Identifier = Identifier.fromFile(file)
        ): StyleSheetResource {
            requireThat(identifier !in book.resources) { "expected 'identifier' to be unique, but there already exists a resource with the same identifier '$identifier'" }
            val mediaType = file.mediaType
            requireThat(mediaType in MEDIA_TYPES) { "expected 'file' to have a media-type that matches any of $MEDIA_TYPES, got '$mediaType'" }
            val styleSheet = file.newBufferedReader().use {
                val settings = CSSReaderSettings()
                    .setCustomErrorHandler(ThrowingCSSParseErrorHandler())
                CSSReader.readFromReader({ it }, settings)
            } ?: throw IllegalArgumentException("Could not read file '${file.name}' into a style-sheet")
            return StyleSheetResource(book, file, identifier, styleSheet)
        }

        /**
         * @throws [IllegalArgumentException]
         * - if [book] already contains a resource with the given [identifier]
         * - if [contents] [is blank][String.isBlank] or invalid CSS
         * - if [fileName] points to an already existing file
         * @throws [IOException] if an i/o error occurred
         */
        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun fromString(
            contents: String,
            book: Book,
            fileName: String,
            identifier: Identifier = Identifier.of("x_$fileName.css")
        ): StyleSheetResource {
            requireThat(identifier !in book.resources) { "expected 'identifier' to be unique, but there already exists a resource with the same identifier '$identifier'" }
            requireThat(contents.isNotBlank()) { "given 'contents' is blank" }
            requireThat(CSSReader.isValidCSS(contents, ECSSVersion.CSS30)) { "given 'contents' is not valid CSS" }
            val styleSheetSettings = CSSReaderSettings()
                .setCustomErrorHandler(ThrowingCSSParseErrorHandler())
            val styleSheet = requireNotNull(CSSReader.readFromStringReader(contents, styleSheetSettings)) {
                "given 'contents' could not be parsed as CSS"
            }
            val file = book
                .resources
                .getDirectoriesUsedBy<StyleSheetResource>()
                .first()
                .resolve("$fileName.css")
            requireThat(file.notExists) { "given 'fileName' points to an existing file: '$file'" }
            file.newBufferedWriter().use { CSSWriter().writeCSS(styleSheet, it) }
            return StyleSheetResource(book, file, identifier, styleSheet)
        }
    }
}

class ImageResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Images/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    private var isBufferedImageAvailable: Boolean = false

    /**
     * Lazily reads and returns a [BufferedImage] instance based on the [file] of `this` resource.
     *
     * @throws [ResourceException] if an error occurred when attempting to read the [file] into a [BufferedImage]
     */
    @get:Throws(ResourceException::class)
    val image: BufferedImage by lazy {
        try {
            file.newInputStream().use(ImageIO::read).also { isBufferedImageAvailable = true }
        } catch (e: IOException) {
            raiseException("Could not read file '${file.name}' into a buffered-image", e)
        }
    }

    /**
     * Returns the current dimension of the [image].
     *
     * Note that if `image` has not already been invoked, then invoking this property will result in `image` being
     * invoked and lazily read, which may produce overhead.
     */
    val dimension: Dimension get() = Dimension(image.width, image.height)

    /**
     * Returns `true` if `this` resource represents the `cover-image` of the [book], otherwise `false`.
     */
    fun isCoverImage(): Boolean = when {
        book.version < BookVersion.EPUB_3_0 -> book
            .metadata
            .metaElements
            .asSequence()
            .filterIsInstance<OPF2Meta>()
            .filter { it.name == "cover" }
            .any { it.content == identifier.toString() }
        book.version >= BookVersion.EPUB_3_0 -> ManifestVocabulary.COVER_IMAGE in properties
        else -> throw IllegalStateException("Unknown version '${book.version}'")
    }

    /**
     * Sets the image that `this` resource represents as the `cover-image` of the [book].
     */
    fun setAsCoverImage() {
        when {
            book.version < BookVersion.EPUB_3_0 -> {
                book.metadata.metaElements.removeIf { it is OPF2Meta && it.name == "cover" }
                book.metadata.addMeta(OPF2Meta.withName("cover", identifier.toString()))
            }
            book.version >= BookVersion.EPUB_3_0 -> {
                for ((_, image) in book.resources.images) {
                    image.properties.remove(ManifestVocabulary.COVER_IMAGE)
                }

                properties.add(ManifestVocabulary.COVER_IMAGE)
            }
        }
    }

    // TODO: 'removeAsCoverImage'

    override fun toString(): String =
        "ImageResource(identifier='$identifier', href='$href', isCoverImage=${isCoverImage()}, book=$book)"

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        if (isBufferedImageAvailable) {
            logger.trace { "Writing buffered-image contents to file <$file>.." }
            image.writeTo(fileSystem.getPath(file.toString()))
        }
    }

    internal companion object {
        @JvmField internal val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(
            MediaType.GIF,
            MediaType.JPEG,
            MediaType.PNG,
            MediaType.create("image", "svg+xml"),
            MediaType.SVG_UTF_8
        )
    }
}

class FontResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Fonts/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    override fun toString(): String = "FontResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField internal val MEDIA_TYPES: ImmutableSet<MediaType> =
            persistentHashSetOf(MediaType.create("application", "vnd.ms-opentype"), MediaType.WOFF)
    }
}

class AudioResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Audio/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    override fun toString(): String = "AudioResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField internal val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.MPEG_AUDIO)
    }
}

class ScriptResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Scripts/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    override fun toString(): String = "ScriptResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField internal val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(
            MediaType.create("text", "javascript"),
            MediaType.TEXT_JAVASCRIPT_UTF_8,
            MediaType.create("application", "javascript")
        )
    }
}

class VideoResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Video/") {
    override val mediaTypes: ImmutableSet<MediaType> get() = MEDIA_TYPES

    override fun toString(): String = "VideoResource(identifier='$identifier', href='$href', book=$book)"

    internal companion object {
        @JvmField internal val MEDIA_TYPES: ImmutableSet<MediaType> = persistentHashSetOf(MediaType.MP4_VIDEO)
    }
}

class MiscResource internal constructor(override val book: Book, file: Path, identifier: Identifier) :
    Resource(file, identifier, "Misc/") {
    override val mediaTypes: ImmutableSet<MediaType> = persistentSetOf()

    override fun toString(): String = "MiscResource(identifier='$identifier', href='$href', book=$book)"
}