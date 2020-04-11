/*
 * Copyright 2019-2020 Oliver Berg
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
import moe.kanon.epubby.BookReadMode
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.DeprecatedFeature
import moe.kanon.epubby.LegacyFeature
import moe.kanon.epubby.NewFeature
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.metainf.MetaInf
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.prefixes.Prefixes
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.child
import moe.kanon.epubby.utils.docScope
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
 * @property [guide] The [guide][PackageGuide] element for the [book] this package is tied to.
 *
 * There is no guarantee that a book will have a `guide` element, and therefore there is no guarantee that this
 * property will exist.
 * @property [bindings] The [bindings][PackageBindings] element for the [book] this package is tied to.
 *
 * There is no guarantee that a book will have a `bindings` element, and therefore there is no guarantee that this
 * property will exist.
 * @property [collection] The [collection][PackageCollection] element for the [book] this package is tied to.
 *
 * There is no guarantee that a book will have a `collection` element, and therefore there is no guarantee that this
 * property will exist.
 * @property [tours] The [tours][PackageTours] element for the [book] this package is tied to.
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
    val metadata: PackageMetadata,
    val manifest: PackageManifest,
    val spine: PackageSpine,
    @LegacyFeature(since = BookVersion.EPUB_3_0)
    var guide: PackageGuide?,
    @NewFeature(since = BookVersion.EPUB_3_0)
    @DeprecatedFeature(since = BookVersion.EPUB_3_2)
    var bindings: PackageBindings?,
    @NewFeature(since = BookVersion.EPUB_3_0)
    var collection: PackageCollection?,
    @DeprecatedFeature(since = BookVersion.EPUB_2_0)
    var tours: PackageTours?
) {
    var uniqueIdentifier: Identifier by Delegates.vetoable(uniqueIdentifier) { _, _, new ->
        requireThat(new.value.isNotBlank()) { "unique-identifier for package-document should not be blank" }
        // TODO: Make sure that this identifier actually points towards an existing element
        true
    }

    /**
     * Returns `true` if `this` package-document has a [guide][PackageGuide], `false` otherwise.
     */
    fun hasGuide(): Boolean = guide != null

    /**
     * Returns `true` if `this` package-document has a [bindings][PackageBindings], `false` otherwise.
     */
    fun hasBindings(): Boolean = bindings != null

    /**
     * Returns `true` if `this` package-document has a [collection][PackageCollection], `false` otherwise.
     */
    fun hasCollection(): Boolean = collection != null

    /**
     * Returns `true` if `this` package-document has a [tours][PackageTours], `false` otherwise.
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
        internal fun fromMetaInf(metaInf: MetaInf, fileSystem: FileSystem, mode: BookReadMode): PackageDocument {
            val epub = metaInf.epub
            val container = metaInf.container.packageDocument.path
            parseXmlFile(container) { _, root ->
                val namespace = root.namespace
                val uniqueIdentifier = root.attr("unique-identifier", epub, container).let { Identifier.of(it) }
                val direction = root.getAttributeValue("dir")?.let(Direction.Companion::of)
                val identifier = root.getAttributeValue("id")?.let { Identifier.of(it) }
                val prefix = root.getAttributeValue("prefix")?.let { Prefixes.parse(it) } ?: Prefixes.empty()
                val language = root.getAttributeValue("lang", Namespace.XML_NAMESPACE)?.let(Locale::forLanguageTag)
                val version: BookVersion = root.attr("version", epub, container).let { BookVersion.fromString(it) }
                val book = Book(metaInf, version, epub, fileSystem, fileSystem.getPath("/"))

                // mandatory
                val manifest = root.child("manifest", epub, container, namespace).let {
                    PackageManifest.fromElement(book, it, container, prefix)
                }
                book.resources.populateFromManifest(manifest)
                val spine = root.child("spine", epub, container, namespace).let {
                    PackageSpine.fromElement(book, manifest, it, container, prefix)
                }
                book.pages.populateFromSpine(spine)
                // we parse 'metadata' last of the mandatory elements so that we can reference resources
                val metadata = root.child("metadata", epub, container, namespace).let {
                    PackageMetadata.fromElement(book, it, container, prefix)
                }

                // optional
                val guide = root.getChild("guide", namespace)?.let { PackageGuide.fromElement(book, it, container) }
                val bindings = root.getChild("bindings", namespace)?.let { PackageBindings.fromElement(book, it, container) }
                val collection =
                    root.getChild("collection", namespace)?.let { PackageCollection.fromElement(book, it, container) }
                val tours = root.getChild("tours", namespace)?.let { PackageTours.fromElement(book, it, container) }

                return PackageDocument(
                    book,
                    container,
                    uniqueIdentifier,
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
                ).also { book.packageDocument = it }
            }
        }
    }
}