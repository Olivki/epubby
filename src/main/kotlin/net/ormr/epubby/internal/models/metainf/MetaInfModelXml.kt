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

import com.github.michaelbull.result.Result
import dev.epubby.metainf.MetaInfReadError
import net.ormr.epubby.internal.util.effect
import net.ormr.epubby.internal.util.loadDocument
import java.nio.file.Path
import kotlin.io.path.div

internal object MetaInfModelXml {
    fun readFiles(directory: Path): Result<MetaInfModel, MetaInfReadError> = effect {
        // TODO: handle potential failure of loading document
        val containerDocument = loadDocument(directory / "container.xml")
        val container = MetaInfContainerModelXml.read(containerDocument.rootElement).bind()
        // TODO: the other files
        MetaInfModel(container)
    }
}