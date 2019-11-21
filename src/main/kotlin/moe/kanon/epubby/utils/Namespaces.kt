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

package moe.kanon.epubby.utils

import org.jdom2.Namespace

/**
 * Contains all the namespaces used by various parts of the framework.
 */
object Namespaces {
    /**
     * DublinCore namespace.
     *
     * Used for the dublin-core metadata elements defined in the package document.
     */
    @JvmField val DUBLIN_CORE: Namespace = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/")

    /**
     * TODO
     */
    @JvmField val OPF: Namespace = Namespace.getNamespace("http://www.idpf.org/2007/opf")

    /**
     * TODO
     */
    @JvmField val OPF_WITH_PREFIX: Namespace = Namespace.getNamespace("opf", "http://www.idpf.org/2007/opf")

    /**
     * Daisy namespace.
     *
     * Used for the `'.ncx'` navigation document.
     */
    @JvmField val DAISY_NCX: Namespace = Namespace.getNamespace("http://www.daisy.org/z3986/2005/ncx/")
}