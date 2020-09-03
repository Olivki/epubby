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

import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.internal.ifNotNull
import dev.epubby.resources.PageResource

class GuideReference @JvmOverloads constructor(
    override val book: Book,
    val type: ReferenceType,
    var reference: PageResource,
    var title: String? = null,
) : BookElement {
    override val elementName: String
        get() = "PackageGuide.Reference"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is GuideReference -> false
        type != other.type -> false
        reference != other.reference -> false
        title != other.title -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + reference.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = buildString {
        append("Reference(")
        append("type=$type")
        append(", reference=$reference")
        title ifNotNull { append(", title='$it'") }
        append(")")
    }
}