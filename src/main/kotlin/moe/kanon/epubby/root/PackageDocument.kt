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

package moe.kanon.epubby.root

import moe.kanon.epubby.Book
import moe.kanon.epubby.DocumentSerializer
import moe.kanon.epubby.EpubDeprecated
import moe.kanon.epubby.EpubLegacy
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.logger
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.epubby.utils.Direction
import moe.kanon.epubby.utils.combineWith
import moe.kanon.epubby.utils.getAttributeValueOrNone
import moe.kanon.epubby.utils.getChildOrNone
import moe.kanon.epubby.utils.localeOf
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.epubby.utils.requireMaxFormat
import moe.kanon.epubby.utils.saveTo
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.xml.xml
import org.jdom2.Document
import org.jdom2.Namespace
import java.nio.file.Path
import java.util.*

/**
 * Represents a [package document](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-package-doc).
 *
 * TODO
 *
 * @property [book] The [Book] instance that `this` package-document is tied to.
 * @property [file] The underlying package document file that this class is wrapped around.
 * @property [direction] Specifies the base text direction of the content and attribute values of the carrying element
 * and its descendants.
 *
 * Inherent directionality specified using unicode takes precedence over this property.
 * @property [identifier] The identifier of `this` package-document.
 *
 * This *MUST* be unique within the document scope.
 * @property [prefix] Defines additional prefix mappings not reserved by the epub specification.
 *
 * [Read more](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-prefix-attr)
 * @property [language] Specifies the language used in all the sub-elements of this package-document.
 * @property [uniqueIdentifier] Should be an `IDREF` that identifies the `dc:identifier` element.
 * @property [version] This specifies which epub specification the [book] was made to follow.
 * @property [metadata] TODO
 * @property [manifest] TODO
 * @property [spine] TODO
 */
