/*
 * Copyright 2019-2021 Oliver Berg
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

import java.awt.image.BufferedImage

/**
 * An orientation that a image can be in.
 */
enum class ImageOrientation {
    /**
     * The orientation of a portrait photograph, where the `height` is greater than the `width` of the image.
     */
    PORTRAIT,

    /**
     * The orientation of a landscape photograph, where the `width` is greater than the `height` of the image.
     */
    LANDSCAPE,

    /**
     * The orientation of a rectangle photograph, where the `width` and `height` of the image are the same.
     */
    RECTANGLE;

    companion object {
        /**
         *  Returns a [ImageOrientation] that matches the `width` and `height` of the given [image].
         *
         *  @throws [IllegalArgumentException] if the `width` and `height` of [image] somehow doesn't match any of the
         *  known dimensions
         */
        @JvmStatic
        fun fromBufferedImage(image: BufferedImage): ImageOrientation = when {
            image.width == image.height -> RECTANGLE
            image.width > image.height -> LANDSCAPE
            image.height < image.width -> LANDSCAPE
            image.width < image.height -> PORTRAIT
            image.height > image.width -> PORTRAIT
            else -> throw IllegalArgumentException("(${image.width}, ${image.height}) represents an unknown dimension.")
        }
    }
}