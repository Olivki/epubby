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

package dev.epubby.property

import cc.ekblad.konbini.parseToEnd
import dev.epubby.shouldBeError
import dev.epubby.shouldBeOk
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import net.ormr.epubby.internal.property.ParsedProperty
import net.ormr.epubby.internal.property.propertyParser

class PropertyParserTest : FunSpec({
    // TODO: better context name
    context("Valid property data types") {
        withData(
            PropertyResult("marc:relators", ParsedProperty(prefix = "marc", reference = "relators")),
            PropertyResult("onix:codelist5", ParsedProperty(prefix = "onix", reference = "codelist5")),
            PropertyResult("role", ParsedProperty(prefix = null, reference = "role")),
            PropertyResult("file-as", ParsedProperty(prefix = null, reference = "file-as")),
            PropertyResult("display-seq", ParsedProperty(prefix = null, reference = "display-seq")),
        ) { (input, expected) ->
            propertyParser.parseToEnd(input).shouldBeOk { (property, remainingInput) ->
                remainingInput.shouldBeEmpty()
                property shouldBe expected
            }
        }
    }

    context("Invalid property data types") {
        withData(
            "marc::relators",
            "::role",
            "m arc:relators",
        ) { input ->
            propertyParser.parseToEnd(input).shouldBeError()
        }
    }
})

private data class PropertyResult(val input: String, val expected: ParsedProperty) : WithDataTestName {
    override fun dataTestName(): String = input
}