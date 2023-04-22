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

package net.ormr.epubby.internal.models.metainf

import cc.ekblad.konbini.ParserResult
import com.github.michaelbull.result.flatMap
import dev.epubby.metainf.MetaInfContainerReadError
import dev.epubby.metainf.MetaInfContainerReadError.*
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.metainf.MetaInfContainerModel.LinkModel
import net.ormr.epubby.internal.models.metainf.MetaInfContainerModel.RootFileModel
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import org.jdom2.Element
import net.ormr.epubby.internal.Namespaces.META_INF_CONTAINER as NAMESPACE

internal object MetaInfContainerModelXml : ModelXmlSerializer<MetaInfContainerReadError>() {
    fun read(container: Element) = effect {
        val rootFiles = container
            .childrenWrapper("rootfiles", "rootfile", NAMESPACE)
            .bind()
            .map { readRootFile(it).bind() }
        ensure(rootFiles.isNotEmpty()) { EmptyRootFiles }
        val links = container
            .optionalChildrenWrapper("links", "link", NAMESPACE)
            ?.map { readLink(it).bind() } ?: emptyList()
        MetaInfContainerModel(
            version = container.attr("version").bind(),
            rootFiles = rootFiles,
            links = links,
        )
    }

    private fun readRootFile(rootFile: Element) = effect {
        RootFileModel(
            fullPath = rootFile.attr("full-path").bind(),
            mediaType = rootFile
                .rawAttr("media-type")
                .flatMap(::parseMediaType)
                .bind(),
        )
    }

    private fun readLink(link: Element) = effect {
        LinkModel(
            href = link.attr("href").bind(),
            relation = link
                .rawOptionalAttr("rel")
                ?.let(::parseProperty)
                ?.bind(),
            mediaType = link
                .rawOptionalAttr("mediaType")
                ?.let(::parseMediaType)
                ?.bind(),
        )
    }

    fun write(container: MetaInfContainerModel): Element = buildElement("container", NAMESPACE) {
        this["version"] = container.version
        addChildrenWithWrapper("rootfiles", NAMESPACE, container.rootFiles, ::writeRootFile)
        addChildrenWithWrapper("links", NAMESPACE, container.links, ::writeLink)
    }

    private fun writeRootFile(rootFile: RootFileModel): Element = buildElement("rootfile", NAMESPACE) {
        this["full-path"] = rootFile.fullPath
        this["media-type"] = rootFile.mediaType.toString()
    }

    private fun writeLink(link: LinkModel): Element = buildElement("link", NAMESPACE) {
        this["href"] = link.href
        this["rel"] = link.relation?.asString()
        this["mediaType"] = link.mediaType?.toString()
    }

    override fun missingAttribute(name: String, path: String): MetaInfContainerReadError =
        MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): MetaInfContainerReadError =
        MissingElement(name, path)

    override fun invalidProperty(value: String, cause: ParserResult.Error, path: String): MetaInfContainerReadError =
        InvalidProperty(value, cause, path)

    override fun invalidMediaType(value: String, path: String): MetaInfContainerReadError =
        InvalidMediaType(value, path)
}