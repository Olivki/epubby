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

package moe.kanon.epubby.structs.props

import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.NewFeature
import moe.kanon.epubby.metainf.MetaInfContainer
import moe.kanon.epubby.packages.PackageManifest
import moe.kanon.epubby.packages.PackageMetadata
import moe.kanon.epubby.packages.PackageSpine
import moe.kanon.epubby.structs.prefixes.PackagePrefix
import moe.kanon.epubby.structs.prefixes.Prefix
import moe.kanon.epubby.structs.prefixes.Prefixes
import moe.kanon.epubby.structs.props.vocabs.ManifestVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataLinkRelVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataLinkVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataMetaVocabulary
import moe.kanon.epubby.structs.props.vocabs.SpineVocabulary
import moe.kanon.epubby.structs.props.vocabs.VocabularyParseMode
import moe.kanon.kommons.collections.getOrThrow
import java.net.URI
import kotlin.reflect.KClass

/**
 * Represents the [property data-type](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-property-datatype).
 *
 * It is a compact means of expressing an [IRI](https://tools.ietf.org/html/rfc3987).
 *
 * The data type is derived from the CURIE data type defined in  [RDFA-CORE](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#bib-rdfa-core),
 * and represents a subset of CURIEs.
 *
 * @see [BasicProperty]
 */
// this is kept as an interface in case a user wants to create their own vocabulary implementation with an enum in Java
// as Java does not allow enums to extend abstract classes
@NewFeature(since = BookVersion.EPUB_3_0)
interface Property {
    /**
     * The prefix that this property is prefixed with.
     *
     * This is used when [proccessing][process] this property into a IRI.
     */
    val prefix: Prefix

    /**
     * The entry that this property is referring to.
     */
    val reference: String

    /**
     * Returns the result of resolve the [uri][Prefix.uri] of the [prefix] against the [reference] of this property.
     *
     * The operation is done in the exact way defined in the [property processing](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-property-processing)
     * section of the EPUB 3.2 spec.
     */
    @JvmDefault
    fun process(): URI = prefix.uri.resolve(reference)//URL("${prefix.iri}$reference")

    companion object {
        @JvmStatic
        fun of(prefix: Prefix, reference: String): Property = BasicProperty(prefix, reference)

        @JvmSynthetic
        internal fun parse(
            caller: KClass<*>,
            input: String,
            prefixes: Prefixes,
            mode: VocabularyParseMode = VocabularyParseMode.PROPERTY
        ): Property = when {
            ':' in input -> {
                val prefix = input.substringBefore(':').let {
                    PackagePrefix.getByPrefixOrNull(it) ?: prefixes.getOrThrow(it) { "Unknown prefix '$it'" }
                }
                val reference = input.substringAfter(':')
                of(prefix, reference)
            }
            else -> when (caller) {
                // meta-inf
                MetaInfContainer.Link::class -> MetadataLinkRelVocabulary.fromReference(input) as Property
                // package-documents
                PackageManifest::class -> ManifestVocabulary.fromReference(input) as Property
                PackageMetadata.Link::class -> when (mode) {
                    VocabularyParseMode.PROPERTY -> MetadataLinkVocabulary.fromReference(input) as Property
                    VocabularyParseMode.RELATION -> MetadataLinkRelVocabulary.fromReference(input) as Property
                }
                PackageMetadata.OPF3Meta::class -> MetadataMetaVocabulary.fromReference(input) as Property
                PackageSpine.ItemReference::class -> SpineVocabulary.fromReference(input) as Property
                else -> throw IllegalArgumentException("Caller <$caller> does not have any known vocabularies")
            }
        }
    }
}