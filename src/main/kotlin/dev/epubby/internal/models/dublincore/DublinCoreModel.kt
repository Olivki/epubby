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

package dev.epubby.internal.models.dublincore

import com.google.common.collect.Maps
import dev.epubby.Book
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.MalformedBookException
import dev.epubby.dublincore.CreativeRole
import dev.epubby.dublincore.DateEvent
import dev.epubby.internal.Namespaces
import dev.epubby.internal.elementOf
import dev.epubby.internal.ifNotNull
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.models.dublincore.LocalizedDublinCoreModel.*
import dev.epubby.utils.Direction
import org.jdom2.Element
import org.jdom2.Namespace
import java.util.Collections
import java.util.Locale
import dev.epubby.dublincore.DublinCore as DC
import dev.epubby.dublincore.LocalizedDublinCore as LDC

sealed class DublinCoreModel(val name: String) {
    abstract val identifier: String?
    abstract val content: String

    // only relevant in EPUB 2.x
    @get:JvmSynthetic
    internal val attributes: MutableMap<String, String> by lazy {
        when (this) {
            is Date, is Identifier -> Maps.newHashMapWithExpectedSize<String, String>(1)
            is Contributor, is Creator -> Maps.newHashMapWithExpectedSize<String, String>(2)
            else -> Collections.emptyMap<String, String>()
        }
    }

    @JvmSynthetic
    internal fun toElement(): Element = elementOf(name, Namespaces.DUBLIN_CORE) {
        if (identifier != null) {
            it.setAttribute("id", identifier)
        }

        if (this is LocalizedDublinCoreModel) {
            if (direction != null) {
                it.setAttribute("dir", direction)
            }

            if (language != null) {
                it.setAttribute("lang", language, Namespace.XML_NAMESPACE)
            }
        }

        for ((key, value) in attributes) {
            it.setAttribute(key, value, Namespaces.OPF_WITH_PREFIX)
        }

        it.text = content
    }

    @JvmSynthetic
    internal fun toDublinCore(book: Book): DC {
        val event = attributes["event"]?.let { DateEvent.of(it) }
        val scheme = attributes["scheme"]
        val role = attributes["role"]?.let { CreativeRole.create(it) }
        val fileAs = attributes["file-as"]
        val direction = when (this) {
            is LocalizedDublinCoreModel -> direction?.let { Direction.fromTag(it) }
            else -> null
        }
        val language = when (this) {
            is LocalizedDublinCoreModel -> language?.let(Locale::forLanguageTag)
            else -> null
        }

        return when (this) {
            is Date -> DC.Date(book, content, identifier, event)
            is Format -> DC.Format(book, content, identifier)
            is Identifier -> DC.Identifier(book, content, identifier, scheme)
            is Language -> DC.Language(book, content, identifier)
            is Source -> DC.Source(book, content, identifier)
            is Type -> DC.Type(book, content, identifier)
            is Contributor -> LDC.Contributor(book, content, identifier, direction, language, role, fileAs)
            is Coverage -> LDC.Coverage(book, content, identifier, direction, language)
            is Creator -> LDC.Creator(book, content, identifier, direction, language, role, fileAs)
            is Description -> LDC.Description(book, content, identifier, direction, language)
            is Publisher -> LDC.Publisher(book, content, identifier, direction, language)
            is Relation -> LDC.Relation(book, content, identifier, direction, language)
            is Rights -> LDC.Rights(book, content, identifier, direction, language)
            is Subject -> LDC.Subject(book, content, identifier, direction, language)
            is Title -> LDC.Title(book, content, identifier, direction, language)
        }
    }

