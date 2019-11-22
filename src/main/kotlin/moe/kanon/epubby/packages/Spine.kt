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
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.PageProgressionDirection
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.utils.internal.Namespaces
import moe.kanon.kommons.collections.asUnmodifiable
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path
import kotlin.properties.Delegates

/**
 * Represents the [spine](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-spine) element.
 */
class Spine(
    val book: Book,
    var identifier: Identifier?,
    var pageProgressionDirection: PageProgressionDirection?,
    private var _tableOfContents: Identifier?,
    private val _references: MutableList<ItemReference>
) : Iterable<Spine.ItemReference> {
    val references: ImmutableList<ItemReference> get() = _references.toImmutableList()

    // TODO: Add functions for modifying the spine
    // -- REFERENCE-AT -- \\
    /**
     * Returns the [itemref][ItemReference] at the given [index].
     *
     * @throws [IndexOutOfBoundsException] if the given [index] is out of range
     */
    fun getReferenceAt(index: Int): ItemReference = _references[index]

    /**
     * Returns the [itemref][ItemReference] at the given [index], or `null`if `index` is out of range.
     */
    fun getReferenceAtOrNull(index: Int): ItemReference? = _references.getOrNull(index)

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
     * Returns whether or not this `spine` element contains any [itemref][ItemReference] elements that reference
     * an [item][Manifest.Item] that has a [id][Manifest.Item.identifier] that matches the given [resource].
     */
    fun hasReferenceOf(resource: PageResource): Boolean = _references.any { it.item.identifier == resource.identifier }

    /**
     * Returns whether or not this `spine` element contains any [itemref][ItemReference] elements that reference the
     * given [item].
     */
    fun hasReferenceOf(item: Manifest.Item<*>): Boolean = _references.any { it.item == item }

    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("spine", namespace).apply {
        identifier?.also { setAttribute(it.toAttribute()) }
        pageProgressionDirection?.also { setAttribute(it.toAttribute()) }
        _tableOfContents?.also { setAttribute(it.toAttribute("toc")) }
        for (ref in _references) {
            addContent(ref.toElement())
        }
    }

    override fun iterator(): Iterator<ItemReference> = _references.iterator().asUnmodifiable()

    /**
     * Represents the [itemref](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-spine-itemref)
     * element.
     *
     * @property [item] TODO
     * @property [identifier] TODO
     * @property [properties] TODO
     */
    class ItemReference internal constructor(
        var item: Manifest.Item<*>,
        var identifier: Identifier? = null,
        isLinear: Boolean? = null,
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
        var isLinear: Boolean by Delegates.observable(isLinear ?: true) { _, _, _ -> isLinearExplicit = true }

        private var isLinearExplicit: Boolean = isLinear != null

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("itemref", namespace).apply {
            setAttribute(item.identifier.toAttribute("idref"))
            identifier?.also { setAttribute(it.toAttribute()) }
            if (isLinearExplicit) setAttribute("linear", if (isLinear) "yes" else "no")
            if (properties.isNotEmpty()) setAttribute(properties.toAttribute())
        }

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

        override fun toString(): String =
            "ItemReference(reference=$item, identifier=$identifier, properties=$properties, isLinear=$isLinear)"
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, file: Path): Spine = TODO()
    }
}