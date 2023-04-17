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

import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import net.ormr.epubby.internal.util.ncNameToken

/**
 * ```
 *  property = (prefix ":")? reference
 *  prefix = xsd:NCName
 *  reference = irelative-ref
 * ```
 */
// https://www.w3.org/publishing/epub3/epub-packages.html#sec-property-syntax
internal object PropertyParser : Grammar<ParsedProperty>() {
    private val prefix by ncNameToken()

    // https://tools.ietf.org/html/rfc3987#section-2.2
    // we just accept anything here because nothing can be after the reference
    private val reference by regexToken(".*")

    private val colon by literalToken(":")

    private val property by optional(prefix * -colon) * reference

    override val rootParser: Parser<ParsedProperty> by property map { (prefix, reference) ->
        ParsedProperty(prefix?.text, reference.text)
    }
}