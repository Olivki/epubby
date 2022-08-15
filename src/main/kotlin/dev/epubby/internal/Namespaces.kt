/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.internal

import org.jdom2.Namespace

internal object Namespaces {
    @JvmField
    val DUBLIN_CORE: Namespace = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/")

    @JvmField
    val OPF: Namespace = Namespace.getNamespace("http://www.idpf.org/2007/opf")

    @JvmField
    val OPF_WITH_PREFIX: Namespace = Namespace.getNamespace("opf", "http://www.idpf.org/2007/opf")

    @JvmField
    val DAISY_NCX: Namespace = Namespace.getNamespace("http://www.daisy.org/z3986/2005/ncx/")

    @JvmField
    val META_INF_CONTAINER: Namespace = Namespace.getNamespace("", "urn:oasis:names:tc:opendocument:xmlns:container")
}