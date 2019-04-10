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

package moe.kanon.epubby.resources.opf

import arrow.core.Option
import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubFormat
import moe.kanon.epubby.resources.HREF
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.opf.PackageGuide.Reference
import moe.kanon.epubby.resources.toc.TableOfContents
import moe.kanon.kommons.collections.putAndReturn
import moe.kanon.kommons.lang.normalizedName

/**
 * Implementation of the [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) specification.
 *
 * Within the [package][PackageDocument] there **may** be one `guide element`, containing one or more reference
 * elements. The guide element identifies fundamental structural components of the publication, to enable
 * [Reading Systems](http://www.idpf.org/epub/31/spec/epub-spec.html#gloss-epub-reading-system) to provide convenient
 * access to them.
 *
 * Each [reference][Reference] must have a [href][Reference.href] referring to an [OPS Content Document][PageResource]
 * included in the [manifest][PackageManifest], and which may include a fragment identifier as defined in section 4.1 of
 * [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt). `Reading Systems` may use the bounds of the referenced element to
 * determine the scope of the reference. If a fragment identifier is not used, the scope is considered to be the entire
 * document. This specification does not require `Reading Systems` to mark or otherwise identify the entire scope of a
 * referenced element.
 *
 * **NOTE:** Starting from the [epub 3.0 format][EpubFormat.EPUB_3_0] the `guide` element is considered
 * [deprecated](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-guide-elem), and should be replaced with
 * `landmarks`.
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
    
    // TODO: Remember to remove any 'other.' from custom values when reading the XML file
    
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
     * consistent across all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     */
    @JvmName("getCustomReferenceOrNone")
    operator fun get(customType: String): Option<GuideReference> = Option.fromNullable(elements["other.$customType"])
    
    // - known values
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
     * consistent across all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     */
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
     * This means that if this function is invoked with `(customType = "tn")` the system will *not* store the created
     * `reference` under the key `"tn"`, instead it will store the `reference` under the key `"other.tn"`. This
     * behaviour is consistent across all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     * @param [resource] the [Resource] to inherit the [href][Resource.href] of
     * @param [title] the *(optional)* title attribute
     *
     * @return the newly created `reference` element
     */
    @JvmOverloads
    @JvmName("addCustomReference")
    fun addCustom(customType: String, resource: PageResource, title: String? = null): GuideReference =
        elements.putAndReturn(
            "other.$customType",
            GuideReference(this, customType, resource, title = Option.fromNullable(title))
        )
    
    // - known values
    /**
     * Creates and adds a [reference][Reference] element to `this` guide.
     *
     * The `reference` element is constructed from the specified [type], [resource] and [title].
     *
     * @param [type] the `type` to store the element under
     * @param [resource] the [Resource] to inherit the [href][Resource.href] of
     * @param [title] the *(optional)* title attribute
     *
     * @return the newly created `reference` element
     */
    @JvmOverloads
    @JvmName("addReference")
    fun add(type: GuideType, resource: PageResource, title: String? = type.normalizedName): GuideReference =
        elements.putAndReturn(
            type.serializedName,
            GuideReference(this, type.serializedName, resource, title = Option.fromNullable(title))
        )
    
    // remove
    // - custom values
    /**
     * Removes the [reference][Reference] element stored under the specified [customType].
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][GuideType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, epubby prepends the `"other."` string to the specified `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* remove a `reference` stored
     * under the key `"tn"`, instead it removes a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     */
    @JvmName("removeCustomReference")
    fun removeCustom(customType: String) {
        elements -= "other.$customType"
    }
    
    /**
     * Removes the [reference][Reference] element stored under the specified [customType].
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][GuideType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, epubby prepends the `"other."` string to the specified `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* remove a `reference` stored
     * under the key `"tn"`, instead it removes a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     */
    @JvmSynthetic
    operator fun minusAssign(customType: String) {
        elements -= "other.$customType"
    }
    
    // - known values
    /**
     * Removes the [reference][Reference] element stored under the specified [type].
     *
     * @param [type] the `type` to remove
     */
    @JvmName("removeReference")
    fun remove(type: GuideType) {
        elements -= type.serializedName
    }
    
    /**
     * Removes the [reference][Reference] element stored under the specified [type].
     *
     * @param [type] the `type` to remove
     */
    @JvmSynthetic
    operator fun minusAssign(type: GuideType) {
        elements -= type.serializedName
    }
    
    // set
    // - custom values
    /**
     * Changes the [resource][Reference.resource] and [href][Reference.href] properties of the `reference` element
     * stored under the specified [customType], or throws a  [NoSuchElementException] if no element is found.
     *
     * The `resource` and `href` properties of the found element are set to be that that of the specified [resource].
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][GuideType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, epubby prepends the `"other."` string to the specified `customType`.
     *
     * This means that if this function is invoked with `(customType = "tn")` the system does *not* look for a
     * `reference` stored under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`.
     * This behaviour is consistent across all functions that accept a `customType`.
     *
     * @param [customType] the custom type string
     * @param [resource] the new [PageResource] instance to use for the `reference` element stored under the specified
     * [customType]
     */
    operator fun set(customType: String, resource: PageResource) {
        this["other.$customType"].fold(
            { throw NoSuchElementException("No guide reference element found under the custom type <other.$customType>") },
            { ref ->
                elements.put("other.$customType", ref.copy(resource = resource, href = resource.href))
            }
        )
    }
    
    // - known values
    /**
     * Changes the [resource][Reference.resource] and [href][Reference.href] properties of the `reference` element
     * stored under the specified [type], or throws a  [NoSuchElementException] if no element is found.
     *
     * @param [type] the `type` that the element is stored under
     * @param [resource] the new [PageResource] instance to use for the `reference` element stored under the specified
     * [type]
     */
    operator fun set(type: GuideType, resource: PageResource) {
        this[type.serializedName].fold(
            { throw NoSuchElementException("No guide reference element found under the type <${type.serializedName}>") },
            { ref ->
                elements.put(type.serializedName, ref.copy(resource = resource, href = resource.href))
            }
        )
    }
    
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
    
    override fun toString(): String = "Guide{${elements.values.joinToString()}}"
    
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
     * @property [resource] The [PageResource] that `this` reference is pointing towards.
     * @property [href] The [href][PageResource.href] of the [resource] that `this` reference is pointing towards.
     * @property [title] The title that a `Reading System` would use to display `this` reference.
     *
     * The `title` property is *not* required for a `Reference` to be valid.
     */
    data class Reference internal constructor(
        val parent: PackageGuide,
        val type: String,
        val resource: PageResource,
        val href: HREF = resource.href,
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
        
        override fun toString(): String =
            "Reference[type=\"$type\", href=\"${href.get()}\"${if (title.nonEmpty()) ", title=\"${title.orNull()}\"" else ""}]"
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
        fun getOrNone(type: String): Option<GuideType> =
            Option.fromNullable(values().firstOrNull { it.serializedName == type })
    }
}