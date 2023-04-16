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

import dev.epubby.Epub3LegacyFeature
import dev.epubby.opf.OpfReadError
import dev.epubby.opf.OpfReadError.MissingAttribute
import dev.epubby.opf.OpfReadError.MissingElement
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.opf.GuideModel.ReferenceModel
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import org.jdom2.Element
import net.ormr.epubby.internal.Namespaces.OPF_NO_PREFIX as NAMESPACE

@OptIn(Epub3LegacyFeature::class)
internal object GuideModelXml : ModelXmlSerializer<OpfReadError>() {
    fun read(spine: Element) = effect {
        val references = spine
            .children("reference", NAMESPACE)
            .map { readReference(it).bind() }
        GuideModel(
            references = references,
        )
    }

    private fun readReference(ref: Element) = effect {
        ReferenceModel(
            // TODO: handle custom types when converting from model to user facing instance
            type = ref.attr("type").bind(),
            href = ref.attr("href").bind(),
            title = ref.optionalAttr("title"),
        )
    }

    fun write(guide: GuideModel): Element = buildElement("guide", NAMESPACE) {
        addChildren(guide.references, ::writeReference)
    }

    private fun writeReference(ref: ReferenceModel): Element = buildElement("reference", NAMESPACE) {
        this["type"] = ref.type
        this["href"] = ref.href
        this["title"] = ref.title
    }

    override fun missingAttribute(name: String, path: String): OpfReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): OpfReadError = MissingElement(name, path)

    override fun missingText(path: String): OpfReadError = error("'missingText' should never be used")
}