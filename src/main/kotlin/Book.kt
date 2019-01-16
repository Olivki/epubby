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

@file:Suppress("MemberVisibilityCanBePrivate")

package moe.kanon.epubby

import net.swiftzer.semver.SemVer
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger
import java.io.IOException
import java.nio.file.Path

// TODO: Add DSL builder for book settings
// TODO: Add DSL builder for creating new epubs from nothing, builder will be for initial settings like name and author.

public data class Book(public val file: Path) {
    
    /**
     * The [logger][KotlinLogger] instance used for any and all logging done by epubby.
     */
    internal val logger: KotlinLogger = logger("epubby")
    
    /**
     * The format version used by this EPUB.
     */
    public var version: Version = Version.UNKNOWN
        internal set(value) = when (value.format) {
            Version.Format.NOT_SUPPORTED -> throw BookVersionException(value.semantic)
            else -> field = value
        }
    
    companion object {
        
        @JvmStatic
        @Throws(IOException::class, BookReadException::class)
        public fun from(epub: Path): Book {
            TODO("Implement factory method.")
        }
        
        @JvmStatic
        @Throws(IOException::class, BookWriteException::class)
        public fun to(directory: Path): Book {
            TODO("Implement factory method.")
        }
    }
    
    /**
     * A data class holding information about the version of this EPUB.
     */
    public class Version(_semVer: String) {
        
        internal constructor(format: Format) : this(format.version.toString())
        
        /**
         * The [semantic version][SemVer] instance.
         */
        public val semantic: SemVer = SemVer.parse(_semVer)
        
        /**
         * The closest matching [version format][Format].
         */
        public val format: Format = Format.from(semantic)
        
        /**
         * Compares this [version][Version] to the [other] version and returns which one is the bigger.
         */
        public operator fun compareTo(other: Version): Int = semantic.compareTo(other.semantic)
        
        companion object {
            
            /**
             * Represents an unknown version.
             */
            @JvmStatic public val UNKNOWN: Version = Version(Format.UNKNOWN)
        }
        
        /**
         * Represents the format version of the epub.
         *
         * This enum is used to sort the ebook into a certain "version category".
         *
         * This is important to know because there are generally pretty large differences between the formats between
         * major versions, and trying to parse, say, an epub in the v3 format with v2 format rules will end up with
         * a slightly corrupted file.
         *
         * So it's safer to just loudly fail and alert the user that this version is not supported rather than greedily
         * trying to make something that this application isn't built for do.
         *
         * @property version The lowest supported version number for this version format.
         *
         * ie. If a book is found to be using `v3.1` of the format, it will be assigned the [EPUB_3][Format.EPUB_3]
         * version, but if it is using `v4.x` it will be assigned the [NOT_SUPPORTED][Format.NOT_SUPPORTED] version.
         */
        public enum class Format(public val version: SemVer) {
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
                 * Returns the [Format] with the closest matching version to the supplied [semVer].
                 */
                public fun from(semVer: SemVer): Format = when {
                    semVer >= EPUB_2.version && semVer < EPUB_3.version -> EPUB_2
                    // if version is equals to or greater than 3.0.0 AND version is less than 4.0.0
                    semVer >= EPUB_3.version && semVer < NOT_SUPPORTED.version -> EPUB_3
                    semVer >= NOT_SUPPORTED.version -> NOT_SUPPORTED
                    else -> UNKNOWN
                }
            }
        }
    }
}