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

package moe.kanon.epubby.resources.toc

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.internal.Patterns
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.NonEmptyList
import moe.kanon.epubby.structs.nonEmptyListOf
import moe.kanon.epubby.structs.toNonEmptyList
import moe.kanon.kommons.checkThat
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.io.paths.touch
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Element
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Path
import moe.kanon.epubby.resources.toc.NavigationDocument.Content.Link as NavContentLink
import moe.kanon.epubby.resources.toc.NavigationDocument.Content.Span as NavContentSpan
import moe.kanon.epubby.resources.toc.NavigationDocument.ListItem as NavListItem
import moe.kanon.epubby.resources.toc.NavigationDocument.OrderedList as NavOrderedList

class TableOfContents private constructor(
    val book: Book,
    val entries: NonEmptyList<Entry>,
    val navigationDocument: NavigationDocument? = null // TODO: Make it not be 'null'?
) : Iterable<TableOfContents.Entry> {
    lateinit var ncxDocument: NcxDocument
        @JvmSynthetic internal set

    fun getOrCreatePage(): Page {
        TODO(
            """
            Create and return a custom HTML5 page that represents the toc if EPUB is 2.0 (also add it to the resources 
            and mark it as the toc in the guide of the book), otherwise create and return the navigationDocument page
        """.trimIndent()
        )
    }

    // TODO: make these update functions smarter in the way that they update things, so that they don't just create
    //       everything from scratch again

    private fun updateNcxDoc() {
        logger.debug { "Updating ncx document information to match book information.." }
        ncxDocument.apply {
            title.apply {
                text = text.copy(content = book.title)
            }
            authors.apply {
                clear()
                addAll(book.metadata.authors.map { NcxDocument.DocAuthor(NcxDocument.Text(it.content)) })
            }
            navMap.points.clear()
            navMap.points.addAll(entries.mapNotNull(::createNcxNavPoint))
        }
    }

    private fun updateNavDoc() {
        navigationDocument?.apply {
            logger.debug { "Updating navigation document information to match book information.." }
            tocNav.orderedList.apply {
                entries.tail.clear()
                val newEntries = this@TableOfContents.entries.map(::createNavItem).toNonEmptyList()
                entries[0] = newEntries.head
                entries.addAll(newEntries.drop(1))
            }
        }
    }

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        updateNcxDoc()
        updateNavDoc()
        ncxDocument.writeToFile(fileSystem)
    }

    override fun iterator(): Iterator<Entry> = entries.iterator()

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
        fun addChild(
            identifier: Identifier,
            title: String,
            resource: PageResource? = null,
            fragmentIdentifier: String? = null
        ): Entry {
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
        internal fun fromNcxDocument(book: Book, ncx: NcxDocument): TableOfContents {
            val entries = ncx.navMap.points
                .asSequence()
                .map { createEntryFromNcx(book, null, it) }
                .filterNotNull()
                // TODO: .ifEmpty {  }
                .toNonEmptyList()
            return TableOfContents(book, entries).apply { ncxDocument = ncx }
        }

        private fun createNcxNavPoint(entry: Entry): NcxDocument.NavPoint? {
            if (entry.resource == null) return null

            // TODO: Check if the 2.x TOC format can store entries without any refs

            val identifier = entry.identifier
            // TODO: This
            val fixedPath = entry.resource.relativeHref.substringAfter("../")
            val pathWithFragment = entry.fragmentIdentifier?.let { "$fixedPath#$it" } ?: fixedPath
            val content = NcxDocument.Content(URI(pathWithFragment))
            val title = NcxDocument.NavLabel(NcxDocument.Text(entry.title))
            val children = entry.children.mapNotNullTo(ArrayList(), this::createNcxNavPoint)
            return NcxDocument.NavPoint(identifier, content, nonEmptyListOf(title), children = children)
        }

        private fun createEntryFromNcx(book: Book, parent: Entry?, point: NcxDocument.NavPoint): Entry {
            val title = point.labels.head.text.content
            val identifier = point.identifier
            val resource = point.content.toResource(book)
            checkThat(resource is PageResource) { "'resource' should be a page-resource: $resource" }
            val fragmentIdentifier = point.content.source.fragment
            return Entry(parent, identifier, title, resource, fragmentIdentifier).apply {
                children.addAll(point.children.map { createEntryFromNcx(book, this, it) })
            }
        }

        @JvmSynthetic
        internal fun fromNavigationDocument(book: Book, nav: NavigationDocument): TableOfContents {
            val entries = nav.tocNav.orderedList.entries.map { createEntryFromNav(book, null, it) }.toNonEmptyList()
            return TableOfContents(book, entries, nav).apply {
                val file: Path = book.packageRoot.resolve("toc.ncx").touch()
                ncxDocument = NcxDocument.create(book, file, entries.mapNotNull(::createNcxNavPoint).toNonEmptyList())
            }
        }

        // TODO: maybe filter through the available 'Content.Link' instances and match any known 'href' attributes and
        //       replace those and remove any non-known ones? Could do something similar for 'span' elements where we
        //       match any known titles, but we'd want to do some serious double checking there by checking against
        //       parents and what have you to make sure that everything matches maybe? Or maybe just don't support the
        //       creation of 'span' types in the framework in the future, so that we don't need to have 'resource' in
        //       'Entry' marked as nullable

        private fun createNavItem(entry: Entry): NavListItem {
            val content = when (entry.resource) {
                null -> NavContentSpan(Element("span").text(entry.title), Attributes())
                else -> {
                    val path = entry.resource.relativeHref.substringAfter("../")
                    val pathWithFragment = entry.fragmentIdentifier?.let { "$path#$it" } ?: path
                    NavContentLink(URI(pathWithFragment), Element("a").text(entry.title), Attributes())
                }
            }
            val children = entry.children.map(this::createNavItem)
            val orderedList = when {
                children.isNotEmpty() -> NavOrderedList(children.toNonEmptyList(), Attributes())
                else -> null
            }
            return NavListItem(content, orderedList, Attributes())
        }

        private fun createEntryFromNav(book: Book, parent: Entry?, item: NavListItem): Entry {
            val title = item.content.phrasingContent.text()
            val identifier = item
                .content
                .attributes
                .firstOrNull { it.key == "id" }
                // TODO: this might fail if we implement proper checks for 'Identifier' later on, because the title
                //       might contain invalid symbols
                .let { Identifier.of(it?.value ?: title.toLowerCase().replace(Patterns.WHITESPACE, "_")) }
            val resource = when (item.content) {
                is NavContentLink -> item.content.toResource(book)
                is NavContentSpan -> null
            }
            checkThat(resource is PageResource?) { "'resource' should be a page-resource: $resource" }
            val fragmentIdentifier = when (item.content) {
                is NavContentLink -> item.content.href.fragment
                is NavContentSpan -> null
            }
            return Entry(parent, identifier, title, resource, fragmentIdentifier).apply {
                item.orderedList?.also { ol ->
                    children.addAll(ol.entries.map { createEntryFromNav(book, this, it) })
                }
            }
        }
    }
}