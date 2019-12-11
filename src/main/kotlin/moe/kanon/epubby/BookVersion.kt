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

@file:Suppress("DataClassPrivateConstructor")

package moe.kanon.epubby

import moe.kanon.kommons.requireThat

// TODO: EPUB has actually used a patch version before, with 2.0.1, this system will fail on parsing that version
//       Look into changing how this is done?
// because EPUB format versions do not adhere to semantic versioning (as they do not have a patch version) I've opted
// to create my own simple version implementation just for EPUB format versions.
class BookVersion private constructor(val major: Int, val minor: Int) : Comparable<BookVersion> {
    init {
        requireThat(major >= 0) { "major should be positive" }
        requireThat(minor >= 0) { "minor should be positive" }
    }

    override fun compareTo(other: BookVersion): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        else -> 0
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is BookVersion -> false
        major != other.major -> false
        minor != other.minor -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        return result
    }

    override fun toString(): String = "$major.$minor"

    class FaultyVersionException internal constructor(val version: String, message: String, cause: Throwable? = null) :
        EpubbyException(message, cause)

    class UnsupportedVersionException internal constructor(val version: String) :
        EpubbyException("Epubby does not know how to handle version '$version' of the EPUB format")

    companion object {
        private val KNOWN_VERSIONS = HashMap<String, BookVersion>()

        /**
         * Represents the [EPUB 2.x](http://www.idpf.org/epub/dir/#epub201) format.
         */
        @JvmField val EPUB_2_0 = getOrCache(2, 0)
        /**
         * Represents the [EPUB 3.0](http://www.idpf.org/epub/dir/#epub301) format.
         */
        @JvmField val EPUB_3_0 = getOrCache(3, 0)
        /**
         * Represents the [EPUB 3.1](http://www.idpf.org/epub/dir/#epub31) format.
         *
         * The EPUB 3.1 format is [officially discouraged](http://www.idpf.org/epub/dir/#epub31) from use, and as such
         * the format is explicitly ***not*** supported by epubby, and it should never be used.
         */
        @JvmField val EPUB_3_1 = getOrCache(3, 1) // TODO: Crash if the EPUB version is this lol
        /**
         * Represents the [EPUB 3.2](http://www.idpf.org/epub/dir/#epub32) format.
         */
        @JvmField val EPUB_3_2 = getOrCache(3, 2)

        private fun getOrCache(major: Int, minor: Int): BookVersion =
            KNOWN_VERSIONS.getOrPut("$major.$minor") { BookVersion(major, minor) }

        @JvmSynthetic
        internal fun fromInteger(major: Int, minor: Int): BookVersion =
            KNOWN_VERSIONS["$major.$minor"] ?: throw UnsupportedVersionException("$major.$minor")

        @JvmSynthetic
        internal fun fromString(version: String): BookVersion {
            validateThat(version, version.isNotBlank()) { "can't be blank / empty" }
            validateThat(version, version.first().isDigit()) { "needs to start with a digit" }
            validateThat(version, '.' in version) { "needs to contain exactly one '.' character" }
            validateThat(version, version.count { it == '.' } == 1) { "can't contain more than one '.' character" }
            validateThat(version, version.last().isDigit()) { "needs to end with a digit" }

            fun unknownChar(char: Char, index: Int): Nothing =
                fail(version, "contains an unknown character '$char' at index $index")

            var state = ParseState.UNINITIALIZED
            val builder = StringBuilder()
            lateinit var majorString: String
            lateinit var minorString: String

            for ((i, char) in version.withIndex()) {
                state = when (state) {
                    ParseState.UNINITIALIZED -> {
                        builder.append(char)
                        ParseState.NUMBER
                    }
                    ParseState.NUMBER -> when {
                        char == '.' -> {
                            majorString = builder.toString()
                            builder.clear()
                            ParseState.DOT
                        }
                        char.isDigit() -> {
                            builder.append(char)
                            ParseState.NUMBER
                        }
                        else -> unknownChar(char, i)
                    }
                    ParseState.DOT -> when {
                        char.isDigit() -> {
                            builder.append(char)
                            ParseState.NUMBER
                        }
                        else -> unknownChar(char, i)
                    }
                }
            }

            minorString = builder.toString()
            builder.clear()

            val major = try {
                majorString.toInt()
            } catch (e: Exception) {
                fail(
                    version,
                    "'major' version can't be converted to an integer'",
                    e
                )
            }

            val minor = try {
                minorString.toInt()
            } catch (e: Exception) {
                fail(
                    version,
                    "'minor' version can't be converted to an integer",
                    e
                )
            }

            return fromInteger(major, minor)
        }

        private inline fun validateThat(version: String, condition: Boolean, message: () -> Any) {
            if (!condition) fail(version, message().toString())
        }

        private fun fail(version: String, info: String, cause: Throwable? = null): Nothing =
            throw FaultyVersionException(
                version,
                "Given version '$version' $info",
                cause
            )

        private enum class ParseState { UNINITIALIZED, NUMBER, DOT }
    }
}