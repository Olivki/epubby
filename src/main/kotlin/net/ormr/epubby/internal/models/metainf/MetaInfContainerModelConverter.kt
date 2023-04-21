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

import dev.epubby.Epub3Feature
import dev.epubby.metainf.MetaInfContainer
import dev.epubby.metainf.MetaInfContainer.Link
import dev.epubby.metainf.MetaInfContainer.RootFile
import dev.epubby.toNonEmptyMutableList
import net.ormr.epubby.internal.metainf.MetaInfContainerImpl
import net.ormr.epubby.internal.metainf.MetaInfContainerImpl.LinkImpl
import net.ormr.epubby.internal.metainf.MetaInfContainerImpl.RootFileImpl
import net.ormr.epubby.internal.models.metainf.MetaInfContainerModel.LinkModel
import net.ormr.epubby.internal.models.metainf.MetaInfContainerModel.RootFileModel
import net.ormr.epubby.internal.prefix.EmptyPrefixMap
import net.ormr.epubby.internal.property.PropertyResolver
import net.ormr.epubby.internal.property.toPropertyModel

@OptIn(Epub3Feature::class)
internal object MetaInfContainerModelConverter {
    // model -> instance
    fun MetaInfContainerModel.toMetaInfContainer(): MetaInfContainerImpl =
        MetaInfContainerImpl(
            version = version,
            rootFiles = rootFiles.map { it.toRootFile() }.toNonEmptyMutableList(),
            links = links.map { it.toLink() }.toMutableList(),
        )

    fun RootFileModel.toRootFile(): RootFileImpl = RootFileImpl(
        fullPath = fullPath,
        mediaType = mediaType,
    )

    private fun LinkModel.toLink(): LinkImpl = LinkImpl(
        href = href,
        relation = relation?.let { PropertyResolver.resolveLinkRel(it, EmptyPrefixMap) },
        mediaType = mediaType,
    )

    // instance -> model
    fun MetaInfContainer.toMetaInfContainerModel(): MetaInfContainerModel = MetaInfContainerModel(
        version = version,
        rootFiles = rootFiles.map { it.toRootFileModel() },
        links = links.map { it.toLinkModel() },
    )

    fun RootFile.toRootFileModel(): RootFileModel = RootFileModel(
        fullPath = fullPath,
        mediaType = mediaType,
    )

    private fun Link.toLinkModel(): LinkModel = LinkModel(
        href = href,
        relation = relation?.toPropertyModel(),
        mediaType = mediaType,
    )
}