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

package moe.kanon.epubby.resources.root

import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.EpubLegacy
import moe.kanon.kommons.func.Option
import org.jdom2.Element

@EpubLegacy("3.0")
class PackageGuide(val book: Book) : ElementSerializer {
    override fun toElement(): Element {
        TODO("not implemented")
    }

    /**
     * Implementation of the "list of `type` values" declared in the
     * [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) specification
     *
     * @property [serializedName] The name used in the actual serialized form of `this` type.
     */
    enum class Type(val serializedName: String) {
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

        override fun toString(): String = serializedName

        companion object {
            /**
             * Returns the first [Type] that has a [serializedName] that matches the specified [type].
             *
             * @param [type] the type to match all `guide-types` against
             */
            @JvmStatic fun getOrNone(type: String): Option<Type> =
                Option(values().firstOrNull { it.serializedName == type })
        }
    }

}