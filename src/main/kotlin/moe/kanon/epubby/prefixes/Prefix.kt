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

package moe.kanon.epubby.prefixes

import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.internal.NewFeature
import moe.kanon.kommons.requireThat
import java.net.URI
import java.net.URISyntaxException

@NewFeature(since = BookVersion.EPUB_3_0)
interface Prefix {
    /**
     * The shorthand name used by properties when referring to the [uri] mapping.
     */
    val prefix: String

    /**
     * TODO
     */
    val uri: URI

    companion object {
        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [prefix] is [blank][String.isBlank] or equal to `"_"`
         */
        @JvmStatic
        fun of(prefix: String, uri: URI): Prefix {
            requireThat(prefix.isNotBlank()) { "expected 'prefix' to not be blank" }
            requireThat(prefix != "_") { "'_' is a reserved prefix name and can therefore not be used" }
            return BasicPrefix(prefix, uri)
        }

        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [prefix] is [blank][String.isBlank] or equal to `"_"`
         * @throws [URISyntaxException] if [uri] can not be parsed into a valid [URI]
         */
        @JvmStatic
        @Throws(URISyntaxException::class)
        fun of(prefix: String, uri: String): Prefix = of(prefix, URI(uri))
    }
}