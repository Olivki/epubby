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

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.internal.getBookPathFromHref
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.packages.Guide.Reference
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.epubby.utils.attr
import moe.kanon.kommons.checkThat
import moe.kanon.kommons.collections.getOrThrow
import moe.kanon.kommons.requireThat
import org.apache.commons.collections4.map.CaseInsensitiveMap
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path

/**
 * Represents the [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) element.
 *
 * Within the [package][PackageDocument] there **may** be one `guide element`, containing one or more reference
 * elements. The guide element identifies fundamental structural components of the publication, to enable
 * [Reading Systems](http://www.idpf.org/epub/31/spec/epub-spec.html#gloss-epub-reading-system) to provide convenient
 * access to them.
 *
 * Each [reference][Reference] must have a [href][Reference.reference] referring to an [OPS Content Document][PageResource]
 * included in the [manifest][PackageManifest], and which may include a fragment identifier as defined in section 4.1 of
 * [RFC 2396](http://www.ietf.org/rfc/rfc2396.txt). `Reading Systems` may use the bounds of the referenced element to
 * determine the scope of the reference. If a fragment identifier is not used, the scope is considered to be the entire
 * document. This specification does not require `Reading Systems` to mark or otherwise identify the entire scope of a
 * referenced element.
 *
 * **NOTE:** Starting from [EPUB 3.0][Book.Format.EPUB_3_0] the `guide` element is considered to be a
 * [legacy](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-guide-elem) feature, and should be replaced
 * with `landmarks`.
 *
 * @property [book] The [Book] instance that this `guide` is bound to.
 */
class Guide private constructor(val book: Book, private val _references: CaseInsensitiveMap<String, Reference>) {
    // TODO: Clean up the class documentation
    // TODO: Make two separate classes for Reference and CustomReference? Maybe add a CustomType too?

    /**
     * Returns a map of all the [references][Reference] of `this` guide whose [type][Reference.type] is a known
     * [type][Type].
     *
     * There is no guarantee that the returned map will contain any entries.
     *
     * @see [customReferences]
     * @see [allReferences]
     */
    val references: ImmutableMap<Type, Reference>
        get() = _references
            .filterValues { !it.isCustomType }
            .mapKeys { (_, ref) -> ref.guideType!! }
            .toPersistentHashMap()

    /**
     * Returns a map of all the [references][Reference] of `this` guide whose [type][Reference.type] is custom.
     *
     * Note that all custom-types will be prefixed with `"other."`.
     *
     * There is no guarantee that the returned map will contain any entries.
     *
     * @see [references]
     * @see [allReferences]
     */
    val customReferences: ImmutableMap<String, Reference>
        get() = _references.filterValues { it.isCustomType }.toPersistentHashMap()

    /**
     * Returns a map of all the [references][Reference] of `this` guide.
     *
     * There is no guarantee that the returned map will contain any entries.
     *
     * @see [references]
     * @see [customReferences]
     */
    val allReferences: ImmutableMap<String, Reference>
        get() = _references.toPersistentHashMap()

    // -- NORMAL REFERENCES -- \\
    /**
     * Adds a new [reference][Reference] instance based on the given [type], [reference] and [title] to this guide.
     *
     * Note that if a `reference` already exists under the given [type], then it will be overridden.
     *
     * @param [type] the `type` to store the element under
     * @param [reference] the [Resource] to inherit the [href][Resource.href] of
     * @param [title] the *(optional)* title
     *
     * @return the newly created `reference` element
     */
    @JvmOverloads
    fun addReference(type: Type, reference: PageResource, title: String? = null): Reference {
        val ref = Reference(type.attributeName, reference, title)
        _references[type.attributeName] = ref
        logger.debug { "Added the reference <$ref> to this guide <$this>" }
        return ref
    }

    /**
     * Removes the [reference][Reference] element stored under the specified [type].
     *
     * @param [type] the `type` to remove
     */
    fun removeReference(type: Type) {
        if (type.attributeName in _references) {
            val ref = _references[type.attributeName]
            _references -= type.attributeName
            logger.debug { "Removed the reference <$ref> from this guide <$this>" }
        }
    }

    /**
     * Returns the [reference][Reference] stored under the given [type], or throws a [NoSuchElementException] if none
     * is found.
     */
    fun getReference(type: Type): Reference =
        _references.getOrThrow(type.attributeName) { "No reference found with the given type '$type'" }

