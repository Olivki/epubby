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

package moe.kanon.epubby.props

import moe.kanon.epubby.internal.Patterns
import moe.kanon.epubby.prefixes.Prefixes
import moe.kanon.epubby.props.vocabs.ManifestVocabulary
import moe.kanon.epubby.props.vocabs.MetadataLinkRelVocabulary
import moe.kanon.epubby.props.vocabs.MetadataLinkVocabulary
import moe.kanon.epubby.props.vocabs.MetadataMetaVocabulary
import moe.kanon.epubby.props.vocabs.SpineVocabulary

typealias Relationship = Property

fun Property.toStringForm(): String = when {
    prefix.prefix.isBlank() -> reference
    else -> "${prefix.prefix}:$reference"
}

// resolving single
@JvmSynthetic
internal fun resolveManifestProperty(input: String, prefixes: Prefixes): Property = when {
    ':' in input -> Property.parse(input, prefixes)
    else -> ManifestVocabulary.fromReference(input)
}

@JvmSynthetic
internal fun resolveLinkProperty(input: String, prefixes: Prefixes): Property = when {
    ':' in input -> Property.parse(input, prefixes)
    else -> MetadataLinkVocabulary.fromReference(input)
}

@JvmSynthetic
internal fun resolveLinkRelationship(input: String, prefixes: Prefixes): Relationship = when {
    ':' in input -> Property.parse(input, prefixes)
    else -> MetadataLinkRelVocabulary.fromReference(input)
}

@JvmSynthetic
internal fun resolveMetaProperty(input: String, prefixes: Prefixes): Property = when {
    ':' in input -> Property.parse(input, prefixes)
    else -> MetadataMetaVocabulary.fromReference(input)
}

@JvmSynthetic
internal fun resolveSpineProperty(input: String, prefixes: Prefixes): Property = when {
    ':' in input -> Property.parse(input, prefixes)
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