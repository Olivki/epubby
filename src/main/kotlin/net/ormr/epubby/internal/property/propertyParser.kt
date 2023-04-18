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
import cc.ekblad.konbini.oneOf
import cc.ekblad.konbini.parser
import cc.ekblad.konbini.regex
import net.ormr.epubby.internal.util.ncName

// https://www.w3.org/publishing/epub3/epub-packages.html#sec-property-syntax

// it's actually a irelative-ref but that's a non-trivial regex to create, so until this comes back and bites us in the
// ass in the future, it's staying like this
private val reference = regex(".*")

private val fullProperty = parser {
    val prefix = ncName()
    char(':')
    val ref = reference()
    ParsedProperty(prefix, ref)
}

private val onlyReference = parser {
    val ref = reference()
    ParsedProperty(prefix = null, ref)
}

internal val propertyParser = oneOf(fullProperty, onlyReference)