class PackageDocument private constructor(
    val book: Book,
    val file: Path,
    @SerializedName("dir") val direction: Option<Direction>,
    @SerializedName("id") val identifier: Option<String>,
    val prefix: Option<String>,
    @SerializedName("xml:lang") val language: Option<Locale>,
    val uniqueIdentifier: String,
    val metadata: PackageMetadata,
    val manifest: PackageManifest,
    val spine: PackageSpine
) : DocumentSerializer {
    companion object {
        private const val NAMESPACE_URI = "http://www.idpf.org/2007/opf"
        internal val NAMESPACE = Namespace.getNamespace(NAMESPACE_URI)

        internal fun parse(book: Book, file: Path): PackageDocument = parseXmlFile(file) {
            fun malformed(reason: String): Nothing = raiseMalformedError(book.originFile, file, reason)

            val dir = getAttributeValueOrNone("dir").map(Direction.Companion::of)
            val id = getAttributeValueOrNone("id")
            val prefix = getAttributeValueOrNone("prefix")
            val lang = getAttributeValueOrNone("lang", Namespace.XML_NAMESPACE).map(::localeOf)
            val uniqueIdentifier =
                getAttributeValue("unique-identifier") ?: malformed("missing required 'unique-identifier' attribute")
            val metadata = PackageMetadata.parse(
                book,
                file,
                getChild("metadata", Namespace.getNamespace("", NAMESPACE_URI))
                    ?: malformed("missing 'metadata' element")
            )
            val manifest = PackageManifest.parse(
                book,
                file,
                getChild("manifest", namespace) ?: malformed("missing 'manifest' element")
            )
            val spine = PackageSpine.parse(
                book,
                file,
                manifest,
                getChild("spine", namespace) ?: malformed("missing 'spine' element")
            )

            PackageDocument(book, file, dir, id, prefix, lang, uniqueIdentifier, metadata, manifest, spine).apply {
                guide = getChildOrNone("guide", namespace).map { PackageGuide.parse(book, file, it) }
                bindings = getChildOrNone("bindings", namespace).map { TODO() }
                collection = getChildOrNone("collection", namespace).map { TODO() }
                tours = getChildOrNone("tours", namespace).map { PackageTours.parse(book, file, it) }
            }
        }
    }

    /**
     * Returns the [guide][PackageGuide] tied to this package document, or [None] if this package document has no
     * `guide` element.
     */
    @EpubLegacy(Book.Format.EPUB_3_0)
    var guide: Option<PackageGuide> = None
        /**
         * Sets the [guide] property to the given [value].
         *
         * Note that the `guide` property can *only* be set once, any further attempts will result in a
         * [EpubbyException] being thrown.
         *
         * @throws [EpubbyException] if the [guide] property is already set.
         */
        set(value) {
            if (value != None) {
                if (field.isPresent) throw EpubbyException(
                    book.file,
                    "Can't overwrite already existing 'guide' element"
                )
                field = value
            }
        }

    /**
     * Returns the [bindings][PackageBindings] tied to this package document, or [None] if this package document has no
     * `bindings` element.
     */
    @EpubDeprecated(Book.Format.EPUB_3_2)
    var bindings: Option<PackageBindings> = None
        /**
         * Sets the [bindings] property to the given [value].
         *
         * Note that the `bindings` property can *only* be set once, any further attempts will result in a
         * [EpubbyException] being thrown.
         *
         * @throws [EpubbyException] if the [bindings] property is already set.
         */
        set(value) {
            if (value != None) {
                if (field.isPresent) throw EpubbyException(
                    book.file,
                    "Can't overwrite already existing 'bindings' element"
                )
                field = value
            }
        }

    /**
     * Returns the [collection][PackageCollection] tied to this package document, or [None] if this package document
     * has no `collection` element.
     */
    var collection: Option<PackageCollection> = None
        /**
         * Sets the [collection] property to the given [value].
         *
         * Note that the `collection` property can *only* be set once, any further attempts will result in a
         * [EpubbyException] being thrown.
         *
         * @throws [EpubbyException] if the [collection] property is already set.
         */
        set(value) {
            if (value != None) {
                if (field.isPresent) throw EpubbyException(
                    book.file,
                    "Can't overwrite already existing 'collection' element"
                )
                field = value
            }
        }

    /**
     * Returns the [tours][PackageTours] of this package document, or [None] if this package document has no `tours`
     * element.
     */
    @EpubDeprecated(Book.Format.EPUB_2_0)
    var tours: Option<PackageTours> = None
        /**
         * Sets the [tours] property to the given [value].
         *
         * Note that the `tours` property can *only* be set once, any further attempts will result in a
         * [EpubbyException] being thrown.
         *
         * @throws [EpubbyException] if the [tours] property is already set, or [book.format][Book.format] >
         * [EPUB 2.0][Book.Format.EPUB_2_0]
         */
        set(value) {
            if (value != None) {
                requireMaxFormat(book, Book.Format.EPUB_2_0) { "'tours' element has been deprecated since EPUB 2.0" }
                if (field.isPresent) throw EpubbyException(
                    book.file,
                    "Can't overwrite already existing 'tours' element"
                )
                field = value
            }
        }

    internal fun save() {
        metadata.updateLastModified()
        logger.debug { "Saving package document to file <${book.file.combineWith(file)}>" }
        toDocument().saveTo(file)
    }

    override fun toDocument(): Document = xml("package", Namespace.getNamespace(NAMESPACE_URI)) {
        attributes {
            "version" { book.version.toString() }
            "unique-identifier" { uniqueIdentifier }
            direction.ifPresent { "dir" { it } }
            identifier.ifPresent { "id" { it } }
            language.ifPresent {
                source.setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE)
            }
            prefix.ifPresent { "prefix" { it } }
        }
        addContent(metadata.toElement())
        addContent(manifest.toElement())
        addContent(spine.toElement())
        guide.ifPresent { addContent(it.toElement()) }
        bindings.ifPresent { addContent(it.toElement()) }
        collection.ifPresent { addContent(it.toElement()) }
        tours.ifPresent { addContent(it.toElement()) }
    }.document

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageDocument -> false
        book != other.book -> false
        direction != other.direction -> false
        identifier != other.identifier -> false
        prefix != other.prefix -> false
        language != other.language -> false
        uniqueIdentifier != other.uniqueIdentifier -> false
        metadata != other.metadata -> false
        manifest != other.manifest -> false
        spine != other.spine -> false
        guide != other.guide -> false
        bindings != other.bindings -> false
        collection != other.collection -> false
        tours != other.tours -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + prefix.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + uniqueIdentifier.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + manifest.hashCode()
        result = 31 * result + spine.hashCode()
        result = 31 * result + guide.hashCode()
        result = 31 * result + bindings.hashCode()
        result = 31 * result + collection.hashCode()
        result = 31 * result + tours.hashCode()
        return result
    }

    override fun toString(): String =
        "PackageDocument(book=$book, uniqueIdentifier='$uniqueIdentifier', hasGuide=${guide.isPresent}, hasBindings=${bindings.isPresent}, hasCollection=${collection.isPresent}, hasTours=${tours.isPresent})"
}