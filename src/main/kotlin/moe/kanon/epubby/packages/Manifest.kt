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
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.internal.Namespaces
import moe.kanon.epubby.utils.internal.getBookPathFromHref
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.malformed
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.filterValuesIsInstance
import moe.kanon.kommons.collections.getValueOrThrow
import org.apache.commons.validator.routines.UrlValidator
import org.jdom2.Element
import org.jdom2.Namespace
import java.net.URI
import java.nio.file.Path
import java.util.EnumSet

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
     * Returns the [item][Item] stored under the given [identifier], or throws a [NoSuchElementException] if none is
     * found.
     */
    fun getItem(identifier: Identifier): Item<*> =
        items[identifier] ?: throw NoSuchElementException("No manifest item found under id <$identifier>")

    /**
     * Returns the [item][Item] stored under the given [identifier], or `null` if none is found.
     */
    fun getItemOrNull(identifier: Identifier): Item<*>? = items[identifier]

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
    fun hasItemFor(resource: Resource): Boolean = resource.identifier in items

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
    // we only allow the removal & addition of remote items, as local items should be created through resources rather
    // than this way

    fun addRemoteItem(item: Item.Remote): Item.Remote {
        items[item.identifier] = item
        return item
    }

    fun removeRemoteItem(identifier: Identifier): Boolean {
        val result = identifier in remoteItems
        if (result) {
            items -= identifier
        }
        return result
    }

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

    override fun iterator(): Iterator<Item<*>> = items.values.iterator().asUnmodifiable()

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("manifest", namespace).apply {
        identifier?.also { setAttribute(it.toAttribute()) }
        for ((_, item) in items) addContent(item.toElement(book))
    }

    @JvmSynthetic
    internal fun addItemForResource(resource: Resource, props: EnumSet<ManifestVocabulary>) {
        if (hasItemFor(resource)) {
            logger.warn { "There already exists an item entry for the given resource <$resource>" }
        } else {
            val item = Item.forResource(resource, props)
            items[item.identifier] = item
            logger.trace { "Added manifest local-item <$item> for resource <$resource>" }
        }
    }

    @JvmSynthetic
    internal fun updateManifestItemIdentifier(oldIdentifier: Identifier, newIdentifier: Identifier) {
        val oldItem = getLocalItem(oldIdentifier)
        logger.trace { "Updating identifier of local item <$oldItem> to '$newIdentifier'" }
        val newItem = oldItem.copy(identifier = newIdentifier)
        items -= oldIdentifier
        items[newIdentifier] = newItem
    }

    /**
     * Represents the [item](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-item-elem)
     * element.
     */
    sealed class Item<out T> {
        // TODO: Documentation
        abstract val identifier: Identifier
        abstract val href: T
        abstract val mediaType: MediaType?
        // TODO: Add the ability to define these things from the resource class somehow?
        abstract val fallback: String? // TODO: Change to identifier?
        abstract val mediaOverlay: String?
        abstract val properties: Properties

        @JvmSynthetic
        internal fun toElement(book: Book, namespace: Namespace = Namespaces.OPF): Element =
            Element("item", namespace).apply {
                setAttribute(identifier.toAttribute())
                setAttribute(
                    "href", when (this@Item) {
                        is Local -> book.packageFile.relativize(href).toString().substringAfter("../")
                        is Remote -> href.toString()
                    }
                )
                mediaType?.also { setAttribute("media-type", it.toString()) }
                fallback?.also { setAttribute("fallback", it) }
                mediaOverlay?.also { setAttribute("media-overlay", it) }
                if (properties.isNotEmpty()) setAttribute(properties.toAttribute())
            }

        /**
         * Represents a [local-item](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-local-resource).
         */
        data class Local internal constructor(
            override val identifier: Identifier,
            override val href: Path,
            override val mediaType: MediaType? = null,
            override val fallback: String? = null,
            override val mediaOverlay: String? = null,
            override val properties: Properties = Properties.empty()
        ) : Item<Path>()

        /**
         * Represents a [remote-item](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#dfn-remote-resource).
         */
        data class Remote internal constructor(
            override val identifier: Identifier,
            override val href: URI,
            override val mediaType: MediaType? = null,
            override val fallback: String? = null,
            override val mediaOverlay: String? = null,
            override val properties: Properties = Properties.empty()
        ) : Item<URI>()

        internal companion object {
            @JvmSynthetic
            internal fun forResource(resource: Resource, props: EnumSet<ManifestVocabulary>): Local = Item.Local(
                resource.identifier,
                resource.file,
                resource.mediaType,
                properties = Properties.copyOf(props)
            ).also { logger.trace { "Created manifest local-item instance <$it> for resource <$resource>" } }
        }
    }

    internal companion object {
        @JvmField val URL_VALIDATOR = UrlValidator()

        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, file: Path): Manifest = with(element) {
            val items: MutableMap<Identifier, Item<*>> = getChildren("item", namespace)
                .asSequence()
                .map { createItem(it, book, book.file, file) }
                .onEach { logger.trace { "Constructed manifest item instance <$it>" } }
                .associateByTo(HashMap()) { it.identifier }

            if (items.isEmpty()) {
                malformed(book.file, file, "the 'manifest' element needs to contain at least one child")
            }

            val identifier = getAttributeValue("id")?.let { Identifier.of(it) }

            return@with Manifest(book, identifier, items).also {
                logger.trace { "Constructed manifest instance <$it> from file '$file'" }
            }
        }

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
                    Item.Local(id, getBookPathFromHref(book, href, current), mediaType, fallback, mediaOverlay, properties)
                }
            } else {
                if (URL_VALIDATOR.isValid(href)) {
                    logger.warn { "Encountered invalid 'item' element, 'href' is a URL, but it does not have a 'properties' attribute: $element" }
                    Item.Remote(id, URI.create(href), mediaType, fallback, mediaOverlay, properties)
                } else {
                    Item.Local(id, getBookPathFromHref(book, href, current), mediaType, fallback, mediaOverlay, properties)
                }
            }
        }
    }
}