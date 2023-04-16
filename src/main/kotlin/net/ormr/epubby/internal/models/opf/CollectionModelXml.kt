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

import com.github.michaelbull.result.Ok
import dev.epubby.Epub3Feature
import dev.epubby.opf.OpfReadError
import dev.epubby.opf.OpfReadError.MissingAttribute
import dev.epubby.opf.OpfReadError.MissingElement
import net.ormr.epubby.internal.models.ModelXmlSerializer
import org.jdom2.Element

@OptIn(Epub3Feature::class)
internal object CollectionModelXml : ModelXmlSerializer<OpfReadError>() {
    fun read(collection: Element) = Ok(CollectionModel(collection))

    fun write(collection: CollectionModel): Element = collection.element

    override fun missingAttribute(name: String, path: String): OpfReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): OpfReadError = MissingElement(name, path)

    override fun missingText(path: String): OpfReadError = error("'missingText' should never be used")
}