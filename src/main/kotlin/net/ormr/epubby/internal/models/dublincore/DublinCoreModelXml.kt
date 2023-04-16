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

package net.ormr.epubby.internal.models.dublincore

import dev.epubby.Epub2Feature
import dev.epubby.ReadingDirection
import dev.epubby.dublincore.DateEvent
import dev.epubby.dublincore.DublinCoreReadError
import dev.epubby.dublincore.DublinCoreReadError.*
import dev.epubby.marc.CreativeRole
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE
import net.ormr.epubby.internal.Namespaces.OPF
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.SerializedName
import net.ormr.epubby.internal.models.WriterData
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel.*
import net.ormr.epubby.internal.models.dublincore.LocalizedDublinCoreModel.*
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import org.jdom2.Element
import org.jdom2.Namespace.XML_NAMESPACE
import kotlin.reflect.full.findAnnotation

internal object DublinCoreModelXml : ModelXmlSerializer<DublinCoreReadError>() {
    // TODO: only read epub2 specific attributes if version is actually epub2?
    @OptIn(Epub2Feature::class)
    fun read(element: Element) = effect {
        // TODO: should this be normalized or just taken raw? the DC docs are a bit of a pita to traverse
        val content = element.optionalOwnText()
        val identifier = element.optionalAttr("id")
        val scheme = element.optionalAttr("scheme", OPF)
        val dateEvent = element.optionalAttr("event", OPF)?.let(DateEvent.Companion::of)
        when (val name = element.name) {
            "language" -> LanguageModel(identifier, content)
            "identifier" -> IdentifierModel(identifier, scheme, content)
            "date" -> DateModel(identifier, dateEvent, content)
            "format" -> FormatModel(identifier, content)
            "source" -> SourceModel(identifier, content)
            "type" -> TypeModel(identifier, content)
            else -> {
                val direction = element
                    .optionalAttr("dir")
                    ?.let(ReadingDirection.Companion::fromValue)
                    ?.bind(::UnknownReadingDirection)
                val language = element.optionalAttr("lang", XML_NAMESPACE)
                val role = element.optionalAttr("role")?.let(CreativeRole.Companion::create)
                val fileAs = element.optionalAttr("file-as")
                when (name) {
                    "title" -> TitleModel(identifier, direction, language, content)
                    "contributor" -> ContributorModel(identifier, direction, language, role, fileAs, content)
                    "coverage" -> CoverageModel(identifier, direction, language, content)
                    "creator" -> CreatorModel(identifier, direction, language, role, fileAs, content)
                    "description" -> DescriptionModel(identifier, direction, language, content)
                    "publisher" -> PublisherModel(identifier, direction, language, content)
                    "relation" -> RelationModel(identifier, direction, language, content)
                    "rights" -> RightsModel(identifier, direction, language, content)
                    "subject" -> SubjectModel(identifier, direction, language, content)
                    else -> shift(UnknownDublinCoreElement(name))
                }
            }
        }
    }

    @OptIn(Epub2Feature::class)
    fun write(element: DublinCoreModel, data: WriterData): Element = buildElement(element.name, DUBLIN_CORE) {
        this["id"] = element.identifier

        if (element is DateModel && data.version.isEpub2()) {
            this["event"] = element.event?.name
        }

        if (element is IdentifierModel && data.version.isEpub2()) {
            this["scheme"] = element.scheme
        }

        if (element is LocalizedDublinCoreModel) {
            this["dir"] = element.direction?.value
            this["lang", XML_NAMESPACE] = element.language
        }

        if (element is ContributorModel && data.version.isEpub2()) {
            this["role"] = element.role?.code
            this["file-as"] = element.fileAs
        }

        if (element is CreatorModel && data.version.isEpub2()) {
            this["role"] = element.role?.code
            this["file-as"] = element.fileAs
        }

        text = element.content
    }

    override fun missingAttribute(name: String, path: String): DublinCoreReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): DublinCoreReadError = MissingElement(name, path)

    override fun missingText(path: String): DublinCoreReadError = error("'missingText' should never be used")

    private inline fun <reified T : Any> findSerializedNames(): Set<String> = T::class
        .sealedSubclasses
        .asSequence()
        .filterNot { it.java.isInterface }
        .mapTo(hashSetOf()) { it.findAnnotation<SerializedName>()?.value ?: error("Missing @SerializedName on $it") }
}