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

package moe.kanon.epubby.structs.prefixes

import java.net.URI

/**
 * Represents all the [reserved property prefixes](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-metadata-reserved-prefixes)
 * for the `package-document`
 *
 * ### Warning
 *
 * Although reserved prefixes are an authoring convenience, reliance on them can lead to interoperability
 * issues. Validation tools will often reject new prefixes until the tools are updated, for example. Authors
 * are strongly encouraged to declare all prefixes they use to avoid such issues.
 */
// TODO: Add enums for easy creation of properties for things that are tied here?
enum class PackagePrefix(override val prefix: String, override val uri: URI) : Prefix {
    A11Y("a11y", URI.create("http://www.idpf.org/epub/vocab/package/a11y/#")),
    DC_TERMS("dcterms", URI.create("http://purl.org/dc/terms/")),
    MARC("marc", URI.create("http://id.loc.gov/vocabulary/")),
    MEDIA("media", URI.create("http://www.idpf.org/epub/vocab/overlays/#")),
    ONIX("onix", URI.create("http://www.editeur.org/ONIX/book/codelists/current.html#")),
    RENDITION("rendition", URI.create("http://www.idpf.org/vocab/rendition/#")),
    SCHEMA("schema", URI.create("http://schema.org/")),
    XSD("xsd", URI.create("http://www.w3.org/2001/XMLSchema#"));

    override fun toString(): String = "$prefix: $uri"

    companion object {
        @JvmStatic
        fun getByPrefix(prefix: String): PackagePrefix = getByPrefixOrNull(prefix)
            ?: throw NoSuchElementException("Prefix '$prefix' does not match any of the reserved package document prefixes")

        @JvmStatic
        fun getByPrefixOrNull(prefix: String): PackagePrefix? = values().firstOrNull { it.prefix == prefix }
    }
}