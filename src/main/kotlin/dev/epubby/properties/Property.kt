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

package dev.epubby.properties

import dev.epubby.EpubVersion
import dev.epubby.internal.IntroducedIn
import dev.epubby.prefixes.Prefix
import dev.epubby.prefixes.requireKnown
import java.net.URI
import java.net.URISyntaxException

@IntroducedIn(version = EpubVersion.EPUB_3_0)
interface Property {
    /**
     * The prefix of `this` property.
     *
     * Used when [processing][process] `this` property.
     */
    val prefix: Prefix

    /**
     * The entry that `this` property is referring to.
     */
    val reference: URI

    /**
     * Returns the result of resolving [reference] against the `uri` of the [prefix] of `this` property.
     */
    @JvmDefault
    fun process(): URI = prefix.uri.resolve(reference)

    companion object {
        @JvmStatic
        fun of(prefix: Prefix, reference: URI): Property {
            requireKnown(prefix)
            return BasicProperty(prefix, reference)
        }

        @JvmStatic
        @Throws(URISyntaxException::class)
        fun of(prefix: Prefix, reference: String): Property = of(prefix, URI(reference))
    }
}