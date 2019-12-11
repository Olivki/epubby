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

package moe.kanon.epubby.resources.toc

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.resources.PageResource
import moe.kanon.kommons.collections.asUnmodifiable


class TableOfContents private constructor(val book: Book, val _entries: MutableList<Entry>) {
    // TODO: Functions for deep-removal of all entries that point towards a specific entry and the like
    // TODO: Functions for retrieving the deepest child(?)
    class Entry private constructor(
        val parent: Entry?,
        val title: String,
        val resource: PageResource?,
        val fragmentIdentifier: String?,
        private val entries: MutableList<Entry> = ArrayList()
    ) : Iterable<Entry> {
        val children: ImmutableList<Entry> get() = entries.toImmutableList()

        // TODO: This?
        //val detached: Entry
        //    @JvmName("detached") get() = if (parent != null) copy(parent = null) else this

        @JvmOverloads
        fun addChild(title: String, resource: PageResource? = null, fragmentIdentifier: String? = null): Entry {
            val entry = Entry(this, title, resource, fragmentIdentifier)
            entries.add(entry)
            return entry
        }

        @JvmOverloads
        fun removeChild(title: String, ignoreCase: Boolean = false): Boolean =
            entries.firstOrNull { it.title.equals(title, ignoreCase) }?.let(entries::remove) ?: false

        @JvmOverloads
        fun removeChildren(title: String, ignoreCase: Boolean = false): Boolean =
            entries.removeIf { it.title.equals(title, ignoreCase) }

        fun removeChildren(resource: PageResource): Boolean = entries.removeIf { it.resource == resource }

        fun removeChildren(children: Iterable<Entry>): Boolean = entries.removeAll(children)

        /**
         * Returns a list of all the children of `this` entry that have a [title][Entry.title] that matches the given
         * [title], or an empty-list if none are found.
         */
        @JvmOverloads
        fun getChildrenByTitle(title: String, ignoreCase: Boolean = false): ImmutableList<Entry> =
            entries.filter { it.title.equals(title, ignoreCase) }.toImmutableList()

        /**
         * Returns a list of all the children of `this` entry that have a [resource][Entry.resource] that matches the
         * given [resource], or an empty-list if none are found.
         */
        fun getChildrenByResource(resource: PageResource): ImmutableList<Entry> =
            entries.filter { it.resource == resource }.toImmutableList()

        @JvmName("hasChildWithTitle")
        operator fun contains(title: String): Boolean = entries.any { it.title == title }

        @JvmName("hasChildFor")
        operator fun contains(resource: PageResource): Boolean = entries.any { it.resource == resource }

        @JvmName("isChild")
        operator fun contains(entry: Entry): Boolean = entry in entries

        override fun iterator(): Iterator<Entry> = entries.iterator().asUnmodifiable()

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Entry -> false
            parent != other.parent -> false
            title != other.title -> false
            resource != other.resource -> false
            fragmentIdentifier != other.fragmentIdentifier -> false
            entries != other.entries -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = parent?.hashCode() ?: 0
            result = 31 * result + title.hashCode()
            result = 31 * result + (resource?.hashCode() ?: 0)
            result = 31 * result + (fragmentIdentifier?.hashCode() ?: 0)
            result = 31 * result + entries.hashCode()
            return result
        }

        override fun toString(): String = "Entry[TODO]"
    }

    internal companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book, entries: MutableList<Entry>): TableOfContents =
            TableOfContents(book, entries)
    }
}