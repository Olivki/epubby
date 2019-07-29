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

package moe.kanon.epubby.toc

import moe.kanon.epubby.Book
import moe.kanon.epubby.DocumentSerializer
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.EpubVersion
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.utils.parseDocFile
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import org.jdom2.Document
import org.jdom2.Element
import java.nio.file.Path

class TableOfContents private constructor(val book: Book, private val entries: MutableList<Entry>) : DocumentSerializer,
    Iterable<TableOfContents.Entry> {
    companion object {
        @EpubVersion(Book.Format.EPUB_2_0)
        internal fun parseNcx(book: Book, ncxFile: Path): TableOfContents = parseXmlFile(ncxFile) {
            TODO()
        }

        @EpubVersion(Book.Format.EPUB_3_0)
        internal fun parseNavDoc(book: Book, navFile: Path): TableOfContents = parseDocFile(navFile) {
            TODO()
        }

        /**
         * Returns a new [TableOfContents] instance based on the entries of the result of applying [scope] to a new
         * [TOCContainer].
         */
        @JvmStatic fun build(book: Book, scope: TOCContainer.() -> Unit): TableOfContents {
            val entries = TOCContainer().apply(scope).entries.map(TOCEntryContainer::toEntry).toMutableList()
            return TableOfContents(book, entries)
        }
    }

    override fun iterator(): Iterator<Entry> = entries.iterator().asUnmodifiable()

    override fun toDocument(): Document {
        TODO("not implemented")
    }

    /**
     * TODO
     *
     * @property [parent] The parent [Entry] of `this` entry, or [None] if this entry has no parent.
     *
     * An entry will *not* have a parent if they're the first elements found in the [TableOfContents].
     * @property [title] TODO
     * @property [resource] TODO
     * @property [fragmentIdentifier] The fragment-identifier of this entry.
     *
     * The fragment-identifier is that which TODO
     */
    data class Entry @JvmOverloads constructor(
        val parent: Option<Entry> = None,
        val title: String,
        val resource: PageResource,
        val fragmentIdentifier: Option<String> = None,
        private val children: MutableList<Entry> = ArrayList()
    ) : ElementSerializer, Iterable<Entry> {
        /**
         * Returns a new instance of `this` entry where the [parent] property has been set to [None], or `this` if
         * `parent` is already `None`.
         */
        val detached: Entry get() = if (parent.isPresent) copy(parent = None) else this

        /**
         * Returns how many children this entry has.
         */
        val size: Int get() = children.size

        /**
         * Returns `true` if this entry has no children, `false` if it does.
         */
        val isEmpty: Boolean get() = children.isEmpty()

        /**
         * Returns `true` if this entry has children, `false` if it does not.
         */
        val isNotEmpty: Boolean get() = children.isNotEmpty()

        /**
         * Returns whether or not this entry has a child with the given [title].
         */
        @JvmName("hasChild")
        operator fun contains(title: String): Boolean = children.any { it.title == title }

        /**
         * Returns whether or not this entry has a child that references the given [resource].
         */
        @JvmName("hasChild")
        operator fun contains(resource: PageResource): Boolean = children.any { it.resource == resource }

        /**
         * Returns whether or not the given [entry] is a child of this entry.
         */
        @JvmName("isChild")
        operator fun contains(entry: Entry): Boolean = entry in children

        override fun iterator(): Iterator<Entry> = children.iterator().asUnmodifiable()

        override fun toElement(): Element {
            TODO("not implemented")
        }
    }
}