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
import moe.kanon.epubby.resources.PageResource
import moe.kanon.kommons.collections.asUnmodifiableMap
import moe.kanon.kommons.collections.emptyEnumMap
import moe.kanon.kommons.collections.getOrThrow
import moe.kanon.kommons.requireThat
import org.apache.commons.collections4.map.CaseInsensitiveMap

class PackageGuide(val book: Book) {
    @get:JvmSynthetic
    internal val _references: MutableMap<Type, Reference> = emptyEnumMap()

    @get:JvmSynthetic
    internal val _customReferences: MutableMap<String, CustomReference> = CaseInsensitiveMap()

    val references: Map<Type, Reference> = _references.asUnmodifiableMap()

    val customReferences: Map<String, CustomReference> = _customReferences.asUnmodifiableMap()

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
     * @throws [IllegalArgumentException] if the given [customType] matches an already known [type][Type]
     */
    @JvmOverloads
    fun addCustomReference(customType: String, reference: PageResource, title: String? = null): CustomReference {
        requireThat(Type.isUnknownType(customType)) { "Expected custom-type '$customType' to be original, but it matches an officially defined type." }
        val ref = CustomReference(book, customType, reference, title)
        _customReferences[customType] = ref
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
     * Note that as guide references are *case-insensitive* the casing of the given `customType` does not matter,
     * meaning that invoking this function with `customType` as `"deStROyeR"` will remove the same `reference` as if
     * invoking it with `customType` as `"destroyer"` or any other casing variation of the same string.
     */
    fun removeCustomReference(customType: String) {
        if (customType in _customReferences) {
            _customReferences -= customType
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
    fun getCustomReference(customType: String): CustomReference =
        _customReferences.getOrThrow(customType) { "No reference found with the given custom type 'other.$customType'" }

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
    fun getCustomReferenceOrNull(customType: String): CustomReference? = _customReferences[customType]

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
        "PackageGuide(book=$book, references=$_references, customReferences=$_customReferences)"

    class Reference internal constructor(
        val book: Book,
        val type: Type,
        var reference: PageResource,
        var title: String? = null
    ) {
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
            if (title != null) append(", title='$reference'")
            append(")")
        }
    }

    class CustomReference internal constructor(
        val book: Book,
        val customType: String,
        var reference: PageResource,
        var title: String? = null
    ) {
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is CustomReference -> false
            customType.toLowerCase() != other.customType.toLowerCase() -> false
            reference != other.reference -> false
            title != other.title -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = customType.toLowerCase().hashCode()
            result = 31 * result + reference.hashCode()
            result = 31 * result + (title?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("Reference(")
            append("customType='$customType'")
            append(", reference=$reference")
            if (title != null) append(", title='$reference'")
            append(")")
        }
    }

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
            private val typeToInstance: Map<String, Type> by lazy {
                values().associateByTo(CaseInsensitiveMap(), Type::type)
            }

            /**
             * Returns `true` if the given [type] represents an officially known type, otherwise `false`.
             */
            @JvmStatic
            fun isKnownType(type: String): Boolean = type in typeToInstance

            /**
             * Returns `true` if the given [type] does not represent an officially known type, otherwise `false`.
             */
            @JvmStatic
            fun isUnknownType(type: String): Boolean = type !in typeToInstance

            /**
             * Returns the [Type] that has a [type][Type.type] that matches the given [type], or throws a
             * [NoSuchElementException] if none is found.
             */
            @JvmStatic
            fun byType(type: String): Type = typeToInstance.getOrThrow(type) { "'$type' is not a known type." }

            /**
             * Returns the [Type] that has a [type][Type.type] that matches the given [type], or `null` if none is
             * found.
             */
            @JvmStatic
            fun byTypeOrNull(type: String): Type? = typeToInstance[type]
        }
    }
}