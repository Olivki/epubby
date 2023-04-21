/*
 * Copyright 2023 Oliver Berg
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
import cc.ekblad.konbini.many
import cc.ekblad.konbini.parser

/*
 * https://www.w3.org/publishing/epub3/epub-packages.html#attrdef-properties
 *
 * The spec doesn't actually say much about this outside of "it's a space separated list" which is pretty darn bad
 * documentation for a spec. Is it max 1 space between each entry? Is other whitespace ok? Can it be empty?
 * So for all we know this parser could just not be spec compliant, because the spec is awfully vague here for some
 * reason.
 *
 * The example they give doesn't even contain more than one element in it either, truly amazing.
*/

internal val propertyListParser = parser {
    val head = propertyParser()
    val tail = many {
        char(' ')
        propertyParser()
    }
    PropertyModelList(listOf(head) + tail)
}