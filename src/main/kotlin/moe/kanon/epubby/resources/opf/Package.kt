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

@file:JvmName("")
@file:Suppress("DataClassPrivateConstructor")

package moe.kanon.epubby.resources.opf

import arrow.core.Option
import moe.kanon.epubby.Book
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.toc.TableOfContents
import moe.kanon.kommons.collections.putAndReturn
import moe.kanon.kommons.lang.normalizedName
import moe.kanon.xml.parseAsDocument
import java.nio.file.Path
import java.util.Objects

/**
 * Implementation of the [OPF](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.0) format.
 */
class PackageDocument private constructor(val book: Book) {
    /**
     * The meta-data of the underlying [book].
     */
    val metadata: BookMetadata = BookMetadata.newInstance(book)
    
    /**
     * The [manifest](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-manifest-elem) of the underlying
     * [book].
     */
    val manifest: BookManifest = BookManifest.newInstance(book)
    
    /**
     * The spine of the underlying [book].
     */
    val spine: BookSpine = BookSpine.newInstance(book)
    
    /**
     * The guide of the underlying [book], if it has one.
     */
    val guide: Option<PackageGuide> = Option.just(PackageGuide.newInstance(book))
    
    /**
     * Serializes all of the sub-classes of this content container into XML and then saves it as the `OPF` file of this
     * epub.
     */
    fun save() {
        TODO()
    }
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageDocument -> false
        manifest != other.manifest -> false
        guide != other.guide -> false
        metadata != other.metadata -> false
        spine != other.spine -> false
        else -> true
    }
    
    override fun hashCode(): Int = Objects.hash(manifest, guide, metadata, spine)
    
    override fun toString(): String = "BookContent(manifest=$manifest, guide=$guide, metadata=$metadata, spine=$spine)"
    
    companion object {
        /**
         * Creates and populates a [PackageDocument] instance from the specified [opfFile].
         */
        @JvmStatic
        fun from(book: Book, opfFile: Path): PackageDocument {
            val content = PackageDocument(book)
            
            opfFile.parseAsDocument {
                element("thing") {
                
                }
            }
            
            return content
        }
    }
}

/**
 * Implementation of the [metadata](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.2) specification.
 *
 * @property [book] The [Book] instance that `this` metadata is bound to.
 */
class BookMetadata private constructor(val book: Book) {
    
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book): BookMetadata = BookMetadata(book)
    }
}

/**
 * Implementation of the [manifest](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.3) specification.
 *
 * Note that the layout of the manifest will change depending on the [format][Book.format] used by the specified `book`.
 *
 * @property [book] The [Book] instance that `this` manifest is bound to.
 */
class BookManifest private constructor(val book: Book) {
    
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book): BookManifest = BookManifest(book)
    }
}

/**
 * Implementation of the [spine](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.4) specification.
 *
 * @property [book] The [Book] instance that `this` spine is bound to.
 */
class BookSpine private constructor(val book: Book) {
    
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book): BookSpine = BookSpine(book)
    }
}

/**
 * Implementation of the [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) specification.
 *
 * Within the [package][PackageDocument] there **may** be one `guide element`, containing one or more reference
 * elements. The guide element identifies fundamental structural components of the publication, to enable
 * [Reading Systems](http://www.idpf.org/epub/31/spec/epub-spec.html#gloss-epub-reading-system) to provide convenient
 * access to them.
 *
 * @property [book] The [Book] instance that `this` guide is bound to.
 */
class PackageGuide private constructor(val book: Book) : Iterable<PackageGuide.Reference> {
    private val delegate: MutableMap<String, Reference> = LinkedHashMap()
    
    // TODO: Documentation
    
    // get
    @JvmName("getReference")
    operator fun get(type: String): Option<Reference> = Option.fromNullable(delegate[type])
    
    @JvmName("getReference")
    operator fun get(type: GuideType): Option<Reference> = Option.fromNullable(delegate[type.serializedName])
    
    // add
    // - custom values
    @JvmOverloads
    @JvmName("addReference")
    fun add(type: String, href: String, title: String? = null): Reference =
        delegate.putAndReturn("other.$type", Reference.newInstance(this, type, href, Option.fromNullable(title)))
    
    @JvmOverloads
    @JvmName("addReference")
    fun add(type: String, resource: PageResource, title: String? = null): Reference =
        delegate.putAndReturn(
            "other.$type",
            Reference.newInstance(this, type, resource.href, Option.fromNullable(title))
        )
    
    // - known values
    @JvmOverloads
    @JvmName("addReference")
    fun add(type: GuideType, href: String, title: String? = type.normalizedName) =
        delegate.putAndReturn(
            type.serializedName,
            Reference.newInstance(this, type.serializedName, href, Option.fromNullable(title))
        )
    
    @JvmOverloads
    @JvmName("addReference")
    fun add(type: GuideType, resource: PageResource, title: String? = type.normalizedName) =
        delegate.putAndReturn(
            type.serializedName,
            Reference.newInstance(this, type.serializedName, resource.href, Option.fromNullable(title))
        )
    
    // remove
    // - custom values
    @JvmName("removeReference")
    fun remove(type: String) {
        delegate -= "other.$type"
    }
    
