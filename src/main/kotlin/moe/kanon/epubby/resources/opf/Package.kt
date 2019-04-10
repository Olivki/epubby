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
import moe.kanon.epubby.BookException
import moe.kanon.epubby.resources.HREF
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.opf.PackageGuide.Reference
import moe.kanon.epubby.resources.toc.TableOfContents
import moe.kanon.epubby.utils.orElseThrow
import moe.kanon.kommons.collections.putAndReturn
import moe.kanon.kommons.lang.normalizedName
import moe.kanon.xml.parseAsDocument
import java.nio.file.Path

/**
 * Implementation of the [OPF](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.0) format.
 */
class PackageDocument private constructor(val book: Book) {
    /**
     * The meta-data of the underlying [book].
     */
    val metadata: BookMetadata = BookMetadata(this)
    
    /**
     * The [manifest](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-manifest-elem) of the underlying
     * [book].
     */
    val manifest: BookManifest = BookManifest(this)
    
    /**
     * The spine of the underlying [book].
     */
    val spine: BookSpine = BookSpine(this)
    
    /**
     * The guide of the underlying [book], if it has one.
     *
     * A `book` is *not* guaranteed to have a guide, as per the OPF format specification; "Within the package there
     * **may** be one `guide` element"[¹](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6)
     *
     * The specification also states that if a guide exists, it needs to contain *at least* one `reference` element,
     * due to this epubby will remove and *not* serialize any existing `guide`s that are empty, even if they were
     * explicitly created by the user.
     *
     * Due to the above reasons, this `guide` property will start out as an `empty` [Option], to create a `guide` for
     * a book, use the [createGuide] function. Note that the `createGuide` function will throw an exception if a
     * `guide` already exists.
     */
    var guide: Option<PackageGuide> = Option.empty()
        @JvmSynthetic internal set
    
    /**
     * Returns whether or not `this` OPF document has a [guide] defined.
     */
    fun hasGuide(): Boolean = guide.nonEmpty()
    
    /**
     * Attempts to create a new [guide] for `this` OPF document, throwing a [UnsupportedOperationException] if a
     * `guide` already exists.
     */
    fun createGuide() {
        if (hasGuide()) throw UnsupportedOperationException("OPF document already has a 'guide' defined")
        guide = Option.just(PackageGuide(this))
    }
    
    /**
     * Serializes all the contents of `this` OPF document into the [epub file][Book.file] of the underlying [book].
     *
     * Note that this function will replace any OPF files that already exist.
     */
    fun createFile() {
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
    
    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + manifest.hashCode()
        result = 31 * result + spine.hashCode()
        result = 31 * result + guide.hashCode()
        return result
    }
    
    override fun toString(): String =
        "PackageDocument(metadata=$metadata, manifest=$manifest, spine=$spine${if (hasGuide()) ", guide=$guide)" else ")"}"
    
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
 * @property [container] The [OPF document][PackageDocument] that `this` metadata belongs to.
 * @property [book] The [Book] instance that `this` metadata is bound to.
 */
class BookMetadata internal constructor(val container: PackageDocument, val book: Book = container.book)

/**
 * Implementation of the [manifest](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.3) specification.
 *
 * Note that the layout of the manifest will change depending on the [format][Book.format] used by the specified `book`.
 *
 * @property [container] The [OPF document][PackageDocument] that `this` manifest belongs to.
 * @property [book] The [Book] instance that `this` manifest is bound to.
 */
class BookManifest internal constructor(val container: PackageDocument, val book: Book = container.book)

/**
 * Implementation of the [spine](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.4) specification.
 *
 * @property [container] The [OPF document][PackageDocument] that `this` spine belongs to.
 * @property [book] The [Book] instance that `this` spine is bound to.
 */
class BookSpine internal constructor(val container: PackageDocument, val book: Book = container.book)

/**
 * Implementation of the [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) specification.
 *
 * Within the [package][PackageDocument] there **may** be one `guide element`, containing one or more reference
 * elements. The guide element identifies fundamental structural components of the publication, to enable
 * [Reading Systems](http://www.idpf.org/epub/31/spec/epub-spec.html#gloss-epub-reading-system) to provide convenient
 * access to them.
 *
 * Each [reference][Reference] must have a [href][Reference.href] referring to an [OPS Content Document][PageResource]
 * included in the [manifest][BookManifest], and which may include a fragment identifier as defined in section 4.1 of
 * [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt). `Reading Systems` may use the bounds of the referenced element to
 * determine the scope of the reference. If a fragment identifier is not used, the scope is considered to be the entire
 * document. This specification does not require `Reading Systems` to mark or otherwise identify the entire scope of a
 * referenced element.
 *
 * @property [container] The [OPF document][PackageDocument] that `this` guide belongs to.
 * @property [book] The [Book] instance that `this` guide is bound to.
 */
