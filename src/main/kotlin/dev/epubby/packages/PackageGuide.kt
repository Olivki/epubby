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

package dev.epubby.packages

import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.internal.MarkedAsLegacy
import dev.epubby.internal.ifNotNull
import dev.epubby.resources.PageResource
import moe.kanon.kommons.collections.asUnmodifiableMap
import moe.kanon.kommons.collections.emptyEnumMap
import moe.kanon.kommons.collections.getOrThrow
import org.apache.commons.collections4.map.CaseInsensitiveMap

@MarkedAsLegacy(`in` = EPUB_3_0)
class PackageGuide(override val book: Book) : BookElement {
    @get:JvmSynthetic
    internal val _references: MutableMap<Type, Reference> = emptyEnumMap()

    @get:JvmSynthetic
    internal val _customReferences: MutableMap<String, CustomReference> = CaseInsensitiveMap()

    val references: Map<Type, Reference>
        get() = _references.asUnmodifiableMap()

    val customReferences: Map<String, CustomReference>
        get() = _customReferences.asUnmodifiableMap()

    override val elementName: String
        get() = "PackageGuide"

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
        val ref = Reference(book, type, reference, title)
        _references[type] = ref
        return ref
    }

    /**
     * Removes the [reference][Reference] element stored under the specified [type].
     *
     * @param [type] the `type` to remove
     */
    fun removeReference(type: Type) {
        if (type in _references) {
            _references -= type
        }
    }

    /**
     * Returns the [reference][Reference] stored under the given [type], or throws a [NoSuchElementException] if none
     * is found.
     */
    fun getReference(type: Type): Reference =
        _references.getOrThrow(type) { "No reference found with the given type '$type'" }

    /**
     * Returns the [reference][Reference] stored under the given [type], or `null` if none is found.
     */
    fun getReferenceOrNull(type: Type): Reference? = _references[type]

    /**
     * Adds a new [reference][Reference] instance based on the given [type], [reference] and [title] to this guide.
     *
     * Note that if a `reference` already exists under the given [type], then it will be overridden.
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
     * Note that as guide references are *case-insensitive* the casing of the given [type] does not matter when
     * attempting to return it from [getCustomReference] or removing it via [removeCustomReference].
     *
     * @throws [IllegalArgumentException] if the given [type] matches an already known [type][Type]
     */
    @JvmOverloads
    fun addCustomReference(
        type: String,
        reference: PageResource,
        title: String? = null
    ): CustomReference {
        require(Type.isUnknownType(type)) { "'customType' must not match any officially defined types ($type)" }
        val ref = CustomReference(book, type, reference, title)
        _customReferences[type] = ref
        return ref
    }

    /**
     * Removes the [reference][Reference] element stored under the specified [type].
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
     * Note that as guide references are *case-insensitive* the casing of the given `customType` does not matter,
     * meaning that invoking this function with `customType` as `"deStROyeR"` will remove the same `reference` as if
     * invoking it with `customType` as `"destroyer"` or any other casing variation of the same string.
     */
    fun removeCustomReference(type: String) {
        if (type in _customReferences) {
            _customReferences -= type
        }
    }

    /**
     * Returns the [reference][Reference] stored under the given [type], or throws a [NoSuchElementException] if
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
    fun getCustomReference(type: String): CustomReference =
        _customReferences.getOrThrow(type) { "No reference found with the given custom type 'other.$type'" }

    /**
     * Returns the [reference][Reference] stored under the given [type], or `null` if none is found.
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
    fun getCustomReferenceOrNull(type: String): CustomReference? = _customReferences[type]

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageGuide -> false
        _references != other._references -> false
        _customReferences != other._customReferences -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = _references.hashCode()
        result = 31 * result + _customReferences.hashCode()
        return result
    }

    override fun toString(): String =
        "PackageGuide(references=$_references, customReferences=$_customReferences)"

    class Reference @JvmOverloads constructor(
        override val book: Book,
        val type: Type,
        var reference: PageResource,
        var title: String? = null
    ) : BookElement {
        override val elementName: String
            get() = "PackageGuide.Reference"

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Reference -> false
            type != other.type -> false
            reference != other.reference -> false
            title != other.title -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + reference.hashCode()
            result = 31 * result + (title?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("Reference(")
            append("type=$type")
            append(", reference=$reference")
            title ifNotNull { append(", title='$it'") }
            append(")")
        }
    }

    class CustomReference @JvmOverloads constructor(
        override val book: Book,
        val type: String,
        var reference: PageResource,
        var title: String? = null
    ) : BookElement {
        override val elementName: String
            get() = "PackageGuide.CustomReference"

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is CustomReference -> false
            !type.equals(other.type, ignoreCase = true) -> false
            reference != other.reference -> false
            title != other.title -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = type.toLowerCase().hashCode()
            result = 31 * result + reference.hashCode()
            result = 31 * result + (title?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("Reference(")
            append("customType='$type'")
            append(", reference=$reference")
            title ifNotNull { append(", title='$it'") }
            append(")")
        }
    }

    // TODO: move this up and rename to 'ReferenceType'?
    enum class Type(val type: String) {
        /**
         * A [page][Page] containing the book cover(s), jacket information, etc..
         */
        COVER("cover"),

        /**
         * A [page][Page] possibly containing the title, author, publisher, and other metadata
         */
        TITLE_PAGE("title-page"),

        /**
         * The [page][Page] representing the [table of contents][TableOfContents].
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
            private val TYPES: Map<String, Type> = values().associateByTo(CaseInsensitiveMap(), Type::type)

            /**
             * Returns `true` if the given [type] represents an officially known type, otherwise `false`.
             */
            @JvmStatic
            fun isKnownType(type: String): Boolean = type in TYPES

            /**
             * Returns `true` if the given [type] does not represent an officially known type, otherwise `false`.
             */
            @JvmStatic
            fun isUnknownType(type: String): Boolean = type !in TYPES

            /**
             * Returns `true` if the given [type] represents an officially known type, otherwise `false`.
             */
            @JvmSynthetic
            operator fun contains(type: String): Boolean = type in TYPES

            /**
             * Returns the [Type] that has a [type][Type.type] that matches the given [type], or throws a
             * [NoSuchElementException] if none is found.
             */
            @JvmStatic
            fun fromType(type: String): Type = TYPES.getOrThrow(type) { "'$type' is not a known type." }

            /**
             * Returns the [Type] that has a [type][Type.type] that matches the given [type], or `null` if none is
             * found.
             */
            @JvmStatic
            fun fromTypeOrNull(type: String): Type? = TYPES[type]
        }
    }
}