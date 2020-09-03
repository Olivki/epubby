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

package dev.epubby.resources

import com.google.auto.service.AutoService
import com.google.common.net.MediaType
import dev.epubby.Book
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.packages.metadata.Opf2Meta
import dev.epubby.properties.vocabularies.ManifestVocabulary.COVER_IMAGE
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.collections.immutable.persistentHashSetOf
import moe.kanon.kommons.io.ImageResizeMode
import moe.kanon.kommons.io.ImageScalingMethod
import moe.kanon.kommons.io.paths.createDirectories
import moe.kanon.kommons.io.paths.isSameAs
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.writeTo
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path
import javax.imageio.ImageIO

class ImageResource(
    book: Book,
    identifier: String,
    file: Path,
    override val mediaType: MediaType,
) : LocalResource(book, identifier, file) {
    var isCoverImage: Boolean
        get() = when {
            book.version.isOlder(EPUB_3_0) -> additionalMetadata.any { it.name == "cover" }
            else -> COVER_IMAGE in properties
        }
    set(value) {
        // there should only ever be one cover image, so we'll just remove all other entries that are marked with
        // it, regardless if there's more than one

        if (book.version.isOlder(EPUB_3_0)) {
            val entries = book.metadata.opf2MetaEntries
            entries.removeIf { it is Opf2Meta.Name && it.name == "cover" && if (!value) it.isFor(this) else true }

            if (value) {
                entries += Opf2Meta.Name(book, "cover", identifier)
            }
        } else {
            book.manifest.visitResources(CoverImagePropertyRemover, ResourceFilters.ONLY_IMAGES)

            if (value) {
                properties += COVER_IMAGE
            }
        }
    }

    private object CoverImagePropertyRemover : ResourceVisitorUnit {
        override fun visitImage(image: ImageResource) {
            image.properties -= COVER_IMAGE
        }
    }

    // whether or not 'image' should be reread from the 'file' next time the getter for 'image' is invoked
    private var shouldRefreshImage: Boolean = false

    /**
     * Returns `true` if `image` has been cached yet, otherwise `false`.
     *
     * If `image` *has* been cached, then that means that invoking [getOrLoadImage] should not incur any additional
     * overhead, and it should have no chance of throwing any [IOException]s.
     */
    var isImageLoaded: Boolean = false
        private set

    // the underlying backing field that 'image' represents
    private var _image = atomic<BufferedImage?>(null)

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
     * Whenever the [book] that this resource belongs to is saved back into it's file form, and this image
     * [is loaded][isImageLoaded], the contents of the `BufferedImage` returnd by this function will be written to the
     * `file` belonging to this resource.
     *
     * @see [isImageLoaded]
     * @see [refreshImage]
     */
    @Throws(IOException::class)
    fun getOrLoadImage(): BufferedImage {
        if (shouldRefreshImage || _image.value == null) {
            _image.update {
                try {
                    val result = file.newInputStream().use(ImageIO::read)
                    isImageLoaded = true
                    shouldRefreshImage = false
                    it?.flush()
                    result
                } catch (e: IOException) {
                    throw IOException(
                        "Could not read image-resource $identifier into a buffered-image; ${e.message}",
                        e
                    )
                }
            }
        }

        return _image.value ?: throw IllegalStateException("'_image' of $this should be loaded, but it is not")
    }

    /**
     * Sets the `image` of this resource to the given [image].
     *
     * If `image` has already been defined, it will be [flushed][BufferedImage.flush] before setting it to the given
     * `image`.
     */
    fun setImage(image: BufferedImage) {
        if (image !== _image.value) {
            _image.update {
                it?.flush()
                isImageLoaded = true
                image
            }
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
    @Throws(IOException::class)
    fun refreshImage() {
        if (isImageLoaded) {
            shouldRefreshImage = true
            getOrLoadImage()
        }
    }

    /**
     * Resizes the [image] to the given [width] and [height].
     *
     * The quality of the resize depends on the [scalingMethod] used, and whether or not the proportions of the resized
     * image are kept depends on the given [resizeMode].
     *
     * Note that invoking this function will result in [image] being cached *(if it is not already cached)*, which may
     * result in some severe overhead depending on the size of the `image` this resource represents.
     *
     * @param [width] the width to resize `image` to
     * @param [height] the height to resize `height` to
     * @param [scalingMethod] the method to use for resizing the image *([ULTRA_QUALITY][Scalr.Method.ULTRA_QUALITY] by
     * default)*
     * @param [resizeMode] the mode to use for resizing the image *([FIT_EXACT][Scalr.Mode.FIT_EXACT] by default)*
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
    ) {
        setImage(Scalr.resize(getOrLoadImage(), scalingMethod, resizeMode, width, height, *operations))
    }

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
    fun apply(vararg operations: BufferedImageOp) {
        setImage(Scalr.apply(getOrLoadImage(), *operations))
    }

    // TODO: implement more wrapper functions for 'Scalr' utility functions?

    @JvmSynthetic
    internal fun writeTo(fileSystem: FileSystem) {
        if (isImageLoaded) {
            val path = fileSystem.getPath(file.toString())
            path.createDirectories()
            getOrLoadImage().writeTo(path)
        }
    }

    override fun onFileChanged(oldFile: Path, newFile: Path) {
        if (isImageLoaded && !(oldFile isSameAs newFile)) {
            refreshImage()
        }
    }

    /**
     * Returns the result of invoking the [visitImage][ResourceVisitor.visitImage] function of the given [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitImage(this)

    override fun toString(): String = "ImageResource(identifier='$identifier', mediaType=$mediaType, file='$file')"
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