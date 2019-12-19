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

package moe.kanon.epubby.structs.prefixes

import moe.kanon.epubby.internal.Patterns
import moe.kanon.kommons.collections.mapToTypedArray
import moe.kanon.kommons.requireThat

class Prefixes private constructor(private val delegate: MutableMap<String, Prefix>) : MutableMap<String, Prefix> by delegate {
    // unsure if this the correct form to output this to
    fun toStringForm(): String = delegate.values.joinToString(separator = " ", transform = Prefix::toStringForm)

    override fun toString(): String = delegate.toString()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Prefixes -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    companion object {
        @JvmStatic
        fun empty(): Prefixes = Prefixes(hashMapOf())

        // TODO: Document the throws
        @JvmStatic
        fun of(vararg prefixes: Prefix): Prefixes {
            requireThat(prefixes.isNotEmpty()) { "expected 'prefixes' to not be empty" }
            requireThat(prefixes.none { it.isDefaultVocabularyPrefix() }) { "prefixes are not allowed to map to default vocabularies" }
            requireThat(prefixes.none { it.prefix.isBlank() }) { "prefixes are not allowed to have a blank 'prefix'" }
            return Prefixes(hashMapOf(*prefixes.asIterable().mapToTypedArray { it.prefix to it }))
        }

        @JvmStatic
        fun copyOf(prefixes: Iterable<Prefix>): Prefixes {
            requireThat(prefixes.any()) { "expected 'prefixes' to not be empty" }
            requireThat(prefixes.none { it.isDefaultVocabularyPrefix() }) { "prefixes are not allowed to map to default vocabularies" }
            requireThat(prefixes.none { it.prefix.isBlank() }) { "prefixes are not allowed to have a blank 'prefix'" }
            return Prefixes(prefixes.associateByTo(HashMap()) { it.prefix })
        }

        @JvmStatic
        fun copyOf(prefixes: Map<String, Prefix>): Prefixes {
            requireThat(prefixes.isNotEmpty()) { "expected 'prefixes' to not be empty" }
            requireThat(prefixes.values.none { it.isDefaultVocabularyPrefix() }) { "prefixes are not allowed to map to default vocabularies" }
            requireThat(prefixes.values.none { it.prefix.isBlank() }) { "prefixes are not allowed to have a blank 'prefix'" }
            return Prefixes(prefixes.toMap(HashMap()))
        }

        // whitespace = (#x20 = SPACE | #x9 = CHARACTER TABULATION | #xD = CARRIAGE RETURN | #xA = LINE FEED) ;
        @JvmSynthetic
        internal fun parse(input: String): Prefixes = input
            // this might be a bit excessive seeing as the EBNF grammar only defines space, character tabulation,
            // carriage return and line feed as the valid whitespace characters
            .replace(Patterns.WHITESPACE, " ")
            .replace(Patterns.EXCESSIVE_WHITESPACE, " ")
            .splitToSequence(" ")
            .windowed(2, 2)
            .map { Prefix.of(it[0].substringBeforeLast(':'), it[1]) }
            .associateByTo(HashMap()) { it.prefix }
            .let { Prefixes(it) }
    }
}