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

@file:JvmName("PropertyUtils")

package dev.epubby.properties

import dev.epubby.internal.Patterns
import dev.epubby.internal.parser.PropertyParser
import dev.epubby.prefixes.Prefixes

typealias Relationship = Property

// TODO: remove as we now have sealed interfaces
@get:JvmSynthetic
internal val Property.isKnownInstance: Boolean
    get() = this is PropertyImpl || this is ManifestVocabulary || this is MetadataLinkRelVocabulary
            || this is MetadataLinkVocabulary || this is MetadataMetaVocabulary || this is SpineVocabulary

// TODO: remove as we now have sealed interfaces
@JvmSynthetic
internal fun requireKnown(property: Property) {
    if (!property.isKnownInstance) {
        throw IllegalArgumentException("Custom implementations of 'Property' are not supported (${property.javaClass})")
    }
}

fun Property.encodeToString(): String = when {
    prefix.title.isBlank() -> reference.toString()
    else -> "${prefix.title}:$reference"
}

/**
 * Returns a string containing all the [property][Property] instances stored in this `properties`, separated by a
 * space.
 */
// TODO: change back to 'it.title' ?
fun Properties.encodeToString(): String = joinToString(separator = " ") { it.encodeToString() }

infix fun Property.matches(other: Property): Boolean = when {
    this.prefix != other.prefix -> false
    this.reference != other.reference -> false
    else -> true
}

// resolving single
@JvmSynthetic
internal fun resolveManifestProperty(input: String, prefixes: Prefixes): Property? = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> ManifestVocabulary.fromReferenceOrNull(input)
}

@JvmSynthetic
internal fun resolveLinkProperty(input: String, prefixes: Prefixes): Property? = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> MetadataLinkVocabulary.fromReferenceOrNull(input)
}

@JvmSynthetic
internal fun resolveLinkRelationship(input: String, prefixes: Prefixes): Relationship? = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> MetadataLinkRelVocabulary.fromReferenceOrNull(input)
}

@JvmSynthetic
internal fun resolveMetaProperty(input: String, prefixes: Prefixes): Property? = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> MetadataMetaVocabulary.fromReferenceOrNull(input)
}

@JvmSynthetic
internal fun resolveSpineProperty(input: String, prefixes: Prefixes): Property? = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> SpineVocabulary.fromReferenceOrNull(input)
}

// resolving many
@JvmSynthetic
internal fun resolveManifestProperties(input: String, prefixes: Prefixes): Properties =
    input.replace(Patterns.EXCESSIVE_WHITESPACE, "")
        .splitToSequence(' ')
        .map { resolveManifestProperty(input, prefixes) }
        .filterNotNull()
        .toProperties()

@JvmSynthetic
internal fun resolveLinkProperties(input: String, prefixes: Prefixes): Properties =
    input.replace(Patterns.EXCESSIVE_WHITESPACE, "")
        .splitToSequence(' ')
        .map { resolveLinkProperty(input, prefixes) }
        .filterNotNull()
        .toProperties()

@JvmSynthetic
internal fun resolveMetaProperties(input: String, prefixes: Prefixes): Properties =
    input.replace(Patterns.EXCESSIVE_WHITESPACE, "")
        .splitToSequence(' ')
        .map { resolveMetaProperty(input, prefixes) }
        .filterNotNull()
        .toProperties()

@JvmSynthetic
internal fun resolveSpineProperties(input: String, prefixes: Prefixes): Properties =
    input.replace(Patterns.EXCESSIVE_WHITESPACE, "")
        .splitToSequence(' ')
        .map { resolveSpineProperty(input, prefixes) }
        .filterNotNull()
        .toProperties()