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

package net.ormr.epubby.internal.prefix

import dev.epubby.Epub3Feature
import dev.epubby.prefix.MappedPrefix
import dev.epubby.prefix.Prefixes

@OptIn(Epub3Feature::class)
internal class PrefixesImpl(
    private val delegate: MutableMap<String, MappedPrefix>,
) : AbstractMutableMap<String, MappedPrefix>(), Prefixes {
    override val entries: MutableSet<MutableMap.MutableEntry<String, MappedPrefix>>
        get() = delegate.entries

    override fun put(key: String, value: MappedPrefix): MappedPrefix? = delegate.put(key, value)

    override fun add(prefix: MappedPrefix): MappedPrefix? = put(prefix.name, prefix)

    override fun asString(): String = values.joinToString(separator = " ") { it.asString() }
}