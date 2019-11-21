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
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.utils.attr
import moe.kanon.kommons.io.paths.name
import org.jdom2.Attribute
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path

/**
 * Represents the `id` attribute.
 *
 * TODO: Some sort of verification of the validity of `value` as an identifier?
 */
data class Identifier(val value: String) {
    private lateinit var resource: Resource

    fun findResourceOrNull(book: Book): Resource? = TODO()

    @JvmSynthetic
    internal fun toAttribute(namespace: Namespace = Namespace.NO_NAMESPACE): Attribute =
        Attribute("id", value, namespace)

    companion object {
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