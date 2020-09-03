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
import dev.epubby.dublincore.*
import dev.epubby.dublincore.DublinCore.*
import dev.epubby.dublincore.LocalizedDublinCore.*
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

sealed class DublinCoreModel(val name: String) {
    abstract val identifier: String?
    abstract val content: String

    // only relevant in EPUB 2.x
    @get:JvmSynthetic
    internal val attributes: MutableMap<String, String> by lazy {
        when (this) {
            is DateModel, is IdentifierModel -> Maps.newHashMapWithExpectedSize(1)
            is ContributorModel, is CreatorModel -> Maps.newHashMapWithExpectedSize(2)
            else -> Collections.emptyMap()
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
    internal fun toDublinCore(book: Book): DublinCore {
        val event = attributes["event"]?.let { DateEvent.of(it) }
        val scheme = attributes["scheme"]
        val role = attributes["role"]?.let { CreativeRole.create(it) }
        val fileAs = attributes["file-as"]
        val direction = when (this) {
            is LocalizedDublinCoreModel -> direction?.let { Direction.fromStringOrNull(it) }
            else -> null
        }
        val language = when (this) {
            is LocalizedDublinCoreModel -> language?.let(Locale::forLanguageTag)
            else -> null
        }

        return when (this) {
            is DateModel -> Date(book, content, identifier, event)
            is FormatModel -> Format(book, content, identifier)
            is IdentifierModel -> Identifier(book, content, identifier, scheme)
            is LanguageModel -> Language(book, content, identifier)
            is SourceModel -> Source(book, content, identifier)
            is TypeModel -> Type(book, content, identifier)
            is ContributorModel -> Contributor(book, content, identifier, direction, language, role, fileAs)
            is CoverageModel -> Coverage(book, content, identifier, direction, language)
            is CreatorModel -> Creator(book, content, identifier, direction, language, role, fileAs)
            is DescriptionModel -> Description(book, content, identifier, direction, language)
            is PublisherModel -> Publisher(book, content, identifier, direction, language)
            is RelationModel -> Relation(book, content, identifier, direction, language)
            is RightsModel -> Rights(book, content, identifier, direction, language)
            is SubjectModel -> Subject(book, content, identifier, direction, language)
            is TitleModel -> Title(book, content, identifier, direction, language)
        }
    }

    @SerializedName("date")
    data class DateModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("date")

    @SerializedName("format")
    data class FormatModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("format")

    @SerializedName("identifier")
    data class IdentifierModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("identifier")

    @SerializedName("language")
    data class LanguageModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("language")

    @SerializedName("source")
    data class SourceModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("source")

    @SerializedName("type")
    data class TypeModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("type")

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(element: Element): DublinCoreModel {
            val content = element.textNormalize
            val identifier = element.getAttributeValue("id")
            val dublinCore = when (element.name.toLowerCase()) {
                "language" -> LanguageModel(content, identifier)
                "identifier" -> IdentifierModel(content, identifier)
                "date" -> DateModel(content, identifier)
                "format" -> FormatModel(content, identifier)
                "source" -> SourceModel(content, identifier)
                "type" -> TypeModel(content, identifier)
                else -> LocalizedDublinCoreModel.fromElement(element, content, identifier)
            }
            val attributes = element.attributes
                .filter { it.namespace == Namespaces.OPF_WITH_PREFIX }
                .associate { it.name to it.value }
            dublinCore.attributes.putAll(attributes)

            return dublinCore
        }

        private object DublinCoreToModelVisitor : DublinCoreVisitor<DublinCoreModel> {
            override fun visitDate(date: Date): DateModel = DateModel(date.content, date.identifier).apply {
                if (date.book.version.isOlder(EPUB_3_0)) {
                    date.event ifNotNull { attributes["event"] = it.name }
                }
            }

            override fun visitFormat(format: Format): FormatModel = FormatModel(format.content, format.identifier)

            override fun visitIdentifier(
                identifier: Identifier,
            ): IdentifierModel = IdentifierModel(identifier.content, identifier.identifier).apply {
                if (identifier.book.version.isOlder(EPUB_3_0)) {
                    identifier.scheme ifNotNull { attributes["scheme"] = it }
                }
            }

            override fun visitLanguage(language: Language): LanguageModel =
                LanguageModel(language.content, language.identifier)

            override fun visitSource(source: Source): SourceModel = SourceModel(source.content, source.identifier)

            override fun visitType(type: Type): TypeModel = TypeModel(type.content, type.identifier)

            override fun visitContributor(contributor: Contributor): ContributorModel = ContributorModel(
                contributor.content,
                contributor.identifier,
                contributor.direction?.attributeName,
                contributor.language?.toLanguageTag()
            ).apply {
                if (contributor.book.version.isOlder(EPUB_3_0)) {
                    contributor.role ifNotNull { attributes["role"] = it.code }
                    contributor.fileAs ifNotNull { attributes["file-as"] = it }
                }
            }

            override fun visitCoverage(coverage: Coverage): CoverageModel = CoverageModel(
                coverage.content,
                coverage.identifier,
                coverage.direction?.attributeName,
                coverage.language?.toLanguageTag()
            )

            override fun visitCreator(creator: Creator): CreatorModel = CreatorModel(
                creator.content,
                creator.identifier,
                creator.direction?.attributeName,
                creator.language?.toLanguageTag()
            ).apply {
                if (creator.book.version.isOlder(EPUB_3_0)) {
                    creator.role ifNotNull { attributes["role"] = it.code }
                    creator.fileAs ifNotNull { attributes["file-as"] = it }
                }
            }

            override fun visitDescription(description: Description): DescriptionModel = DescriptionModel(
                description.content,
                description.identifier,
                description.direction?.attributeName,
                description.language?.toLanguageTag()
            )

            override fun visitPublisher(publisher: Publisher): PublisherModel = PublisherModel(
                publisher.content,
                publisher.identifier,
                publisher.direction?.attributeName,
                publisher.language?.toLanguageTag()
            )

            override fun visitRelation(relation: Relation): RelationModel = RelationModel(
                relation.content,
                relation.identifier,
                relation.direction?.attributeName,
                relation.language?.toLanguageTag()
            )

            override fun visitRights(rights: Rights): RightsModel = RightsModel(
                rights.content,
                rights.identifier,
                rights.direction?.attributeName,
                rights.language?.toLanguageTag()
            )

            override fun visitSubject(subject: Subject): SubjectModel = SubjectModel(
                subject.content,
                subject.identifier,
                subject.direction?.attributeName,
                subject.language?.toLanguageTag()
            )

            override fun visitTitle(title: Title): TitleModel = TitleModel(
                title.content,
                title.identifier,
                title.direction?.attributeName,
                title.language?.toLanguageTag()
            )
        }

        @JvmSynthetic
        internal fun fromDublinCore(origin: DublinCore): DublinCoreModel = origin.accept(DublinCoreToModelVisitor)
    }
}

sealed class LocalizedDublinCoreModel(name: String) : DublinCoreModel(name) {
    abstract val direction: String?
    abstract val language: String?

