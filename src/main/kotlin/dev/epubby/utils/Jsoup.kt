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

@file:JvmName("JsoupUtils")
@file:Suppress("NOTHING_TO_INLINE")

package dev.epubby.utils

// TODO: migrate to it's own library?

import org.jsoup.nodes.*
import org.jsoup.parser.Tag
import org.jsoup.select.Elements

inline var Document.title: String
    get() = title()
    set(value) {
        title(value)
    }

inline val Document.location: String
    get() = location()

inline val Document.head: Element
    get() = head()

inline val Document.body: Element
    get() = body()

inline val Element.attributes: Attributes
    get() = attributes()

inline val Element.baseUri: String
    get() = baseUri()

inline val Element.childNodeSize: Int
    get() = childNodeSize()

inline val Element.nodeName: String
    get() = nodeName()

inline var Element.tagName: String
    get() = tagName()
    set(value) {
        tagName(value)
    }

inline val Element.normalName: String
    get() = normalName()

inline val Element.tag: Tag
    get() = tag()

inline val Element.id: String
    get() = id()

inline val Element.dataSet: Map<String, String>
    get() = dataset()

inline val Element.parent: Element?
    get() = parent()

inline val Element.parents: Elements
    get() = parents()

inline val Element.children: List<Element>
    get() = children()

inline val Element.textNodes: List<TextNode>
    get() = textNodes()

inline val Element.dataNodes: List<DataNode>
    get() = dataNodes()

inline var Element.text: String
    get() = text()
    set(value) {
        text(value)
    }

inline val Element.ownText: String
    get() = ownText()

inline val Element.data: String
    get() = data()

inline val Element.className: String
    get() = className()

inline var Element.classNames: Set<String>
    get() = classNames()
    set(value) {
        classNames(value)
    }

inline var Element.value: String
    get() = `val`()
    set(value) {
        `val`(value)
    }

inline var Element.html: String
    get() = html()
    set(value) {
        html(value)
    }

inline val Element.outerHtml: String
    get() = outerHtml()

/**
 * Returns `true` if the [text][Element.text] of `this` element [is blank][String.isBlank], otherwise `false`.
 */
val Element.isTextBlank: Boolean
    get() = text.isBlank()

/**
 * Returns `true` if the [text][Element.text] of `this` element [is not blank][String.isNotBlank], otherwise `false`.
 */
val Element.isTextNotBlank: Boolean
    get() = text.isNotBlank()

/**
 * Returns `true` if the [own text][Element.ownText] of `this` element [is blank][String.isBlank], otherwise `false`.
 */
val Element.isOwnTextBlank: Boolean
    get() = ownText.isBlank()

/**
 * Returns `true` if the [own text][Element.ownText] of `this` element [is not blank][String.isBlank], `false`
 * otherwise.
 */
val Element.isOwnTextNotBlank: Boolean
    get() = ownText.isNotBlank()

/**
 * Infix function for [Element. is].
 *
 * This is mainly because `is` *is* a keyword in Kotlin, so every time that function needs to be used, you have to do
 * `` `is` `` to actually use it, which is just ugly.
 */
@Suppress("NOTHING_TO_INLINE")
inline infix fun Element.matches(query: String): Boolean = this.`is`(query)

/**
 * Checks if this [Element] has a `class` attribute *at all*, returns `true` if it does, `false` if not.
 *
 * Unlike the other `hasClass` method that accepts a `String`, this one doesn't check for a specific class, but rather
 * if the `element` has the `attribute` "class" at all.
 */
val Element.hasClass: Boolean
    @JvmName("hasClass")
    get() = this.hasClass(this.className())

/**
 * Returns `true` if `this` element has no [children][Element.children] and its [text][Element.text] is empty, `false`
 * otherwise.
 */
// TODO: name
val Element.isEmpty: Boolean
    get() = this.children().size <= 0 && this.isOwnTextBlank

/**
 * Returns `true` if `this` element has [children][Element.children] and its [text][Element.text] is not empty, `false`
 * otherwise.
 */
// TODO: name
val Element.isNotEmpty: Boolean
    get() = !this.isEmpty

/**
 * Returns `true` if `this` element is the only child of its [parent][Element.parent], otherwise `false`.
 * Returns whether or not the `parent` of `this` element is empty.
 *
 * Note that if the `parent` of `this` elements [own text][Element.ownText] is not blank, then `false` will be returned,
 * as text content is considered to be a child in this context.
 */
val Element.isOnlyChildOfParent: Boolean
    get() = this.parent().children().size <= 1 && this.parent().isOwnTextBlank

/**
 * Removes the "class" `attribute` of this [Element], without needing to know the name of it.
 *
 * @param force Whether or not the "class" `attribute` should be **forcefully** removed.
 *
 * When this is `true`, this function doesn't use the inbuilt `removeClass(String)` method, but rather uses the
 * [removeAttr][Element.removeAttr] method to accomplish it.
 *
 * This is sometimes needed, as JSoup decides to *not* remove classes when using the inbuilt method for it every now
 * and then.
 */
@JvmOverloads
fun Element.removeClass(force: Boolean = false) {
    if (force) {
        for (it in this.attributes().filter { it.key.equals("class", true) }) this.removeAttr(it.key)
    } else {
        this.removeClass(this.className())
    }
}

