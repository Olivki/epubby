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

interface HtmlElementList : MutableList<HtmlElement> {
    val parents: HtmlElementList

    val text: List<String>

    /**
     * The first element of `this` list, or `null` if `this` list is empty.
     */
    val first: HtmlElement?

    /**
     * The last element of `this` list, or `null` if `this` list is empty.
     */
    val last: HtmlElement?

    fun getAttributes(key: String): List<String>

    fun setAttributes(key: String, value: String)

    fun removeAttributes(key: String)

    fun containsAttribute(key: String): Boolean

    /**
     * Returns a new [HtmlElementList] that contains all the [HtmlElement]s of `this` list that match the given [query].
     *
     * @see [filterNot]
     */
    fun filter(query: String): HtmlElementList

    /**
     * Returns a new [HtmlElementList] that contains all the [HtmlElement]s of `this` list that *do not* match the
     * given [query].
     *
     * @see [filter]
     */
    fun filterNot(query: String): HtmlElementList

    /**
     * Returns `true` if any of the elements in `this` list match the given [query], otherwise `false`.
     */
    fun any(query: String): Boolean

    /**
     * Returns a string containing the combined HTML output of all elements contained in `this` list.
     */
    fun toHtml(): String

    fun toOuterHtml(): String

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    override fun toString(): String
}