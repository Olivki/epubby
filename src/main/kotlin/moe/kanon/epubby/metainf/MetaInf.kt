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

package moe.kanon.epubby.metainf

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.DocumentSerializer
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.epubby.resources.root.PackageDocument
import moe.kanon.epubby.utils.SemVer
import moe.kanon.epubby.utils.SemVerType
import moe.kanon.epubby.utils.parseFile
import moe.kanon.epubby.utils.saveTo
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.io.paths.notExists
import moe.kanon.xml.Namespace
import moe.kanon.xml.xml
import org.jdom2.Document
import java.io.IOException
import java.nio.file.Path

/**
 * Represents the [META-INF](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf)
 * directory found in the root of an epub container.
 *
 * This class contains references to the [container.xml][Container], [encryption.xml][Encryption],
 * [manifest.xml][Manifest], [metadata.xml][Metadata], [rights.xml][Rights] and [signatures.xml][Signatures] files
 * located inside the `META-INF` directory.
 *
 * Note that *only* [container.xml][container] is REQUIRED to exist in an epub, the others are OPTIONAL.
 *
 * @property [directory] The path that points to the `META-INF` directory of the epub container.
 * @property [container] TODO
 * @property [encryption] TODO
 * @property [manifest] TODO
 * @property [metadata] TODO
 * @property [rights] TODO
 * @property [signatures] TODO
 */
class MetaInf private constructor(
    val epub: Path,
    val directory: Path,
    val container: Container,
    val encryption: Option<Encryption>,
    val manifest: Option<Manifest>,
    val metadata: Option<Metadata>,
    val rights: Option<Rights>,
    val signatures: Option<Signatures>
) {
    companion object {
        /**
         * Traverses the given [directory] parsing each file into their appropriate classes, and then returns the
         * resulting [MetaInf] instance.
         *
         * @param [epub] the path to the epub container file
         * @param [directory] the path to the `META-INF` directory located at the root of the [epub] container
         *
         * @throws [EpubbyException] if something went wrong when traversing the [directory] file
         * @throws [IOException] if an i/o error occurred
         */
        internal fun parse(epub: Path, directory: Path): MetaInf {
            if (directory.resolve("container.xml").notExists) {
                raiseMalformedError(epub, directory, "'container.xml' is missing")
            }

            val container = Container.parse(epub, directory.resolve("container.xml"))

            // TODO: Serialize the rest of the files

            return MetaInf(
                epub,
                directory,
                container,
                encryption = None,
                manifest = None,
                metadata = None,
                rights = None,
                signatures = None
            )
        }
    }

    /**
     * Attempts to save all the available classes into their file form.
     */
    @Throws(EpubbyException::class, IOException::class)
    fun saveAll() {
        container.toDocument().saveTo(directory, "container.xml")
        encryption.ifPresent { it.toDocument().saveTo(directory, "encryption.xml") }
        manifest.ifPresent { it.toDocument().saveTo(directory, "manifest.xml") }
        metadata.ifPresent { it.toDocument().saveTo(directory, "metadata.xml") }
        rights.ifPresent { it.toDocument().saveTo(directory, "rights.xml") }
        signatures.ifPresent { it.toDocument().saveTo(directory, "signatures.xml") }
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is MetaInf -> false
        container != other.container -> false
        encryption != other.encryption -> false
        manifest != other.manifest -> false
        metadata != other.metadata -> false
        rights != other.rights -> false
        signatures != other.signatures -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = container.hashCode()
        result = 31 * result + encryption.hashCode()
        result = 31 * result + manifest.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + rights.hashCode()
        result = 31 * result + signatures.hashCode()
        return result
    }
}

/**
 * Represents the [container.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-container.xml)
 * file found inside of the [META-INF][MetaInf] directory.
 */
