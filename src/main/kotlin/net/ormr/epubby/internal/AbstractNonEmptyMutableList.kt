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

package net.ormr.epubby.internal

import dev.epubby.NonEmptyMutableList

internal abstract class AbstractNonEmptyMutableList<E>(
    head: E,
    final override val tail: MutableList<E>,
) : AbstractMutableList<E>(), NonEmptyMutableList<E> {
    final override var head: E = head
        private set

    override val size: Int
        get() = tail.size + 1

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

    override fun isEmpty(): Boolean = false

    override fun removeAt(index: Int): E = when (index) {
        0 -> throw UnsupportedOperationException("Can't remove the head of a NonEmptyMutableList")
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
        super<NonEmptyMutableList>.clear()
    }
}