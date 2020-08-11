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

interface HtmlNode {
    val parent: HtmlNode?

    val root: HtmlNode

    val nodeName: String

    var baseUri: String

    val attributes: HtmlAttributes

    val childNodes: List<HtmlNode>

    val siblingNodes: List<HtmlNode>

    val previousSibling: HtmlNode?

    val nextSibling: HtmlNode?

    val ownerDocument: HtmlDocument?

    fun getAttribute(key: String): String

    fun getAbsoluteUrl(key: String): String

    fun setAttribute(key: String, value: String)

    fun removeAttribute(key: String)

    fun hasAttribute(key: String): Boolean

    fun clearAttributes()

    /**
     * Deletes `this` node from the DOM tree.
     *
     * If `this` node has any children, then those are also deleted.
     */
    fun remove()

    fun prepend(html: String)

    fun prepend(node: HtmlNode)

    fun append(html: String)

    fun append(node: HtmlNode)

    fun wrap(html: String)

    fun unwrap()

    fun replaceWith(other: HtmlNode)

    fun toOuterHtml(): String

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}