    @JvmSynthetic
    operator fun minusAssign(type: String) {
        delegate -= "other.$type"
    }
    
    // - known values
    @JvmName("removeReference")
    fun remove(type: GuideType) {
        delegate -= type.serializedName
    }
    
    @JvmSynthetic
    operator fun minusAssign(type: GuideType) {
        delegate -= type.serializedName
    }
    
    // TODO: set / change
    
    // iterator
    override fun iterator(): Iterator<Reference> = delegate.values.toList().iterator()
    
    // object defaults
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageGuide -> false
        book != other.book -> false
        delegate != other.delegate -> false
        else -> true
    }
    
    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + delegate.hashCode()
        return result
    }
    
    override fun toString(): String = "PackageGuide(book=$book, references=$delegate)"
    
    // reference
    /**
     * Implementation of the `reference` element contained inside of the [guide element][PackageGuide].
     *
     * @property [parent] The parent [PackageGuide] of `this` reference.
     * @property [type] The `type` of `this` reference.
     * @property [href] The `href` to the location of the `resource` that `this` reference is referencing.
     * @property [title] The title that a `Reading System` would use to display `this` reference.
     *
     * The `title` property is *not* required for a `Reference` to be valid.
     */
    data class Reference private constructor(
        val parent: PackageGuide,
        val type: String,
        val href: String,
        val title: Option<String>
    ) {
        /**
         * Returns the [GuideType] tied to the specified [type] of `this` reference.
         *
         * This returns a [Option] as [GuideType.of] is not guaranteed to always return a value, as `this` reference
         * might be using a custom `type`.
         */
        val guideType: Option<GuideType> = GuideType.of(type)
        
        /**
         * Returns whether or not `this` reference is using a custom [type].
         */
        val isCustomType: Boolean get() = guideType.isEmpty()
        
        // TODO: Throw a hissy fit if a resource can't be found with the specified 'href'?
        
        /**
         * Lazily returns the [PageResource] tied to the specified [href] of `this` reference.
         *
         * This returns a [Option] as the `href` of `this` reference might be pointing to a non-existent resource.
         */
        val resource: Option<PageResource> by lazy { parent.book.resources.getOr<PageResource>(href) }
        
        companion object {
            @JvmSynthetic
            internal fun newInstance(
                parent: PackageGuide,
                type: String,
                href: String,
                title: Option<String>
            ): Reference = Reference(parent, type, href, title)
        }
    }
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book): PackageGuide = PackageGuide(book)
    }
}

/**
 * Implementation of the "list of `type` values" declared in the
 * [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) specification
 *
 * @property [serializedName] The name used in the actual serialized form of `this` type.
 */
enum class GuideType(val serializedName: String) {
    /**
     * A page containing the book cover(s), jacket information, etc..
     */
    COVER("cover"),
    /**
     * A page possibly containing the title, author, publisher, and other metadata
     */
    TITLE_PAGE("title-page"),
    /**
     * The [table of contents][TableOfContents] page.
     * TODO: Replace above reference with actual page class implementation
     */
    TABLE_OF_CONTENTS("toc"),
    /**
     * A back-of-book style index page.
     */
    INDEX("index"),
    /**
     * A glossary page.
     */
    GLOSSARY("glossary"),
    /**
     * A page containing various acknowledgements.
     */
    ACKNOWLEDGEMENTS("acknowledgements"),
    /**
     * A page containing the bibliography of the author.
     */
    BIBLIOGRAPHY("bibliography"),
    /**
     * A [colophon](https://en.wikipedia.org/wiki/Colophon_(publishing)) page.
     */
    COLOPHON("colophon"),
    /**
     * A page detailing the copyright that the [book] is under.
     */
    COPYRIGHT_PAGE("copyright-page"),
    /**
     * A page describing who the [book] is dedicated towards.
     */
    DEDICATION("dedication"),
    /**
     * A [epigraph](https://en.wikipedia.org/wiki/Epigraph_(literature)) page.
     */
    EPIGRAPH("epigraph"),
    /**
     * A page containing a [foreword](https://en.wikipedia.org/wiki/Foreword) from the author, translator, editor,
     * etc..
     */
    FOREWORD("foreword"),
    /**
     * A page containing a list of all the illustrations used throughout the [book].
     */
    LIST_OF_ILLUSTRATIONS("loi"),
    /**
     * A page containing a list of all the tables used throughout the [book].
     */
    LIST_OF_TABLES("lot"),
    /**
     * A page containing some sort of notes; authors notes, editors notes, translation notes, etc..
     */
    NOTES("notes"),
    /**
     * A page containing a [preface](https://en.wikipedia.org/wiki/Preface) to the [book].
     */
    PREFACE("preface"),
    /**
     * First "real" page of content. *(e.g. "Chapter 1")*
     */
    TEXT("text");
    
    companion object {
        /**
         * Returns the first [GuideType] that has a [serializedName] that matches the specified [type].
         *
         * @param [type] the type to match all `guide-types` against
         */
        @JvmStatic
        fun of(type: String): Option<GuideType> =
            Option.fromNullable(values().firstOrNull { it.serializedName == type })
    }
}