    @SerializedName("date")
    data class Date internal constructor(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("date")

    @SerializedName("format")
    data class Format internal constructor(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("format")

    @SerializedName("identifier")
    data class Identifier internal constructor(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("identifier")

    @SerializedName("language")
    data class Language internal constructor(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("language")

    @SerializedName("source")
    data class Source internal constructor(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("source")

    @SerializedName("type")
    data class Type internal constructor(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("type")

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(element: Element): DublinCoreModel {
            val content = element.textNormalize
            val identifier = element.getAttributeValue("id")
            val dublinCore = when (element.name.toLowerCase()) {
                "language" -> Language(content, identifier)
                "identifier" -> Identifier(content, identifier)
                "date" -> Date(content, identifier)
                "format" -> Format(content, identifier)
                "source" -> Source(content, identifier)
                "type" -> Type(content, identifier)
                else -> LocalizedDublinCoreModel.fromElement(element, content, identifier)
            }
            val attributes = element.attributes
                .filter { it.namespace == Namespaces.OPF_WITH_PREFIX }
                .associate { it.name to it.value }
            dublinCore.attributes.putAll(attributes)

            return dublinCore
        }

        @JvmSynthetic
        internal fun fromDublinCore(origin: DC): DublinCoreModel = when (origin) {
            is DC.Date -> Date(
                origin.content,
                origin.identifier
            ).apply {
                if (origin.book.version isOlder EPUB_3_0) {
                    origin.event.ifNotNull { attributes["event"] = it.name }
                }
            }
            is DC.Format -> Format(origin.content, origin.identifier)
            is DC.Identifier -> Identifier(origin.content, origin.identifier).apply {
                if (origin.book.version isOlder EPUB_3_0) {
                    origin.scheme.ifNotNull { attributes["scheme"] = it }
                }
            }
            is DC.Language -> Language(origin.content, origin.identifier)
            is DC.Source -> Source(origin.content, origin.identifier)
            is DC.Type -> Type(origin.content, origin.identifier)
            is LDC.Contributor -> Contributor(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            ).apply {
                if (origin.book.version isOlder EPUB_3_0) {
                    origin.role.ifNotNull { attributes["role"] = it.code }
                    origin.fileAs.ifNotNull { attributes["file-as"] = it }
                }
            }
            is LDC.Coverage -> Coverage(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            )
            is LDC.Creator -> Creator(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            ).apply {
                if (origin.book.version isOlder EPUB_3_0) {
                    origin.role.ifNotNull { attributes["role"] = it.code }
                    origin.fileAs.ifNotNull { attributes["file-as"] = it }
                }
            }
            is LDC.Description -> Description(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            )
            is LDC.Publisher -> Publisher(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            )
            is LDC.Relation -> Relation(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            )
            is LDC.Rights -> Rights(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            )
            is LDC.Subject -> Subject(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            )
            is LDC.Title -> Title(
                origin.content,
                origin.identifier,
                origin.direction?.attributeName,
                origin.language?.toLanguageTag()
            )
        }
    }
}

sealed class LocalizedDublinCoreModel(name: String) : DublinCoreModel(name) {
    abstract val direction: String?
    abstract val language: String?

    @SerializedName("contributor")
    data class Contributor internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("contributor")

    @SerializedName("coverage")
    data class Coverage internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("coverage")

    @SerializedName("creator")
    data class Creator internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("creator")

    @SerializedName("description")
    data class Description internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("description")

    @SerializedName("publisher")
    data class Publisher internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("publisher")

    @SerializedName("relation")
    data class Relation internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("relation")

    @SerializedName("rights")
    data class Rights internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("rights")

    @SerializedName("subject")
    data class Subject internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("subject")

    @SerializedName("title")
    data class Title internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("title")

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(element: Element, content: String, identifier: String?): LocalizedDublinCoreModel {
            val direction = element.getAttributeValue("dir")
            val language = element.getAttributeValue("lang", Namespace.XML_NAMESPACE)

            return when (element.name.toLowerCase()) {
                "title" -> Title(content, identifier, direction, language)
                "contributor" -> Contributor(content, identifier, direction, language)
                "coverage" -> Coverage(content, identifier, direction, language)
                "creator" -> Creator(content, identifier, direction, language)
                "description" -> Description(content, identifier, direction, language)
                "publisher" -> Publisher(content, identifier, direction, language)
                "relation" -> Relation(content, identifier, direction, language)
                "rights" -> Rights(content, identifier, direction, language)
                "subject" -> Subject(content, identifier, direction, language)
                else -> throw MalformedBookException("Unknown dublin-core element '${element.name}'.")
            }
        }
    }
}