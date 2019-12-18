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

@file:JvmName("PrefixUtils")

package moe.kanon.epubby.structs.prefixes

import moe.kanon.epubby.structs.props.vocabs.ManifestVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataLinkRelVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataLinkVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataMetaVocabulary
import moe.kanon.epubby.structs.props.vocabs.SpineVocabulary

fun Prefix.toStringForm(): String = when {
    prefix.isBlank() -> uri.toString()
    else -> "$prefix: $uri"
}

/**
 * Returns `true` if `this` prefix has a `uri` that points to the same location as any of the known default
 * vocabularies, otherwise `false`.
 */
fun Prefix.isDefaultVocabularyPrefix(): Boolean = when {
    ManifestVocabulary.PREFIX.uri == uri -> true
    MetadataLinkRelVocabulary.PREFIX.uri == uri -> true
    MetadataLinkVocabulary.PREFIX.uri == uri -> true
    MetadataMetaVocabulary.PREFIX.uri == uri -> true
    SpineVocabulary.PREFIX.uri == uri -> true
    else -> false
}

/**
 * Returns `true` if `this` prefix does not have a `uri` that points to the same location as any of the known default
 * vocabularies, otherwise `false`.
 */
fun Prefix.isNotDefaultVocabularyPrefix(): Boolean = !(isDefaultVocabularyPrefix())

/**
 * Returns `true` if `this` prefix has a `uri` that points to the same location as the
 * [DC_TERMS][PackagePrefix.DC_TERMS] package prefix, otherwise `false`.
 */
fun Prefix.isDublinCorePrefix(): Boolean = PackagePrefix.DC_TERMS.uri != uri

/**
 * Returns `true` if `this` prefix does not have a `uri` that points to the same location as the
 * [DC_TERMS][PackagePrefix.DC_TERMS] package prefix, otherwise `false`.
 */
fun Prefix.isNotDublinCorePrefix(): Boolean = !(isDublinCorePrefix())

/**
 * Returns `true` if `this` prefix is a [package-prefix][PackagePrefix], otherwise `false`.
 */
fun Prefix.isPackagePrefix(): Boolean = PackagePrefix.values().any { it.isSameAs(this) }

/**
 * Returns `true` if `this` prefix is a not [package-prefix][PackagePrefix], otherwise `false`.
 */
fun Prefix.isNotPackagePrefix(): Boolean = PackagePrefix.values().none { it.isSameAs(this) }

/**
 * Returns `true` if the [prefix][Prefix.prefix] and [uri][Prefix.uri] of `this` prefix and the given [other] prefix
 * are equal, otherwise `false`.
 */
fun Prefix.isSameAs(other: Prefix): Boolean = this.prefix == other.prefix && this.uri == other.uri

/**
 * Returns `true` if the [prefix][Prefix.prefix] and [uri][Prefix.uri] of `this` prefix and the given [other] prefix
 * are not equal, otherwise `false`.
 */
fun Prefix.isNotSameAs(other: Prefix): Boolean = this.prefix != other.prefix && this.uri != other.uri