    /**
     * Returns the [reference][Reference] stored under the given [type], or `null` if none is found.
     */
    fun getReferenceOrNull(type: Type): Reference? = _references[type.attributeName]

    /**
     * Returns `true` if this guide has a reference with the given [type], `false` otherwise.
     */
    fun hasType(type: Type): Boolean = type.attributeName in _references

    // convenience
    // TODO: Documentation
    @JvmOverloads
    fun setCoverPage(page: PageResource, title: String? = "Cover Image"): Reference =
        addReference(Type.COVER, page, title)

    @JvmOverloads
    fun setTitlePage(page: PageResource, title: String? = "Title Page"): Reference =
        addReference(Type.TITLE_PAGE, page, title)

    @JvmOverloads
    fun setTableOfContentsPage(page: PageResource, title: String? = "Table of Contents"): Reference =
        addReference(Type.TABLE_OF_CONTENTS, page, title)

    @JvmOverloads
    fun setIndexPage(page: PageResource, title: String? = "Index"): Reference = addReference(Type.INDEX, page, title)

    @JvmOverloads
    fun setGlossaryPage(page: PageResource, title: String? = "Glossary"): Reference =
        addReference(Type.GLOSSARY, page, title)

    @JvmOverloads
    fun setBibliographyPage(page: PageResource, title: String? = "Bibliography"): Reference =
        addReference(Type.BIBLIOGRAPHY, page, title)

    @JvmOverloads
    fun setColophonPage(page: PageResource, title: String? = "Colophon"): Reference =
        addReference(Type.COLOPHON, page, title)

    @JvmOverloads
    fun setCopyrightPage(page: PageResource, title: String? = "Copyright Page"): Reference =
        addReference(Type.COPYRIGHT_PAGE, page, title)

    @JvmOverloads
    fun setDedicationPage(page: PageResource, title: String? = "Dedication"): Reference =
        addReference(Type.DEDICATION, page, title)

    @JvmOverloads
    fun setEpigraphPage(page: PageResource, title: String? = "Epigraph"): Reference =
        addReference(Type.EPIGRAPH, page, title)

    @JvmOverloads
    fun setForewordPage(page: PageResource, title: String? = "Foreword"): Reference =
        addReference(Type.FOREWORD, page, title)

    @JvmOverloads
    fun setListOfIllustrationsPage(page: PageResource, title: String? = "List of Illustrations"): Reference =
        addReference(Type.LIST_OF_ILLUSTRATIONS, page, title)

    @JvmOverloads
    fun setListOfTablesPage(page: PageResource, title: String? = "List of Tables"): Reference =
        addReference(Type.LIST_OF_TABLES, page, title)

    @JvmOverloads
    fun setNotesPage(page: PageResource, title: String? = "Notes"): Reference = addReference(Type.NOTES, page, title)

    @JvmOverloads
    fun setPrefacePage(page: PageResource, title: String? = "Preface"): Reference =
        addReference(Type.PREFACE, page, title)

    @JvmOverloads
    fun setTextPage(page: PageResource, title: String? = "Begin Reading"): Reference =
        addReference(Type.TEXT, page, title)

    // -- CUSTOM REFERENCES -- \\
    /**
     * Adds a new [reference][Reference] instance based on the given [customType], [reference] and [title] to this guide.
     *
     * Note that if a `reference` already exists under the given [customType], then it will be overridden.
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `(customType = "tn")` the system will *not* store the created
     * `reference` under the key `"tn"`, instead it will store the `reference` under the key `"other.tn"`. This
     * behaviour is consistent across all functions that accept a `customType`.
     *
     * Note that as guide references are *case-insensitive* the casing of the given [customType] does not matter when
     * attempting to return it from [getCustomReference] or removing it via [removeCustomReference].
     *
     * @param [customType] the custom type string
     * @param [reference] the [Resource] to inherit the [href][Resource.href] of TODO: This part
     * @param [title] the *(optional)* title attribute
     *
     * @throws [IllegalArgumentException] if the given [customType] matches an already known [type][Type]
     *
     * @return the newly created `reference` instance
     */
    @JvmOverloads
    fun addCustomReference(customType: String, reference: PageResource, title: String? = null): Reference {
        // TODO: Might be a bit extreme?
        requireThat(!(Type.isKnownType(customType))) { "expected custom-type '$customType' to be original, but it matches an officially defined type" }
        val type = "other.$customType"
        val ref = Reference("other.$customType", reference, title)
        _references[type] = ref
        logger.debug { "Added the custom reference <$ref> to this guide <$this>" }
        return ref
    }

