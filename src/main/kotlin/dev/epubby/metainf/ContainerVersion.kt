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

package dev.epubby.metainf

import dev.epubby.BookVersion
import java.lang.NumberFormatException

data class ContainerVersion(val major: Int, val minor: Int) : Comparable<ContainerVersion> {
    companion object {
        /**
         * The version used by default for EPUBs from version [2.0][BookVersion.EPUB_2_0] to
         * [3.2][BookVersion.EPUB_3_2].
         */
        @JvmField
        val DEFAULT: ContainerVersion = ContainerVersion(1, 0)

        /**
         * Returns a new [ContainerVersion] instance based on the given [version] string.
         *
         * @throws [IllegalArgumentException] if [version] doesn't contain the `.` character
         * @throws [NumberFormatException] if the parts [version] can't be parsed to an integer
         */
        @JvmStatic
        fun fromString(version: String): ContainerVersion {
            require('.' in version) { "'version' must contain a '.' character" }
            val (rawMajor, rawMinor) = version.split('.')
            val major = rawMajor.toInt()
            val minor = rawMinor.toInt()

            return ContainerVersion(major, minor)
        }
    }

    init {
        require(major >= 0) { "'major' must not be negative" }
        require(minor >= 0) { "'minor' must not be negative" }
    }

    override fun compareTo(other: ContainerVersion): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        else -> 0
    }

    override fun toString(): String = "$major.$minor"
}