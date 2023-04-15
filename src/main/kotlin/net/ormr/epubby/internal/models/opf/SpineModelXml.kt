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

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.epubby.Epub3Feature
import dev.epubby.Epub3LegacyFeature
import dev.epubby.opf.ContentReadError
import dev.epubby.opf.ContentReadError.*
import dev.epubby.opf.SpineReadError.InvalidLinearValue
import dev.epubby.opf.SpineReadError.NoItemRefElements
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.WriterData
import net.ormr.epubby.internal.models.opf.SpineModel.ItemRefModel
import net.ormr.epubby.internal.models.supportsEpub3Features
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import org.jdom2.Element
import net.ormr.epubby.internal.Namespaces.OPF_NO_PREFIX as NAMESPACE

@OptIn(Epub3LegacyFeature::class, Epub3Feature::class)
internal object SpineModelXml : ModelXmlSerializer<ContentReadError>() {
    fun read(spine: Element) = effect {
        val refs = spine
            .children("itemref", NAMESPACE)
            .map { readItemRef(it).bind() }
        ensure(refs.isNotEmpty()) { NoItemRefElements }
        SpineModel(
            identifier = spine.optionalAttr("id"),
            pageProgressionDirection = spine
                .optionalAttr("page-progression-direction")
                ?.let(::parseReadingDirection)
                ?.bind(::UnknownReadingDirection),
            toc = spine.optionalAttr("toc"),
            references = refs,
        )
    }

    private fun readItemRef(ref: Element) = effect {
        ItemRefModel(
            idRef = ref.attr("idref").bind(),
            identifier = ref.optionalAttr("id"),
            isLinear = ref
                .optionalAttr("linear")
                ?.let(::parseLinear)
                // if 'linear' is not defined, then it's implicitly assumed to be 'yes'
                ?.bind() ?: true,
            properties = ref.optionalAttr("properties"),
        )
    }

    private fun parseLinear(value: String): Result<Boolean, InvalidLinearValue> = when (value) {
        "no" -> Ok(false)
        "yes" -> Ok(true)
        else -> Err(InvalidLinearValue(value))
    }

    fun write(spine: SpineModel, data: WriterData): Element = buildElement("spine", NAMESPACE) {
        this["id"] = spine.identifier
        this["toc"] = spine.toc
        this["page-progression-direction"] = spine.pageProgressionDirection?.value
        addChildren(spine.references) { writeItemRef(it, data) }
    }

    private fun writeItemRef(ref: ItemRefModel, data: WriterData): Element = buildElement("itemref", NAMESPACE) {
        this["idref"] = ref.idRef
        this["id"] = ref.idRef
        // only output 'linear' attribute if it's false, because no 'linear' attribute means implicit 'true'
        // so there's no point in us bloating the XML with unneeded attributes
        if (!ref.isLinear) this["linear"] = "no"
        if (data.supportsEpub3Features()) {
            this["properties"] = ref.properties
        }
    }

    override fun missingAttribute(name: String, path: String): ContentReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): ContentReadError = MissingElement(name, path)

    override fun missingText(path: String): ContentReadError = error("'missingText' should never be used")
}