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

@file:Suppress("FunctionName", "UnnecessaryOptInAnnotation")

package dev.epubby.prefix

import dev.epubby.Epub3Feature
import dev.epubby.UnstableEpubFeature
import net.ormr.epubby.internal.util.requireValidElementName
import org.xbib.net.IRI

// https://www.w3.org/publishing/epub3/epub-packages.html#sec-prefix-attr
@Epub3Feature
public sealed interface Prefix {
    /**
     * The shorthand name used by properties when referring to the [iri] mapping that this prefix represents.
     *
     * May be `null` if the prefix is from the [default vocabulary][VocabularyPrefix].
     */
    public val name: String?

    /**
     * The iri used for resolving property references.
     *
     * May be `null` if the prefix is [unknown][UnknownPrefix].
     */
    public val iri: IRI?

    /**
     * Returns `true` if the [name] and [iri] of [other] matches that of `this` prefix, otherwise `false`.
     */
    public infix fun matches(other: Prefix?): Boolean = when {
        other == null -> false
        name != other.name -> false
        iri != other.iri -> false
        else -> true
    }

    public fun asString(): String
}

/**
 * TODO
 *
 * @throws [IllegalArgumentException] if [name] is not a valid `NCName` or if it is a
 * [reserved name][ReservedPrefix.isReservedName] or equal to `_`
 */
@Epub3Feature
@OptIn(UnstableEpubFeature::class)
public fun Prefix(name: String, iri: IRI): MappedPrefix {
    requireValidElementName(name)
    require(!ReservedPrefix.isReservedName(name)) { "'name' should not be a reserved name" }
    require(name != "_") { "'name' should not be '_'" }
    return PrefixImpl(name, iri)
}