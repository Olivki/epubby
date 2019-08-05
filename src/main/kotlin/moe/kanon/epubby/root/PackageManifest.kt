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

@file:Suppress("DataClassPrivateConstructor")

package moe.kanon.epubby.root

import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.EpubVersion
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.logger
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.root.ManifestItem.Local
import moe.kanon.epubby.root.ManifestItem.Remote
import moe.kanon.epubby.utils.getAttributeValueOrNone
import moe.kanon.epubby.utils.stringify
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.getValueOrNone
import moe.kanon.kommons.requireThat
import org.apache.commons.validator.routines.UrlValidator
import org.jdom2.Attribute
import org.jdom2.Element
import org.jdom2.output.Format
import java.nio.file.Path

/**
 * Represents the [manifest](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-manifest)
 * element.
 *
 * The manifest provides an exhaustive list of the [resources][Resource] used by the [book].
 */
class PackageManifest private constructor(
    val book: Book,
    val identifier: Option<String>,
    @JvmSynthetic internal val items: MutableMap<String, ManifestItem<*>>
) : ElementSerializer, Iterable<ManifestItem<*>> {
    companion object {
        internal fun parse(book: Book, packageDocument: Path, element: Element): PackageManifest = with(element) {
            fun malformed(reason: String): Nothing = raiseMalformedError(book.originFile, packageDocument, reason)
            fun hrefToPath(href: String): Path = book.pathOf(href)
                .let { if (it.isAbsolute) it else packageDocument.parent.resolve(it) }

            val items: MutableMap<String, ManifestItem<*>> = getChildren("item", namespace)
                .asSequence()
                .map { createItem(it, ::malformed, ::hrefToPath) }
                .associateByTo(HashMap()) { it.identifier }
            if (items.isEmpty()) malformed("'manifest' element needs to contain at least one 'item' child, but it's empty")
            return@with PackageManifest(book, getAttributeValueOrNone("id"), items)
        }

        private fun createItem(
            element: Element,
            malformed: (String) -> Nothing,
            getPath: (String) -> Path
        ): ManifestItem<*> {
            val urlValidator = UrlValidator()
            val textual = element.stringify(Format.getCompactFormat())
            val id = element.getAttributeValue("id") ?: malformed("item element is missing required 'id' element")
            val href =
                element.getAttributeValue("href") ?: malformed("item element is missing required 'href' element")
            val fallback: String? = element.getAttributeValue("fallback")
            val mediaType: String? = element.getAttributeValue("media-type")
            val mediaOverlay: String? = element.getAttributeValue("media-overlay")
            val properties: String? = element.getAttributeValue("properties")
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
            * property, in fact, it does not even have a `properties` attribute at all. Needless to say, this
            * confused me a bit, so for safetys sake, I'll be double checking whether a resource is remote
            * or not, just to try and avoid any possibly issues.
            */
            return if (element.attributes.any { attr -> attr.name == "properties" }) {
                if ("remote-resources" in element.getAttributeValue("properties")) {
                    Remote(id, href, fallback, mediaType, mediaOverlay, properties)
                } else Local(id, getPath(href), fallback, mediaType, mediaOverlay, properties)
            } else {
                if (urlValidator.isValid(href)) {
                    logger.warn { "'item' element has href that's a valid url, but no 'remote-resources' property; $textual" }
                    Remote(id, href, fallback, mediaType, mediaOverlay, properties)
                } else Local(id, getPath(href), fallback, mediaType, mediaOverlay, properties)
            }
        }
    }

    // -- ITEMS -- \\
    /**
     * Returns the [item][ManifestItem] stored under the given [id], or throws a [NoSuchElementException] if none is
     * found.
     */
    fun getItem(id: String): ManifestItem<*> =
        items[id] ?: throw NoSuchElementException("No manifest item found under id <$id>")

    /**
     * Returns the [item][ManifestItem] stored under the given [id], or [None] if none is found.
     */
    fun getItemOrNone(id: String): Option<ManifestItem<*>> = items.getValueOrNone(id)

    // -- LOCAL ITEMS -- \\
    /**
     * Returns the [local item][ManifestItem.Local] stored under the given [id], or throws a [NoSuchElementException]
     * if none is found.
     *
     * @throws [IllegalArgumentException] if the [item][ManifestItem] stored under the given [id] is a
     * [remote item][ManifestItem.Remote]
     */
    fun getLocalItem(id: String): Local = items[id]?.let {
        requireThat(it is Local) { "Given id <$id> points towards a remote item, expected a local item" }
        return@let it as Local // TODO: type inference is being buggy in kotlin 1.3.41
    } ?: throw NoSuchElementException("No manifest item found under id <$id>")

    /**
     * Returns the [local item][ManifestItem.Local] stored under the given [id], or [None] if none is found.
     *
     * @throws [IllegalArgumentException] if the [item][ManifestItem] stored under the given [id] is a
     * [remote item][ManifestItem.Remote]
     */
    fun getLocalItemOrNone(id: String): Option<Local> = items.getValueOrNone(id).map {
        requireThat(it is Local) { "Given id <$id> points towards a remote item, expected a local item" }
        return@map it as Local // TODO: type inference is being buggy in kotlin 1.3.41
    }

    // -- REMOTE ITEMS -- \\
    fun addRemoteItem(id: String, item: Remote): Nothing = TODO()

    /**
     * Returns the [remote item][ManifestItem.Remote] stored under the given [id], or throws a [NoSuchElementException]
     * if none is found.
     *
     * @throws [IllegalArgumentException] if the [item][ManifestItem] stored under the given [id] is a
     * [local item][ManifestItem.Local]
     */
    fun getRemoteItem(id: String): Remote = items[id]?.let {
        requireThat(it is Remote) { "Given id <$id> points towards a local item, expected a remote item" }
        return@let it as Remote // TODO: type inference is being buggy in kotlin 1.3.41
    } ?: throw NoSuchElementException("No manifest item found under id <$id>")

    /**
     * Returns the [remote item][ManifestItem.Remote] stored under the given [id], or [None] if none is found.
     *
     * @throws [IllegalArgumentException] if the [item][ManifestItem] stored under the given [id] is a
     * [local item][ManifestItem.Local]
     */
    fun getRemoteItemOrNone(id: String): Option<Remote> = items.getValueOrNone(id).map {
        requireThat(it is Remote) { "Given id <$id> points towards a local item, expected a remote item" }
        return@map it as Remote // TODO: type inference is being buggy in kotlin 1.3.41
    }

    // TODO: Add functions for modifying the manifest?
    // Not sure how needed that is seeing as the 'ResourceRepository' is the class that will mainly be dealing with
    // updating and keeping the manifest updated, only thing it would be needed for would be for remote items?

    override fun toElement(): Element = Element("manifest", PackageDocument.NAMESPACE).apply {
        for ((_, item) in items) {
            if (item is Local) {
                addContent(
                    item.toElement().setAttribute(
                        "href",
                        // pretty ugly
                        book.packageDocument.file
                            .relativize(item.href)
                            .toString()
                            .substringAfter("..${book.fileSystem.separator}")
                    )
                )
            } else addContent(item.toElement())
        }
    }

    override fun iterator(): Iterator<ManifestItem<*>> = items.values.iterator().asUnmodifiable()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageManifest -> false
        book != other.book -> false
        identifier != other.identifier -> false
        items != other.items -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + items.hashCode()
        return result
    }

    override fun toString(): String = "PackageManifest(book=$book, identifier=$identifier, items=$items)"
}

