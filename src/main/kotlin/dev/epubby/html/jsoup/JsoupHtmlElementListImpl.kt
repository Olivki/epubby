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

import dev.epubby.html.HtmlElement
import dev.epubby.html.HtmlElementList
import org.jsoup.select.Elements

class JsoupHtmlElementListImpl(val delegate: Elements) : AbstractMutableList<HtmlElement>(), HtmlElementList {
    override val size: Int
        get() = delegate.size

    override fun add(index: Int, element: HtmlElement) {
        delegate.add(index, element.toJsoup())
    }

    override fun get(index: Int): HtmlElement {
        TODO("not implemented")
    }

    override fun removeAt(index: Int): HtmlElement {
        TODO("not implemented")
    }

    override fun set(index: Int, element: HtmlElement): HtmlElement {
        TODO("not implemented")
    }

    override val parents: HtmlElementList
        get() = TODO("not implemented")
    override val text: List<String>
        get() = TODO("not implemented")
    override val first: HtmlElement?
        get() = TODO("not implemented")
    override val last: HtmlElement?
        get() = TODO("not implemented")

    override fun getAttributes(key: String): List<String> {
        TODO("not implemented")
    }

    override fun setAttributes(key: String, value: String) {
        TODO("not implemented")
    }

    override fun removeAttributes(key: String) {
        TODO("not implemented")
    }

    override fun containsAttribute(key: String): Boolean {
        TODO("not implemented")
    }

    override fun filter(query: String): HtmlElementList {
        TODO("not implemented")
    }

    override fun filterNot(query: String): HtmlElementList {
        TODO("not implemented")
    }

    override fun any(query: String): Boolean {
        TODO("not implemented")
    }

    override fun toHtml(): String {
        TODO("not implemented")
    }

    override fun toOuterHtml(): String {
        TODO("not implemented")
    }
}