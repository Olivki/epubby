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

package moe.kanon.epubby.resources.root

import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.EpubLegacy
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.epubby.resources.root.PackageSpine.ItemReference
import moe.kanon.epubby.utils.getAttributeValueOrNone
import moe.kanon.epubby.utils.stringify
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.firstOrNone
import moe.kanon.kommons.func.getOrNone
import moe.kanon.kommons.lang.ParseException
import moe.kanon.kommons.lang.parse
import org.jdom2.Element
import org.jdom2.output.Format
import java.nio.file.Path

/**
 * Represents the [spine](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-spine) element.
 */
class PackageSpine private constructor(
    val book: Book,
    @SerializedName("id")
    val identifier: Option<String>,
    val pageProgressionDirection: Option<String>,
    @EpubLegacy(Book.Format.EPUB_3_0)
    @SerializedName("toc")
    private var _tableOfContents: Option<String>,
    @JvmSynthetic internal val references: MutableList<ItemReference>
) : ElementSerializer, Iterable<ItemReference> {
    companion object {
        internal fun parse(
            book: Book,
            packageDocument: Path,
            manifest: PackageManifest,
            spine: Element
        ): PackageSpine = with(spine) {
            fun malformed(reason: String, cause: Throwable? = null): Nothing =
                raiseMalformedError(book.originFile, packageDocument, reason, cause)

            val identifier = getAttributeValueOrNone("id")
            val pageProgressionDirection = getAttributeValueOrNone("page-progression-direction")
            val toc = getAttributeValueOrNone("toc")
            val references = getChildren("itemref", namespace)
                .asSequence()
                .map { element ->
                    val textual = element.stringify(Format.getCompactFormat())
                    val idref = element.getAttributeValue("idref")
                        ?: malformed("'itemref' element missing required 'idref' attribute; $textual")
                    val id = element.getAttributeValueOrNone("id")
                    val rawLinear = element.getAttributeValueOrNone("linear")
                    val linear = try {
                        rawLinear.map { Boolean.parse(it.trim()) }
                    } catch (e: ParseException) {
                        // we know it's safe to access the value of 'rawLinear' because if 'rawLinear' was 'None' then
                        // it would not be throwing a 'ParseException'
                        malformed("expected value of 'linear' to be 'yes' or 'no', got '${rawLinear.value}'", e)
                    }
                    val properties = element.getAttributeValueOrNone("properties")
                    val reference: ManifestItem<*> = try {
                        manifest[idref]
                    } catch (e: NoSuchElementException) {
                        malformed("'itemref' element is referencing an unknown manifest item; $textual", e)
                    }
                    return@map ItemReference(reference, id, linear, properties)
                }
                .toMutableList()
                .ifEmpty { malformed("'spine' element needs to contain at least one 'itemref', but it's empty") }

            return@with PackageSpine(book, identifier, pageProgressionDirection, toc, references)
        }
    }

    /**
     * The [ManifestItem] that has been marked as the table-of-contents for the [book].
     *
     * Note that the 'toc' attribute that this relies on is marked as a **LEGACY** feature as of
     * [EPUB 3.0][Book.Format.EPUB_3_0].
     */
    var tableOfContents: Option<ManifestItem<*>>
        /**
         * Returns the [ManifestItem] marked as the table-of-contents for the [book], or [None] if there is no such
         * item.
         *
         * Note that the 'toc' attribute that this relies on is marked as a **LEGACY** feature as of
         * [EPUB 3.0][Book.Format.EPUB_3_0], so there is no guarantee that this will always return some value.
         */
        get() = _tableOfContents.flatMap { book.packageDocument.manifest.getItemOrNone(it) }
        /**
         * Sets the 'toc' attribute of this `spine` element to the [identifier][ManifestItem.identifier] of the given
         * [value].
         *
         * Note that the 'toc' attribute on the `spine` element is marked as a **LEGACY** feature as of
         * [EPUB 3.0][Book.Format.EPUB_3_0], so it's highly discouraged to set the value of it if the book is above
         * [EPUB 2.0][Book.Format.EPUB_2_0]
         */
        set(value) {
            _tableOfContents = value.map { it.identifier }
        }

    // TODO: Add functions for modifying the spine?
    /**
     * Returns the [itemref][ItemReference] at the given [index].
     *
     * @throws [IndexOutOfBoundsException] if the given [index] is out of range
     */
    fun getReferenceAt(index: Int): ItemReference = references[index]

    /**
     * Returns the [itemref][ItemReference] at the given [index], or [None] if `index` is out of range.
     */
    fun getReferenceAtOrNone(index: Int): Option<ItemReference> = references.getOrNone(index)

    /**
     * Returns the first [itemref][ItemReference] that references an [item][ManifestItem] that has an
     * [id][ManifestItem.identifier] that matches the given [id], or throws a [NoSuchElementException] if none is found.
     */
    fun getReference(id: String): ItemReference = references.find { it.reference.identifier == id }
        ?: throw NoSuchElementException("No 'itemref' found that references the id <$id>")

    /**
     * Returns the first [itemref][ItemReference] that references an [item][ManifestItem] that has an
     * [id][ManifestItem.identifier] that matches the given [id], or [None] if none is found.
     */
    fun getReferenceOrNone(id: String): Option<ItemReference> =
        references.firstOrNone { it.reference.identifier == id }

    /**
     * Returns the first [itemref][ItemReference] that references the given [item], or throws [NoSuchElementException]
     * if none is found.
     */
    fun getReferenceOf(item: ManifestItem<*>): ItemReference =
        references.find { it.reference == item }
            ?: throw NoSuchElementException("No 'itemref' found that references the item <$item>")

    /**
     * Returns the first [itemref][ItemReference] that references the given [item], or [None] if none is found.
     */
    fun getReferenceOfOrNone(item: ManifestItem<*>): Option<ItemReference> =
        references.firstOrNone { it.reference == item }

    /**
     * Returns whether or not this `spine` element contains any [itemref][ItemReference] elements that reference
     * an [item][ManifestItem] that has a [id][ManifestItem.identifier] that matches the given [id].
     */
    @JvmName("hasReference")
    operator fun contains(id: String): Boolean = references.any { it.reference.identifier == id }

    /**
     * Returns whether or not this `spine` element contains any [itemref][ItemReference] elements that reference the
     * given [item].
     */
    @JvmName("hasReferenceOf")
    operator fun contains(item: ManifestItem<*>): Boolean = references.any { it.reference == item }

    override fun toElement(): Element = Element("spine", PackageDocument.NAMESPACE).apply {
        identifier.ifPresent { setAttribute("id", it) }
        pageProgressionDirection.ifPresent { setAttribute("page-progression-direction", it) }
        _tableOfContents.ifPresent { setAttribute("toc", it) }
        for (ref in references) addContent(ref.toElement())
    }

    override fun iterator(): Iterator<ItemReference> = references.iterator().asUnmodifiable()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageSpine -> false
        book != other.book -> false
        identifier != other.identifier -> false
        pageProgressionDirection != other.pageProgressionDirection -> false
        _tableOfContents != other._tableOfContents -> false
        references != other.references -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + pageProgressionDirection.hashCode()
        result = 31 * result + _tableOfContents.hashCode()
        result = 31 * result + references.hashCode()
        return result
    }

    override fun toString(): String =
        "PackageSpine(book=$book, identifier=$identifier, pageProgressionDirection=$pageProgressionDirection, tableOfContents=$_tableOfContents, references=$references)"

    /**
     * Represents the [itemref](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-spine-itemref)
     * element.
     *
     * @property [reference] TODO
     * @property [identifier] TODO
     * @property [linear] Indicates whether the referenced [item][reference] contains content that contributes the
     * primary reading order that has to be read sequentially *(`true`)* or auxilary content that enhances or augments
     * the primary content and can be accessed out of sequence *(`false`)*.
     *
     * If an `itemref` does *not* explicitly define a `linear` attribute, then it is *implicitly* assumed to be `true`.
     * @property [properties]
     */
    data class ItemReference(
        @SerializedName("idref") val reference: ManifestItem<*>,
        @SerializedName("id") val identifier: Option<String>,
        val linear: Option<Boolean>,
        val properties: Option<String>
    ) : ElementSerializer {
        override fun toElement(): Element = Element("itemref", PackageDocument.NAMESPACE).apply {
            setAttribute("idref", reference.identifier)
            identifier.ifPresent { setAttribute("id", it) }
            linear.ifPresent { setAttribute("linear", if (it) "yes" else "false") }
            properties.ifPresent { setAttribute("properties", it) }
        }
    }
}

/**
 * Returns the [itemref][ItemReference] at the given [index].
 *
 * @throws [IndexOutOfBoundsException] if the given [index] is out of range
 */
operator fun PackageSpine.get(index: Int): ItemReference = this.getReferenceAt(index)

/**
 * Returns the first [itemref][ItemReference] that references an [item][ManifestItem] that has an
 * [id][ManifestItem.identifier] that matches the given [id], or throws a [NoSuchElementException] if none is found.
 */
operator fun PackageSpine.get(id: String): ItemReference = this.getReference(id)

/**
 * Returns the first [itemref][ItemReference] that references the given [item], or throws [NoSuchElementException]
 * if none is found.
 */
operator fun PackageSpine.get(item: ManifestItem<*>): ItemReference = this.getReferenceOf(item)