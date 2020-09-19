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

@file:JvmName("_kt_properties_extensions")

package dev.epubby.properties

import dev.epubby.EpubElement
import dev.epubby.EpubVersion
import dev.epubby.internal.IntroducedIn

// TODO: when I originally implemented this class, I made it so that it couldn't be empty, but now after thinking about
//       it for a bit, I can't recall that being an *actual* requirement set forth by the standard, as an empty
//       'properties' attribute is not uncommon, so this has been removed because it seems like me misunderstanding
//       something quite severely. Maybe look further into this later? Maybe only some 'properties' attributes need to
//       actually contain data? If so, maybe make a 'NonEmptyProperties' class or something like that?

/**
 * Represents a list of [Property] instances used by various [EpubElement]s.
 *
 * Only [Property] implementations known by the Epubby system is allowed to be contained in a `Properties` instance.
 */
@IntroducedIn(version = EpubVersion.EPUB_3_0)
class Properties private constructor(private val delegate: MutableSet<Property>) : AbstractMutableSet<Property>() {
    override val size: Int
        get() = delegate.size

    override fun add(element: Property): Boolean = delegate.add(element)

    override fun iterator(): MutableIterator<Property> = delegate.iterator()

    companion object {
        // TODO: document
        @JvmStatic
        fun withInitialCapacity(initialCapacity: Int): Properties = Properties(LinkedHashSet(initialCapacity))

        /**
         * Returns a new [Properties] instance that contains no entries.
         */
        @JvmStatic
        fun empty(): Properties = Properties(linkedSetOf())

        /**
         * Returns a new [Properties] instance that contains the property instances contained in the given
         * [properties].
         */
        @JvmStatic
        fun of(vararg properties: Property): Properties = when (properties.size) {
            0 -> empty()
            else -> copyOf(properties.asList())
        }

        /**
         * Returns a new [Properties] instance containing the property values in the given [properties].
         */
        @JvmStatic
        fun copyOf(properties: Iterable<Property>): Properties = Properties(properties.toMutableSet())
    }
}

fun Iterable<Property>.toProperties(): Properties = Properties.copyOf(this)

fun Sequence<Property>.toProperties(): Properties = Properties.copyOf(this.asIterable())

fun propertiesOf(vararg properties: Property): Properties = when (properties.size) {
    0 -> Properties.empty()
    else -> properties.asList().toProperties()
}

fun propertiesOf(): Properties = Properties.empty()