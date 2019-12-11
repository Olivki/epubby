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

import moe.kanon.epubby.Book
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.child
import moe.kanon.epubby.utils.docScope
import moe.kanon.epubby.utils.internal.Namespaces
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.epubby.utils.writeTo
import moe.kanon.kommons.lang.delegates.KDelegates
import moe.kanon.kommons.lang.delegates.isWriteOnceSet
import moe.kanon.kommons.requireThat
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.Locale
import kotlin.properties.Delegates

/**
 * Represents a [package document](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-package-doc).
 *
 * TODO
 *
 * @property [book] The [Book] instance that `this` package-document is tied to.
 * @property [file] The underlying package document file that this class is wrapped around.
 * @property [uniqueIdentifier] Should be an `IDREF` that identifies the `dc:identifier` element.
 * @property [version] This specifies which epub specification the [book] was made to follow.
 * @property [metadata] TODO
 * @property [manifest] TODO
 * @property [spine] TODO
 */
class PackageDocument private constructor(
    val book: Book,
    val file: Path,
    uniqueIdentifier: Identifier,
    val attributes: Attributes,
    val metadata: Metadata,
    val manifest: Manifest,
    val spine: Spine
) {
    var uniqueIdentifier: Identifier by Delegates.vetoable(uniqueIdentifier) { _, _, new ->
        requireThat(new.value.isNotBlank()) { "unique-identifier for package-document should not be blank" }
        true
    }

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

    // TODO: Replace these with something that doesn't use reflection as to improve performance?

    /**
     * Returns `true` if `this` package-document has a [guide][Guide], `false` otherwise.
     */
    fun hasGuide(): Boolean = this::guide.isWriteOnceSet

    /**
     * Returns `true` if `this` package-document has a [bindings][Bindings], `false` otherwise.
     */
    fun hasBindings(): Boolean = this::bindings.isWriteOnceSet

    /**
     * Returns `true` if `this` package-document has a [collection][Collection], `false` otherwise.
     */
    fun hasCollection(): Boolean = this::collection.isWriteOnceSet

    /**
     * Returns `true` if `this` package-document has a [tours][Tours], `false` otherwise.
     */
    fun hasTours(): Boolean = this::tours.isWriteOnceSet

    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        toDocument().writeTo(fileSystem.getPath(file.toString()))
    }

    @JvmSynthetic
    internal fun toDocument(): Document = Document(Element("package", Namespaces.OPF)).docScope {
        setAttribute("version", book.version.toString())
        setAttribute("unique-identifier", uniqueIdentifier.value)
        this@PackageDocument.attributes.applyTo(this)

        addContent(metadata.toElement())
        addContent(manifest.toElement())
        addContent(spine.toElement())
        if (hasTours()) addContent(tours.toElement())
        if (hasGuide()) addContent(guide.toElement())
        // TODO: Bindings and Collections
    }

    // TODO: Maybe do it a different way, or keep it like this to separate the serialization more from the public api?
    /**
     * Contains all the extra attributes that may be declared on the package document header.
     *
     * @property [direction] Specifies the base text direction of the content and attribute values of the carrying
     * element and its descendants.
     *
     * Inherent directionality specified using unicode takes precedence over this property.
     * @property [identifier] The identifier of `this` package-document.
     *
     * This *MUST* be unique within the document scope.
     * @property [prefix] Defines additional prefix mappings not reserved by the epub specification.
     *
     * [Read more](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-prefix-attr)
     * @property [language] Specifies the language used in all the sub-elements of this package-document.
     */
    data class Attributes internal constructor(
        var direction: Direction?,
        var identifier: Identifier?,
        var prefix: String?,
        var language: Locale?
    ) {
        @JvmSynthetic
        internal fun applyTo(element: Element) {
            direction?.also { element.setAttribute("dir", it.serializedName) }
            identifier?.also { element.setAttribute(it.toAttribute()) }
            prefix?.also { element.setAttribute("lang", it, Namespace.XML_NAMESPACE) }
            language?.also { element.setAttribute("prefix", it.toLanguageTag()) }
        }
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromBook(book: Book): PackageDocument {
            val file = book.metaInf.container.packageDocument.path
            parseXmlFile(file) { _, root ->
                val namespace = root.namespace
                val uniqueId = root.attr("unique-identifier", book.file, file).let { Identifier.of(it) }
                val attributes = createAttributes(root)
                book.version = root.attr("version", book.file, file).let { BookVersion.fromString(it) }
                val metadataElement = root.child("metadata", book.file, file, namespace)
                val metadata = Metadata.fromElement(book, metadataElement, file)
                val manifestElement = root.child("manifest", book.file, file, namespace)
                val manifest = Manifest.fromElement(book, manifestElement, file)
                book.resources.populateFromManifest(manifest)
                val spineElement = root.child("spine", book.file, file, namespace)
                val spine = Spine.fromElement(book, manifest, spineElement, file)
                book.pages.populateFromSpine(spine)
                return PackageDocument(book, file, uniqueId, attributes, metadata, manifest, spine).also {
                    root.getChild("guide", namespace)?.also { element ->
                        it.guide = Guide.fromElement(book, element, file)
                    }
                    root.getChild("bindings", namespace)?.also { element ->
                        it.bindings = Bindings.fromElement(book, element, file)
                    }
                    root.getChild("collection", namespace)?.also { element ->
                        it.collection = Collection.fromElement(book, element, file)
                    }
                    root.getChild("tours", namespace)?.also { element ->
                        it.tours = Tours.fromElement(book, element, file)
                    }
                }
            }
        }

        private fun createAttributes(element: Element): Attributes = with(element) {
            val dir = getAttributeValue("dir")?.let(Direction.Companion::of)
            val id = getAttributeValue("id")?.let { Identifier.of(it) }
            val prefix = getAttributeValue("prefix")
            val lang = getAttributeValue("lang", Namespace.XML_NAMESPACE).let(Locale::forLanguageTag)
            return Attributes(dir, id, prefix, lang).also {
                logger.trace { "Constructed package-document attributes instance <$it>" }
            }
        }
    }
}