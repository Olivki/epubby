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

import dev.epubby.html.*
import org.jsoup.nodes.Element

class JsoupHtmlElementImpl(val delegate: Element) : HtmlElement {
    override val parent: HtmlElement?
        get() = TODO("not implemented")
    override val parents: HtmlElementList
        get() = TODO("not implemented")
    override var id: String
        get() = TODO("not implemented")
        set(value) {}
    override val tag: HtmlTag
        get() = TODO("not implemented")
    override var tagName: String
        get() = TODO("not implemented")
        set(value) {}

    override fun equals(other: Any?): Boolean {
        TODO("not implemented")
    }

    override fun hashCode(): Int {
        TODO("not implemented")
    }

    override fun toString(): String {
        TODO("not implemented")
    }

    override val root: HtmlNode
        get() = TODO("not implemented")
    override val nodeName: String
        get() = TODO("not implemented")
    override var baseUri: String
        get() = TODO("not implemented")
        set(value) {}
    override val attributes: HtmlAttributes
        get() = TODO("not implemented")
    override val childNodes: List<HtmlNode>
        get() = TODO("not implemented")
    override val siblingNodes: List<HtmlNode>
        get() = TODO("not implemented")
    override val previousSibling: HtmlNode?
        get() = TODO("not implemented")
    override val nextSibling: HtmlNode?
        get() = TODO("not implemented")
    override val ownerDocument: HtmlDocument?
        get() = TODO("not implemented")

    override fun getAttribute(key: String): String {
        TODO("not implemented")
    }

    override fun getAbsoluteUrl(key: String): String {
        TODO("not implemented")
    }

    override fun setAttribute(key: String, value: String) {
        TODO("not implemented")
    }

    override fun removeAttribute(key: String) {
        TODO("not implemented")
    }

    override fun hasAttribute(key: String): Boolean {
        TODO("not implemented")
    }

    override fun clearAttributes() {
        TODO("not implemented")
    }

    override fun remove() {
        TODO("not implemented")
    }

    override fun prepend(html: String) {
        TODO("not implemented")
    }

    override fun prepend(node: HtmlNode) {
        TODO("not implemented")
    }

    override fun append(html: String) {
        TODO("not implemented")
    }

    override fun append(node: HtmlNode) {
        TODO("not implemented")
    }

    override fun wrap(html: String) {
        TODO("not implemented")
    }

    override fun unwrap() {
        TODO("not implemented")
    }

    override fun replaceWith(other: HtmlNode) {
        TODO("not implemented")
    }

    override fun toOuterHtml(): String {
        TODO("not implemented")
    }
}