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

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

/**
 * ```
 *  prefixes = mapping ((whitespace)+ mapping)*
 *  mapping = prefix ":" (space)+ xsd:anyURI
 *  prefix = xsd:NCName
 *  space = ' '
 *  whitespace = (' ' | '\t' | '\r' | '\n')
 * ```
 */
// https://www.w3.org/publishing/epub3/epub-packages.html#sec-prefix-attr
internal object PrefixesParser : Grammar<List<ParsedMapping>>() {
    private val mapping by MappingParser
    private val whitespace by regexToken("[ |\t|\r|\n]")
    private val prefixes by mapping * zeroOrMore(-oneOrMore(whitespace) * mapping)

    override val rootParser: Parser<List<ParsedMapping>> by prefixes map { (head, tail) -> tail + head }
}