/*
 * Copyright 2019-2022 Oliver Berg
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

import dev.epubby.EpubVersion
import dev.epubby.internal.IntroducedIn
import org.jdom2.Verifier
import java.net.URI
import java.net.URISyntaxException

@IntroducedIn(version = EpubVersion.EPUB_3_0)
sealed interface Prefix {
    /**
     * The shorthand name used by properties when referring to the [uri] mapping that this prefix represents.
     */
    val title: String

    /**
     * TODO
     */
    val uri: URI

    /**
     * Whether this prefix is a reserved package prefix.
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
        fun of(prefix: String, uri: URI): Prefix {
            val verification = Verifier.checkElementName(prefix)
            require(verification == null) { verification }
            require(!PackagePrefix.isReservedPrefix(prefix)) { "'prefix' must not be a reserved prefix" }
            require(prefix != "_") { "'_' is a reserved prefix name" }
            return PrefixImpl(prefix, uri)
        }

        /**
         * TODO
         *
         * @throws [IllegalArgumentException] if [prefix] is blank or equal to `"_"`
         * @throws [URISyntaxException] if [uri] can not be parsed into a valid [URI]
         */
        // TODO: replace with 'Result<Prefix>'
        fun of(prefix: String, uri: String): Prefix = of(prefix, URI(uri))
    }
}

internal data class PrefixImpl(override val title: String, override val uri: URI) : Prefix {
    override val isReserved: Boolean
        get() = false
}

internal data class VocabularyPrefix internal constructor(override val uri: URI) : Prefix {
    override val title: String = ""

    override val isReserved: Boolean
        get() = false
}