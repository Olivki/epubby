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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.PageProgressionDirection
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.internal.Namespaces
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.internal.malformed
import moe.kanon.epubby.utils.stringify
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.firstOrNone
import moe.kanon.kommons.func.getOrNone
import moe.kanon.kommons.lang.ParseException
import moe.kanon.kommons.lang.parse
import moe.kanon.kommons.requireThat
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.output.Format
import java.nio.file.Path
import kotlin.properties.Delegates

/**
 * Represents the [spine](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-spine) element.
 */
class Spine(
    val book: Book,
    var identifier: Identifier?,
    var pageProgressionDirection: PageProgressionDirection?,
    private var tableOfContentsIdentifier: Identifier?,
    @get:JvmSynthetic internal val _references: MutableList<ItemReference>
) : Iterable<Spine.ItemReference> {
    /**
     * Returns a list of all the [itemref][ItemReference]s of `this` guide.
     */
    val references: ImmutableList<ItemReference> get() = _references.toImmutableList()

    /**
     * The [manifest-item][Manifest.Item] that has been marked as the table-of-contents for the [book].
     *
     * Note that the `toc` attribute that this relies on is marked as a **LEGACY** feature as of
     * [EPUB 3.0][BookVersion.EPUB_3_0], so there is no guarantee that this will return anything.
     */
    var tableOfContents: Manifest.Item<*>?
        get() = tableOfContentsIdentifier?.let { book.manifest.getLocalItemOrNull(it) }
        set(value) {
            logger.debug { "Setting the spine table-of-contents to $tableOfContents." }
            if (book.version > BookVersion.EPUB_2_0 && value != null) {
                logger.warn { "The spine table-of-contents is marked as a legacy feature since 3.0, current book format is set to ${book.version}. It is not recommended to use legacy features." }
            }
            tableOfContentsIdentifier = value?.identifier
        }

    // -- REFERENCE-AT -- \\
    // TODO: Rename to getReferenceAt?
    /**
     * Returns the [itemref][ItemReference] at the given [index].
     *
     * @throws [IndexOutOfBoundsException] if the given [index] is out of range
     */
    fun getReference(index: Int): ItemReference = _references[index]

    /**
     * Returns the [itemref][ItemReference] at the given [index], or `null` if `index` is out of range.
     */
    fun getReferenceOrNull(index: Int): ItemReference? = _references.getOrNull(index)

    /**
     * Returns the [itemref][ItemReference] at the given [index], or `None` if `index` is out of range.
     */
    fun getReferenceOrNone(index: Int): Option<ItemReference> = _references.getOrNone(index)

    // -- REFERENCE-OF -- \\
    /**
     * Returns the first [itemref][ItemReference] that references an [item][ItemReference] that has an
     * [id][Manifest.Item.identifier] that matches the given [resource], or throws a [NoSuchElementException] if none
     * is found.
     */
    fun getReferenceOf(resource: PageResource): ItemReference = getReferenceOfOrNull(resource)
        ?: throw NoSuchElementException("No 'itemref' found that references the id <$resource>")

    /**
     * Returns the first [itemref][ItemReference] that references the given [item], or throws [NoSuchElementException]
     * if none is found.
     */
    fun getReferenceOf(item: Manifest.Item<*>): ItemReference =
        _references.find { it.item == item }
            ?: throw NoSuchElementException("No 'itemref' found that references the item <$item>")

    /**
     * Returns the first [itemref][ItemReference] that references the given [item], or `null` if none is found.
     */
    fun getReferenceOfOrNull(item: Manifest.Item<*>): ItemReference? =
        _references.firstOrNull { it.item == item }

    /**
     * Returns the first [itemref][ItemReference] that references an [item][Manifest.Item] that has an
     * [id][Manifest.Item.identifier] that matches the given [resource], or `null` if none is found.
     */
    fun getReferenceOfOrNull(resource: PageResource): ItemReference? =
        _references.firstOrNull { it.item.identifier == resource.identifier }

    /**
     * Returns the first [itemref][ItemReference] that references the given [item], or `None` if none is found.
     */
    fun getReferenceOfOrNone(item: Manifest.Item<*>): Option<ItemReference> =
        _references.firstOrNone { it.item == item }

    /**
     * Returns the first [itemref][ItemReference] that references an [item][Manifest.Item] that has an
     * [id][Manifest.Item.identifier] that matches the given [resource], or `None` if none is found.
     */
    fun getReferenceOfOrNone(resource: PageResource): Option<ItemReference> =
        _references.firstOrNone { it.item.identifier == resource.identifier }

    /**
     * Returns whether or not this `spine` element contains any [itemref][ItemReference] elements that reference
     * an [item][Manifest.Item] that has a [id][Manifest.Item.identifier] that matches the given [resource].
     */
    fun hasReferenceOf(resource: PageResource): Boolean = _references.any { it.item.identifier == resource.identifier }

    /**
     * Returns whether or not this `spine` element contains any [itemref][ItemReference] elements that reference the
     * given [item].
     */
    fun hasReferenceOf(item: Manifest.Item<*>): Boolean = _references.any { it.item == item }

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun updateReferenceItemFor(resource: PageResource, newItem: Manifest.Item<*>) {
        val ref = getReferenceOfOrNull(resource)

        if (ref != null) {
            logger.trace { "Updating 'item' of item-ref <$ref> to <$newItem>" }
            _references[_references.indexOf(ref)] =
                ItemReference(newItem, ref.identifier, ref.isLinearRaw, ref.properties)
        } else {
            logger.debug { "Page resource <$resource> has no spine entry" } // TODO: Lower to debug?
        }
    }

    @JvmSynthetic
    internal fun addReferenceOf(page: Page) {
        requireThat(page.resource in book.manifest) { "page-resource should be in book manifest" }
        val itemRef = ItemReference(page.resource.manifestItem)
        logger.trace { "Created spine item-ref instance <$itemRef> for page <$page>" }
        _references += itemRef
    }

    @JvmSynthetic
    internal fun addReferenceOf(index: Int, page: Page) {
        requireThat(page.resource in book.manifest) { "page-resource should be in book manifest" }
        val itemRef = ItemReference(page.resource.manifestItem)
        logger.trace { "Created spine item-ref instance <$itemRef> for page <$page>" }
        _references.add(index, itemRef)
    }

    @JvmSynthetic
    internal fun setReferenceOf(index: Int, page: Page) {
        requireThat(page.resource in book.manifest) { "page-resource should be in book manifest" }
        val itemRef = ItemReference(page.resource.manifestItem)
        logger.trace { "Created spine item-ref instance <$itemRef> for page <$page>" }
        _references[index] = itemRef
    }

    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("spine", namespace).apply {
        identifier?.also { setAttribute(it.toAttribute()) }
        pageProgressionDirection?.also { setAttribute(it.toAttribute()) }
        tableOfContentsIdentifier?.also { setAttribute(it.toAttribute("toc")) }
        for (ref in _references) {
            addContent(ref.toElement())
        }
    }

    override fun iterator(): Iterator<ItemReference> = _references.iterator().asUnmodifiable()

    override fun toString(): String = buildString {
        append("Spine(")
        append("references=$_references")
        identifier?.also { append(", identifier='$it'") }
        pageProgressionDirection?.also { append(", pageProgressionDirection=$it") }
        append(")")
    }

    /**
     * Represents the [itemref](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-spine-itemref)
     * element.
     *
     * @property [item] The [manifest item][Manifest.Item] that `this` spine entry is representing the order of.
     * @property [identifier] TODO
     * @property [properties] TODO
     */
    // TODO: Change 'item' to Manifest.Item.Local?
    class ItemReference internal constructor(
        val item: Manifest.Item<*>,
        var identifier: Identifier? = null,
        @get:JvmSynthetic internal val isLinearRaw: Boolean? = null,
        val properties: Properties = Properties.empty()
    ) {
        /**
         * Indicates whether the referenced [item][item] contains content that contributes the primary reading
         * order that has to be read sequentially *(`true`)* or auxilary content that enhances or augments the primary
         * content and can be accessed out of sequence *(`false`)*.
         *
         * If an `itemref` does *not* explicitly define a `linear` attribute, then it is *implicitly* assumed to be
         * `true`.
         */
        var isLinear: Boolean by Delegates.observable(isLinearRaw ?: true) { _, _, _ -> isLinearExplicit = true }

        private var isLinearExplicit: Boolean = isLinearRaw != null

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is ItemReference -> false
            item != other.item -> false
            identifier != other.identifier -> false
            properties != other.properties -> false
            isLinear != other.isLinear -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = item.hashCode()
            result = 31 * result + (identifier?.hashCode() ?: 0)
            result = 31 * result + properties.hashCode()
            result = 31 * result + isLinear.hashCode()
            return result
        }

        override fun toString(): String = buildString {
            append("ItemReference(")
            append("item=$item")
            identifier?.also { append(", identifier='$identifier'") }
            if (isLinearExplicit) append(", isLinear=$isLinear")
            if (properties.isNotEmpty()) append(", properties=$properties")
            append(")")
        }

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("itemref", namespace).apply {
            setAttribute(item.identifier.toAttribute("idref"))
            identifier?.also { setAttribute(it.toAttribute()) }
            if (isLinearExplicit) setAttribute("linear", if (isLinear) "yes" else "no")
            if (properties.isNotEmpty()) setAttribute(properties.toAttribute())
        }
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, manifest: Manifest, element: Element, file: Path): Spine = with(element) {
            val identifier = getAttributeValue("id")?.let { Identifier.of(it) }
            val pageProgressionDirection =
                getAttributeValue("page-progression-direction")?.let { PageProgressionDirection.of(it) }
            val tocIdentifier = getAttributeValue("toc")?.let { Identifier.of(it) }
            val references = getChildren("itemref", namespace)
                .mapTo(mutableListOf()) { createReference(manifest, it, book.file, file) }
                .ifEmpty { malformed(book.file, file, "The book spine should not be empty") }
            return Spine(book, identifier, pageProgressionDirection, tocIdentifier, references).also {
                logger.trace { "Constructed spine instance <$it> from file '$file'" }
            }
        }

        private fun createReference(manifest: Manifest, element: Element, epub: Path, container: Path): ItemReference {
            val textual = element.stringify(Format.getCompactFormat())
            val idRef = element.attr("idref", epub, container).let { Identifier.of(it) }
            val item = try {
                manifest.getItem(idRef)
            } catch (e: NoSuchElementException) {
                malformed(epub, container, "'itemref' element [$textual] is referencing an unknown manifest item")
            }
            val identifier = element.getAttributeValue("id")?.let { Identifier.of(it) }
            val isLinear = element.getAttributeValue("linear")?.let {
                try {
                    Boolean.parse(it)
                } catch (e: ParseException) {
                    malformed(epub, container, "'linear' should be 'yes' or 'no' was: $it")
                }
            }
            val properties = element.getAttributeValue("properties")?.let {
                Properties.parse(ItemReference::class, it)
            } ?: Properties.empty()
            return ItemReference(item, identifier, isLinear, properties).also {
                logger.trace { "Constructed spine item-ref instance <$it>" }
            }
        }
    }
}