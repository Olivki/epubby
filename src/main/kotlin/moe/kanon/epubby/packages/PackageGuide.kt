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

import org.apache.commons.collections4.map.CaseInsensitiveMap

class PackageGuide {
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
            private val nameToType: Map<String, Type> by lazy {
                values().associateByTo(CaseInsensitiveMap(), Type::attributeName)
            }

            // these functions all use equalsIgnoreCase because guide references are case-insensitive according to the
            // EPUB specification.

            /**
             * Returns `true` if the given [type] represents an officially known type, otherwise `false`.
             */
            @JvmStatic
            fun isKnownType(type: String): Boolean = type in nameToType

            /**
             * Returns the first [Type] that has a [attributeName] that matches the specified [type], or `null` if
             * none is found.
             *
             * @param [type] the type to match all `guide-types` against
             */
            @JvmStatic
            fun getOrNull(type: String): Type? = nameToType[type]
        }
    }
}