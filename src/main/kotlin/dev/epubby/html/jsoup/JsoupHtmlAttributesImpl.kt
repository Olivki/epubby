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

package dev.epubby.html.jsoup

import dev.epubby.html.HtmlAttribute
import dev.epubby.html.HtmlAttributes
import moe.kanon.kommons.collections.asUnmodifiableList
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Attributes

class JsoupHtmlAttributesImpl(val delegate: Attributes) : HtmlAttributes {
    override val size: Int
        get() = delegate.size()

    override fun get(key: String): String = delegate[key]

    override fun getIgnoreCase(key: String): String = delegate.getIgnoreCase(key)

    override fun set(key: String, value: String) {
        delegate.put(key, value)
    }

    override fun set(key: String, value: Boolean) {
        delegate.put(key, value)
    }

    override fun add(attribute: HtmlAttribute) {
        delegate.put(attribute.toJsoup())
    }

    override fun remove(key: String) {
        delegate.remove(key)
    }

    override fun removeIgnoreCase(key: String) {
        delegate.removeIgnoreCase(key)
    }

    override fun containsKey(key: String): Boolean = delegate.hasKey(key)

    override fun containsKeyIgnoreCase(key: String): Boolean = delegate.hasKeyIgnoreCase(key)

    override fun toHtml(): String = delegate.html()

    override fun toList(): List<HtmlAttribute> = delegate.asList().map { JsoupHtmlAttributeImpl(it) }

    override fun toDataset(): Map<String, String> = delegate.dataset()

    override fun iterator(): Iterator<HtmlAttribute> = object : MutableIterator<HtmlAttribute> {
        val delegateIterator = delegate.iterator()

        override fun hasNext(): Boolean = delegateIterator.hasNext()

        override fun next(): HtmlAttribute = JsoupHtmlAttributeImpl(delegateIterator.next())

        override fun remove() {
            delegateIterator.remove()
        }
    }

    override fun equals(other: Any?): Boolean = delegate == other

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = delegate.toString()
}