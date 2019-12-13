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

package moe.kanon.epubby.structs.prefixes

import moe.kanon.epubby.structs.props.Property
import moe.kanon.kommons.requireThat
import java.net.URI
import java.net.URISyntaxException

/**
 * Represents a prefix to IRI mapping.
 */
interface Prefix {
    /**
     * The shorthand name used to refer to the underlying [uri] when [processing][Property.process] a property.
     */
    val prefix: String

    /**
     * The [IRI] that this prefix maps to.
     */
    val uri: URI

    @JvmDefault
    fun toStringForm(): String = when {
        prefix.isBlank() -> uri.toString()
        else -> "$prefix: $uri"
    }

    companion object {
        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [prefix] is [blank][String.isBlank] or equal to `"_"`
         */
        @JvmStatic
        fun of(prefix: String, uri: URI): Prefix {
            requireThat(prefix.isNotBlank()) { "expected 'prefix' to not be blank" }
            requireThat(prefix != "_") { "'_' is not an allowed prefix name" }
            return BasicPrefix(prefix, uri)
        }

        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [prefix] is [blank][String.isBlank] or equal to `"_"`
         */
        @JvmStatic
        @Throws(URISyntaxException::class)
        fun of(prefix: String, uri: String): Prefix = of(prefix, URI(uri))

        @JvmSynthetic
        internal fun forVocabulary(iri: String): Prefix = BasicPrefix("", URI.create(iri))
    }
}