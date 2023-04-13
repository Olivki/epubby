/*
 * Copyright 2019-2023 Oliver Berg
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

package dev.epubby.content.guide

import dev.epubby.Epub
import dev.epubby.Epub3LegacyFeature

// TODO: update [Page] references
@Epub3LegacyFeature
public enum class GuideReferenceType(public val type: String) {
    /**
     * A [page][Page] containing the epub cover(s), jacket information, etc..
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
     * A back-of-epub style index [page][Page].
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
     * A [page][Page] containing the colophon of the [epub][Epub].
     */
    COLOPHON("colophon"),

    /**
     * A [page][Page] detailing the copyright that the [epub][Epub] is under.
     */
    COPYRIGHT_PAGE("copyright-page"),

    /**
     * A [page][Page] describing who the [epub][Epub] is dedicated towards.
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
     * A [page][Page] containing a list of all the illustrations used throughout the [epub][Epub].
     */
    LIST_OF_ILLUSTRATIONS("loi"),

    /**
     * A [page][Page] containing a list of all the tables used throughout the [epub][Epub].
     */
    LIST_OF_TABLES("lot"),

    /**
     * A [page][Page] containing some sort of notes; authors notes, editors notes, translation notes, etc..
     */
    NOTES("notes"),

    /**
     * A [page][Page] containing a preface to the [epub][Epub].
     */
    PREFACE("preface"),

    /**
     * First "real" [page][Page] of content. *(e.g. "Chapter 1")*
     */
    TEXT("text");
}