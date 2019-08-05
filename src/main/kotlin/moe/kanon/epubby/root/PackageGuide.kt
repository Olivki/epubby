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

package moe.kanon.epubby.root

import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.EpubLegacy
import moe.kanon.epubby.logger
import moe.kanon.epubby.raiseMalformedError
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.root.PackageGuide.Reference
import moe.kanon.epubby.utils.getAttributeValueOrNone
import moe.kanon.epubby.utils.stringify
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.getValueOrNone
import moe.kanon.kommons.func.isEmpty
import moe.kanon.kommons.lang.normalizedName
import org.jdom2.Element
import org.jdom2.output.Format
import java.nio.file.Path

/**
 * Represents the [guide](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6) element.
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
 * **NOTE:** Starting from [EPUB 3.0][Book.Format.EPUB_3_0] the `guide` element is considered to be a
 * [legacy](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-guide-elem) feature, and should be replaced
 * with `landmarks`.
 *
 * @property [book] The [Book] instance that this `guide` is bound to.
 */
@EpubLegacy(Book.Format.EPUB_3_0)
class PackageGuide private constructor(val book: Book, private val references: MutableMap<String, Reference>) :
    ElementSerializer, Iterable<Reference> {
    companion object {
        internal fun parse(book: Book, packageDocument: Path, element: Element): PackageGuide = with(element) {
            fun malformed(reason: String, cause: Throwable? = null): Nothing =
                raiseMalformedError(book.originFile, packageDocument, reason, cause)

            val references = getChildren("reference", namespace)
                .asSequence()
                .map { createReference(it, ::malformed) }
                .associateByTo(LinkedHashMap()) { it.type }

            return PackageGuide(book, references)
        }

        private fun createReference(element: Element, malformed: (String) -> Nothing): Reference {
            val textual = element.stringify(Format.getCompactFormat())
            val type = (element.getAttributeValue("type")
                ?: malformed("'reference' element is missing required 'type' attribute; [$textual]")).let {
                return@let if (Type.getOrNone(it).isEmpty && !(it.startsWith("other.", ignoreCase = true))) {
                    // we want to log the occurrence in case the user might get confused as to why a value
                    // completely changed
                    logger.warn { "Encountered unknown guide reference type <$it> that is not marked with 'other.'" }
                    "other.$it"
                } else it
            }
            val href = element.getAttributeValue("href")
                ?: malformed("'reference' element is missing required 'href' attribute; [$textual]")
            val title = element.getAttributeValueOrNone("title")
            return Reference(type, href, title)
        }
    }

    // might need to use something like a case insensitive map for storing this stuff as the value of the type attribute
    // is case-sensitive

    // TODO: Remember to remove any 'other.' from custom values when reading the XML file
    // -- NORMAL REFERENCES -- \\
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
    @JvmOverloads fun addReference(type: Type, href: String, title: String? = type.normalizedName): Reference =
        Reference(type.serializedName, href, Option(title)).also { references[it.type] = it }

    /**
     * Removes the [reference][Reference] element stored under the specified [type].
     *
     * @param [type] the `type` to remove
     */
    fun removeReference(type: Type) {
        references -= type.serializedName
    }

    /**
     * Changes the [href][Reference.href] properties of the `reference` element stored under the given [type], or
     * throws a [NoSuchElementException] if no element is found.
     *
     * @param [type] the `type` that the element is stored under
     * @param [href] the new href to use
     */
    fun setReference(type: Type, href: String) {
        this.getReferenceOrNone(type).fold<Unit>(
            { throw NoSuchElementException("No 'reference' element found with the given type <$type>") }
        ) { references[type.serializedName] = it.copy(href = href) }
    }

    /**
     * Returns the [reference][Reference] stored under the given [type], or throws a [NoSuchElementException] if none
     * is found.
     */
    fun getReference(type: Type): Reference = references[type.serializedName]
        ?: throw NoSuchElementException("No 'reference' element found with the given type <$type>")

    /**
     * Returns the [reference][Reference] stored under the given [type], or [None] if none is found.
     */
    fun getReferenceOrNone(type: Type): Option<Reference> = references.getValueOrNone(type.serializedName)

    // -- CUSTOM REFERENCES -- \\
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
    @JvmOverloads fun addCustomReference(customType: String, href: String, title: String? = null): Reference =
        Reference("other.$customType", href, Option(title)).also { references[it.type] = it }

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
    fun removeCustomReference(customType: String) {
        references -= "other.$customType"
    }

    /**
     * Changes the [href][Reference.href] property of the `reference` element  stored under the specified [customType],
     * or throws a  [NoSuchElementException] if no element is found.
     *
     * The `resource` and `href` properties of the found element are set to be that that of the specified [href].
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
     * @param [href] the href to use
     */
    fun setCustomReference(customType: String, href: String) {
        val type = "other.$customType"
        this.getCustomReferenceOrNone(type).fold<Unit>(
            { throw NoSuchElementException("No 'reference' element found with the given type <$type>") }
        ) { references[type] = it.copy(href = href) }
    }

    /**
     * Returns the [reference][Reference] stored under the given [customType], or [None] if none is found.
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
     */
    fun getCustomReference(customType: String): Reference = "other.$customType".let {
        references[it] ?: throw NoSuchElementException("No 'reference' element found with the given type <$it>")
    }

    /**
     * Returns the [reference][Reference] stored under the given [customType], or [None] if none is found.
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
     */
    fun getCustomReferenceOrNone(customType: String): Option<Reference> = references.getValueOrNone("other.$customType")

    override fun toElement(): Element = Element("guide", PackageDocument.NAMESPACE).apply {
        for ((_, ref) in references) addContent(ref.toElement())
    }

    override fun iterator(): Iterator<Reference> = references.values.iterator().asUnmodifiable()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageGuide -> false
        book != other.book -> false
        references != other.references -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + references.hashCode()
        return result
    }

    override fun toString(): String = "PackageGuide(references=$references)"

    /**
     * Implementation of the `reference` element contained inside of the [guide element][PackageGuide].
     *
     * The **required** [type] parameter describes the publication component `this` reference is pointing towards. The
     * value for the `type` property **must** be a [Type] constant when applicable. Other types **may** be used
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
    data class Reference internal constructor(val type: String, val href: String, val title: Option<String>) :
        ElementSerializer {
        /**
         * Returns the [Type] tied to the specified [type] of this `reference`.
         *
         * This returns a [Option] as [Type.getOrNone] is not guaranteed to always return a value, as `this`
         * reference might be using a custom `type`.
         */
        val guideType: Option<Type> = Type.getOrNone(type)

        /**
         * Returns whether or not `this` reference is using a custom [type].
         */
        val isCustomType: Boolean get() = guideType.isEmpty()

        /**
         * Returns the [PageResource] that this `reference` points towards.
         */
        fun getResource(book: Book): PageResource = TODO()

        override fun toElement(): Element = Element("reference", PackageDocument.NAMESPACE).apply {
            setAttribute("type", type)
            setAttribute("href", href)
            title.ifPresent { setAttribute("title", it) }
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
operator fun PackageGuide.minusAssign(customType: String) {
    removeCustomReference(customType)
}

/**
 * Removes the [reference][Reference] element stored under the specified [type].
 *
 * @param [type] the `type` to remove
 */
operator fun PackageGuide.minusAssign(type: PackageGuide.Type) {
    removeReference(type)
}

/**
 * Changes the [href][Reference.href] properties of the `reference` element stored under the given [type], or
 * throws a [NoSuchElementException] if no element is found.
 *
 * @param [type] the `type` that the element is stored under
 * @param [href] the new href to use
 */
operator fun PackageGuide.set(type: PackageGuide.Type, href: String) {
    setReference(type, href)
}

/**
 * Changes the [href][Reference.href] property of the `reference` element  stored under the specified [customType],
 * or throws a  [NoSuchElementException] if no element is found.
 *
 * The `resource` and `href` properties of the found element are set to be that that of the specified [href].
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
 * @param [href] the href to use
 */
operator fun PackageGuide.set(customType: String, href: String) {
    setCustomReference(customType, href)
}

/**
 * Returns the [reference][Reference] stored under the given [type], or throws a [NoSuchElementException] if none
 * is found.
 */
operator fun PackageGuide.get(type: PackageGuide.Type): Reference = getReference(type)

/**
 * Returns the [reference][Reference] stored under the given [customType], or [None] if none is found.
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
 */
operator fun PackageGuide.get(customType: String): Reference = getCustomReference(customType)