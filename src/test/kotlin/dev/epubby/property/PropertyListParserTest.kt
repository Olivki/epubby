/*
 * Copyright 2023 Oliver Berg
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
import dev.epubby.shouldBeOk
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import net.ormr.epubby.internal.property.PropertyModel
import net.ormr.epubby.internal.property.PropertyModelList
import net.ormr.epubby.internal.property.propertyListParser

class PropertyListParserTest : FunSpec({
    context("Valid property list types") {
        withData(
            PropertyListResult(
                "nav",
                listOf(PropertyModel(prefix = null, reference = "nav")),
            ),
            PropertyListResult(
                "marc:relators",
                listOf(PropertyModel(prefix = "marc", reference = "relators")),
            ),
            PropertyListResult(
                "nav marc:relators",
                listOf(
                    PropertyModel(prefix = null, reference = "nav"),
                    PropertyModel(prefix = "marc", reference = "relators"),
                ),
            ),
        ) { (input, expected) ->
            propertyListParser.parseToEnd(input).shouldBeOk { (result, remainingInput) ->
                remainingInput.shouldBeEmpty()
                result shouldBe expected
            }
        }
    }
})

private fun PropertyListResult(
    input: String,
    expected: List<PropertyModel>,
    name: String = input,
): PropertyListResult = PropertyListResult(input, PropertyModelList(expected), name)

private data class PropertyListResult(
    val input: String,
    val expected: PropertyModelList,
    val name: String = input,
) : WithDataTestName {
    override fun dataTestName(): String = name
}