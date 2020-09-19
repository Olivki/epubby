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

package dev.epubby.packages.guide

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.internal.utils.ifNotNull
import dev.epubby.resources.PageResource

class CustomGuideReference internal constructor(
    override val epub: Epub,
    val type: String,
    var reference: PageResource,
    var title: String?,
) : EpubElement {
    init {
        require(ReferenceType.isUnknownType(type)) { "The type of a custom guide reference ($type) must not be an officially known type." }
    }

    override val elementName: String
        get() = "PackageGuide.CustomReference"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is CustomGuideReference -> false
        !type.equals(other.type, ignoreCase = true) -> false
        reference != other.reference -> false
        title != other.title -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = type.toLowerCase().hashCode()
        result = 31 * result + reference.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = buildString {
        append("Reference(")
        append("customType='$type'")
        append(", reference=$reference")
        title ifNotNull { append(", title='$it'") }
        append(")")
    }
}