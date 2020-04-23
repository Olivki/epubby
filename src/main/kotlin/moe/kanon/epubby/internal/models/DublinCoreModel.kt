/*
 * Copyright 2019-2020 Oliver Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to ::in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package moe.kanon.epubby.internal.models

import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.MalformedBookException
import moe.kanon.epubby.dublincore.CreativeRole
import moe.kanon.epubby.dublincore.DateEvent
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.internal.elementOf
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Contributor
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Coverage
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Creator
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Description
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Publisher
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Relation
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Rights
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Subject
import moe.kanon.epubby.internal.models.LocalizedDublinCoreModel.Title
import moe.kanon.epubby.utils.Direction
import org.jdom2.Element
import org.jdom2.Namespace
import java.util.Locale
import moe.kanon.epubby.dublincore.DublinCore as DC
import moe.kanon.epubby.dublincore.LocalizedDublinCore as LDC

internal sealed class DublinCoreModel(internal val name: String) {
    internal abstract val identifier: String?
    internal abstract val content: String

    // only relevant in EPUB 2.x
    internal val attributes: MutableMap<String, String> by lazy {
        when (this) {
            is Date, is Identifier -> HashMap(1)
            is Contributor, is Creator -> HashMap(2)
            else -> HashMap(0)
        }
    }

    internal fun toElement(): Element = elementOf(name, Namespaces.DUBLIN_CORE) {
        if (identifier != null) it.setAttribute("id", identifier)

        if (this is LocalizedDublinCoreModel) {
            if (direction != null) it.setAttribute("dir", direction)
            if (language != null) it.setAttribute("lang", language, Namespace.XML_NAMESPACE)
        }

        for ((key, value) in attributes) {
            it.setAttribute(key, value, Namespaces.OPF_WITH_PREFIX)
        }

        it.text = content
    }

    internal fun toDublinCore(book: Book): DC {
        val event = attributes["event"]?.let { DateEvent.of(it) }
        val scheme = attributes["scheme"]
        val role = attributes["role"]?.let { CreativeRole._of(it) }
        val fileAs = attributes["file-as"]
        val direction = if (this is LocalizedDublinCoreModel) direction?.let { Direction.fromTag(it) } else null
        val language = if (this is LocalizedDublinCoreModel) language?.let(Locale::forLanguageTag) else null
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

    @SerialName("date")
    internal data class Date(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("date")

    @SerialName("format")
    internal data class Format(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("format")

    @SerialName("identifier")
    internal data class Identifier(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("identifier")

    @SerialName("language")
    internal data class Language(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("language")

    @SerialName("source")
    internal data class Source(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("source")

    @SerialName("type")
    internal data class Type(
        override val content: String,
        override val identifier: String?
    ) : DublinCoreModel("type")

    internal companion object {
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
            val attributes = element
                .attributes
                .asSequence()
                .filter { it.namespace == Namespaces.OPF_WITH_PREFIX }
                .associate { it.name to it.value }
            dublinCore.attributes.putAll(attributes)
            return dublinCore
        }

        internal fun fromDublinCore(origin: DC): DublinCoreModel = when (origin) {
            is DC.Date -> Date(origin.content, origin.identifier).apply {
                if (origin.book.version.isOlderThan(BookVersion.EPUB_3_0)) {
                    val event = origin.event
                    if (event != null) attributes["event"] = event.name
                }
            }
            is DC.Format -> Format(origin.content, origin.identifier)
            is DC.Identifier -> Identifier(origin.content, origin.identifier).apply {
                if (origin.book.version.isOlderThan(BookVersion.EPUB_3_0)) {
                    val scheme = origin.scheme
                    if (scheme != null) attributes["scheme"] = scheme
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
                if (origin.book.version.isOlderThan(BookVersion.EPUB_3_0)) {
                    val role = origin.role
                    val fileAs = origin.fileAs
                    if (role != null) attributes["role"] = role.code
                    if (fileAs != null) attributes["file-as"] = fileAs
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
                if (origin.book.version.isOlderThan(BookVersion.EPUB_3_0)) {
                    val role = origin.role
                    val fileAs = origin.fileAs
                    if (role != null) attributes["role"] = role.code
                    if (fileAs != null) attributes["file-as"] = fileAs
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

internal sealed class LocalizedDublinCoreModel(name: String) : DublinCoreModel(name) {
    internal abstract val direction: String?
    internal abstract val language: String?

    @SerialName("contributor")
    internal data class Contributor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("contributor")

    @SerialName("coverage")
    internal data class Coverage(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("coverage")

    @SerialName("creator")
    internal data class Creator(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("creator")

    @SerialName("description")
    internal data class Description(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("description")

    @SerialName("publisher")
    internal data class Publisher(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("publisher")

    @SerialName("relation")
    internal data class Relation(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("relation")

    @SerialName("rights")
    internal data class Rights(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("rights")

    @SerialName("subject")
    internal data class Subject(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("subject")

    @SerialName("title")
    internal data class Title(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?
    ) : LocalizedDublinCoreModel("title")

    internal companion object {
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