/**
 * Returns the [item][ManifestItem] stored under the given [id], or throws a [NoSuchElementException] if none is
 * found.
 */
operator fun PackageManifest.get(id: String): ManifestItem<*> = this.getItem(id)

/**
 * Represents an `item` element that may be a [local][Local] item, or a [remote][Remote] item.
 */
sealed class ManifestItem<out T> : ElementSerializer {
    @SerializedName("id")
    abstract val identifier: String
    abstract val href: T
    abstract val mediaType: Option<String>
    abstract val fallback: Option<String>
    @EpubVersion(Book.Format.EPUB_3_0)
    abstract val mediaOverlay: Option<String>
    @EpubVersion(Book.Format.EPUB_3_0)
    abstract val properties: Option<String>

    /**
     * A list containing any extra attributes that may exist on this `item` element.
     *
     * This is here to catch any extra attributes that may exist on the element as the EPUB 2.01 specification
     * allows extra attributes other than the ones explicitly defined if the `item` is an "Out-Of-Line XML Island".
     */
    val extraAttributes: MutableList<Attribute> = ArrayList()

    abstract override fun toElement(): Element

    data class Local private constructor(
        @SerializedName("id") override val identifier: String,
        override val href: Path,
        override val fallback: Option<String>,
        override val mediaType: Option<String>,
        override val mediaOverlay: Option<String>,
        override val properties: Option<String>
    ) : ManifestItem<Path>() {
        @JvmOverloads constructor(
            id: String,
            href: Path,
            fallback: String? = null,
            mediaType: String? = null,
            mediaOverlay: String? = null,
            properties: String? = null
        ) : this(id, href, Option(fallback), Option(mediaType), Option(mediaOverlay), Option(properties))

        /**
         * Returns the [Resource] instance that this `item` points towards.
         *
         * @throws [NoSuchElementException] if this `item` doesn't point towards any known resources
         */
        fun getResource(book: Book): Resource = TODO()

        override fun toElement(): Element = Element("item", PackageDocument.NAMESPACE).apply {
            setAttribute("id", identifier)
            //setAttribute("href", href.toString())
            fallback.ifPresent { setAttribute("fallback", it) }
            mediaType.ifPresent { setAttribute("media-type", it) }
            mediaOverlay.ifPresent { setAttribute("media-overlay", it) }
            properties.ifPresent { setAttribute("properties", it) }
        }
    }

    data class Remote private constructor(
        @SerializedName("id") override val identifier: String,
        override val href: String,
        override val fallback: Option<String>,
        override val mediaType: Option<String>,
        override val mediaOverlay: Option<String>,
        override val properties: Option<String>
    ) : ManifestItem<String>() {
        @JvmOverloads constructor(
            id: String,
            href: String,
            fallback: String? = null,
            mediaType: String? = null,
            mediaOverlay: String? = null,
            properties: String? = null
        ) : this(id, href, Option(fallback), Option(mediaType), Option(mediaOverlay), Option(properties))

        override fun toElement(): Element = Element("item", PackageDocument.NAMESPACE).apply {
            setAttribute("id", identifier)
            setAttribute("href", href)
            fallback.ifPresent { setAttribute("fallback", it) }
            mediaType.ifPresent { setAttribute("media-type", it) }
            mediaOverlay.ifPresent { setAttribute("media-overlay", it) }
            properties.ifPresent { setAttribute("properties", it) }
        }
    }
}