/**
 * Replaces the "class" `attribute` of this [Element] with the specified [newClass].
 *
 * This is accomplished by first removing the class of this element, and then adding the `newClass` to it.
 *
 * @param force Whether or not the "class" `attribute` should be **forcefully** removed.
 *
 * When this is `true`, this function doesn't use the inbuilt `removeClass(String)` method, but rather uses the
 * [removeAttr][Element.removeAttr] method to accomplish it.
 *
 * This is sometimes needed, as JSoup decides to *not* remove classes when using the inbuilt method for it every now
 * and then.
 */
@JvmOverloads
fun Element.replaceClass(newClass: String, force: Boolean = false) {
    this.removeClass(force)
    this.addClass(newClass)
}

/**
 * Returns whether or not any of the children of `this` [Element] `matches` the specified [query].
 *
 * This is done by *recursively* calling this function on all of the children of this element.
 *
 * **Note:** It does *not* check if `this` element [matches] the query.
 */
// TODO: Check if this one actually works.
operator fun Element.contains(query: String): Boolean = this.children().any { it.contains(query) }

/**
 * Returns `true` if `this` element *only* contains the specified [query], ignoring the given [ignoredBodies] and
 * [ignoredElements].
 *
 * @param [ignoredBodies] which element "bodies" to ignore when looking through the document
 * @param [ignoredElements] which elements to ignore when looking through the document
 */
@JvmOverloads
fun Element.onlyContains(
    query: String,
    ignoredBodies: Array<String> = arrayOf("div", "section", "body"),
    ignoredElements: Array<String> = arrayOf("p", "span")
): Boolean = this.allElements
    .asSequence()
    .filter { !(it matches query) }
    .any { element ->
        ((ignoredBodies.any { element matches it }) || ((ignoredElements.any { element matches it }) && element.isTextBlank))
    }

/**
 * Returns `true` if `this` element has a child that [matches] the given [query], otherwise `false`.
 */
fun Element.hasChild(query: String): Boolean = this.children().any { it matches query }

/**
 * Returns `true` if `this` elment has *any* children that matches *any* of the given [queries], otherwise `false`.
 */
fun Element.hasChildren(vararg queries: String): Boolean =
    this.children().any { queries.any { query -> it matches query } }

// -- ELEMENTS -- \\
/**
 * Returns `true` if `this` element has a child that [matches] the given [query], otherwise `false`.
 */
fun Elements.hasChild(query: String): Boolean = this.any { it matches query }

/**
 * Returns `true` if `this` element has *any* children that matches *any* of the given [queries], otherwise `false`.
 */
fun Elements.hasChildren(vararg queries: String): Boolean =
    this.any { queries.any { query -> it matches query } }

/**
 * Returns `true` if `this` elements does not have any children, otherwise `false`.
 */
fun Elements.isNotEmpty(): Boolean = !this.isEmpty()

/**
 * Removes the "class" `attributes` from all [Element]s contained in this [Elements].
 *
 * @param force Whether or not the "class" `attribute` should be **forcefully** removed.
 *
 * When this is `true`, this function doesn't use the inbuilt `removeClass(String)` method, but rather uses the
 * [removeAttr][Element.removeAttr] method to accomplish it.
 *
 * This is sometimes needed, as JSoup decides to *not* remove classes when using the inbuilt method for it every now
 * and then.
 */
@JvmOverloads
fun Elements.removeClasses(force: Boolean = false) {
    for (element in this) {
        if (force) {
            for (it in element.attributes().filter { it.key.equals("class", true) }) {
                this.removeAttr(it.key)
            }
        } else {
            element.removeClass(element.className())
        }
    }
}

/**
 * Replaces **all** "class" `attributes` of the [Element]s in this [Elements] with the specified [newClass].
 *
 * This is accomplished by first removing all of the classes on the elements, and then adding the `newClass` to all
 * of them.
 *
 * @param force Whether or not the "class" `attribute` should be **forcefully** removed.
 *
 * When this is `true`, this function doesn't use the inbuilt `removeClass(String)` method, but rather uses the
 * [removeAttr][Element.removeAttr] method to accomplish it.
 *
 * This is sometimes needed, as JSoup decides to *not* remove classes when using the inbuilt method for it every now
 * and then.
 */
@JvmOverloads
fun Elements.replaceAllClasses(newClass: String, force: Boolean = false) {
    this.removeClasses(force)
    this.addClass(newClass)
}

inline val Attributes.size: Int
    get() = size()

inline operator fun Attributes.set(key: String, value: String) {
    put(key, value)
}

inline operator fun Attributes.set(key: String, value: Boolean) {
    put(key, value)
}

inline operator fun Attributes.plusAssign(attribute: Attribute) {
    put(attribute)
}

inline operator fun Attributes.minusAssign(key: String) {
    remove(key)
}

inline operator fun Attributes.contains(key: String): Boolean = hasKey(key)

inline fun Attributes.toHtml(): String = html()

fun Attributes.addAll(attributes: Iterable<Attribute>) {
    for (attribute in attributes) {
        this += attribute
    }
}