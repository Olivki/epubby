/*
 * Copyright 2019-2023 Oliver Berg
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

@file:Suppress("FunctionName")

package dev.epubby.property

import dev.epubby.Epub3Feature
import dev.epubby.prefix.Prefix
import dev.epubby.prefix.ResolvedPrefix
import org.xbib.net.IRI

// https://www.w3.org/publishing/epub3/epub-packages.html#sec-property-datatype
@Epub3Feature
public sealed interface Property {
    /**
     * The prefix of the property.
     *
     * Used when [processing][process] the property.
     */
    public val prefix: Prefix?

    /**
     * The name of the entry that the property is referring to.
     */
    public val reference: String

    /**
     * Returns the result of resolving [reference] against [prefix.iri][Prefix.iri], or `null` if the property is
     * [unknown][UnknownProperty].
     *
     * See [4.2.5.2 Processing](https://www.w3.org/publishing/epub3/epub-packages.html#sec-property-processing).
     *
     * @see [IRI.resolve]
     */
    public fun process(): IRI?

    /**
     * Returns `true` if the [prefix] and [reference] of [other] matches that of `this` property, otherwise `false`.
     */
    public infix fun matches(other: Property): Boolean = when (this) {
        is ResolvedProperty -> when {
            !(prefix matches other.prefix) -> false
            reference != other.reference -> false
            else -> true
        }
        else -> when {
            reference != other.reference -> false
            else -> true
        }
    }

    public fun asString(): String = when (val name = prefix?.name) {
        null -> reference
        else -> "$name:$reference"
    }
}

// TODO: use 'of' factory function instead?
@Epub3Feature
public fun Property(prefix: ResolvedPrefix, reference: String): ResolvedProperty = PropertyImpl(prefix, reference)