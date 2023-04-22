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

package net.ormr.epubby.internal.models.opf

import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.mapError
import dev.epubby.*
import dev.epubby.opf.OpfReadError
import dev.epubby.opf.OpfReadError.*
import dev.epubby.opf.PackageDocumentReadError.InvalidVersion
import dev.epubby.version.parseEpubVersion
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.WriterData
import net.ormr.epubby.internal.models.supportsEpub3Features
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import org.jdom2.Element
import org.jdom2.Namespace.XML_NAMESPACE
import net.ormr.epubby.internal.Namespaces.OPF_NO_PREFIX as NAMESPACE

@OptIn(
    Epub2Feature::class,
    Epub2DeprecatedFeature::class,
    Epub3Feature::class,
    Epub3LegacyFeature::class,
    Epub3DeprecatedFeature::class,
)
internal object OpfModelXml : ModelXmlSerializer<OpfReadError>() {
    fun read(root: Element) = effect {
        OpfModel(
            version = root
                .attr("version")
                .flatMap { parseEpubVersion(it).mapError(::InvalidVersion) }
                .bind(),
            uniqueIdentifier = root.attr("unique-identifier").bind(),
            readingDirection = root
                .rawOptionalAttr("dir")
                ?.let(::parseReadingDirection)
                ?.bind(),
            identifier = root.optionalAttr("id"),
            prefixes = root.optionalAttr("prefix"),
            language = root.optionalAttr("lang", XML_NAMESPACE),
            metadata = root.child("metadata", NAMESPACE).flatMap(MetadataModelXml::read).bind(),
            manifest = root.child("manifest", NAMESPACE).flatMap(ManifestModelXml::read).bind(),
            spine = root.child("spine", NAMESPACE).flatMap(SpineModelXml::read).bind(),
            guide = root.optionalChild("guide", NAMESPACE)?.let(GuideModelXml::read)?.bind(),
            bindings = root.optionalChild("bindings", NAMESPACE)?.let(BindingsModelXml::read)?.bind(),
            collection = root.optionalChild("collection", NAMESPACE)?.let(CollectionModelXml::read)?.bind(),
            tours = root.optionalChild("tours", NAMESPACE)?.let(ToursModelXml::read)?.bind(),
        )
    }

    fun write(content: OpfModel, data: WriterData): Element = buildElement("package", NAMESPACE) {
        this["version"] = content.version.toString()
        this["unique-identifier"] = content.uniqueIdentifier
        this["dir"] = content.readingDirection?.value
        this["id"] = content.identifier
        if (data.supportsEpub3Features()) {
            this["prefix"] = content.prefixes
        }
        this["lang", XML_NAMESPACE] = content.language
        addChild(MetadataModelXml.write(content.metadata, data))
        addChild(ManifestModelXml.write(content.manifest, data))
        addChild(SpineModelXml.write(content.spine, data))
        addChild(content.guide?.let(GuideModelXml::write))
        if (data.supportsEpub3Features()) {
            addChild(content.bindings?.let(BindingsModelXml::write))
            addChild(content.collection?.let(CollectionModelXml::write))
        }
        if (data.version.isEpub2()) {
            addChild(content.tours?.let(ToursModelXml::write))
        }
    }

    override fun missingAttribute(name: String, path: String): OpfReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): OpfReadError = MissingElement(name, path)

    override fun unknownReadingDirection(value: String, path: String): OpfReadError =
        UnknownReadingDirection(value, path)
}