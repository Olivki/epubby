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

import dev.epubby.metainf.MetaInfContainer.RootFile
import net.ormr.epubby.internal.metainf.MetaInfContainerImpl.RootFileImpl
import net.ormr.epubby.internal.models.metainf.MetaInfContainerModel.RootFileModel

internal object MetaInfContainerModelConverter {
    fun RootFileModel.toRootFile(): RootFileImpl = RootFileImpl(
        fullPath = fullPath,
        mediaType = mediaType,
    )

    fun RootFile.toRootFileModel(): RootFileModel = RootFileModel(
        fullPath = fullPath,
        mediaType = mediaType,
    )
}