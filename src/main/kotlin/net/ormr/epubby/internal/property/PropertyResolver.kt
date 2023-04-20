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

package net.ormr.epubby.internal.property

import dev.epubby.Epub3Feature
import dev.epubby.UnstableEpubFeature
import dev.epubby.prefix.PrefixMap
import dev.epubby.prefix.ReservedPrefix
import dev.epubby.prefix.UnknownPrefix
import dev.epubby.property.*

@OptIn(Epub3Feature::class, UnstableEpubFeature::class)
internal object PropertyResolver {
    private val manifest = buildMap<ManifestVocabulary>()
    private val metadataMeta = buildMap<MetadataMetaVocabulary>()
    private val metadataLink = buildMap<MetadataLinkVocabulary>()
    private val metadataLinkRel = buildMap<MetadataLinkRelVocabulary>()
    private val spine = buildMap<SpineVocabulary>()

    fun resolveManifest(property: PropertyModel, prefixes: PrefixMap): Property = property.fold(
        { fromVocab(manifest, it) },
        { prefix, ref -> toProperty(prefix, ref, prefixes) },
    )

    fun resolveMeta(property: PropertyModel, prefixes: PrefixMap): Property = property.fold(
        { fromVocab(metadataMeta, it) },
        { prefix, ref -> toProperty(prefix, ref, prefixes) },
    )

    fun resolveLink(property: PropertyModel, prefixes: PrefixMap): Property = property.fold(
        { fromVocab(metadataLink, it) },
        { prefix, ref -> toProperty(prefix, ref, prefixes) },
    )

    fun resolveLinkRel(property: PropertyModel, prefixes: PrefixMap): Relationship = property.fold(
        { fromVocab(metadataLinkRel, it) },
        { prefix, ref -> toProperty(prefix, ref, prefixes) },
    )

    fun resolveSpine(property: PropertyModel, prefixes: PrefixMap): Relationship = property.fold(
        { fromVocab(spine, it) },
        { prefix, ref -> toProperty(prefix, ref, prefixes) },
    )

    private fun fromVocab(entries: Map<String, Property>, ref: String): Property =
        entries[ref] ?: UnknownProperty(prefix = null, ref)

    private fun toProperty(prefix: String, reference: String, prefixes: PrefixMap): Property {
        // if the 'prefix' attribute defines a mapping to a reserved prefix, we must allow it to overwrite
        // that mapping, so we check the 'prefixes' before checking 'ReservedPrefix'.
        val foundPrefix = prefixes[prefix] ?: ReservedPrefix.fromNameOrNull(prefix)
        return foundPrefix?.let { Property(it, reference) } ?: UnknownProperty(UnknownPrefix(prefix), reference)
    }

    private inline fun <reified T> buildMap(): Map<String, T>
            where T : Enum<T>,
                  T : VocabularyProperty = enumValues<T>().associateByTo(hashMapOf()) { it.reference }

    private inline fun <T> PropertyModel.fold(
        noPrefix: (reference: String) -> T,
        withPrefix: (prefix: String, reference: String) -> T,
    ): T = when (prefix) {
        null -> noPrefix(reference)
        else -> withPrefix(prefix, reference)
    }
}