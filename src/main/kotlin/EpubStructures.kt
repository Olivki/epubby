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

@file:JvmName("EpubUtils")

package moe.kanon.epubby

import net.swiftzer.semver.SemVer

// This is just general epub utilities.

/**
 * Represents the **major** version formats available for epubs as of creation. (2019-01-23).
 *
 * This enum is used to sort the epub into a certain "version category".
 *
 * This is important to know because there are generally pretty large differences between the formats between
 * major versions, and trying to parse, say, an epub in the v3 format with v2 format rules will end up with
 * a slightly corrupted file.
 *
 * So it's safer to just loudly fail and alert the user that this version is not supported rather than greedily
 * trying to still parse a document that most likely has features this library wasn't built to process.
 *
 * @property version The lowest supported version number for this version format.
 *
 * ie. If a book is found to be using `v3.1` of the format, it will be assigned the [EPUB_3][EpubFormat.EPUB_3]
 * version, but if it is using `v4.x` it will be assigned the [NOT_SUPPORTED][EpubFormat.NOT_SUPPORTED] version.
 */
public enum class EpubFormat(public val version: SemVer) {
    /**
     * Represents an unknown epub format version.
     *
     * This *should* generally only be available during startup when [Book.version] has not been assigned a proper
     * version yet.
     */
    UNKNOWN(SemVer(0, 0, 0)),
    
    /**
     * Represents the [EPUB 2.x.x](http://idpf.org/epub/201) format versions.
     */
    EPUB_2(SemVer(2, 0, 0)),
    
    /**
     * Represents the [EPUB 3.x.x](http://idpf.org/epub/30) format versions.
     */
    EPUB_3(SemVer(3, 0, 0)),
    
    /**
     * Represents an unsupported EPUB format version.
     *
     * This should always be set to the highest **major** version number that's not supported by epubby.
     * Generally this will be an unreleased version of the EPUB format, unless support for this library has been
     * dropped.
     *
     * Currently this is set to react on any `v4.x.x` versions.
     */
    NOT_SUPPORTED(SemVer(4, 0, 0));
    
    companion object {
        
        /**
         * Returns the [EpubFormat] with the closest matching version to the supplied [version].
         */
        @JvmStatic
        public fun from(version: SemVer): EpubFormat = when {
            version >= EPUB_2.version && version < EPUB_3.version -> EPUB_2
            // if version is equals to or greater than 3.0.0 AND version is less than 4.0.0
            version >= EPUB_3.version && version < NOT_SUPPORTED.version -> EPUB_3
            version >= NOT_SUPPORTED.version -> NOT_SUPPORTED
            else -> UNKNOWN
        }
    }
}