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

package net.ormr.epubby.internal

internal object Namespaces {
    const val DUBLIN_CORE_PREFIX = "dc"
    const val DUBLIN_CORE_URI = "http://purl.org/dc/elements/1.1/"

    const val OPF_PREFIX = "opf"
    const val OPF_URI = "http://www.idpf.org/2007/opf"

    const val META_INF_CONTAINER_PREFIX = ""
    const val META_INF_CONTAINER_URI = "urn:oasis:names:tc:opendocument:xmlns:container"

    const val XML_PREFIX = "xml"
    const val XML_URI = "http://www.w3.org/XML/1998/namespace"
}