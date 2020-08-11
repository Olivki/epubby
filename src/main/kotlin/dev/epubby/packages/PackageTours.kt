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

package dev.epubby.packages

import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.BookVersion
import dev.epubby.internal.MarkedAsDeprecated
import dev.epubby.utils.NonEmptyList

@MarkedAsDeprecated(`in` = BookVersion.EPUB_2_0)
class PackageTours(override val book: Book, val entries: MutableList<Tour>) : BookElement, Iterable<PackageTours.Tour> {
    override val elementName: String
        get() = "PackageTours"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageTours -> false
        entries != other.entries -> false
        else -> true
    }

    override fun hashCode(): Int = entries.hashCode()

    override fun toString(): String = "PackageTours(entries=$entries)"

    override fun iterator(): Iterator<Tour> = entries.iterator()

    class Tour(
        override val book: Book,
        val identifier: String,
        var title: String,
        val sites: NonEmptyList<Site>
    ) : BookElement, Iterable<Tour.Site> {
        override val elementName: String
            get() = "PackageTours.Tour"

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Tour -> false
            identifier != other.identifier -> false
            title != other.title -> false
            sites != other.sites -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = identifier.hashCode()
            result = 31 * result + title.hashCode()
            result = 31 * result + sites.hashCode()
            return result
        }

        override fun toString(): String = "Tour(identifier='$identifier', title='$title', sites=$sites)"

        override fun iterator(): Iterator<Site> = sites.iterator()

        class Site(override val book: Book, val href: String, val title: String) : BookElement {
            override val elementName: String
                get() = "PackageTours.Tour.Site"

            override fun equals(other: Any?): Boolean = when {
                this === other -> true
                other !is Site -> false
                href != other.href -> false
                title != other.title -> false
                else -> true
            }

            override fun hashCode(): Int {
                var result = href.hashCode()
                result = 31 * result + title.hashCode()
                return result
            }

            override fun toString(): String = "Site(href='$href', title='$title')"
        }
    }
}