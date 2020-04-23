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

package moe.kanon.epubby.props

import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.internal.NewFeature
import moe.kanon.kommons.affirmThat
import moe.kanon.kommons.collections.isNotEmpty
import moe.kanon.kommons.requireThat

/**
 * Represents a list of [Property] instances used by a book.
 *
 * A properties instance is not allowed to be empty, and therefore invoking [clear] or invoking [remove]/[removeAt]
 * when the element is the last one in the properties instance will result in a [UnsupportedOperationException] being
 * thrown.
 */
@NewFeature(since = BookVersion.EPUB_3_0)
class Properties private constructor(private val delegate: MutableList<Property>) : AbstractMutableList<Property>() {
    override val size: Int get() = delegate.size

    override fun add(index: Int, element: Property) {
        delegate.add(index, element)
    }

    override fun get(index: Int): Property = delegate[index]

    override fun removeAt(index: Int): Property {
        affirmThat(size > 1) { "Removing the last element of a properties instance is not allowed." }
        return delegate.removeAt(index)
    }

    override fun set(index: Int, element: Property): Property = delegate.set(index, element)

    override fun clear() {
        throw UnsupportedOperationException("Clearing a properties instance is not allowed.")
    }

    /**
     * Returns a string containing all the [property][Property] instances stored in this `properties`, separated by a
     * space.
     */
    fun toStringForm(): String = joinToString(separator = " ", transform = Property::reference)

    companion object {
        /**
         * Returns a new properties instance that contains the [first] property and any values defined in [rest].
         */
        @JvmStatic
        fun of(first: Property, vararg rest: Property): Properties = Properties(mutableListOf(first, *rest))

        /**
         * Returns a new properties instance containing the property values in the given [properties].
         *
         * @throws [IllegalArgumentException] if [properties] is empty
         */
        @JvmStatic
        fun copyOf(properties: Iterable<Property>): Properties {
            requireThat(properties.isNotEmpty) { "expected 'properties' to not be empty" }
            return Properties(properties.toMutableList())
        }

        @JvmSynthetic
        internal fun empty(): Properties = Properties(mutableListOf())

        // TODO: parse?
    }
}