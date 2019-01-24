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

@file:JvmName("JSoupUtils")

package moe.kanon.epubby.utils

import org.jsoup.nodes.Element
import org.jsoup.select.Elements


// Utilities and extensions for JSoup, these utilities were originally developed for Java, so they should work
// to use with Java.
// This might be moved into it's own library.

// Element
/**
 * Returns whether or not the `trimmed` version of the `text` inside of this [Element] is empty.
 */
public fun Element.isTextEmpty(): Boolean = this.text().trim().isEmpty()

/**
 * Returns whether or not the `trimmed` version of this [Element]'s own `text` is empty.
 */
public fun Element.isOwnTextEmpty(): Boolean = this.ownText().trim().isEmpty()

/**
 * Infix function for [Element. is].
 *
 * This is mainly because `is` *is* a keyword in Kotlin, so every time that function needs to be used, you have to do
 * `` `is` `` to actually use it, which is just ugly.
 */
public infix fun Element.matches(query: String): Boolean = this.`is`(query)

/**
 * Checks if this [Element] has a `class` attribute *at all*, returns `true` if it does, `false` if not.
 *
 * Unlike the other `hasClass` method that accepts a `String`, this one doesn't check for a specific class, but rather
 * if the `element` has the `attribute` "class" at all.
 */
public fun Element.hasClass(): Boolean = this.hasClass(this.className())

/**
 * Returns whether or not this [Element] is empty.
 *
 * This is done by checking if this element has any children and that the text of it is empty.
 */
public fun Element.isEmpty(): Boolean = this.children().size <= 0 && this.isOwnTextEmpty()

/**
 * Returns whether or not the `parent` of this [Element] is empty.
 *
 * This is done by checking that the amount of `children` the parent has is **not** greater than `1` *(This is done
 * because there'll always be at least `1` child, which is `this` element.)*, and that the text of the `parent` is
 * empty after being trimmed.
 *
 * Calling it `isParentEmpty` might be a bit misleading, but most other names that could explain what this does
 * properly would end up being very long-winded.
 */
public fun Element.isParentEmpty(): Boolean =
    this.parent().children().size <= 1 && this.parent().isOwnTextEmpty()

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
public fun Element.removeClass(force: Boolean = false) {
    if (force) {
        this.attributes().filter { it.key.equals("class", true) }.forEach { this.removeAttr(it.key) }
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
public fun Element.replaceClass(newClass: String, force: Boolean = false) {
    this.removeClass(force)
    this.addClass(newClass)
}
/**
 * Returns whether or not any of the `children` of this [Element] `matches` the specified [query].
 *
 * This is done by *recursively* calling this function on all of the children of this element.
 *
 * **Note:** It does *not* check if `this` element matches the query.
 */
// TODO: Check if this one actually works.
public operator fun Element.contains(query: String): Boolean = this.children().any { it.contains(query) }

/**
 * Returns whether or not this [Element] ***only*** contains the specified [query].
 *
 * **Note:** This function ignores a few elements, namely; `div`, `section`, `body`, `p` and `span`.
 */
// TODO: Turn the hardcoded "ignored" elements into optional ones.
public infix fun Element.onlyContains(query: String): Boolean = this.allElements.filter { !(it matches query) }.any {
    ((it matches "div" || it matches "section" || it matches "body") || ((it matches "p" || it matches "span") && it.isTextEmpty()))
}

// Elements
/**
 * Checks if the [Elements] instance has any entries, if it doesn't, this returns `false`, otherwise it returns `true`.
 */
public fun Elements.exists(): Boolean = !this.isEmpty()

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
public fun Elements.removeClasses(force: Boolean = false) {
    for (element in this) {
        if (force) {
            element.attributes().filter { it.key.equals("class", true) }.forEach { this.removeAttr(it.key) }
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
public fun Elements.replaceClasses(newClass: String, force: Boolean = false) {
    this.removeClasses(force)
    this.addClass(newClass)
}