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

package dev.epubby.toc

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.internal.utils.buildPersistentList
import dev.epubby.resources.PageResource
import kotlinx.collections.immutable.PersistentList
import moe.kanon.kommons.INDEX_NOT_FOUND

class TableOfContents internal constructor(
    override val epub: Epub,
) : EpubElement, MutableIterable<TableOfContents.Entry> {
    override val elementName: String
        get() = "toc"

    val entries: MutableList<Entry> = arrayListOf()

    @JvmOverloads
    fun hasEntry(entry: Entry, maxDepth: Int = TRAVERSE_ALL): Boolean = TODO()

    @JvmOverloads
    fun hasEntryWithTitle(title: String, maxDepth: Int = TRAVERSE_ALL): Boolean = TODO()

    fun isEmpty(): Boolean = entries.isEmpty()

    fun isNotEmpty(): Boolean = entries.isNotEmpty()

    @JvmOverloads
    fun <R> collect(
        visitor: TableOfContentsVisitor<R>,
        maxDepth: Int = TRAVERSE_ALL,
    ): PersistentList<R> = buildPersistentList {
        TODO()
    }

    @JvmOverloads
    fun visit(
        visitor: TableOfContentsVisitor<*>,
        maxDepth: Int = TRAVERSE_ALL,
    ) {
        TODO()
    }

    override fun iterator(): MutableIterator<Entry> = entries.iterator()

    class Entry internal constructor(
        val container: TableOfContents,
        var parent: Entry?,
        var identifier: String,
        var title: String,
        var resource: PageResource?,
        var fragmentIdentifier: String?,
    ) : EpubElement, MutableIterable<Entry> {
        override val epub: Epub
            get() = container.epub

        override val elementName: String
            get() = "toc/entry"

        val children: MutableList<Entry> = arrayListOf()

        /**
         * Returns how deep into the hierarchy this entry is.
         *
         * If this entry is at the first level, that being the ones stored in [entries][TableOfContents.entries] in
         * [TableOfContents], then the returned value will be `0`.
         */
        val currentDepth: Int
            get() = when (val parent = parent) {
                null -> 0
                else -> parent.currentDepth + 1
            }

        // TODO: remove?
        val currentIndex: Int
            get() = when (val parent = parent) {
                null -> INDEX_NOT_FOUND
                else -> parent.children.indexOf(this)
            }

        fun isEmpty(): Boolean = children.isEmpty()

        fun isNotEmpty(): Boolean = children.isNotEmpty()

        override fun iterator(): MutableIterator<Entry> = children.iterator()

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Entry -> false
            parent != other.parent -> false
            identifier != other.identifier -> false
            title != other.title -> false
            resource != other.resource -> false
            fragmentIdentifier != other.fragmentIdentifier -> false
            children != other.children -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = parent?.hashCode() ?: 0
            result = 31 * result + identifier.hashCode()
            result = 31 * result + title.hashCode()
            result = 31 * result + resource.hashCode()
            result = 31 * result + (fragmentIdentifier?.hashCode() ?: 0)
            result = 31 * result + children.hashCode()
            return result
        }

        override fun toString(): String =
            "Entry(parent=$parent, identifier='$identifier', title='$title', resource=$resource, fragmentIdentifier=$fragmentIdentifier, children=$children)"
    }

    private companion object {
        private const val TRAVERSE_ALL: Int = -1
    }
}