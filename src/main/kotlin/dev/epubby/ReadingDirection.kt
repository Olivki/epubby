/*
 * Copyright 2019-2023 Oliver Berg
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

package dev.epubby

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import kotlinx.serialization.SerialName

/**
 * Represents the two possible values a [dir](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#attrdef-dir)
 * attribute can have.
 */
public enum class ReadingDirection(public val value: String) {
    @SerialName("ltr")
    LEFT_TO_RIGHT("ltr"),

    @SerialName("rtl")
    RIGHT_TO_LEFT("rtl");

    public companion object {
        public fun fromValue(value: String): Result<ReadingDirection, String> =
            fromValueOrNull(value).toResultOr { value }

        public fun fromValueOrNull(value: String): ReadingDirection? = when (value) {
            "ltr" -> LEFT_TO_RIGHT
            "rtl" -> RIGHT_TO_LEFT
            else -> null
        }
    }
}