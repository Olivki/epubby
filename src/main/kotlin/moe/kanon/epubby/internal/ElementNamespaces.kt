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

package moe.kanon.epubby.internal

import org.jdom2.Namespace

internal object ElementNamespaces {
    const val XML = "http://www.w3.org/XML/1998/namespace"

    const val META_INF_CONTAINER = "urn:oasis:names:tc:opendocument:xmlns:container"

    const val META_INF_METADATA = "http://www.idpf.org/2013/metadata"

    const val META_INF_MANIFEST = "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0"

    const val OPF = "http://www.idpf.org/2007/opf"

    const val DUBLIN_CORE = "http://purl.org/dc/elements/1.1/"

    const val DAISY_NCX = "http://www.daisy.org/z3986/2005/ncx/"
}