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

package moe.kanon.epubby.structs

import moe.kanon.epubby.Book
import moe.kanon.epubby.utils.attr
import moe.kanon.kommons.io.paths.name
import org.jdom2.Attribute
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path
import java.util.UUID

/**
 * Represents an identifier value used through-out a [Book] instance.
 */
class Identifier private constructor(val value: String) {
    @JvmSynthetic
    internal fun toAttribute(name: String = "id", namespace: Namespace = Namespace.NO_NAMESPACE): Attribute =
        Attribute(name, value, namespace)

    @JvmSynthetic
    operator fun component1(): String = value

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Identifier -> false
        value != other.value -> false
        else -> true
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    companion object {
        /**
         * Returns a new identifier where the `value` is a [random-uuid][UUID.randomUUID].
         */
        @JvmStatic
        fun createUnique(): Identifier = Identifier(UUID.randomUUID().toString())

        /**
         * Returns a new identifier where the `value` is the given [value].
         */
        @JvmStatic
        fun of(value: String): Identifier = Identifier(value)

        /**
         * Returns a new identifier where the [name][Path.name] of the given [file] is used as the value.
         */
        @JvmStatic
        fun fromFile(file: Path): Identifier = Identifier(file.name)

        @JvmSynthetic
        internal fun fromElement(element: Element, container: Path, current: Path): Identifier =
            Identifier(element.attr("id", container, current))
    }
}