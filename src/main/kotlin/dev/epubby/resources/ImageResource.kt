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
import kotlinx.collections.immutable.persistentHashSetOf
import moe.kanon.kommons.io.paths.createDirectories
import moe.kanon.kommons.io.paths.isSameAs
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.writeTo
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path
import javax.imageio.ImageIO

class ImageResource(
    book: Book,
    identifier: String,
    file: Path,
    override val mediaType: MediaType
) : Resource(book, identifier, file) {
    private var shouldRefreshImage: Boolean = false

    var isImageCached: Boolean = false
        private set

    private var _image: BufferedImage? = null

    val image: BufferedImage
        @Throws(IOException::class)
        get() {
            if (shouldRefreshImage || _image == null) {
                _image = try {
                    isImageCached = true
                    file.newInputStream().use { ImageIO.read(it) }
                } catch (e: IOException) {
                    throw IOException(
                        "Could not read image-resource $identifier into a buffered-image; ${e.message}",
                        e
                    )
                }
                shouldRefreshImage = false
            }

            return _image ?: throw IllegalStateException("_image should not be null here")
        }

    /**
     * Returns the dimensions of the [image].
     *
     * Note that when invoking this property, `image` will be cached, which may produce overhead.
     */
    val dimension: Dimension
        get() = Dimension(image.width, image.height)

    @JvmSynthetic
    internal fun writeTo(fileSystem: FileSystem) {
        if (isImageCached) {
            val path = fileSystem.getPath(file.toString())
            path.createDirectories()
            image.writeTo(path)
        }
    }

    override fun onFileChanged(oldFile: Path, newFile: Path) {
        if (!(oldFile isSameAs newFile)) {
            shouldRefreshImage = true
            image
        }
    }

    override fun <R : Any> accept(visitor: ResourceVisitor<R>): R = visitor.visitImage(this)

    override fun toString(): String = "ImageResource(identifier='$identifier', mediaType=$mediaType, file='$file')"
}

@AutoService(ResourceLocator::class)
internal object ImageResourceLocator : ResourceLocator {
    private val TYPES: Set<MediaType> = persistentHashSetOf(
        MediaType.GIF,
        MediaType.JPEG,
        MediaType.PNG,
        MediaType.create("image", "svg+xml"),
        MediaType.SVG_UTF_8
    )
    private val FACTORY: ResourceFactory = ::ImageResource

    override fun findFactory(mediaType: MediaType): ResourceFactory? = FACTORY.takeIf { mediaType in TYPES }
}