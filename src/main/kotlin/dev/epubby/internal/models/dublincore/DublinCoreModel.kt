/*
 * Copyright 2019-2022 Oliver Berg
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
import dev.epubby.Epub
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.MalformedBookException
import dev.epubby.dublincore.CreativeRole
import dev.epubby.dublincore.DateEvent
import dev.epubby.dublincore.DublinCore
import dev.epubby.dublincore.DublinCore.*
import dev.epubby.dublincore.DublinCore.Date
import dev.epubby.dublincore.DublinCoreVisitor
import dev.epubby.dublincore.LocalizedDublinCore.*
import dev.epubby.internal.Namespaces
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.models.dublincore.LocalizedDublinCoreModel.*
import dev.epubby.internal.utils.elementOf
import dev.epubby.internal.utils.ifNotNull
import dev.epubby.utils.Direction
import org.jdom2.Element
import org.jdom2.Namespace
import java.util.*

internal sealed class DublinCoreModel(internal val name: String) {
    internal abstract val identifier: String?
    internal abstract val content: String

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
    internal fun toDublinCore(epub: Epub): DublinCore {
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
            is DateModel -> Date(epub, content, identifier, event)
            is FormatModel -> Format(epub, content, identifier)
            is IdentifierModel -> Identifier(epub, content, identifier, scheme)
            is LanguageModel -> Language(epub, content, identifier)
            is SourceModel -> Source(epub, content, identifier)
            is TypeModel -> Type(epub, content, identifier)
            is ContributorModel -> Contributor(epub, content, identifier, direction, language, role, fileAs)
            is CoverageModel -> Coverage(epub, content, identifier, direction, language)
            is CreatorModel -> Creator(epub, content, identifier, direction, language, role, fileAs)
            is DescriptionModel -> Description(epub, content, identifier, direction, language)
            is PublisherModel -> Publisher(epub, content, identifier, direction, language)
            is RelationModel -> Relation(epub, content, identifier, direction, language)
            is RightsModel -> Rights(epub, content, identifier, direction, language)
            is SubjectModel -> Subject(epub, content, identifier, direction, language)
            is TitleModel -> Title(epub, content, identifier, direction, language)
        }
    }

    @SerializedName("date")
    internal data class DateModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("date")

    @SerializedName("format")
    internal data class FormatModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("format")

    @SerializedName("identifier")
    internal data class IdentifierModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("identifier")

    @SerializedName("language")
    internal data class LanguageModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("language")

    @SerializedName("source")
    internal data class SourceModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("source")

    @SerializedName("type")
    internal data class TypeModel internal constructor(
        override val content: String,
        override val identifier: String?,
    ) : DublinCoreModel("type")

    internal companion object {
        @JvmSynthetic
        internal fun fromElement(element: Element): DublinCoreModel {
            val content = element.textNormalize
            val identifier = element.getAttributeValue("id")
            val dublinCore = when (element.name.lowercase()) {
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
            override fun visitDate(date: Date): DateModel = DateModel(date.value, date.identifier).apply {
                if (date.epub.version.isOlder(EPUB_3_0)) {
                    date.event ifNotNull { attributes["event"] = it.name }
                }
            }

            override fun visitFormat(format: Format): FormatModel = FormatModel(format.value, format.identifier)

            override fun visitIdentifier(
                identifier: Identifier,
            ): IdentifierModel = IdentifierModel(identifier.value, identifier.identifier).apply {
                if (identifier.epub.version.isOlder(EPUB_3_0)) {
                    identifier.scheme ifNotNull { attributes["scheme"] = it }
                }
            }

            override fun visitLanguage(language: Language): LanguageModel =
                LanguageModel(language.value, language.identifier)

            override fun visitSource(source: Source): SourceModel = SourceModel(source.value, source.identifier)

            override fun visitType(type: Type): TypeModel = TypeModel(type.value, type.identifier)

            override fun visitContributor(contributor: Contributor): ContributorModel = ContributorModel(
                contributor.value,
                contributor.identifier,
                contributor.direction?.value,
                contributor.language?.toLanguageTag()
            ).apply {
                if (contributor.epub.version.isOlder(EPUB_3_0)) {
                    contributor.role ifNotNull { attributes["role"] = it.code }
                    contributor.fileAs ifNotNull { attributes["file-as"] = it }
                }
            }

            override fun visitCoverage(coverage: Coverage): CoverageModel = CoverageModel(
                coverage.value,
                coverage.identifier,
                coverage.direction?.value,
                coverage.language?.toLanguageTag()
            )

            override fun visitCreator(creator: Creator): CreatorModel = CreatorModel(
                creator.value,
                creator.identifier,
                creator.direction?.value,
                creator.language?.toLanguageTag()
            ).apply {
                if (creator.epub.version.isOlder(EPUB_3_0)) {
                    creator.role ifNotNull { attributes["role"] = it.code }
                    creator.fileAs ifNotNull { attributes["file-as"] = it }
                }
            }

            override fun visitDescription(description: Description): DescriptionModel = DescriptionModel(
                description.value,
                description.identifier,
                description.direction?.value,
                description.language?.toLanguageTag()
            )

            override fun visitPublisher(publisher: Publisher): PublisherModel = PublisherModel(
                publisher.value,
                publisher.identifier,
                publisher.direction?.value,
                publisher.language?.toLanguageTag()
            )

            override fun visitRelation(relation: Relation): RelationModel = RelationModel(
                relation.value,
                relation.identifier,
                relation.direction?.value,
                relation.language?.toLanguageTag()
            )

            override fun visitRights(rights: Rights): RightsModel = RightsModel(
                rights.value,
                rights.identifier,
                rights.direction?.value,
                rights.language?.toLanguageTag()
            )

            override fun visitSubject(subject: Subject): SubjectModel = SubjectModel(
                subject.value,
                subject.identifier,
                subject.direction?.value,
                subject.language?.toLanguageTag()
            )

            override fun visitTitle(title: Title): TitleModel = TitleModel(
                title.value,
                title.identifier,
                title.direction?.value,
                title.language?.toLanguageTag()
            )
        }

        @JvmSynthetic
        internal fun fromDublinCore(origin: DublinCore): DublinCoreModel = origin.accept(DublinCoreToModelVisitor)
    }
}

internal sealed class LocalizedDublinCoreModel(name: String) : DublinCoreModel(name) {
    internal abstract val direction: String?
    internal abstract val language: String?

    @SerializedName("contributor")
    internal data class ContributorModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("contributor")

    @SerializedName("coverage")
    internal data class CoverageModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("coverage")

    @SerializedName("creator")
    internal data class CreatorModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("creator")

    @SerializedName("description")
    internal data class DescriptionModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("description")

    @SerializedName("publisher")
    internal data class PublisherModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("publisher")

    @SerializedName("relation")
    internal data class RelationModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("relation")

    @SerializedName("rights")
    internal data class RightsModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("rights")

    @SerializedName("subject")
    internal data class SubjectModel internal constructor(
        override val content: String,
        override val identifier: String?,
        override val direction: String?,
        override val language: String?,
    ) : LocalizedDublinCoreModel("subject")

    @SerializedName("title")
    internal data class TitleModel internal constructor(
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

            return when (element.name.lowercase()) {
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