class PackageGuide internal constructor(val container: PackageDocument, val book: Book = container.book) :
    Iterable<GuideReference> {
    private val elements: MutableMap<String, GuideReference> = LinkedHashMap()
    
    /**
     * Returns whether or not there are any `reference` elements stored in `this` guide.
     *
     * If this function returns `true` when [Book.saveTo] has been invoked there will be no `guide` element serialized
     * into the [OPF document][PackageDocument]. This is because while the `guide` element is *not* required to exist
     * in a epub file, if it *does* exist, it needs to contain *at least* one `reference` element.
     */
    fun isEmpty(): Boolean = elements.isEmpty()
    
    // TODO: Documentation
    
    // get
    // - custom values
    /**
     * Returns the [reference][Reference] with the specified [customType], or none if no `reference` is found.
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][GuideType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, epubby prepends the `"other."` string to the specified `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * the same for all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     */
    @JvmName("getCustomReferenceOrNone")
    operator fun get(customType: String): Option<GuideReference> = Option.fromNullable(elements["other.$customType"])
    
    // - known values
    @JvmName("getReferenceOrNone")
    operator fun get(type: GuideType): Option<GuideReference> = Option.fromNullable(elements[type.serializedName])
    
    // add
    // - custom values
    /**
     * Creates and adds a [reference][Reference] element to `this` guide.
     *
     * The `reference` element is constructed from the specified [customType], [resource] and [title].
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][GuideType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, epubby prepends the `"other."` string to the specified `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * the same for all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     * @param [resource] the [Resource] to inherit the [href][Resource.href] of
     *
     * @return the newly created `reference` element
     */
    @JvmOverloads
    @JvmName("addCustomReference")
    fun addCustom(customType: String, resource: PageResource, title: String? = null): GuideReference =
        elements.putAndReturn(
            "other.$customType",
            GuideReference(this, customType, resource.href, Option.fromNullable(title))
        )
    
    // - known values
    @JvmOverloads
    @JvmName("addReference")
    fun add(type: GuideType, resource: PageResource, title: String? = type.normalizedName): GuideReference =
        elements.putAndReturn(
            type.serializedName,
            GuideReference(this, type.serializedName, resource.href, Option.fromNullable(title))
        )
    
    // remove
    // - custom values
    /**
     * TODO
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][GuideType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, epubby prepends the `"other."` string to the specified `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * the same for all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     */
    @JvmName("removeCustomReference")
    fun removeCustom(customType: String) {
        elements -= "other.$customType"
    }
    
    @JvmSynthetic
    operator fun minusAssign(customType: String) {
        elements -= "other.$customType"
    }
    
    // - known values
    @JvmName("removeReference")
    fun remove(type: GuideType) {
        elements -= type.serializedName
    }
    
    @JvmSynthetic
    operator fun minusAssign(type: GuideType) {
        elements -= type.serializedName
    }
    
    // set
    // - custom values
    /**
     * TODO
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][GuideType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, epubby prepends the `"other."` string to the specified `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * the same for all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     */
    @JvmName("setReferenceResource")
    operator fun set(customType: String, resource: PageResource) {
        this[customType].fold(
            { throw NoSuchElementException("No guide reference element found under the custom type <other.$customType>") },
            { ref ->
                elements.put("other.$customType", ref.copy(href = resource.href))
            }
        )
    }
    
    // - known values
    
    
    // iterator
    override fun iterator(): Iterator<GuideReference> = elements.values.toList().iterator()
    
    // object defaults
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageGuide -> false
        book != other.book -> false
        elements != other.elements -> false
        else -> true
    }
    
    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + elements.hashCode()
        return result
    }
    
    override fun toString(): String = "PackageGuide(book=$book, references=$elements)"
    
    // reference
    /**
     * Implementation of the `reference` element contained inside of the [guide element][PackageGuide].
     *
     * The **required** [type] parameter describes the publication component `this` reference is pointing towards. The
     * value for the `type` property **must** be a [GuideType] constant when applicable. Other types **may** be used
     * when none of the predefined types are applicable; their names **must** begin with the string `"other."`. The
     * value for the `type` property is case-sensitive.
     *
     * @property [parent] The parent [PackageGuide] of `this` reference.
     * @property [type] The `type` of `this` reference.
     * @property [href] The [href][PageResource.href] of the [resource][PageResource] that `this` reference is pointing
     * towards.
     *
     * **NOTE:** This `href` **needs** to point towards a valid [Resource] or a [BookException] will be thrown when
     * the [resource] property is accessed.
     * @property [title] The title that a `Reading System` would use to display `this` reference.
     *
     * The `title` property is *not* required for a `Reference` to be valid.
     */
    data class Reference internal constructor(
        val parent: PackageGuide,
        val type: String,
        val href: HREF,
        val title: Option<String>
    ) {
        /**
         * Returns the [GuideType] tied to the specified [type] of `this` reference.
         *
         * This returns a [Option] as [GuideType.getOrNone] is not guaranteed to always return a value, as `this`
         * reference might be using a custom `type`.
         */
        val guideType: Option<GuideType> = GuideType.getOrNone(type)
        
        /**
         * Returns whether or not `this` reference is using a custom [type].
         */
        val isCustomType: Boolean get() = guideType.isEmpty()
    
        /**
         * Lazily returns the [Resource] instance at the location specified by the [href] property, or throws a
         * [BookException] if no resource is found.
         */
        val resource: Resource by lazy {
            href.toResource(parent.book) orElseThrow {
                BookException.create(parent.book, "Resource located at <${href.href}> does not exist")
            }
        }
    }
}

typealias GuideReference = PackageGuide.Reference

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
        fun getOrNone(type: String): Option<GuideType> =
            Option.fromNullable(values().firstOrNull { it.serializedName == type })
    }
}