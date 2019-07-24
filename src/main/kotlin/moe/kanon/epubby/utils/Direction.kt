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

package moe.kanon.epubby.utils

/**
 * Represents the two possible values a [dir](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#attrdef-dir)
 * attribute can have.
 */
enum class Direction(val serializedName: String) {
    LEFT_TO_RIGHT("ltr"),
    RIGHT_TO_LEFT("rtl");

    override fun toString(): String = serializedName

    companion object {
        /**
         * Returns the [Direction] that matches the given [tag], or throws a [IllegalArgumentException] if `tag` does
         * not match a direction.
         */
        @JvmStatic fun of(tag: String): Direction = when {
            tag.equals("ltr", ignoreCase = true) -> LEFT_TO_RIGHT
            tag.equals("rtl", ignoreCase = true) -> RIGHT_TO_LEFT
            else -> throw IllegalArgumentException("Expected \"ltr\" or \"rtl\", got \"$tag\"")
        }
    }
}