    /**
     * Removes the [reference][Reference] element stored under the specified [customType].
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* remove a `reference` stored
     * under the key `"tn"`, instead it removes a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     *
     * Note that as guide references are *case-insensitive* the casing of the given [customType] does not matter,
     * meaning that invoking this function with `customType` as `"deStROyeR"` will remove the same `reference` as if
     * invoking it with `customType` as `"destroyer"` or any other casing variation of the same string.
     *
     * @param [customType] the custom type string
     */
    fun removeCustomReference(customType: String) {
        val type = "other.$customType"
        if (type in _references) {
            val ref = _references[type]
            _references -= type
            logger.debug { "Removed the custom reference <$ref> from this guide <$this>" }
        }
    }

    /**
     * Returns the [reference][Reference] stored under the given [customType], or throws a [NoSuchElementException] if
     * none is found.
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     *
     * Note that as guide references are *case-insensitive* the casing given to this function does not matter, meaning
     * that invoking this function with `"deStROyeR"` will return the same result as if invoking it with `"destroyer"`
     * or any other casing variation of the same string.
     */
    fun getCustomReference(customType: String): Reference =
        _references.getOrThrow(customType) { "No reference found with the given custom type 'other.$customType'" }

    /**
     * Returns the [reference][Reference] stored under the given [customType], or `null` if none is found.
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     *
     * Note that as guide references are *case-insensitive* the casing given to this function does not matter, meaning
     * that invoking this function with `"deStROyeR"` will return the same result as if invoking it with `"destroyer"`
     * or any other casing variation of the same string.
     */
    fun getCustomReferenceOrNull(customType: String): Reference? = _references["other.$customType"]

    /**
     * Returns `true` if this guide has a reference with the given [customType], `false` otherwise.
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     *
     * Note that as guide references are *case-insensitive* the casing given to this function does not matter, meaning
     * that invoking this function with `"deStROyeR"` will return the same result as if invoking it with `"destroyer"`
     * or any other casing variation of the same string.
     */
    fun hasCustomType(customType: String): Boolean = "other.$customType" in _references

    override fun toString(): String = "Guide(references=$_references)"

    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("guide", namespace).apply {
        for ((_, ref) in _references) {
            addContent(ref.toElement())
        }
    }

    /**
     * Implementation of the `reference` element contained inside of the [guide][Guide] of the [book].
     *
     * The **required** [type] parameter describes the publication component `this` reference is pointing towards. The
     * value for the `type` property **must** be a [Type] constant when applicable. Other types **may** be used
     * when none of the predefined types are applicable; their names **must** begin with the string `"other."`. The
     * value for the `type` property is case-sensitive.
     *
     * @property [type] The `type` of `this` reference.
     * @property [reference] The [href][PageResource.file] of the [resource] that `this` reference is pointing towards.
     * @property [title] The title that a `Reading System` would use to display `this` reference.
     *
     * The `title` property is *not* required for a reference to be valid.
     */
    class Reference internal constructor(
        val type: String,
        var reference: PageResource,
        var title: String? = null
    ) {
        /**
         * Returns the [Type] tied to the specified [type] of this `reference`, or `null` if the [type] of `this` ref
         * is custom.
         */
        val guideType: Type? = Type.getOrNull(type)

        /**
         * Returns `true` if the [type] of `this` ref is custom.
         */
        val isCustomType: Boolean get() = guideType == null

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Reference -> false
            type != other.type -> false
            reference != other.reference -> false
            title != other.title -> false
            guideType != other.guideType -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + reference.hashCode()
            result = 31 * result + (title?.hashCode() ?: 0)
            result = 31 * result + (guideType?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("Reference(type='$type'")
            title?.also { append(", title='$it'") }
            append(", reference=$reference, isCustomType=$isCustomType)")
        }

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("reference", namespace).apply {
            setAttribute("type", type)
            setAttribute("href", reference.relativeHref.substringAfter("../"))
            title?.also { setAttribute("title", it) }
        }
    }

