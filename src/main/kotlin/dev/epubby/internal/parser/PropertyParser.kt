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

import dev.epubby.prefixes.PackagePrefix
import dev.epubby.prefixes.Prefixes
import dev.epubby.properties.Property
import org.jdom2.Verifier
import java.net.URI
import java.net.URISyntaxException

/**
 * A parser for parsing a string *([source]*) into a [Property] instance.
 *
 * The following is the grammar for how `property` data types are formatted:
 *
 * ```
 *  property = (prefix ":")? reference
 *  prefix = xsd:NCName
 *  reference = irelative-ref
 * ```
 *
 * - [xsd:NCName](https://www.w3.org/TR/xmlschema11-2/#NCName)
 * - [irelative-ref](https://tools.ietf.org/html/rfc3987#section-2.2)
 *
 * [EPUB Definition](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-property-syntax)
 *
 * While the grammar states that `prefix` is *optional*, it is actually only optional in *certain contexts*, and
 * therefore that is dealt with outside of this parser, so this parser will just assume that `prefix` *always* exists
 * and will fail otherwise.
 */
internal class PropertyParser internal constructor(source: String) {
    internal companion object {
        private const val COLON: Char = '\u003A'

        private val EMPTY_ARRAY: CharArray = charArrayOf()

        @JvmSynthetic
        internal fun parse(source: String, prefixes: Prefixes): Property? = PropertyParser(source).parse(prefixes)
    }

    private val tokenizer: StringTokenizer = StringTokenizer(source, EMPTY_ARRAY)

    fun parse(prefixes: Prefixes): Property? = property(prefixes)

    private fun property(prefixes: Prefixes): Property? {
        val rawPrefix = prefix()
        val prefix = PackagePrefix.fromPrefixOrNull(rawPrefix) ?: prefixes[rawPrefix] ?: return null
        tokenizer.eat(':')
        val reference = reference()

        return Property.of(prefix, reference.toString())
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

    private fun reference(): URI {
        val uri = buildString {
            tokenizer.apply {
                while (!isAtEnd) {
                    append(eatChar())
                }
            }
        }

        return try {
            URI(uri)
        } catch (e: URISyntaxException) {
            tokenizer.exception(e.message ?: "'$uri' is not a valid IRI", e)
        }
    }
}