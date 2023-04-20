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

package net.ormr.epubby.internal.prefix

import cc.ekblad.konbini.*
import net.ormr.epubby.internal.util.ncName

/*
 *  https://www.w3.org/publishing/epub3/epub-packages.html#sec-prefix-attr
 *
 *  prefixes = mapping ((whitespace)+ mapping)*
 *  mapping = prefix ":" (space)+ xsd:anyURI
 *  prefix = xsd:NCName
 *  space = ' '
 *  whitespace = (' ' | '\t' | '\r' | '\n')
 */

internal val prefixesParser = parser {
    val head = mapping()
    val tail = many {
        many1(whitespace)
        mapping()
    }
    listOf(head) + tail
}

private val mapping = parser {
    val name = ncName()
    char(':')
    many1(space)
    val iri = anyUri()
    ParsedMapping(name, iri)
}

private val anyUriRegex =
    """(([a-zA-Z][0-9a-zA-Z+\\.]*:)?/{0,2}[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?""".toRegex()

private val anyUri = regex(anyUriRegex)

private val space = char(' ')

private val whitespaceRegex = "[ |\t|\r|\n]".toRegex()

private val whitespace = regex(whitespaceRegex)