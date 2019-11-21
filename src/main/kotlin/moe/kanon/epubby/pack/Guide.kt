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

package moe.kanon.epubby.pack

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.pack.Guide.Reference
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.epubby.utils.Namespaces
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.getValueOrThrow
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path

// TODO: Clean up the class documentation
// TODO: Store the values in a case insensitive map? Or just hit the custom types up with 'toLowerCase' ?
// TODO: Make two separate classes for Reference and CustomReference? Maybe add a CustomType too?
/**
 * Represents the [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) element.
 *
 * Within the [package][Package] there **may** be one `guide element`, containing one or more reference
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
 * **NOTE:** Starting from [EPUB 3.0][Book.Format.EPUB_3_0] the `guide` element is considered to be a
 * [legacy](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-guide-elem) feature, and should be replaced
 * with `landmarks`.
 *
 * @property [book] The [Book] instance that this `guide` is bound to.
 */
class Guide private constructor(val book: Book, private val refs: MutableMap<String, Reference>) :
    Iterable<Reference> {
    val references: ImmutableMap<Type, Reference>
        get() = refs
            .filterValues { !it.isCustomType }
            .mapKeys { (_, ref) -> ref.guideType!! }
            .toPersistentHashMap()

    val customReferences: ImmutableMap<String, Reference>
        get() = refs.filterValues { it.isCustomType }.toPersistentHashMap()

    // -- NORMAL REFERENCES -- \\
    /**
     * Adds a new [reference][Reference] instance based on the given [type], [href] and [title] to this guide.
     *
     * Note that if a `reference` already exists under the given [type], then it will be overridden.
     *
     * @param [type] the `type` to store the element under
     * @param [href] the [Resource] to inherit the [href][Resource.href] of
     * @param [title] the *(optional)* title
     *
     * @return the newly created `reference` element
     */
    @JvmOverloads
    fun addReference(type: Type, href: String, title: String? = null): Reference {
        val ref = Reference(type.serializedName, href, title)
        refs[type.serializedName] = ref
        logger.debug { "Added the reference <$ref> to this guide <$this>" }
        return ref
    }

    /**
     * Removes the [reference][Reference] element stored under the specified [type].
     *
     * @param [type] the `type` to remove
     */
    fun removeReference(type: Type) {
        if (type.serializedName in refs) {
            val ref = refs[type.serializedName]
            refs -= type.serializedName
            logger.debug { "Removed the reference <$ref> from this guide <$this>" }
        }
    }

    /**
     * Returns the [reference][Reference] stored under the given [type], or throws a [NoSuchElementException] if none
     * is found.
     */
    fun getReference(type: Type): Reference =
        refs.getValueOrThrow(type.serializedName) { "No reference found with the given type '$type'" }

    /**
     * Returns the [reference][Reference] stored under the given [type], or `null` if none is found.
     */
    fun getReferenceOrNull(type: Type): Reference? = refs[type.serializedName]

    /**
     * Returns `true` if this guide has a reference with the given [type], `false` otherwise.
     */
    fun hasType(type: Type): Boolean = type.serializedName in refs

    // -- CUSTOM REFERENCES -- \\
    /**
     * Adds a new [reference][Reference] instance based on the given [customType], [href] and [title] to this guide.
     *
     * Note that if a `reference` already exists under the given [customType], then it will be overridden.
     *
     * The [OPF][Package] specification states that;
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
     * @param [customType] the custom type string
     * @param [href] the [Resource] to inherit the [href][Resource.href] of
     * @param [title] the *(optional)* title attribute
     *
     * @return the newly created `reference` element
     */
    @JvmOverloads
    fun addCustomReference(customType: String, href: String, title: String? = null): Reference {
        val type = "other.$customType"
        val ref = Reference("other.$customType", href, title)
        refs[type] = ref
        logger.debug { "Added the custom reference <$ref> to this guide <$this>" }
        return ref
    }

    /**
     * Removes the [reference][Reference] element stored under the specified [customType].
     *
     * The [OPF][Package] specification states that;
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
     * @param [customType] the custom type string
     */
    fun removeCustomReference(customType: String) {
        val type = "other.$customType"
        if (type in refs) {
            val ref = refs[type]
            refs -= type
            logger.debug { "Removed the custom reference <$ref> from this guide <$this>" }
        }
    }

    /**
     * Returns the [reference][Reference] stored under the given [customType], or throws a [NoSuchElementException] if
     * none is found.
     *
     * The [OPF][Package] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     */
    fun getCustomReference(customType: String): Reference =
        refs.getValueOrThrow(customType) { "No reference found with the given custom type 'other.$customType'" }

    /**
     * Returns the [reference][Reference] stored under the given [customType], or `null` if none is found.
     *
     * The [OPF][Package] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     */
    fun getCustomReferenceOrNull(customType: String): Reference? = refs["other.$customType"]

    /**
     * Returns `true` if this guide has a reference with the given [customType], `false` otherwise.
     *
     * The [OPF][Package] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     */
    fun hasCustomType(customType: String): Boolean = "other.$customType" in refs

    override fun iterator(): Iterator<Reference> = refs.values.iterator().asUnmodifiable()

    override fun toString(): String = "Guide[for='${book.title}']"

    // TODO: Turn the 'href' property into some sort of a data structure?
    data class Reference internal constructor(val type: String, var href: String, var title: String? = null) {
        /**
         * Returns the [Type] tied to the specified [type] of this `reference`, or `null` if the [type] of `this` ref
         * is custom.
         */
        val guideType: Type? = Type.getOrNull(type)

        /**
         * Returns `true` if the [type] of `this` ref is custom.
         */
        val isCustomType: Boolean get() = guideType == null

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.OPF): Element = Element("reference", namespace).apply {
            setAttribute("type", type)
            setAttribute("href", href)
            title?.also { setAttribute("title", it) }
        }

        override fun toString(): String = when (title) {
            null -> "Reference(type='$type', href='$href', isCustomType=$isCustomType)"
            else -> "Reference(type='$type', href='$href', title='$title', isCustomType=$isCustomType)"
        }
    }

    /**
     * Implementation of the "list of `type` values" declared in the
     * [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) specification
     *
     * @property [serializedName] The name used in the actual serialized form of `this` type.
     */
    enum class Type(val serializedName: String) {
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
            /**
             * Returns the first [Type] that has a [serializedName] that matches the specified [type], or `null` if
             * none is found.
             *
             * @param [type] the type to match all `guide-types` against
             */
            @JvmStatic
            fun getOrNull(type: String): Type? = values().firstOrNull { it.serializedName == type }
        }
    }

    companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, documentFile: Path): Guide = with(element) {
            val refs = getChildren("reference", namespace)
                .asSequence()
                .map { createReference(it, book.file, documentFile) }
                .onEach { logger.debug { "Constructed reference instance <$it> from file '${book.file}'" } }
                .associateByTo(hashMapOf()) { it.type }
            return Guide(book, refs)
        }

        private fun createReference(element: Element, container: Path, current: Path): Reference {
            val type = element.attr("type", container, current).let {
                when {
                    Type.getOrNull(it) == null && !(it.startsWith("other.", true)) -> {
                        logger.warn { "Reference type '$it' is not a known type and is missing the 'other.' prefix required for custom types. It will be stored as 'other.$it'" }
                        "other.$it"
                    }
                    else -> it
                }
            }
            val href = element.attr("href", container, current)
            val title = element.getAttributeValue("title")
            return Reference(type, href, title)
        }
    }
}