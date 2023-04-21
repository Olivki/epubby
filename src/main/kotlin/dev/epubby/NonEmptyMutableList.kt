/*
 * Copyright 2019-2023 Oliver Berg
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

package dev.epubby

import net.ormr.epubby.internal.NonEmptyMutableListImpl

/**
 * A [MutableList] implementation with the guarantee that there'll always be *at least* one element available.
 *
 * Note that any attempts to invoke [clear], [removeAt] *(with `index` set to `0`)* or any other removal operations
 * that would remove the *first* element in any manner will result in a [UnsupportedOperationException] being thrown.
 */
public interface NonEmptyMutableList<E> : MutableList<E> {
    public val head: E
    public val tail: MutableList<E>

    /**
     * Returns `false`.
     */
    override fun isEmpty(): Boolean = false

    /**
     * Throws a [UnsupportedOperationException].
     */
    override fun clear() {
        throw UnsupportedOperationException("Can't clear a NonEmptyMutableList")
    }
}

public fun <E> NonEmptyMutableList(head: E, vararg tail: E): NonEmptyMutableList<E> =
    NonEmptyMutableListImpl(head, tail.toMutableList())

public fun <E> NonEmptyMutableList(head: E, tail: Iterable<E>): NonEmptyMutableList<E> =
    NonEmptyMutableListImpl(head, tail.toMutableList())

/**
 * Returns a new [NonEmptyMutableList] containing the elements of `this`.
 *
 * @throws [IllegalArgumentException] if `this` is empty
 */
public fun <T> List<T>.toNonEmptyMutableList(): NonEmptyMutableList<T> {
    require(isNotEmpty()) { "Can't create NonEmptyMutableList from empty list" }
    return NonEmptyMutableList(first(), subList(1, lastIndex).toMutableList())
}