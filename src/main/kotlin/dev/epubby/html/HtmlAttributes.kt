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

package dev.epubby.html

import kotlin.collections.toList as toListK

interface HtmlAttributes : Iterable<HtmlAttribute> {
    val size: Int

    @JvmDefault
    fun isEmpty(): Boolean = size <= 0

    @JvmDefault
    fun isNotEmpty(): Boolean = size > 0

    operator fun get(key: String): String

    fun getIgnoreCase(key: String): String

    operator fun set(key: String, value: String)

    operator fun set(key: String, value: Boolean)

    fun add(attribute: HtmlAttribute)

    fun remove(key: String)

    fun removeIgnoreCase(key: String)

    fun containsKey(key: String): Boolean

    fun containsKeyIgnoreCase(key: String): Boolean

    fun toHtml(): String

    @JvmDefault
    fun toList(): List<HtmlAttribute> = toListK()

    @JvmDefault
    fun toMap(): Map<String, String> = associate { (k, v) -> k to v }

    fun toDataset(): Map<String, String>

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}

operator fun HtmlAttributes.plusAssign(attribute: HtmlAttribute) {
    add(attribute)
}

operator fun HtmlAttributes.minusAssign(key: String) {
    remove(key)
}

operator fun HtmlAttributes.contains(key: String): Boolean = containsKey(key)