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

package net.ormr.epubby.internal.metainf

import com.google.common.net.MediaType
import dev.epubby.Epub3Feature
import dev.epubby.NonEmptyMutableList
import dev.epubby.metainf.MetaInfContainer
import dev.epubby.property.Relationship

internal class MetaInfContainerImpl(override val version: String) : MetaInfContainer {
    override val rootFiles: NonEmptyMutableList<MetaInfContainer.RootFile>
        get() = TODO("Not yet implemented")
    override val links: MutableList<MetaInfContainer.Link>
        get() = TODO("Not yet implemented")

    data class RootFileImpl(
        override val fullPath: String,
        override val mediaType: MediaType
    ) : MetaInfContainer.RootFile

    @OptIn(Epub3Feature::class)
    data class LinkImpl(
        override val href: String,
        override val relation: Relationship?,
        override val mediaType: MediaType?
    ) : MetaInfContainer.Link
}