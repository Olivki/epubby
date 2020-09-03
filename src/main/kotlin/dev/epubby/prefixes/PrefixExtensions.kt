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

@file:JvmName("PrefixUtils")

package dev.epubby.prefixes

import dev.epubby.properties.vocabularies.VocabularyPrefixes.MANIFEST
import dev.epubby.properties.vocabularies.VocabularyPrefixes.METADATA_LINK
import dev.epubby.properties.vocabularies.VocabularyPrefixes.METADATA_META
import dev.epubby.properties.vocabularies.VocabularyPrefixes.SPINE

/**
 * Returns `true` if `this` prefix is an implementation that is known by Epubby, otherwise `false`.
 */
val Prefix.isKnownInstance: Boolean
    get() = this is BasicPrefix || this is PackagePrefix || this is VocabularyPrefix

@JvmSynthetic
internal fun requireKnown(prefix: Prefix) {
    if (!prefix.isKnownInstance) {
        throw IllegalArgumentException("Custom implementations of 'Prefix' are not supported. (${prefix.javaClass})")
    }
}

/**
 * Returns the form used for serializing `this` prefix back into XML.
 */
fun Prefix.encodeToString(): String = when {
    title.isBlank() -> uri.toString()
    else -> "$title: $uri"
}

// TODO: unsure if this the correct form to output this to
fun Prefixes.encodeToString(): String = values.joinToString(separator = " ", transform = Prefix::encodeToString)

/**
 * Returns `true` if `this` prefix has a `uri` that points to the same location as any of the known default
 * vocabularies, otherwise `false`.
 */
fun Prefix.isDefaultVocabularyPrefix(): Boolean = when {
    MANIFEST.uri == uri -> true
    METADATA_LINK.uri == uri -> true
    METADATA_LINK.uri == uri -> true
    METADATA_META.uri == uri -> true
    SPINE.uri == uri -> true
    else -> false
}

// TODO: replace the dublin-core prefix uri checks with 'isSameAs' and 'isNotSameAs' checks?

/**
 * Returns `true` if `this` prefix has a `uri` that points to the same location as the
 * [DC_TERMS][PackagePrefix.DC_TERMS] package prefix, otherwise `false`.
 */
fun Prefix.isDublinCorePrefix(): Boolean = this.uri == PackagePrefix.DC_TERMS.uri

/**
 * Returns `true` if [this] prefix has a [title][Prefix.title] that is a
 * [reserved prefix][PackagePrefix.isReservedPrefix], otherwise `false`.
 */
fun Prefix.isPackagePrefix(): Boolean = PackagePrefix.isReservedPrefix(title)

/**
 * Returns `true` if the `prefix` and `uri` of `this` are the same as those of [other], otherwise `false`.
 */
infix fun Prefix.isSameAs(other: Prefix): Boolean = this.title == other.title && this.uri == other.uri

/**
 * Returns `true` if the `prefix` and `uri` of `this` are not the same as those of [other], otherwise `false`.
 */
infix fun Prefix.isNotSameAs(other: Prefix): Boolean = this.title != other.title && this.uri != other.uri