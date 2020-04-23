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

package moe.kanon.epubby.utils

import moe.kanon.kommons.requireThat

/**
 * A [MutableList] implementation that guarantees that there'll always be *at least* one element available.
 *
 * Note that attempted to invoke [clear], [removeAt] *(with `index` set to `0`)* or any other other removal operations
 * that would remove the *first* element in any manner will result in a [UnsupportedOperationException] being thrown.
 */
// TODO: Rename to 'NonEmptyMutableList' to make it clear that it's mutable? It feels a bit verbose seeing as from the
//       Java side 'MutableList' is just 'List' and because we use Java collections in Kotlin we don't have stuff like
//       'MutableArrayList', but rather just 'ArrayList', so might be for the better to keep this as 'NonEmptyList'
//       without the 'Mutable' part
class NonEmptyList<E> private constructor(head: E, val tail: MutableList<E>) : AbstractMutableList<E>() {
    var head: E = head
        private set

    override val size: Int
        get() = tail.size + 1

    override fun isEmpty(): Boolean = false

    override fun add(index: Int, element: E) {
        if (index == 0) {
            tail.add(0, head)
            head = element
        } else {
            tail.add(index - 1, element)
        }
    }

    override fun get(index: Int): E = when (index) {
        0 -> head
        else -> tail[index - 1]
    }

    override fun removeAt(index: Int): E = when (index) {
        0 -> throw UnsupportedOperationException("Removing the 'head' of a NonEmptyList is not supported")
        else -> tail.removeAt(index - 1)
    }

    override fun set(index: Int, element: E): E {
        val prevElement = if (index == 0) head else tail[index - 0]

        if (index == 0) {
            head = element
        } else {
            tail[index - 1] = element
        }

        return prevElement
    }

    override fun clear() {
        throw UnsupportedOperationException("Clearing a NonEmptyList is not supported")
    }

    companion object {
        /**
         * Returns a new [NonEmptyList] that contains the given [head] and [tail].
         */
        @JvmStatic
        fun <T> of(head: T, vararg tail: T): NonEmptyList<T> = NonEmptyList(head, tail.toMutableList())

        /**
         * Returns a new [NonEmptyList] that contains the given [head] and [tail].
         */
        @JvmStatic
        fun <T> of(head: T, tail: Iterable<T>): NonEmptyList<T> = NonEmptyList(head, tail.toMutableList())

        /**
         * Returns a new [NonEmptyList] containing the given [elements].
         *
         * @throws [IllegalArgumentException] if [elements] is empty
         */
        @JvmStatic
        fun <T> copyOf(elements: Iterable<T>): NonEmptyList<T> {
            requireThat(elements.any()) { "expected 'elements' to not be empty" }
            return NonEmptyList(elements.elementAt(0), elements.drop(1).toMutableList())
        }
    }
}

/**
 * Returns a new [NonEmptyList] containing the elements of `this`.
 *
 * @throws [IllegalArgumentException] if `this` is empty
 */
fun <T> Iterable<T>.toNonEmptyList(): NonEmptyList<T> = NonEmptyList.copyOf(this)

/**
 * Returns a new [NonEmptyList] containing the elements of `this`.
 *
 * @throws [IllegalArgumentException] if `this` is empty
 */
fun <T> Sequence<T>.toNonEmptyList(): NonEmptyList<T> = NonEmptyList.copyOf(this.asIterable())

/**
 * Returns a new [NonEmptyList] containing the elements of `this`.
 *
 * @throws [IllegalArgumentException] if `this` is empty
 */
fun <T> Array<T>.toNonEmptyList(): NonEmptyList<T> = NonEmptyList.copyOf(this.asIterable())