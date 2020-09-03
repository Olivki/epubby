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

package dev.epubby.builders.metainf

import com.google.common.net.MediaType
import dev.epubby.Book
import dev.epubby.builders.AbstractModelBuilder
import dev.epubby.builders.OptionalValue
import dev.epubby.internal.models.metainf.MetaInfContainerModel
import dev.epubby.internal.models.metainf.MetaInfContainerModel.LinkModel
import dev.epubby.internal.models.metainf.MetaInfContainerModel.RootFileModel
import dev.epubby.metainf.ContainerVersion
import dev.epubby.metainf.MetaInfContainer
import dev.epubby.prefixes.Prefixes
import kotlinx.collections.immutable.toPersistentList
import java.nio.file.Path

/**
 * A builder for [MetaInfContainer] instances.
 */
class MetaInfContainerBuilder :
    AbstractModelBuilder<MetaInfContainer, MetaInfContainerModel>(MetaInfContainerModel::class.java) {
    // TODO: documentation

    private var version: ContainerVersion = ContainerVersion.DEFAULT

    @OptionalValue
    fun version(version: ContainerVersion): MetaInfContainerBuilder = apply {
        this.version = version
    }

    private var rootFiles: MutableList<RootFileModel> = mutableListOf()

    /**
     * TODO
     *
     * @throws [IllegalArgumentException] if [rootFiles] is empty
     */
    fun rootFiles(rootFiles: Iterable<RootFileModel>): MetaInfContainerBuilder = apply {
        require(rootFiles.any()) { "'rootFiles' must not be empty" }
        this.rootFiles = rootFiles.toMutableList()
    }

    @OptionalValue
    fun rootFile(rootFile: RootFileModel): MetaInfContainerBuilder = apply {
        rootFiles.add(rootFile)
    }

    @OptionalValue
    fun rootFile(path: Path, mediaType: MediaType): MetaInfContainerBuilder =
        rootFile(RootFileModel(path.toString().substring(1), mediaType.toString()))

    private var links: MutableList<LinkModel> = mutableListOf()

    @OptionalValue
    fun links(links: Iterable<LinkModel>): MetaInfContainerBuilder = apply {
        this.links = links.toMutableList()
    }

    @OptionalValue
    fun link(link: LinkModel): MetaInfContainerBuilder = apply {
        links.add(link)
    }

    @JvmOverloads
    @OptionalValue
    fun link(href: String, mediaType: MediaType, relation: String? = null): MetaInfContainerBuilder =
        link(LinkModel(href, relation, mediaType.toString()))

    override fun build(): MetaInfContainerModel {
        val rootFiles = rootFiles.ifEmpty { malformedValue("rootFiles", "must not be empty") }.toPersistentList()
        val links = links.toPersistentList()

        return MetaInfContainerModel(version.toString(), rootFiles, links)
    }

    override fun build(book: Book): MetaInfContainer = build().toMetaInfContainer(book, Prefixes.empty())
}