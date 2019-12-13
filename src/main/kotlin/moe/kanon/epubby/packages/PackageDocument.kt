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
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.prefixes.Prefixes
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.child
import moe.kanon.epubby.utils.docScope
import moe.kanon.epubby.utils.internal.Namespaces
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.epubby.utils.writeTo
import moe.kanon.kommons.requireThat
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
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
 * @property [metadata] TODO
 * @property [manifest] TODO
 * @property [spine] TODO
 * @property [guide] The [guide][Guide] element for the [book] this package is tied to.
 *
 * There is no guarantee that a book will have a `guide` element, and therefore there is no guarantee that this
 * property will exist.
 * @property [bindings] The [bindings][Bindings] element for the [book] this package is tied to.
 *
 * There is no guarantee that a book will have a `bindings` element, and therefore there is no guarantee that this
 * property will exist.
 * @property [collection] The [collection][Collection] element for the [book] this package is tied to.
 *
 * There is no guarantee that a book will have a `collection` element, and therefore there is no guarantee that this
 * property will exist.
 * @property [tours] The [tours][Tours] element for the [book] this package is tied to.
 *
 * There is no guarantee that a book will have a `tours` element, and therefore there is no guarantee that this
 * property will exist.
 */
class PackageDocument private constructor(
    val book: Book,
    val file: Path,
    uniqueIdentifier: Identifier,
    var direction: Direction?,
    var identifier: Identifier?,
    val prefix: Prefixes,
    var language: Locale?,
    val metadata: Metadata,
    val manifest: Manifest,
    val spine: Spine,
    var guide: Guide?,
    var bindings: Bindings?,
    var collection: Collection?,
    var tours: Tours?
) {
    var uniqueIdentifier: Identifier by Delegates.vetoable(uniqueIdentifier) { _, _, new ->
        requireThat(new.value.isNotBlank()) { "unique-identifier for package-document should not be blank" }
        true
    }

    /**
     * Returns `true` if `this` package-document has a [guide][Guide], `false` otherwise.
     */
    fun hasGuide(): Boolean = guide != null

    /**
     * Returns `true` if `this` package-document has a [bindings][Bindings], `false` otherwise.
     */
    fun hasBindings(): Boolean = bindings != null

    /**
     * Returns `true` if `this` package-document has a [collection][Collection], `false` otherwise.
     */
    fun hasCollection(): Boolean = collection != null

    /**
     * Returns `true` if `this` package-document has a [tours][Tours], `false` otherwise.
     */
    fun hasTours(): Boolean = tours != null

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        toDocument().writeTo(fileSystem.getPath(file.toString()))
    }

    @JvmSynthetic
    internal fun toDocument(): Document = Document(Element("package", Namespaces.OPF)).docScope {
        setAttribute("version", book.version.toString())
        setAttribute("unique-identifier", uniqueIdentifier.value)
        direction?.also { setAttribute("dir", it.attributeName) }
        identifier?.also { setAttribute(it.toAttribute()) }
        if (prefix.isNotEmpty()) setAttribute("prefix", prefix.toStringForm())
        language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }

        addContent(metadata.toElement())
        addContent(manifest.toElement())
        addContent(spine.toElement())
        guide?.also { addContent(it.toElement()) }
        bindings?.also { addContent(it.toElement()) }
        collection?.also { addContent(it.toElement()) }
        tours?.also { addContent(it.toElement()) }
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromFile(book: Book): PackageDocument {
            val file = book.metaInf.container.packageDocument.path
            parseXmlFile(file) { _, root ->
                val namespace = root.namespace
                val uniqueId = root.attr("unique-identifier", book.file, file).let { Identifier.of(it) }
                val direction = root.getAttributeValue("dir")?.let(Direction.Companion::of)
                val identifier = root.getAttributeValue("id")?.let { Identifier.of(it) }
                val prefix = root.getAttributeValue("prefix")?.let { Prefixes.parse(it) } ?: Prefixes.empty()
                val language = root.getAttributeValue("lang", Namespace.XML_NAMESPACE)?.let(Locale::forLanguageTag)
                book.version = root.attr("version", book.file, file).let { BookVersion.parse(it) }

                val metadata = root.child("metadata", book.file, file, namespace).let {
                    Metadata.fromElement(book, it, file)
                }
                val manifest = root.child("manifest", book.file, file, namespace).let {
                    Manifest.fromElement(book, it, file)
                }
                book.resources.populateFromManifest(manifest)
                val spine = root.child("spine", book.file, file, namespace).let {
                    Spine.fromElement(book, manifest, it, file)
                }
                book.pages.populateFromSpine(spine)
                val guide = root.getChild("guide", namespace)?.let { Guide.fromElement(book, it, file) }
                val bindings = root.getChild("bindings", namespace)?.let { Bindings.fromElement(book, it, file) }
                val collection = root.getChild("collection", namespace)?.let { Collection.fromElement(book, it, file) }
                val tours = root.getChild("tours", namespace)?.let { Tours.fromElement(book, it, file) }

                return PackageDocument(
                    book,
                    file,
                    uniqueId,
                    direction,
                    identifier,
                    prefix,
                    language,
                    metadata,
                    manifest,
                    spine,
                    guide,
                    bindings,
                    collection,
                    tours
                )
            }
        }
    }
}