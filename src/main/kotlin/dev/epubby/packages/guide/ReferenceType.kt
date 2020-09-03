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

package dev.epubby.packages.guide

import dev.epubby.Book
import dev.epubby.page.Page
import moe.kanon.kommons.reflection.KServiceLoader
import moe.kanon.kommons.reflection.loadServices
import org.apache.commons.collections4.map.CaseInsensitiveMap

enum class ReferenceType(val type: String) {
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
        private val TYPES: Map<String, ReferenceType> =
            values().associateByTo(CaseInsensitiveMap(), ReferenceType::type)

        private val CACHED_CORRECTIONS: MutableMap<String, ReferenceType> = hashMapOf("copyright" to COPYRIGHT_PAGE)

        private val ERROR_CORRECTORS: KServiceLoader<ReferenceTypeCorrector> by lazy { loadServices() }

        // returns either the official type, or a corrected one
        private fun getTypeOrNull(type: String): ReferenceType? = when (val realType = type.toLowerCase()) {
            in TYPES -> TYPES.getValue(realType)
            in CACHED_CORRECTIONS -> CACHED_CORRECTIONS.getValue(realType)
            else -> ERROR_CORRECTORS.asSequence()
                .mapNotNull { it.getEquivalent(realType) }
                .firstOrNull()
                ?.also { CACHED_CORRECTIONS.putIfAbsent(realType, it) }
        }

        // TODO: document that these functions check for corrections too

        /**
         * Returns `true` if the given [type] represents an officially known type, otherwise `false`.
         */
        @JvmName("isKnownType")
        operator fun contains(type: String): Boolean =
            type.toLowerCase() in TYPES || type.toLowerCase() in CACHED_CORRECTIONS

        /**
         * Returns the [ReferenceType] that has a [type][ReferenceType.type] that matches the given [type], or throws a
         * [NoSuchElementException] if none is found.
         */
        @JvmStatic
        fun fromType(type: String): ReferenceType =
            fromTypeOrNull(type) ?: throw NoSuchElementException("'$type' is not a known reference type.")

        /**
         * Returns the [ReferenceType] that has a [type][ReferenceType.type] that matches the given [type], or `null`
         * if none is found.
         */
        @JvmStatic
        fun fromTypeOrNull(type: String): ReferenceType? = getTypeOrNull(type)
    }
}