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

package dev.epubby.prefix

import dev.epubby.Epub
import dev.epubby.Epub3Feature
import dev.epubby.UnstableEpubFeature
import dev.epubby.property.Property
import net.ormr.epubby.internal.util.getChildObjects

/**
 * Represents a [Prefix] that is reserved by the EPUB specification.
 *
 * This prefix can be used in any [Property] without needing to have it defined in the
 * [prefixes][PackageDocument.prefixes] property of the [PackageDocument] of the [Epub].
 *
 * Any entry defined here basically serves as an inbuilt prefix that all EPUB readers should be able to understand.
 *
 * Usage of these entries should be done with care, as they may be removed, or completely changed, with each
 * new version of the EPUB specification.
 */
@Epub3Feature
@UnstableEpubFeature
public sealed class ReservedPrefix(override val name: String, override val uri: String) : Prefix {
    public object A11y : ReservedPrefix("a11y", "http://www.idpf.org/epub/vocab/package/a11y/#")
    public object DcTerms : ReservedPrefix("dcterms", "http://purl.org/dc/terms/") // TODO: is the actual prefix 'dc'?
    public object Marc : ReservedPrefix("marc", "http://id.loc.gov/vocabulary/")
    public object Media : ReservedPrefix("media", "http://www.idpf.org/epub/vocab/overlays/#")
    public object Onix : ReservedPrefix("onix", "http://www.editeur.org/ONIX/epub/codelists/current.html#")
    public object Rendition : ReservedPrefix("rendition", "http://www.idpf.org/vocab/rendition/#")
    public object Schema : ReservedPrefix("schema", "http://schema.org/")
    public object Xsd : ReservedPrefix("xsd", "http://www.w3.org/2001/XMLSchema#")

    public companion object {
        private val nameToPrefix by lazy { getChildObjects<ReservedPrefix>().associateByTo(hashMapOf()) { it.name } }

        public fun fromNameOrNull(name: String): ReservedPrefix? = nameToPrefix[name]

        @JvmName("isReservedName")
        public operator fun contains(name: String): Boolean = name in nameToPrefix
    }
}