    /**
     * Represents the list of `type` values declared in the
     * [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) specification
     *
     * @property [attributeName] The name used in the actual serialized form of `this` type.
     */
    enum class Type(val attributeName: String) {
        /**
         * A [page][Page] containing the book cover(s), jacket information, etc..
         */
        COVER("cover"),
        /**
         * A [page][Page] possibly containing the title, author, publisher, and other metadata
         */
        TITLE_PAGE("title-page"),
        /**
         * The [table of contents][TableOfContents] [page][Page].
         */
        TABLE_OF_CONTENTS("toc"),
        /**
         * A back-of-book style index [page][Page].
         */
        INDEX("index"),
        /**
         * A glossary [page][Page].
         */
        GLOSSARY("glossary"),
        /**
         * A [page][Page] containing various acknowledgements.
         */
        ACKNOWLEDGEMENTS("acknowledgements"),
        /**
         * A [page][Page] containing the bibliography of the author.
         */
        BIBLIOGRAPHY("bibliography"),
        /**
         * A [page][Page] containing the colophon of the [book][Book].
         */
        COLOPHON("colophon"),
        /**
         * A [page][Page] detailing the copyright that the [book][Book] is under.
         */
        COPYRIGHT_PAGE("copyright-page"),
        /**
         * A [page][Page] describing who the [book][Book] is dedicated towards.
         */
        DEDICATION("dedication"),
        /**
         * A [page][Page] containing an epigraph.
         */
        EPIGRAPH("epigraph"),
        /**
         * A [page][Page] containing a foreword from the author, translator, editor, etc..
         */
        FOREWORD("foreword"),
        /**
         * A [page][Page] containing a list of all the illustrations used throughout the [book][Book].
         */
        LIST_OF_ILLUSTRATIONS("loi"),
        /**
         * A [page][Page] containing a list of all the tables used throughout the [book][Book].
         */
        LIST_OF_TABLES("lot"),
        /**
         * A [page][Page] containing some sort of notes; authors notes, editors notes, translation notes, etc..
         */
        NOTES("notes"),
        /**
         * A [page][Page] containing a preface to the [book][Book].
         */
        PREFACE("preface"),
        /**
         * First "real" [page][Page] of content. *(e.g. "Chapter 1")*
         */
        TEXT("text");

        companion object {
            // TODO: Make a setting for automatically replacing some commonly found faulty names with their actual
            //       representations?

            // these functions all use equalsIgnoreCase because guide references are case-insensitive according to the
            // EPUB specification.

            /**
             * Returns `true` if the given [type] represents an officially known type, otherwise `false`.
             */
            @JvmStatic
            fun isKnownType(type: String): Boolean = values().any { it.attributeName.equals(type, ignoreCase = true) }

            /**
             * Returns the first [Type] that has a [attributeName] that matches the specified [type], or `null` if
             * none is found.
             *
             * @param [type] the type to match all `guide-types` against
             */
            @JvmStatic
            fun getOrNull(type: String): Type? =
                values().firstOrNull { it.attributeName.equals(type, ignoreCase = true) }
        }
    }

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, file: Path): Guide = with(element) {
            val refs = getChildren("reference", namespace)
                .asSequence()
                .map { createReference(book, it, book.file, file) }
                .associateByTo(CaseInsensitiveMap()) { it.type }
            return Guide(book, refs).also {
                logger.trace { "Constructed guide instance <$it> from file '$file'" }
            }
        }

        private fun createReference(book: Book, element: Element, epub: Path, container: Path): Reference {
            val type = element.attr("type", epub, container).let {
                when {
                    !(Type.isKnownType(it)) && !(it.startsWith("other.", true)) -> {
                        logger.warn { "Reference type '$it' is not a known type and is missing the 'other.' prefix required for custom types. It will be stored as 'other.$it'" }
                        "other.$it"
                    }
                    else -> it
                }
            }
            val href = element.attr("href", epub, container)
            val title = element.getAttributeValue("title")
            val hrefFile = getBookPathFromHref(book, href, container)
            val reference = book.resources.getResourceByFileOrNull(hrefFile) ?: malformed(
                book.file,
                container,
                "'href' attribute points to a non-existent resource file: '$hrefFile'"
            )
            checkThat(reference is PageResource) { "resource returned by the hrefFile <$hrefFile> should point towards a page-resource" }
            return Reference(type, reference, title).also {
                logger.trace { "Constructed guide reference instance <$it>" }
            }
        }
    }
}