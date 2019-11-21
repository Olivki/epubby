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

package moe.kanon.epubby.packages

import com.google.common.net.MediaType
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.structs.props.vocabs.ManifestVocabulary
import moe.kanon.epubby.utils.Namespaces
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.malformed
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.filterValuesIsInstance
import moe.kanon.kommons.collections.getValueOrThrow
import moe.kanon.kommons.io.paths.exists
import org.apache.commons.validator.routines.UrlValidator
import org.jdom2.Element
import org.jdom2.Namespace
import java.net.URI
import java.nio.file.Path

/**
 * Represents the [manifest](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-manifest)
 * element.
 *
 * The manifest provides an exhaustive list of the [resources][Resource] used by the [book].
 */
class Manifest private constructor(
    val book: Book,
    val identifier: Identifier?,
    @get:JvmSynthetic internal val items: MutableMap<Identifier, Item<*>>
) : Iterable<Manifest.Item<*>> {
    val localItems: ImmutableMap<Identifier, Item.Local>
        get() = items.filterValuesIsInstance<Identifier, Item.Local>().toPersistentHashMap()

    val remoteItems: ImmutableMap<Identifier, Item.Remote>
        get() = items.filterValuesIsInstance<Identifier, Item.Remote>().toPersistentHashMap()

    /**
     * Removes all [local items][Item.Local] which has a [href][Item.Local.href] that points towards a
     * [non-existent][Path.notExists] file.
     */
    fun removeFaultyLocalItems() {
        val faultyItems = localItems.values
            .asSequence()
            .filterNot { it.href.exists }
            .map { it.identifier }
        items -= faultyItems
    }

    /**
     * Returns `true` if this manifest has an [item][Item] with the given [identifier], `false` otherwise.
     */
    fun hasItem(identifier: Identifier): Boolean = identifier in items

    /**
     * Returns `true` if this manifest contains the given [item], `false` otherwise.
     */
    fun hasItem(item: Item<*>): Boolean = items.containsValue(item)

    /**
     * Returns `true` if this manifest contains a [local item][Item.Local] that points towards the given [resource],
     * `false` otherwise.
     */
    fun hasResource(resource: Resource): Boolean = resource.identifier in items

    /**
     * Returns the [item][Item] stored under the given [identifier], or throws a [NoSuchElementException] if none is
     * found.
     */
    fun getItem(identifier: Identifier): Item<*> =
        items[identifier] ?: throw NoSuchElementException("No manifest item found under id <$identifier>")

    /**
     * Returns the [item][Item] stored under the given [identifier], or `null` if none is found.
     */
    fun getItemOrNull(identifier: Identifier): Item<*>? = items[identifier]

    // -- LOCAL ITEMS -- \\
    /**
     * Returns the [local item][Item.Local] stored under the given [identifier], or throws a [NoSuchElementException]
     * if none is found.
     */
    fun getLocalItem(identifier: Identifier): Item.Local =
        localItems.getValueOrThrow(identifier) { "No local manifest item found under id <$identifier>" }

    /**
     * Returns the [local item][Item.Local] stored under the given [identifier], or `null` if none is found.
     */
    fun getLocalItemOrNull(identifier: Identifier): Item.Local? = localItems[identifier]

    // -- REMOTE ITEMS -- \\
    fun addRemoteItem(identifier: Identifier, item: Item.Remote): Nothing = TODO()

    /**
     * Returns the [remote item][Item.Remote] stored under the given [identifier], or throws a [NoSuchElementException]
     * if none is found.
     */
    fun getRemoteItem(identifier: Identifier): Item.Remote =
        remoteItems.getValueOrThrow(identifier) { "No remote manifest item found under id <$identifier>" }

    /**
     * Returns the [remote item][Item.Remote] stored under the given [identifier], or `null` if none is found.
     */
    fun getRemoteItemOrNull(identifier: Identifier): Item.Remote? = remoteItems[identifier]

    // TODO: Check if this namespace is correct
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element {
        TODO()
    }

    override fun iterator(): Iterator<Item<*>> = items.values.iterator().asUnmodifiable()

    /**
     * Represents the [item](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-item-elem)
     * element.
     */
    sealed class Item<out T> {
        // TODO: Documentation
        abstract val identifier: Identifier
        abstract val href: T
        abstract var mediaType: MediaType?
        abstract var fallback: String?
        abstract var mediaOverlay: String?
        // TODO: Remember to not add the 'properties' attribute to the element if this is empty
        abstract val properties: Properties

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("item", namespace).apply {
            TODO()
        }

        /**
         * Represents a [local-item](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-local-resource).
         */
        data class Local internal constructor(
            override val identifier: Identifier,
            override val href: Path,
            override var mediaType: MediaType? = null,
            override var fallback: String? = null,
            override var mediaOverlay: String? = null,
            override val properties: Properties = Properties.empty()
        ) : Item<Path>() {
            // TODO: retrieve the resource with the 'identifier' and then make sure to verify that the resources 'file'
            //       points towards the same path as the 'href' of this local-item
            fun getResource(book: Book): Resource = TODO()
        }

        /**
         * Represents a [remote-item](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-remote-resource).
         */
        data class Remote internal constructor(
            override val identifier: Identifier,
            override var href: URI,
            override var mediaType: MediaType? = null,
            override var fallback: String? = null,
            override var mediaOverlay: String? = null,
            override val properties: Properties = Properties.empty()
        ) : Item<URI>()
    }

    internal companion object {
        @JvmField val URL_VALIDATOR = UrlValidator()

        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, documentFile: Path): Manifest = with(element) {
            val items: MutableMap<Identifier, Item<*>> = getChildren("item", namespace)
                .asSequence()
                .map { createItem(it, book, book.file, documentFile) }
                .onEach { logger.debug { "Constructed item instance <$it>" } }
                .associateByTo(HashMap()) { it.identifier }

            if (items.isEmpty()) {
                malformed(book.file, documentFile, "the 'manifest' element needs to contain at least one child")
            }

            return@with Manifest(book, getAttributeValue("id")?.let(::Identifier), items)
        }

        private fun getPathFromHref(book: Book, href: String, documentFile: Path): Path =
            book.getPath(href).let { if (it.isAbsolute) it else documentFile.parent.resolve(it) }

        private fun createItem(element: Element, book: Book, container: Path, current: Path): Item<*> = with(element) {
            val id = Identifier.fromElement(this, container, current)
            val href = element.attr("href", container, current)
            val fallback = element.getAttributeValue("fallback")
            val mediaType = element.getAttributeValue("media-type")?.let(MediaType::parse)
            val mediaOverlay = element.getAttributeValue("media-overlay")
            val properties = element.getAttributeValue("properties")?.let {
                Properties.parse(Manifest::class, it)
            } ?: Properties.empty()

            /*
            * Let's talk about 'local' and 'remote' resources for a bit, and by talk I mean, let's talk about
            * the specification. So the EPUB32 specification for "resource-locations" (https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-resource-locations)
            * states the following:
            *      "The inclusion of Remote Resources in an EPUB Publication is indicated via the
            *      remote-resources property on the manifest item element"
            *  while the actual specification for the `manifest` element shows the following example of a
            * remote resource:
            *      Manifest:
            *          <item id="audio01"
            *                href="http://www.example.com/book/audio/ch01.mp4"
            *                media-type="audio/mp4"/>
            *  Notice something missing there? That's right, it's missing the supposed "resource-locations"
            * property, in fact, it does not even have a `properties` attribute at all. So, because of this, just to be
            * extra sure, we're going to be making *very* sure that item element we're working on is *not* a remote one,
            * as that could end up with the system failing *very* hard.
            */

            return if (properties.isNotEmpty()) {
                if (ManifestVocabulary.REMOTE_RESOURCES in properties) {
                    Item.Remote(id, URI.create(href), mediaType, fallback, mediaOverlay, properties)
                } else {
                    Item.Local(id, getPathFromHref(book, href, current), mediaType, fallback, mediaOverlay, properties)
                }
            } else {
                if (URL_VALIDATOR.isValid(href)) {
                    logger.warn { "Encountered invalid 'item' element, 'href' is a URL, but it does not have a 'properties' attribute: $element" }
                    Item.Remote(id, URI.create(href), mediaType, fallback, mediaOverlay, properties)
                } else {
                    Item.Local(id, getPathFromHref(book, href, current), mediaType, fallback, mediaOverlay, properties)
                }
            }
        }
    }
}