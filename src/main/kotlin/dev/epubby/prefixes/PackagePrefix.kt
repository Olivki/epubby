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

package dev.epubby.prefixes

import dev.epubby.Epub
import dev.epubby.EpubVersion
import dev.epubby.internal.IntroducedIn
import dev.epubby.packages.PackageDocument
import dev.epubby.properties.Property
import kotlinx.collections.immutable.toPersistentHashMap
import krautils.collections.getOrThrow
import java.net.URI

/**
 * Represents a [Prefix] that is reserved by the EPUB specification.
 *
 * This prefix can be used in any [Property] without needing to have it defined in the
 * [prefixes][PackageDocument.prefixes] property of the [PackageDocument] of the [Epub].
 *
 * Any entry defined here basically serves as an inbuilt prefix that all EPUB readers should be able to understand.
 *
 * Usage of any of these entries should be done with care, as they may be removed, or completely changed, with each
 * new version of the EPUB specification.
 */
// TODO: fact check the last sentence in the documentation
// TODO: mark these with some 'Experimental' annotation so that the user is more aware that these entries are basically
//       seen as experimental by the EPUB specification?
@IntroducedIn(version = EpubVersion.EPUB_3_0)
enum class PackagePrefix(override val title: String, override val uri: URI) : Prefix {
    A11Y("a11y", URI.create("http://www.idpf.org/epub/vocab/package/a11y/#")),
    // TODO: is the actual prefix 'dc'?
    DC_TERMS("dcterms", URI.create("http://purl.org/dc/terms/")),
    MARC("marc", URI.create("http://id.loc.gov/vocabulary/")),
    MEDIA("media", URI.create("http://www.idpf.org/epub/vocab/overlays/#")),
    ONIX("onix", URI.create("http://www.editeur.org/ONIX/epub/codelists/current.html#")),
    RENDITION("rendition", URI.create("http://www.idpf.org/vocab/rendition/#")),
    SCHEMA("schema", URI.create("http://schema.org/")),
    XSD("xsd", URI.create("http://www.w3.org/2001/XMLSchema#"));

    override val isReserved: Boolean
        get() = true

    companion object {
        private val ENTRIES: Map<String, PackagePrefix> = values().associateBy { it.title }.toPersistentHashMap()

        @JvmStatic
        fun fromPrefix(prefix: String): PackagePrefix =
            ENTRIES.getOrThrow(prefix) { "'$prefix' is not a reserved prefix." }

        @JvmStatic
        fun fromPrefixOrNull(prefix: String): PackagePrefix? = ENTRIES[prefix]

        /**
         * Returns `true` if the given [prefix] is a reserved prefix, otherwise `false`.
         */
        @JvmStatic
        fun isReservedPrefix(prefix: String): Boolean = prefix in ENTRIES
    }
}