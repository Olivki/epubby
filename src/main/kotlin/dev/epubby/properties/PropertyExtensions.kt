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

@file:JvmName("PropertyUtils")

package dev.epubby.properties

import dev.epubby.internal.Patterns
import dev.epubby.internal.parser.PropertyParser
import dev.epubby.prefixes.Prefixes
import dev.epubby.properties.vocabularies.ManifestVocabulary
import dev.epubby.properties.vocabularies.MetadataLinkRelVocabulary
import dev.epubby.properties.vocabularies.MetadataLinkVocabulary
import dev.epubby.properties.vocabularies.MetadataMetaVocabulary
import dev.epubby.properties.vocabularies.SpineVocabulary

typealias Relationship = Property

@get:JvmSynthetic
internal val Property.isKnownInstance: Boolean
    get() = this is BasicProperty || this is ManifestVocabulary || this is MetadataLinkRelVocabulary
        || this is MetadataLinkVocabulary || this is MetadataMetaVocabulary || this is SpineVocabulary

@JvmSynthetic
internal fun requireKnown(property: Property) {
    if (!property.isKnownInstance) {
        throw IllegalArgumentException("Custom implementations of 'Property' are not supported (${property.javaClass})")
    }
}

fun Property.toStringForm(): String = when {
    prefix.title.isBlank() -> reference.toString()
    else -> "${prefix.title}:$reference"
}

// resolving single
@JvmSynthetic
internal fun resolveManifestProperty(input: String, prefixes: Prefixes): Property = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> ManifestVocabulary.fromReference(input)
}

@JvmSynthetic
internal fun resolveLinkProperty(input: String, prefixes: Prefixes): Property = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> MetadataLinkVocabulary.fromReference(input)
}

@JvmSynthetic
internal fun resolveLinkRelationship(input: String, prefixes: Prefixes): Relationship = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> MetadataLinkRelVocabulary.fromReference(input)
}

@JvmSynthetic
internal fun resolveMetaProperty(input: String, prefixes: Prefixes): Property = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> MetadataMetaVocabulary.fromReference(input)
}

@JvmSynthetic
internal fun resolveSpineProperty(input: String, prefixes: Prefixes): Property = when {
    ':' in input -> PropertyParser.parse(input, prefixes)
    else -> SpineVocabulary.fromReference(input)
}

// resolving many
@JvmSynthetic
internal fun resolveManifestProperties(input: String, prefixes: Prefixes): Properties {
    val props = input.replace(Patterns.EXCESSIVE_WHITESPACE, "")
        .splitToSequence(' ')
        .map { resolveManifestProperty(input, prefixes) }
    return Properties.copyOf(props.asIterable())
}

@JvmSynthetic
internal fun resolveLinkProperties(input: String, prefixes: Prefixes): Properties {
    val props = input.replace(Patterns.EXCESSIVE_WHITESPACE, "")
        .splitToSequence(' ')
        .map { resolveLinkProperty(input, prefixes) }
    return Properties.copyOf(props.asIterable())
}

@JvmSynthetic
internal fun resolveMetaProperties(input: String, prefixes: Prefixes): Properties {
    val props = input.replace(Patterns.EXCESSIVE_WHITESPACE, "")
        .splitToSequence(' ')
        .map { resolveMetaProperty(input, prefixes) }
    return Properties.copyOf(props.asIterable())
}

@JvmSynthetic
internal fun resolveSpineProperties(input: String, prefixes: Prefixes): Properties {
    val props = input.replace(Patterns.EXCESSIVE_WHITESPACE, "")
        .splitToSequence(' ')
        .map { resolveSpineProperty(input, prefixes) }
    return Properties.copyOf(props.asIterable())
}