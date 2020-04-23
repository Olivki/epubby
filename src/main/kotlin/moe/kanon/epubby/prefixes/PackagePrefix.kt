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

package moe.kanon.epubby.prefixes

import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.internal.NewFeature
import moe.kanon.kommons.collections.getOrThrow
import java.net.URI

@NewFeature(since = BookVersion.EPUB_3_0)
enum class PackagePrefix(override val prefix: String, override val uri: URI) : Prefix {
    A11Y("a11y", URI.create("http://www.idpf.org/epub/vocab/package/a11y/#")),
    // TODO: is the actual prefix 'dc'?
    DC_TERMS("dcterms", URI.create("http://purl.org/dc/terms/")),
    MARC("marc", URI.create("http://id.loc.gov/vocabulary/")),
    MEDIA("media", URI.create("http://www.idpf.org/epub/vocab/overlays/#")),
    ONIX("onix", URI.create("http://www.editeur.org/ONIX/book/codelists/current.html#")),
    RENDITION("rendition", URI.create("http://www.idpf.org/vocab/rendition/#")),
    SCHEMA("schema", URI.create("http://schema.org/")),
    XSD("xsd", URI.create("http://www.w3.org/2001/XMLSchema#"));

    companion object {
        private val prefixToInstance = values().associateByTo(hashMapOf(), PackagePrefix::prefix)

        @JvmStatic
        fun fromPrefix(prefix: String): PackagePrefix =
            prefixToInstance.getOrThrow(prefix) { "'$prefix' is not a reserved prefix." }

        @JvmStatic
        fun fromPrefixOrNull(prefix: String): PackagePrefix? = prefixToInstance[prefix]
    }
}