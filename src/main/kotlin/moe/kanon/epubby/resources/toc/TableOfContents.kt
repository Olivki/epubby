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
    data class Entry internal constructor(
        val parent: Entry?,
        val title: String,
        val resource: PageResource?,
        val fragmentIdentifier: String?,
        private val _children: MutableList<Entry> = ArrayList()
    ) : Iterable<Entry> {
        /**
         * Returns a list of all the children `this` entry has, or an empty list if `this` entry has no children.
         */
        val children: ImmutableList<Entry> get() = _children.toImmutableList()

        /**
         * Returns a new instance of `this` entry without its [parent], or `this` if `this` entry has no parent.
         */
        val detached: Entry
            @JvmName("detached") get() = if (parent != null) copy(parent = null) else this

        operator fun contains(title: String): Boolean = _children.any { it.title == title }

        operator fun contains(resource: PageResource): Boolean = _children.any { it.resource == resource }

        operator fun contains(entry: Entry): Boolean = entry in _children

        override fun iterator(): Iterator<Entry> = _children.iterator().asUnmodifiable()

        override fun toString(): String = "Entry[TODO]"
    }

    internal companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book, entries: MutableList<Entry>): TableOfContents =
            TableOfContents(book, entries)
    }
}