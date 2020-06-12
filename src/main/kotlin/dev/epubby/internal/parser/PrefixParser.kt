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

import dev.epubby.prefixes.BasicPrefix
import dev.epubby.prefixes.Prefix
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
 */
internal class PrefixParser internal constructor(private val source: String) {
    private companion object {
        private const val SPACE: Char = '\u0020'

        private const val COLON: Char = '\u003A'

        private val WHITESPACE: CharArray = charArrayOf(SPACE, '\t', '\r', '\n')
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

        return BasicPrefix(name, uri)
    }

    private fun uri(): URI {
        val uri = buildString {
            tokenizer.apply {
                while (!isAtEnd && !isSkippable(peek())) {
                    append(eatChar())
                }
            }
        }

        return when (val result = Verifier.checkURI(uri)) {
            null -> URI(uri)
            else -> tokenizer.exception(result)
        }
    }

    // TODO: could we use the 'Verifier.checkElementName' function for verification instead?
    private fun prefix(): String {
        val prefix = buildString {
            tokenizer.apply {
                var char = eatChar()

                if (NCName.isNameStart(char)) {
                    append(char)
                } else {
                    exception("'$char' is not allowed at the start of a prefix name.")
                }

                while (!isAtEnd) {
                    if (isSkippable(peek()) || peek() == COLON) {
                        break
                    }

                    char = eatChar()

                    if (NCName.isNameTrail(char)) {
                        append(char)
                    } else {
                        exception("'$char' is not allowed in a prefix name.")
                    }
                }
            }
        }

        if (prefix.isBlank()) {
            tokenizer.exception("Prefix must not be empty.")
        }

        return prefix
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

fun main() {
    val prefix = """foaf: http://xmlns.com/foaf/spec/
		 dbp: http://dbpedia.org/ontology/"""
    val parser = PrefixParser(prefix)
    println(parser.parse())
}