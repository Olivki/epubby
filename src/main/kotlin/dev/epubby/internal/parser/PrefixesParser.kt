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

package dev.epubby.internal.parser

import dev.epubby.prefixes.Prefix
import dev.epubby.prefixes.PrefixImpl
import dev.epubby.prefixes.Prefixes
import dev.epubby.prefixes.toPrefixes
import org.jdom2.Verifier
import java.net.URI

/**
 * A parser for parsing a string *([source]*) into a [Prefix] instance.
 *
 * The following is the grammar for how `prefix` data types are formatted:
 *
 * ```
 *  prefixes = mapping (whitespace (whitespace)* mapping)*
 *  mapping = prefix ":" space (space)* xsd:anyURI
 *  prefix = xsd:NCName
 *  space = ' '
 *  whitespace = (' ' | '\t' | '\r' | '\n')
 * ```
 *
 * - [xsd:anyURI](https://www.w3.org/TR/xmlschema11-2/#anyURI)
 * - [xsd:NCName](https://www.w3.org/TR/xmlschema11-2/#NCName)
 *
 * [EPUB Definition](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-prefix-attr)
 */
internal class PrefixesParser internal constructor(source: String) {
    internal companion object {
        private const val SPACE: Char = '\u0020'

        private const val COLON: Char = '\u003A'

        private val WHITESPACE: CharArray = charArrayOf(SPACE, '\t', '\r', '\n')

        @JvmSynthetic
        internal fun parse(source: String): Prefixes = PrefixesParser(source).parse().toPrefixes()
    }

    private val tokenizer: StringTokenizer = StringTokenizer(source, WHITESPACE)

    fun parse(): List<Prefix> = prefixes()

    @OptIn(ExperimentalStdlibApi::class)
    private fun prefixes(): List<Prefix> = buildList {
        add(mapping())

        while (!tokenizer.isAtEnd) {
            whitespace()
            add(mapping())
        }
    }

    private fun mapping(): Prefix {
        val name = prefix()
        tokenizer.eat(COLON)
        space()
        val uri = uri()

        return PrefixImpl(name, uri)
    }

    private fun uri(): URI {
        val uri = buildString {
            tokenizer.apply {
                while (!isAtEnd && !isSkippable(peek())) {
                    append(eatChar())
                }
            }
        }

        // TODO: this fails when given a URI that contains '#', even tho that's allowed for this purpose
        return when (val result = Verifier.checkURI(uri.substringBeforeLast('#'))) {
            null -> URI(uri)
            else -> tokenizer.exception("$result -> $uri")
        }
    }

    private fun prefix(): String {
        val name = buildString {
            tokenizer.apply {
                while (!isAtEnd) {
                    if (isSkippable(peek()) || peek() == COLON) {
                        break
                    }

                    append(eatChar())
                }
            }
        }

        return when (val result = Verifier.checkElementName(name)) {
            null -> name
            else -> tokenizer.exception(result)
        }
    }

    private fun space() {
        tokenizer.apply {
            eat(SPACE)

            while (check(SPACE)) {
                moveOne()
            }
        }
    }

    private fun whitespace() {
        tokenizer.apply {
            eat(WHITESPACE)

            while (check(WHITESPACE)) {
                moveOne()
            }
        }
    }
}