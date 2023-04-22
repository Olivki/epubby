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

package net.ormr.epubby.internal.property

import cc.ekblad.konbini.char
import cc.ekblad.konbini.parser
import cc.ekblad.konbini.tryParse
import net.ormr.epubby.internal.iri.nonEmptyIRelativeRef
import net.ormr.epubby.internal.util.ncName

// https://www.w3.org/publishing/epub3/epub-packages.html#sec-property-syntax
internal val propertyParser = parser {
    val prefix = tryParse {
        val name = ncName()
        char(':')
        name
    }
    val ref = nonEmptyIRelativeRef()
    PropertyModel(prefix = prefix, reference = ref)
}