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

import moe.kanon.epubby.metainf.MetaInfContainer
import moe.kanon.epubby.packages.Manifest
import moe.kanon.epubby.packages.Metadata
import moe.kanon.epubby.packages.Spine
import moe.kanon.epubby.structs.props.vocabs.ManifestVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataLinkRelVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataLinkVocabulary
import moe.kanon.epubby.structs.props.vocabs.MetadataMetaVocabulary
import moe.kanon.epubby.structs.props.vocabs.SpineVocabulary
import moe.kanon.epubby.structs.props.vocabs.VocabularyMode
import org.jdom2.Attribute
import org.jdom2.Namespace
import java.net.URL
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
interface Property {
    val prefix: PropertyPrefix

    val reference: String

    /**
     * Returns a new `property` attribute containing the value represented by this property.
     */
    @JvmDefault
    fun toAttribute(name: String = "property", namespace: Namespace = Namespace.NO_NAMESPACE): Attribute =
        Attribute(name, reference, namespace)

    /**
     * [Processes](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-property-processing)
     * this property into a [URL] and returns the result.
     */
    @JvmDefault
    fun process(): URL = URL("${prefix.url}$reference")

    companion object {
        @JvmStatic
        fun of(prefix: PropertyPrefix, reference: String): Property = BasicProperty(prefix, reference)

        @JvmSynthetic
        internal fun parse(
            caller: KClass<*>,
            input: String,
            mode: VocabularyMode = VocabularyMode.PROPERTY
        ): Property = if (':' in input) {
            val (prefix, reference) = input.split(':')
            of(PackagePrefix.fromPrefix(prefix), reference)
        } else when (caller) {
            Manifest::class -> ManifestVocabulary.fromReference(input) as Property
            Metadata.Link::class -> when (mode) {
                VocabularyMode.PROPERTY -> MetadataLinkVocabulary.fromReference(input) as Property
                VocabularyMode.RELATION -> MetadataLinkRelVocabulary.fromReference(input) as Property
            }
            Metadata.Meta::class -> MetadataMetaVocabulary.fromReference(input) as Property
            MetaInfContainer.Link::class -> MetadataLinkRelVocabulary.fromReference(input) as Property
            Spine.ItemReference::class -> SpineVocabulary.fromReference(input) as Property
            else -> throw IllegalArgumentException("Caller <$caller> does not have any known vocabularies")
        }
    }
}