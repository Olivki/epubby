/*
 * Copyright 2019-2022 Oliver Berg
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

import com.github.michaelbull.logging.InlineLogger
import com.google.auto.service.AutoService
import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.files.RegularFile
import dev.epubby.packages.metadata.Opf2Meta
import dev.epubby.properties.ManifestVocabulary.COVER_IMAGE
import dev.epubby.utils.ImageDimension
import dev.epubby.utils.ImageOrientation
import kotlinx.collections.immutable.persistentHashSetOf
import krautils.io.writeTo
import krautils.scalr.ImageResizeMode
import krautils.scalr.ImageScalingMethod
import org.imgscalr.Scalr
import org.jetbrains.annotations.Contract
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.io.IOException
import java.nio.file.FileSystem
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories

class ImageResource(
    epub: Epub,
    identifier: String,
    file: RegularFile,
    override val mediaType: MediaType,
) : LocalResource(epub, identifier, file) {
    var isCoverImage: Boolean
        get() = when {
            epub.version.isOlder(EPUB_3_0) -> additionalMetadata.any { it.name == "cover" }
            else -> when (COVER_IMAGE) {
                in properties -> true
                else -> additionalMetadata.any { it.name == "cover" }
            }
        }
        set(value) {
            // there should only ever be one cover image, so we'll just remove all other entries that are marked with
            // it, regardless if there's more than one

            if (epub.version.isOlder(EPUB_3_0)) {
                val entries = epub.metadata.opf2MetaEntries
                entries.removeIf { it is Opf2Meta.Name && it.name == "cover" && if (!value) it.isFor(this) else true }

                if (value) {
                    entries += Opf2Meta.Name(epub, "cover", identifier)
                }
            } else {
                epub.manifest.visitResources(CoverImagePropertyRemover, ResourceFilters.ONLY_IMAGES)

                if (value) {
                    properties += COVER_IMAGE
                }
            }
        }

    private object CoverImagePropertyRemover : ResourceVisitorUnit {
        override fun visitImage(resource: ImageResource) {
            resource.properties -= COVER_IMAGE
        }
    }

    // whether 'image' should be reread from the 'file' next time the getter for 'image' is invoked
    private var shouldRefreshImage: Boolean = false

    /**
     * Returns the orientation of the `image` of this resource.
     *
     * Note that this will load `image` if it it is not already loaded, see the documentation of [getOrLoadImage] for
     * more information.
     */
    val orientation: ImageOrientation
        @Contract("_ -> new", pure = true)
        get() = ImageOrientation.fromBufferedImage(getOrLoadImage())

    /**
     * Returns the dimensions of the `image` of this resource.
     *
     * Note that this will load `image` if it's not already loaded, see the documentation of [getOrLoadImage] for
     * more information.
     */
    val dimension: ImageDimension
        @Contract("_ -> new", pure = true)
        get() {
            val image = getOrLoadImage()
            return ImageDimension(image.width, image.height)
        }

    /**
     * Returns `true` if `image` has been cached yet, otherwise `false`.
     *
     * If `image` *has* been cached, then that means that invoking [getOrLoadImage] should not incur any additional
     * overhead, and it should have no chance of throwing any [IOException]s.
     */
    var isImageLoaded: Boolean = false
        private set

    // the underlying backing field that 'image' represents
    private var _image: BufferedImage? = null

    /**
     * Returns the [BufferedImage] instance for the [file] that this resource is wrapped around.
     *
     * The first invocation of this function *(if [setImage] has not been invoked at some point prior)* will result in
     * [file] being read into a [BufferedImage] instance using [ImageIO.read], and as such, invoking it may cause some
     * severe overhead, and any exceptions that `ImageIO.read` throws, it will also throw. To see if an image has been
     * loaded or not, see [isImageLoaded].
     *
     * The `BufferedImage` returned by this function may, or may not, represent a different image than what the `file`
     * of this resource represents, this may happen if [setImage] has been invoked at some point. To return `image` to
     * the same image as `file` represents, see [refreshImage].
     *
     * Whenever the [epub] that this resource belongs to is saved back into it's file form, and this image
     * [is loaded][isImageLoaded], the contents of the `BufferedImage` returnd by this function will be written to the
     * `file` belonging to this resource.
     *
     * @see [isImageLoaded]
     * @see [refreshImage]
     */
    @Synchronized
    fun getOrLoadImage(): BufferedImage {
        if (shouldRefreshImage || _image == null) {
            try {
                LOGGER.debug { "Attempting to read '$file' as a buffered-image.." }
                val result = file.newInputStream().use(ImageIO::read)
                isImageLoaded = true
                shouldRefreshImage = false
                _image?.flush()
                _image = result
            } catch (e: IOException) {
                throw IOException(
                    "Could not read image-resource $identifier into a buffered-image; ${e.message}",
                    e
                )
            }
        }

        return _image ?: throw IllegalStateException("'_image' of $this should be loaded, but it is not")
    }

    /**
     * Sets the `image` of this resource to the given [image].
     *
     * If `image` has already been defined, it will be [flushed][BufferedImage.flush] before setting it to the given
     * `image`.
     */
    @Synchronized
    fun setImage(image: BufferedImage): ImageResource = apply {
        if (image !== _image) {
            _image?.flush()
            isImageLoaded = true
            _image = image
        }
    }

    /**
     * Sets `image` back to the result of reading [file] into a [BufferedImage] via [ImageIO.read].
     *
     * Note that this function will only refresh `image` if it has [already been loaded][isImageLoaded], otherwise
     * nothing happens.
     *
     * @throws [IOException] if an i/o error occurs when reading [file] into a [BufferedImage]
     */
    // TODO: rename to 'resetImage'
    @Throws(IOException::class)
    fun refreshImage(): ImageResource = apply {
        if (isImageLoaded) {
            shouldRefreshImage = true
            getOrLoadImage()
        }
    }

    // TODO: implement 'resizeTo' function that takes an integer going from 1 to 100, and use that as a percentage
    //       based resize

    /**
     * Resizes the `image` to the given [width] and [height].
     *
     * The quality of the resize depends on the [scalingMethod] used, and whether or not the proportions of the resized
     * image are kept depends on the given [resizeMode].
     *
     * Note that invoking this function will result in `image` being cached *(if it is not already cached)*, which may
     * result in some severe overhead depending on the size of the `image` this resource represents.
     *
     * @param [width] the width to resize `image` to
     * @param [height] the height to resize `height` to
     * @param [scalingMethod] the method to use for resizing the image *([ULTRA_QUALITY][Scalr.Method.ULTRA_QUALITY] by
     * default)*
     * @param [resizeMode] the mode to use for resizing the image *([FIT_EXACT][Scalr.Mode.FIT_EXACT] by default)*
     * @param [operations] any extra operations to be done on the image after resizing it
     *
     * @see [Scalr.resize]
     */
    @JvmOverloads
    fun resizeTo(
        width: Int,
        height: Int,
        scalingMethod: ImageScalingMethod = ImageScalingMethod.ULTRA_QUALITY,
        resizeMode: ImageResizeMode = ImageResizeMode.FIT_EXACT,
        vararg operations: BufferedImageOp,
    ): ImageResource = apply {
        setImage(Scalr.resize(getOrLoadImage(), scalingMethod, resizeMode, width, height, *operations))
    }

    /**
     * Resizes the `image` to the given [dimension].
     *
     * The quality of the resize depends on the [scalingMethod] used, and whether or not the proportions of the resized
     * image are kept depends on the given [resizeMode].
     *
     * Note that invoking this function will result in `image` being cached *(if it is not already cached)*, which may
     * result in some severe overhead depending on the size of the `image` this resource represents.
     *
     * @param [dimension] the dimension to resize the image to
     * @param [scalingMethod] the method to use for resizing the image *([ULTRA_QUALITY][Scalr.Method.ULTRA_QUALITY] by
     * default)*
     * @param [resizeMode] the mode to use for resizing the image *([FIT_EXACT][Scalr.Mode.FIT_EXACT] by default)*
     * @param [operations] any extra operations to be done on the image after resizing it
     *
     * @see [Scalr.resize]
     */
    @JvmOverloads
    fun resizeTo(
        dimension: ImageDimension,
        scalingMethod: ImageScalingMethod = ImageScalingMethod.ULTRA_QUALITY,
        resizeMode: ImageResizeMode = ImageResizeMode.FIT_EXACT,
        vararg operations: BufferedImageOp,
    ): ImageResource = resizeTo(dimension.width, dimension.height, scalingMethod, resizeMode, *operations)

    /**
     * Applies the given [operations] to [image].
     *
     * Note that invoking this function will result in [image] being cached *(if it is not already cached)*, which may
     * result in some severe overhead depending on the size of the `image` this resource represents.
     *
     * @see [Scalr.OP_ANTIALIAS]
     * @see [Scalr.OP_BRIGHTER]
     * @see [Scalr.OP_DARKER]
     * @see [Scalr.OP_GRAYSCALE]
     */
    fun apply(vararg operations: BufferedImageOp): ImageResource = apply {
        setImage(Scalr.apply(getOrLoadImage(), *operations))
    }

    // TODO: implement more wrapper functions for 'Scalr' utility functions?

    override fun writeToFile() {
        if (isImageLoaded) {
            getOrLoadImage().writeTo(file.delegate)
        }
    }

    @JvmSynthetic
    internal fun writeTo(fileSystem: FileSystem) {
        if (isImageLoaded) {
            val path = fileSystem.getPath(file.toString())
            path.createDirectories()
            getOrLoadImage().writeTo(path)
        }
    }

    /**
     * Returns the result of invoking the [visitImage][ResourceVisitor.visitImage] function of the given [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitImage(this)

    override fun toString(): String = "ImageResource(identifier='$identifier', mediaType=$mediaType, file='$file')"

    private companion object {
        private val LOGGER: InlineLogger = InlineLogger(ImageResource::class)
    }
}

@AutoService(LocalResourceLocator::class)
internal object ImageResourceLocator : LocalResourceLocator {
    private val TYPES: Set<MediaType> = persistentHashSetOf(
        MediaType.GIF,
        MediaType.JPEG,
        MediaType.PNG,
        MediaType.SVG_UTF_8,
        MediaType.SVG_UTF_8.withoutParameters()
    )
    private val FACTORY: LocalResourceFactory = ::ImageResource

    override fun findFactory(mediaType: MediaType): LocalResourceFactory? = FACTORY.takeIf { mediaType in TYPES }
}