class Container private constructor(
    val file: Path,
    val version: SemVer,
    private val _rootFiles: MutableList<RootFile>,
    private val _links: MutableList<Link>
) : DocumentSerializer {
    companion object {
        private const val NAMESPACE_URI = "urn:oasis:names:tc:opendocument:xmlns:container"

        internal fun parse(epub: Path, file: Path): Container = parseFile(file) {
            fun malformed(reason: String): Nothing = raiseMalformedError(epub, file, reason)
            val root = file.parent.parent
            //val namespace = Namespace("", NAMESPACE_URI)
            val version = SemVer(
                getAttributeValue("version") ?: malformed("missing 'version' attribute on 'container' element"),
                SemVerType.LOOSE
            )
            // as stated in the specification; "OCF Processors MUST ignore foreign elements and attributes within a
            // container.xml file.", which means that we want to ONLY filter for elements named "rootfile"
            val rootFiles = getChild("rootfiles", namespace)?.children
                ?.asSequence()
                ?.filter { it.name == "rootfile" }
                ?.map {
                    val fullPath: String? = it.getAttributeValue("full-path")
                    val mediaType: String? = it.getAttributeValue("media-type")
                    RootFile(
                        root.resolve(fullPath ?: malformed("'rootfile' element missing 'full-path' attribute")),
                        mediaType ?: malformed("'rootfile' element missing 'media-type' attribute")
                    )
                }
                ?.toMutableList() ?: malformed("missing 'rootfiles' element")
            val links = getChild("links", namespace)?.children
                ?.asSequence()
                ?.filter { it.name == "link" }
                ?.map {
                    val href: String? = null
                    val relation: String? = null
                    val mediaType: String? = null
                    Link(
                        root.resolve(href ?: malformed("'link' element missing 'href' attribute")),
                        relation ?: malformed("'link' element missing 'rel' attribute"),
                        Option(mediaType)
                    )
                }
                ?.toMutableList() ?: mutableListOf()
            return Container(file, version, rootFiles, links)
        }
    }

    private val root: Path get() = file.parent.parent

    /**
     * Returns a list of all the [RootFile] instances stored in this container.
     */
    val rootFiles: ImmutableList<RootFile> get() = _rootFiles.toImmutableList()

    /**
     * Returns a list of all the [Link] instances stored in this container.
     *
     * Note that the `links` element is *optional*, so there is no guarantee that this list will contain anything.
     */
    val links: ImmutableList<Link> get() = _links.toImmutableList()

    /**
     * Returns the first [root file][RootFile] stored in this container.
     *
     * Per the epub ocf specification, the first element inside of `rootfiles` should *always* be pointing towards the
     * [package document][PackageDocument];
     *
     * > An OCF Processor *MUST* consider the first `rootfile` element within the `rootfiles` element to represent the
     * Default Rendition for the contained EPUB Publication.
     */
    val packageDocument: RootFile get() = _rootFiles[0]

    override fun toDocument(): Document = xml("container", Namespace("", NAMESPACE_URI)) {
        attribute("version") { version }

        element("rootfiles") {
            for ((fullPath, mediaType) in _rootFiles) {
                element("rootfile") {
                    attributes {
                        "full-path" { fullPath.toString().substring(1) }
                        "media-type" { mediaType }
                    }
                }
            }
        }

        if (_links.isNotEmpty()) {
            element("links") {
                for ((href, relation, mediaType) in _links) {
                    element("link") {
                        attributes {
                            "href" { href }
                            "rel" { relation }
                            mediaType.ifPresent { "mediaType" { it } }
                        }
                    }
                }
            }
        }
    }.document


    override fun toString(): String = "Container(version='$version', rootFiles=$_rootFiles, links=$_links)"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Container -> false
        file != other.file -> false
        version != other.version -> false
        _rootFiles != other._rootFiles -> false
        _links != other._links -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + _rootFiles.hashCode()
        result = 31 * result + _links.hashCode()
        return result
    }

    /**
     * Represents the `rootfile` element inside of the `container.xml` file.
     */
    data class RootFile internal constructor(val fullPath: Path, val mediaType: String)

    /**
     * Represents the optional `link` element inside of the `container.xml` file.
     *
     * @property [href] The location of the resource that this is linking to.
     * @property [relation] Identifies the relationship of the resource.
     * @property [mediaType] A [media-type](https://tools.ietf.org/html/rfc2046) that specifies the type and format of
     * the resource reference by this link.
     *
     * A link element *MAY* include a `media-type` attribute, which means that there is no guarantee that this property
     * will hold any value.
     */
    data class Link internal constructor(
        val href: Path,
        @SerializedName("rel") val relation: String,
        val mediaType: Option<String>
    )
}

/**
 * Represents the [encryption.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-encryption.xml)
 * file found inside of the [META-INF][MetaInf] directory.
 *
 * Note that while Epubby does support the parsing of the `encryption.xml` file, as of right now, it does not do
 * anything with the data that is parsed, meaning that while the system takes note that the `encryption.xml` exists,
 * it will not attempt to decrypt any files. This means that Epubby will most likely fail when attempting to parse an
 * epub container that has been encrypted/obfuscated.
 */
class Encryption internal constructor(val file: Path) : DocumentSerializer {
    override fun toDocument(): Document {
        TODO("not implemented")
    }
}

/**
 * Represents the [manifest.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-manifest.xml)
 * file found inside of the [META-INF][MetaInf] directory.
 */
class Manifest internal constructor(val file: Path) : DocumentSerializer {
    override fun toDocument(): Document {
        TODO("not implemented")
    }
}

/**
 * Represents the [metadata.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-metadata.xml)
 * file found inside of the [META-INF][MetaInf] directory.
 */
class Metadata internal constructor(val file: Path) : DocumentSerializer {
    override fun toDocument(): Document {
        TODO("not implemented")
    }
}

/**
 * Represents the [rights.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-rights.xml)
 * file found inside of the [META-INF][MetaInf] directory.
 */
class Rights internal constructor(val file: Path) : DocumentSerializer {
    override fun toDocument(): Document {
        TODO("not implemented")
    }
}

/**
 * Represents the [Digital Signatures](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-signatures.xml)
 * file found inside of the [META-INF][MetaInf] directory.
 */
class Signatures internal constructor(val file: Path) : DocumentSerializer {
    override fun toDocument(): Document {
        TODO("not implemented")
    }
}