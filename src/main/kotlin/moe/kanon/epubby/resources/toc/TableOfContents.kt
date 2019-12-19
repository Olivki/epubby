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
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.epubby.structs.Identifier
import moe.kanon.kommons.checkThat
import moe.kanon.kommons.collections.asUnmodifiable
import java.nio.file.FileSystem


class TableOfContents private constructor(
    val book: Book,
    val entries: MutableList<Entry>,
    @get:JvmSynthetic internal var ncxDocument: NcxDocument? = null,
    @get:JvmSynthetic internal var navigationDocument: NavigationDocument? = null
) : Iterable<TableOfContents.Entry> {
    fun createPage(): Page {
        TODO("""
            Create and return a custom HTML5 page that represents the toc if EPUB is 2.0 (also add it to the resources 
            and mark it as the toc in the guide of the book), otherwise create and return the navigationDocument page
        """.trimIndent())
    }

    override fun iterator(): Iterator<Entry> = entries.iterator()

    private fun update() {
        ncxDocument?.also { ncx ->
            ncx.navMap.points.clear()
            ncx.navMap.points.addAll(entries.map { NcxDocument.NavPoint.fromEntry(it) })
        }
    }

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        update()
        ncxDocument?.also { it.writeToFile(fileSystem) }
    }

    // TODO: Functions for deep-removal of all entries that point towards a specific entry and the like
    // TODO: Functions for retrieving the deepest child(?)
    class Entry internal constructor(
        val parent: Entry?,
        var identifier: Identifier,
        var title: String,
        val resource: PageResource?,
        var fragmentIdentifier: String?
    ) : Iterable<Entry> {
        val children: MutableList<Entry> = ArrayList()

        // TODO: This?
        //val detached: Entry
        //    @JvmName("detached") get() = if (parent != null) copy(parent = null) else this

        @JvmOverloads
        fun addChild(identifier: Identifier, title: String, resource: PageResource? = null, fragmentIdentifier: String? = null): Entry {
            val entry = Entry(this, identifier, title, resource, fragmentIdentifier)
            children.add(entry)
            return entry
        }

        @JvmOverloads
        fun removeChildWithTitle(title: String, ignoreCase: Boolean = false): Boolean =
            children.firstOrNull { it.title.equals(title, ignoreCase) }?.let(children::remove) ?: false

        @JvmOverloads
        fun removeChildrenWithTitle(title: String, ignoreCase: Boolean = false): Boolean =
            children.removeIf { it.title.equals(title, ignoreCase) }

        fun removeChildren(resource: PageResource): Boolean = children.removeIf { it.resource == resource }

        /**
         * Returns a list of all the children of `this` entry that have a [title][Entry.title] that matches the given
         * [title], or an empty-list if none are found.
         */
        @JvmOverloads
        fun getChildrenByTitle(title: String, ignoreCase: Boolean = false): ImmutableList<Entry> =
            children.filter { it.title.equals(title, ignoreCase) }.toImmutableList()

        /**
         * Returns a list of all the children of `this` entry that have a [resource][Entry.resource] that matches the
         * given [resource], or an empty-list if none are found.
         */
        fun getChildrenByResource(resource: PageResource): ImmutableList<Entry> =
            children.filter { it.resource == resource }.toImmutableList()

        @JvmName("hasChildWithTitle")
        operator fun contains(title: String): Boolean = children.any { it.title == title }

        @JvmName("hasChildFor")
        operator fun contains(resource: PageResource): Boolean = children.any { it.resource == resource }

        @JvmName("isChild")
        operator fun contains(entry: Entry): Boolean = entry in children

        override fun iterator(): Iterator<Entry> = children.iterator().asUnmodifiable()

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Entry -> false
            parent != other.parent -> false
            title != other.title -> false
            resource != other.resource -> false
            fragmentIdentifier != other.fragmentIdentifier -> false
            children != other.children -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = parent?.hashCode() ?: 0
            result = 31 * result + title.hashCode()
            result = 31 * result + (resource?.hashCode() ?: 0)
            result = 31 * result + (fragmentIdentifier?.hashCode() ?: 0)
            result = 31 * result + children.hashCode()
            return result
        }

        // having both 'parent' and 'children' shown in the 'toString' function would result in infinite recursion
        override fun toString(): String = buildString {
            append("TableOfContents.Entry(")
            if (parent != null) {
                append("parent=$parent, title='$title'")
            } else {
                append("title='$title'")
            }
            resource?.also { append(", resource=$resource") }
            fragmentIdentifier?.also { append(", fragmentIdentifier='$fragmentIdentifier'") }
            append(")")
        }
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromNcxDocument(ncx: NcxDocument): TableOfContents {
            val entries = ncx.navMap.points
                .asSequence()
                .map { createEntry(ncx.book, null, it) }
                .filterNotNullTo(ArrayList())
            return TableOfContents(ncx.book, entries, ncxDocument = ncx)
        }

        private fun createEntry(book: Book, parent: Entry?, point: NcxDocument.NavPoint): Entry {
            val title = point.labels.first().text.content
            val identifier = point.identifier
            val resource = point.content.toResource(book)
            checkThat(resource is PageResource) { "'resource' should be a page-resource: $resource" }
            val fragmentIdentifier = point.content.source.fragment
            return Entry(parent, identifier, title, resource, fragmentIdentifier).apply {
                children.addAll(point.children.map { createEntry(book, this, it) })
            }
        }

        @JvmSynthetic
        internal fun fromNavigationDocument(nav: NavigationDocument): TableOfContents = TODO()
    }
}