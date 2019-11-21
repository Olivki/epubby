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

package moe.kanon.epubby.pack

import moe.kanon.epubby.Book
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.utils.Namespaces
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.child
import moe.kanon.epubby.utils.docScope
import moe.kanon.epubby.utils.scope
import moe.kanon.kommons.lang.delegates.KDelegates
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path
import java.util.Locale

class Package private constructor(
    val book: Book,
    val document: Path,
    var uniqueIdentifier: Identifier,
    val attributes: Attributes,
    val metadata: Metadata,
    val manifest: Manifest,
    val spine: Spine
) {
    /**
     * The [guide][Guide] element for the [book] this package is tied to.
     *
     * There is no guarantee that a book will have a `guide` element, and therefore there is no guarantee that this
     * property will exist. If a book does *not* have a `guide` element then accessing this property will result in a
     * [UnsupportedOperationException] being thrown.
     *
     * Note that this property can only be set *once*, any further attempts to change it will result in a
     * [UnsupportedOperationException] being thrown.
     */
    var guide: Guide by KDelegates.writeOnce()

    /**
     * The [bindings][Bindings] element for the [book] this package is tied to.
     *
     * There is no guarantee that a book will have a `bindings` element, and therefore there is no guarantee that this
     * property will exist. If a book does *not* have a `bindings` element then accessing this property will result in
     * a [UnsupportedOperationException] being thrown.
     *
     * Note that this property can only be set *once*, any further attempts to change it will result in a
     * [UnsupportedOperationException] being thrown.
     */
    var bindings: Bindings by KDelegates.writeOnce()

    /**
     * The [collection][Collection] element for the [book] this package is tied to.
     *
     * There is no guarantee that a book will have a `collection` element, and therefore there is no guarantee that this
     * property will exist. If a book does *not* have a `collection` element then accessing this property will result
     * in a [UnsupportedOperationException] being thrown.
     *
     * Note that this property can only be set *once*, any further attempts to change it will result in a
     * [UnsupportedOperationException] being thrown.
     */
    var collection: Collection by KDelegates.writeOnce()

    /**
     * The [tours][Tours] element for the [book] this package is tied to.
     *
     * There is no guarantee that a book will have a `tours` element, and therefore there is no guarantee that this
     * property will exist. If a book does *not* have a `tours` element then accessing this property will result in a
     * [UnsupportedOperationException] being thrown.
     *
     * Note that this property can only be set *once*, any further attempts to change it will result in a
     * [UnsupportedOperationException] being thrown.
     */
    var tours: Tours by KDelegates.writeOnce()

    @JvmSynthetic
    internal fun toDocument(): Document = Document(Element("package", Namespaces.OPF)).docScope {
        setAttribute("version", book.version.toString())
        setAttribute("unique-identifier", uniqueIdentifier.value)
        this@Package.attributes.applyTo(this)

        addContent(metadata.toElement())
        addContent(manifest.toElement())
        addContent(spine.toElement())
    }

    // TODO: Maybe do it a different way, or keep it like this to separate the serialization more from the public api?
    data class Attributes internal constructor(
        var direction: Direction?,
        var identifier: Identifier?,
        var prefix: String?,
        var language: Locale?
    ) {
        @JvmSynthetic
        internal fun applyTo(element: Element) {
            direction?.also { element.setAttribute("dir", it.serializedName) }
            identifier?.also { element.setAttribute("id", it.value) }
            prefix?.also { element.setAttribute("lang", it, Namespace.XML_NAMESPACE) }
            language?.also { element.setAttribute("prefix", it.toLanguageTag()) }
        }
    }

    companion object {
        @JvmSynthetic
        internal fun fromDocument(book: Book, file: Path, document: Document): Package = document.scope {
            val uniqueIdentifier = attr("unique-identifier", book.file, file).let(::Identifier)
            val attrs = createAttributes(this)
            // Namespace.getNamespace("", Namespaces.OPF.uri)
            val metadata = Metadata.fromElement(book, child("metadata", book.file, file, namespace), file)
            val manifest = Manifest.fromElement(book, child("manifest", book.file, file, namespace), file)
            val spine = Spine.fromElement(book, child("spine", book.file, file, namespace), file)
            return Package(book, file, uniqueIdentifier, attrs, metadata, manifest, spine).also { pack ->
                getChild("guide", namespace)?.also { pack.guide = Guide.fromElement(book, it, file) }
                getChild("bindings", namespace)?.also { pack.bindings = Bindings.fromElement(book, it, file) }
                getChild("collection", namespace)?.also { pack.collection = Collection.fromElement(book, it, file) }
                getChild("tours", namespace)?.also { pack.tours = Tours.fromElement(book, it, file) }
            }
        }

        private fun createAttributes(element: Element): Attributes = with(element) {
            val dir = getAttributeValue("dir")?.let(Direction.Companion::of)
            val id = getAttributeValue("id")?.let(::Identifier)
            val prefix = getAttributeValue("prefix")
            val lang = getAttributeValue("lang", Namespace.XML_NAMESPACE).let(Locale::forLanguageTag)
            return Attributes(dir, id, prefix, lang)
        }
    }
}