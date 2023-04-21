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

import dev.epubby.opf.OpfElement
import net.ormr.epubby.internal.opf.InternalOpfElement
import net.ormr.epubby.internal.opf.OpfImpl
import net.ormr.epubby.internal.util.adoptElement
import net.ormr.epubby.internal.util.disownElement
import net.ormr.epubby.internal.util.requireImpl

internal class MutableOpfElementList<E : OpfElement>(
    private val delegate: MutableList<E>,
    private val opf: OpfImpl,
) : AbstractMutableList<E>() {
    override val size: Int
        get() = delegate.size

    override fun add(index: Int, element: E) {
        requireImpl<InternalOpfElement>(element)
        delegate.add(index, element)
        opf.adoptElement(element)
    }

    override fun get(index: Int): E = delegate[index]

    override fun removeAt(index: Int): E {
        val element = delegate.removeAt(index)
        requireImpl<InternalOpfElement>(element)
        opf.disownElement(element)
        return element
    }

    override fun set(index: Int, element: E): E {
        requireImpl<InternalOpfElement>(element)
        val previousElement = delegate.set(index, element)
        requireImpl<InternalOpfElement>(previousElement)
        opf.disownElement(previousElement)
        opf.adoptElement(element)
        return previousElement
    }
}

internal inline fun <T, E : InternalOpfElement> List<T>.mapToMutableOpfElementList(
    opf: OpfImpl,
    mapper: (T) -> E,
): MutableOpfElementList<E> {
    val list = MutableOpfElementList<E>(ArrayList(size), opf)
    return mapTo(list, mapper)
}

internal fun <E : InternalOpfElement> List<E>.toMutableOpfElementList(opf: OpfImpl): MutableOpfElementList<E> {
    val list = MutableOpfElementList<E>(ArrayList(size), opf)
    list.addAll(this)
    return list
}