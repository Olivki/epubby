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

package moe.kanon.epubby

import moe.kanon.kommons.requireThat

enum class BookVersion(val major: Int, val minor: Int) {
    /**
     * Represents the [EPUB 2.0](http://www.idpf.org/epub/dir/#epub201) format.
     */
    EPUB_2_0(2, 0),
    /**
     * Represents the [EPUB 3.0](http://www.idpf.org/epub/dir/#epub301) format.
     */
    EPUB_3_0(3, 0),
    /**
     * Represents the [EPUB 3.1](http://www.idpf.org/epub/dir/#epub31) format.
     *
     * The EPUB 3.1 format is [officially discouraged](http://www.idpf.org/epub/dir/#epub31) from use, and as such
     * the format is explicitly ***not*** supported by epubby, and it should never be used.
     */
    EPUB_3_1(3, 1), // TODO: log that this version is officially discouraged from use by the maintainers if a book uses
    //                       this version
    /**
     * Represents the [EPUB 3.2](http://www.idpf.org/epub/dir/#epub32) format.
     */
    EPUB_3_2(3, 2);

    /**
     * Returns `true` if `this` version is the oldest version supported by epubby, otherwise `false`.
     */
    val isOldest: Boolean
        get() = this == values().first()

    /**
     * Returns `true` if `this` version is the newest version supported by epubby, otherwise `false`.
     */
    val isNewest: Boolean
        get() = this == values().last()

    /**
     * Returns `true` if `this` version is newer than the [other] version, otherwise `false`.
     */
    infix fun isNewerThan(other: BookVersion): Boolean = compareAgainst(other) > 0

    /**
     * Returns `true` if `this` version is older than the [other] version, otherwise `false`.
     */
    infix fun isOlderThan(other: BookVersion): Boolean = compareAgainst(other) < 0

    /**
     * Acts the same as the [compareTo] function, except that it works on the [major] & [minor] versions of the
     * version instances.
     */
    fun compareAgainst(other: BookVersion): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        else -> 0
    }

    // TODO: this explanation is awkward, make a better one
    /**
     * Returns a string representing this version where [major] is the prefix, and [minor] is the suffix, they're
     * separated by a `'.'`.
     */
    override fun toString(): String = "$major.$minor"

    internal companion object {
        private fun fromInteger(major: Int, minor: Int): BookVersion =
            values().firstOrNull { it.major == major && it.minor == minor }
                ?: throw UnknownBookVersionException("$major.$minor")

        @JvmSynthetic
        internal fun fromString(version: String): _BookVersion {
            requireThat(version.isNotBlank()) { "expected 'version' to not be blank" }
            requireThat(version.first().isDigit()) { "expected 'version' to start with a digit (0-9); '$version'" }
            requireThat(version.last().isDigit()) { "expected 'version' to end with a digit (0-9); '$version'" }
            requireThat(version.count { it == '.' } == 1) { "expected 'version' to contain exactly one '.' character; '$version'" }
            requireThat(version.all { it in '0'..'9' || it == '.' }) { "'version' contains illegal character, allowed characters are '0'..'9' and '.'; $version" }

            val parts = version.split('.')

            val major = try {
                parts[0].toInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("'major' part <${parts[0]}> of version <$version> is not an integer", e)
            }

            val minor = try {
                parts[1].toInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("'minor' part <${parts[1]}> of version <$version> is not an integer", e)
            }

            return _BookVersion.fromInteger(major, minor)
        }
    }
}