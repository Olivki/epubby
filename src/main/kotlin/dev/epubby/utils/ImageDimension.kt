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

package dev.epubby.utils

import org.jetbrains.annotations.Contract
import java.awt.image.BufferedImage

/**
 * Represents the `width` and `height` of an image.
 *
 * @property [width] The width of this dimension.
 * @property [height] The height of this dimension.
 */
data class ImageDimension(val width: Int, val height: Int) {
    init {
        require(width >= 0) { "'width' is negative" }
        require(height >= 0) { "'height' is negative" }
    }

    /**
     * Returns a new [ImageDimension] instance, with `width` set to the given [width], and `height` set to the [height]
     * of this.
     *
     * @throws [IllegalArgumentException] if [width] is negative
     */
    @Contract("_ -> new", pure = true)
    fun withWidth(width: Int): ImageDimension = copy(width = width)

    /**
     * Returns a new [ImageDimension] instance, with `width` set to the [width] of this, and `height` set to the given
     * [height].
     *
     * @throws [IllegalArgumentException] if [height] is negative
     */
    @Contract("_ -> new", pure = true)
    fun withHeight(height: Int): ImageDimension = copy(height = height)

    // TODO: documentation
    @Contract("_ -> new", pure = true)
    operator fun div(amount: Int): ImageDimension = ImageDimension(width / amount, height / amount)

    @Contract("_ -> new", pure = true)
    operator fun times(amount: Int): ImageDimension = ImageDimension(width * amount, height * amount)

    @Contract("_ -> new", pure = true)
    operator fun rem(amount: Int): ImageDimension = ImageDimension(width % amount, height % amount)

    override fun toString(): String = "[width=$width, height=$height]"

    companion object {
        /**
         * Returns a new [ImageDimension] instance with its `width` and `height` set to that of the given [image].
         */
        @JvmStatic
        @Contract("_ -> new", pure = true)
        fun fromBufferedImage(image: BufferedImage): ImageDimension = ImageDimension(image.width, image.height)
    }
}

/**
 * Returns a new [ImageDimension] instance with its `width` and `height` set to that of the given [image].
 */
val BufferedImage.dimension: ImageDimension
    @Contract("_ -> new", pure = true)
    get() = ImageDimension.fromBufferedImage(this)