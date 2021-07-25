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
import java.net.URI

@IntroducedIn(version = EpubVersion.EPUB_3_0)
sealed interface Property {
    /**
     * The prefix of `this` property.
     *
     * Used when [processing][process] `this` property.
     */
    val prefix: Prefix

    /**
     * The entry that `this` property is referring to.
     */
    // TODO: should this really be a URI? I feel like it shouldn't?
    val reference: URI

    /**
     * Returns the result of resolving [reference] against the `uri` of the [prefix] of `this` property.
     */
    fun process(): URI = prefix.uri.resolve(reference)

    companion object {
        fun of(prefix: Prefix, reference: URI): Property = PropertyImpl(prefix, reference)

        // TODO: Result<Property>
        fun of(prefix: Prefix, reference: String): Property = of(prefix, URI(reference))
    }
}

internal data class PropertyImpl internal constructor(
    override val prefix: Prefix,
    override val reference: URI,
) : Property