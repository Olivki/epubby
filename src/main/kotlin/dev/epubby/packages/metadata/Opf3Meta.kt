/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.packages.metadata

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.dublincore.DublinCore
import dev.epubby.properties.Property
import dev.epubby.utils.Direction

class Opf3Meta(
    override val epub: Epub,
    var value: String,
    var property: Property,
    var identifier: String? = null,
    var direction: Direction? = null,
    var refines: DublinCore? = null,
    var scheme: String? = null,
    var language: String? = null,
) : EpubElement {
    override val elementName: String
        get() = "PackageMetadata.Opf3Meta"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Opf3Meta -> false
        value != other.value -> false
        property != other.property -> false
        identifier != other.identifier -> false
        direction != other.direction -> false
        refines != other.refines -> false
        scheme != other.scheme -> false
        language != other.language -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + property.hashCode()
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + (direction?.hashCode() ?: 0)
        result = 31 * result + (refines?.hashCode() ?: 0)
        result = 31 * result + (scheme?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "Opf3Meta(content='$value', property=$property, identifier=$identifier, direction=$direction, refines=$refines, scheme=$scheme, language=$language)"
}