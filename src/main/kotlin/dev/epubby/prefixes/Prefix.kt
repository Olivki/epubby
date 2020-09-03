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

package dev.epubby.prefixes

import dev.epubby.BookVersion
import dev.epubby.internal.IntroducedIn
import org.jdom2.Verifier
import java.net.URI
import java.net.URISyntaxException

@IntroducedIn(version = BookVersion.EPUB_3_0)
interface Prefix {
    /**
     * The shorthand name used by properties when referring to the [uri] mapping that this prefix represents.
     */
    val title: String

    /**
     * TODO
     */
    val uri: URI

    /**
     * Whether or not this prefix is a reserved package prefix.
     *
     * @see [PackagePrefix]
     */
    val isReserved: Boolean

    companion object {
        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [prefix] is not a valid `NCName` or if is a reserved prefix or equal
         * to `"_"`
         */
        @JvmStatic
        fun of(prefix: String, uri: URI): Prefix {
            val verification = Verifier.checkElementName(prefix)
            require(verification == null) { verification }
            require(!PackagePrefix.isReservedPrefix(prefix)) { "'prefix' must not be a reserved prefix" }
            require(prefix != "_") { "'_' is a reserved prefix name" }
            return BasicPrefix(prefix, uri)
        }

        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [prefix] is blank or equal to `"_"`
         * @throws [URISyntaxException] if [uri] can not be parsed into a valid [URI]
         */
        @JvmStatic
        @Throws(URISyntaxException::class)
        fun of(prefix: String, uri: String): Prefix = of(prefix, URI(uri))
    }
}