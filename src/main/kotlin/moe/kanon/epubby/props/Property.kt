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

package moe.kanon.epubby.props

import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.internal.NewFeature
import moe.kanon.epubby.prefixes.PackagePrefix
import moe.kanon.epubby.prefixes.Prefix
import moe.kanon.epubby.prefixes.Prefixes
import moe.kanon.kommons.collections.getOrThrow
import moe.kanon.kommons.requireThat
import java.net.URI

@NewFeature(since = BookVersion.EPUB_3_0)
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
    val reference: String

    /**
     * Returns the result of resolving [reference] against the `uri` of the [prefix] of `this` property.
     */
    @JvmDefault
    fun process(): URI = prefix.uri.resolve(reference)

    companion object {
        @JvmStatic
        fun of(prefix: Prefix, reference: String): Property = BasicProperty(prefix, reference)

        // TODO: documentation
        @JvmStatic
        @JvmOverloads
        fun parse(input: String, prefixes: Prefixes = Prefixes.EMPTY): Property {
            requireThat(':' in input, "':' in input")
            val (rawPrefix, reference) = input.split(':')
            val prefix = PackagePrefix.fromPrefixOrNull(rawPrefix)
                ?: prefixes.getOrThrow(rawPrefix) { "Unknown prefix '$rawPrefix'." }
            return of(prefix, reference)
        }
    }
}