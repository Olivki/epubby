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

import dev.epubby.Epub3DeprecatedFeature
import dev.epubby.Epub3Feature
import dev.epubby.opf.BindingsReadError.NoMediaTypeElements
import dev.epubby.opf.ContentReadError
import dev.epubby.opf.ContentReadError.MissingAttribute
import dev.epubby.opf.ContentReadError.MissingElement
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.opf.BindingsModel.MediaTypeModel
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import org.jdom2.Element
import net.ormr.epubby.internal.Namespaces.OPF_NO_PREFIX as NAMESPACE

@OptIn(Epub3Feature::class, Epub3DeprecatedFeature::class)
internal object BindingsModelXml : ModelXmlSerializer<ContentReadError>() {
    fun read(spine: Element) = effect {
        val mediaTypes = spine
            .children("mediaType", NAMESPACE)
            .map { readMediaType(it).bind() }
        ensure(mediaTypes.isNotEmpty()) { NoMediaTypeElements }
        BindingsModel(
            mediaTypes = mediaTypes,
        )
    }

    private fun readMediaType(mediaType: Element) = effect {
        MediaTypeModel(
            mediaType = mediaType.attr("media-type").bind(),
            handler = mediaType.attr("handler").bind(),
        )
    }

    fun write(bindings: BindingsModel): Element = buildElement("bindings", NAMESPACE) {
        addChildren(bindings.mediaTypes, ::writeMediaType)
    }

    private fun writeMediaType(mediaType: MediaTypeModel): Element = buildElement("mediaType", NAMESPACE) {
        this["media-type"] = mediaType.mediaType
        this["handler"] = mediaType.handler
    }

    override fun missingAttribute(name: String, path: String): ContentReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): ContentReadError = MissingElement(name, path)

    override fun missingText(path: String): ContentReadError = error("'missingText' should never be used")
}