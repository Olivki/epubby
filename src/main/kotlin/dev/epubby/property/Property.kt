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

package dev.epubby.property

import dev.epubby.Epub3Feature
import dev.epubby.prefix.Prefix
import org.xbib.net.IRI

@Epub3Feature
public sealed interface Property {
    /**
     * The prefix of the property.
     *
     * Used when [processing][process] the property.
     */
    public val prefix: Prefix

    /**
     * The entry that the property is referring to.
     */
    public val reference: String

    public fun process(): IRI = prefix.iri.resolve(reference)

    public infix fun matches(other: Property): Boolean = when {
        prefix != other.prefix -> false
        reference != other.reference -> false
        else -> true
    }

    public fun asString(): String = when {
        prefix.name.isBlank() -> reference
        else -> "${prefix.name}:$reference"
    }
}