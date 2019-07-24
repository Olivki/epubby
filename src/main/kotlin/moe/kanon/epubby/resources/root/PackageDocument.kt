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
import moe.kanon.epubby.DocumentSerializer
import moe.kanon.epubby.EpubDeprecated
import moe.kanon.epubby.EpubLegacy
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.epubby.utils.Direction
import moe.kanon.epubby.utils.SemVer
import moe.kanon.epubby.utils.parseFile
import moe.kanon.epubby.utils.stringify
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.writeOut
import org.jdom2.Document
import org.jdom2.Namespace
import java.nio.file.Path

/**
 * Represents a [package document](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-package-doc).
 *
 * TODO
 *
 * @property [book] The [Book] instance that `this` package-document is tied to.
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
 * @property [lang] Specifies the language used in all the sub-elements of this package-document.
 * @property [uniqueIdentifier] Should be an `IDREF` that identifies the `dc:identifier` element.
 * @property [version] This specifies which epub specification the [book] was made to follow.
 * @property [metadata] TODO
 * @property [manifest] TODO
 * @property [spine] TODO
 */
class PackageDocument private constructor(
    val book: Book,
    @SerializedName("dir") val direction: Option<Direction>,
    @SerializedName("id") val identifier: Option<String>,
    val prefix: Option<String>,
    val lang: Option<String>,
    val uniqueIdentifier: String,
    val version: SemVer,
    val metadata: PackageMetadata,
    val manifest: PackageManifest,
    val spine: PackageSpine
) : DocumentSerializer {
    companion object {
        private const val METADATA_NAMESPACE_URI = "http://www.idpf.org/2007/opf"

        internal fun parse(book: Book, packageDocument: Path): PackageDocument = parseFile(packageDocument) {
            fun malformed(reason: String): Nothing = raiseMalformedError(book.originFile, packageDocument, reason)

            val metadata = PackageMetadata.parse(
                book,
                packageDocument,
                getChild("metadata", Namespace.getNamespace("", METADATA_NAMESPACE_URI))
                    ?: malformed("missing 'metadata' element")
            )

            writeOut(metadata.toElement().stringify())
            TODO()
        }
    }

    // TODO: Make a function for updating last modified

    /**
     * Returns the [guide][PackageGuide] tied to this package document, or [None] if this package document has no
     * `guide` element.
     */
    @EpubLegacy("3.0")
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
            if (field.isPresent) throw EpubbyException(book.file, "Can't overwrite already existing 'guide' element")
            field = value
        }

    /**
     * Returns the [bindings][PackageBindings] tied to this package document, or [None] if this package document has no
     * `bindings` element.
     */
    @EpubDeprecated("3.2")
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
            if (field.isPresent) throw EpubbyException(book.file, "Can't overwrite already existing 'bindings' element")
            field = value
        }

    override fun toDocument(): Document {
        TODO("not implemented")
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageDocument -> false
        book != other.book -> false
        direction != other.direction -> false
        identifier != other.identifier -> false
        prefix != other.prefix -> false
        lang != other.lang -> false
        uniqueIdentifier != other.uniqueIdentifier -> false
        version != other.version -> false
        metadata != other.metadata -> false
        manifest != other.manifest -> false
        spine != other.spine -> false
        guide != other.guide -> false
        bindings != other.bindings -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + prefix.hashCode()
        result = 31 * result + lang.hashCode()
        result = 31 * result + uniqueIdentifier.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + manifest.hashCode()
        result = 31 * result + spine.hashCode()
        result = 31 * result + guide.hashCode()
        result = 31 * result + bindings.hashCode()
        return result
    }
}