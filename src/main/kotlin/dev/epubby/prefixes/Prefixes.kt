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

package dev.epubby.prefixes

import dev.epubby.BookVersion
import dev.epubby.internal.IntroducedIn
import java.util.Collections

@IntroducedIn(version = BookVersion.EPUB_3_0)
class Prefixes private constructor(private val delegate: MutableMap<String, Prefix>) :
    AbstractMutableMap<String, Prefix>() {
    override val entries: MutableSet<MutableMap.MutableEntry<String, Prefix>>
        get() = delegate.entries

    fun add(prefix: Prefix) {
        put(prefix.title, prefix)
    }

    fun addAll(vararg prefixes: Prefix) {
        for (prefix in prefixes) {
            add(prefix)
        }
    }

    @JvmSynthetic
    operator fun plusAssign(prefix: Prefix) {
        add(prefix)
    }

    override fun put(key: String, value: Prefix): Prefix? {
        requireKnown(value)
        return delegate.put(key, value)
    }

    // unsure if this the correct form to output this to
    @JvmSynthetic
    internal fun toStringForm(): String = values.joinToString(separator = " ", transform = Prefix::toStringForm)

    companion object {
        @get:JvmSynthetic
        internal val EMPTY: Prefixes = Prefixes(Collections.emptyMap())

        // TODO: documentation

        @JvmStatic
        fun empty(): Prefixes = Prefixes(hashMapOf())

        @JvmStatic
        fun of(vararg prefixes: Prefix): Prefixes = copyOf(prefixes.asIterable())

        @JvmStatic
        fun copyOf(prefixes: Iterable<Prefix>): Prefixes {
            validate(prefixes)
            return Prefixes(prefixes.associateByTo(hashMapOf()) { it.title })
        }

        @JvmStatic
        fun copyOf(prefixes: Map<String, Prefix>): Prefixes {
            validate(prefixes.values)
            return Prefixes(prefixes.toMap(hashMapOf()))
        }

        private fun validate(prefixes: Iterable<Prefix>) {
            for (prefix in prefixes) {
                requireKnown(prefix)

                if (prefix.isDefaultVocabularyPrefix()) {
                    throw IllegalArgumentException("prefix must not map to default vocabularies (${prefix.toStringForm()})")
                }

                if (prefix.title.isBlank()) {
                    throw IllegalArgumentException("prefix 'title' must not be blank (${prefix.toStringForm()})")
                }
            }
        }
    }
}

fun emptyPrefixes(): Prefixes = Prefixes.empty()

fun prefixesOf(vararg prefixes: Prefix): Prefixes = Prefixes.copyOf(prefixes.asIterable())

fun Iterable<Prefix>.toPrefixes(): Prefixes = Prefixes.copyOf(this)

fun Map<String, Prefix>.toPrefixes(): Prefixes = Prefixes.copyOf(this)