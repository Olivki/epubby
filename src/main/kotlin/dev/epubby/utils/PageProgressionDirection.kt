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

enum class PageProgressionDirection(val attributeName: String) {
    LEFT_TO_RIGHT("ltr"),
    RIGHT_TO_LEFT("rtl"),
    DEFAULT("default");

    companion object {
        /**
         * Returns the [PageProgressionDirection] that matches the given [tag], or throws a [IllegalArgumentException]
         * if `tag` does not match a direction.
         */
        @JvmStatic
        fun fromTag(tag: String): PageProgressionDirection = when (tag.toLowerCase()) {
            "ltr" -> LEFT_TO_RIGHT
            "rtl" -> RIGHT_TO_LEFT
            "default" -> DEFAULT
            else -> throw IllegalArgumentException("Expected \"ltr\", \"rtl\", or \"default\" got \"$tag\"")
        }
    }
}