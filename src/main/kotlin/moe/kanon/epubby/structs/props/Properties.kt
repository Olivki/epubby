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

import moe.kanon.epubby.utils.internal.Patterns
import moe.kanon.kommons.requireThat
import org.jdom2.Attribute
import org.jdom2.Namespace
import kotlin.reflect.KClass

/**
 * Represents a list of [Property] instances.
 */
class Properties private constructor(private val delegate: MutableList<Property>) : MutableList<Property> by delegate {
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Properties -> false
        delegate != other.delegate -> false
        else -> true
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = delegate.joinToString(separator = " ")

    @JvmSynthetic
    internal fun toAttribute(name: String = "properties", namespace: Namespace = Namespace.NO_NAMESPACE): Attribute =
        Attribute(name, toString(), namespace)

    companion object {
        @JvmStatic
        fun empty(): Properties = Properties(mutableListOf())

        @JvmStatic
        fun of(vararg properties: Property): Properties {
            requireThat(properties.isNotEmpty()) { "vararg properties should not be empty" }
            return Properties(properties.toMutableList())
        }

        @JvmSynthetic
        internal fun parse(caller: KClass<*>, input: String): Properties = input
            .replace(Patterns.EXCESSIVE_WHITESPACE, " ")
            .split(' ')
            .mapTo(ArrayList()) { Property.parse(caller, it) }
            .let(::Properties)
    }
}

