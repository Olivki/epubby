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

package moe.kanon.epubby.structs.props

import moe.kanon.epubby.Book
import moe.kanon.epubby.internal.Patterns
import moe.kanon.epubby.packages.PackageDocument
import moe.kanon.epubby.structs.props.vocabs.VocabularyParseMode
import moe.kanon.kommons.collections.isNotEmpty
import moe.kanon.kommons.requireThat
import org.jdom2.Attribute
import org.jdom2.Namespace
import kotlin.reflect.KClass

/**
 * Represents a list of [Property] instances.
 */
class Properties private constructor(private val delegate: MutableList<Property>) : MutableList<Property> by delegate {
    /**
     * Returns a string containing all the [property][Property] instances stored in this `properties`, separated by a
     * space.
     *
     * This form is the way that the `properties` element is displayed in its XML attribute form in the
     * [OPF][PackageDocument] of a [book][Book].
     */
    fun toStringForm(): String = delegate.joinToString(separator = " ") { it.reference }

    @JvmOverloads
    fun toAttribute(name: String = "properties", namespace: Namespace = Namespace.NO_NAMESPACE): Attribute =
        Attribute(name, toStringForm(), namespace)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Properties -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = delegate.toString()

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
        fun empty(): Properties = Properties(mutableListOf())

        @JvmSynthetic
        internal fun parse(
            caller: KClass<*>,
            input: String,
            mode: VocabularyParseMode = VocabularyParseMode.PROPERTY
        ): Properties = input
            .replace(Patterns.EXCESSIVE_WHITESPACE, " ") // TODO: remove?
            .split(' ')
            .mapTo(ArrayList()) { Property.parse(caller, it, mode) }
            .let(::Properties)
    }
}