    @SerializedName("contributor")
    data class ContributorModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("contributor")

    @SerializedName("coverage")
    data class CoverageModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("coverage")

    @SerializedName("creator")
    data class CreatorModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("creator")

    @SerializedName("description")
    data class DescriptionModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("description")

    @SerializedName("publisher")
    data class PublisherModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("publisher")

    @SerializedName("relation")
    data class RelationModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("relation")

    @SerializedName("rights")
    data class RightsModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("rights")

    @SerializedName("subject")
    data class SubjectModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("subject")

    @SerializedName("title")
    data class TitleModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("title")

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(element: Element, content: String, identifier: String?): LocalizedDublinCoreModel {
            val direction = element.getAttributeValue("dir")
            val language = element.getAttributeValue("lang", Namespace.XML_NAMESPACE)

            return when (element.name.toLowerCase()) {
                "title" -> TitleModel(content, identifier, direction, language)
                "contributor" -> ContributorModel(content, identifier, direction, language)
                "coverage" -> CoverageModel(content, identifier, direction, language)
                "creator" -> CreatorModel(content, identifier, direction, language)
                "description" -> DescriptionModel(content, identifier, direction, language)
                "publisher" -> PublisherModel(content, identifier, direction, language)
                "relation" -> RelationModel(content, identifier, direction, language)
                "rights" -> RightsModel(content, identifier, direction, language)
                "subject" -> SubjectModel(content, identifier, direction, language)
                else -> throw MalformedBookException("Unknown dublin-core element '${element.name}'.")
            }
        }
    }
}