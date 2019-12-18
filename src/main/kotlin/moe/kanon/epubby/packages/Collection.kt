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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.NewFeature
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.structs.DublinCore
import moe.kanon.epubby.structs.Identifier
import moe.kanon.kommons.requireThat
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path
import java.util.Locale

/**
 * Represents the [collection](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-collections)
 * element in the [package-document][PackageDocument].
 */
@NewFeature(since = "3.0")
class Collection private constructor(
    val book: Book,
    val role: String,
    var direction: Direction?,
    var identifier: Identifier?,
    var language: Locale?,
    var metadata: Metadata?,
    private val _children: MutableList<Collection>,
    private val _links: MutableList<Link>
) {
    init {
        requireThat(book.version > BookVersion.EPUB_2_0) { "expected version of 'book' to be 3.0 or greater, was ${book.version}" }
    }

    // TODO: if a collection has no children, then it NEEDS to have at least one link element
    //       however if a collection does have children, then it should NOT have any link elements

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("collection", namespace).apply {
        setAttribute("role", role)
        direction?.also { setAttribute("dir", it.attributeName) }
        identifier?.also { setAttribute(it.toAttribute()) }
        language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }
        _children.forEach { addContent(it.toElement()) }
        metadata?.also { addContent(it.toElement()) }
        TODO("add link here")
    }

    class Metadata internal constructor(
        val book: Book,
        private val _dublinCoreElements: MutableList<DublinCore<*>>,
        private val _metaElements: MutableList<Meta>,
        private val _links: MutableList<Link>
    ) {
        val dublinCoreElements: ImmutableList<DublinCore<*>> get() = _dublinCoreElements.toImmutableList()

        val metaElements: ImmutableList<Meta> get() = _metaElements.toImmutableList()

        val links: ImmutableList<Link> get() = _links.toImmutableList()

        // TODO: add functions for modifying this stuff

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("metadata", namespace).apply {
            addNamespaceDeclaration(Namespaces.DUBLIN_CORE)

            if (book.version < BookVersion.EPUB_3_0) {
                addNamespaceDeclaration(Namespaces.OPF_WITH_PREFIX)
            }
        }

        class Meta internal constructor()

        class Link internal constructor(val mediaType: MediaType)
    }

    class Link internal constructor()

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, file: Path): Collection = TODO()
    }
}