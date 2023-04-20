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

package dev.epubby.prefix

import cc.ekblad.konbini.parseToEnd
import dev.epubby.shouldBeError
import dev.epubby.shouldBeOk
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import net.ormr.epubby.internal.prefix.ParsedMapping
import net.ormr.epubby.internal.prefix.prefixesParser

class PrefixParserTest : FunSpec({
    context("Valid prefix data type") {
        withData(
            PrefixResult(
                "foaf: http://xmlns.com/foaf/spec/",
                listOf(ParsedMapping("foaf", "http://xmlns.com/foaf/spec/")),
            ),
            PrefixResult(
                "dbp: http://dbpedia.org/ontology/",
                listOf(ParsedMapping("dbp", "http://dbpedia.org/ontology/")),
            ),
            PrefixResult(
                "foaf: http://xmlns.com/foaf/spec/ dbp: http://dbpedia.org/ontology/",
                listOf(
                    ParsedMapping("foaf", "http://xmlns.com/foaf/spec/"),
                    ParsedMapping("dbp", "http://dbpedia.org/ontology/"),
                ),
            ),
            PrefixResult(
                "foaf: http://xmlns.com/foaf/spec/\n\t\t dbp: http://dbpedia.org/ontology/",
                listOf(
                    ParsedMapping("foaf", "http://xmlns.com/foaf/spec/"),
                    ParsedMapping("dbp", "http://dbpedia.org/ontology/"),
                ),
                name = "foaf: http://xmlns.com/foaf/spec/\\n\\t\\t dbp: http://dbpedia.org/ontology/",
            ),
        ) { (input, expected) ->
            prefixesParser.parseToEnd(input).shouldBeOk { (result, remainingInput) ->
                remainingInput.shouldBeEmpty()
                result shouldBe expected
            }
        }
    }

    context("Invalid prefix data type") {
        withData(
            "foaf:http://xmlns.com/foaf/spec/",
            "foaf",
            "foaf:",
            "http://xmlns.com/foaf/spec/",
        ) { input ->
            prefixesParser.parseToEnd(input).shouldBeError()
        }
    }
})

private data class PrefixResult(
    val input: String,
    val expected: List<ParsedMapping>,
    val name: String = input,
) : WithDataTestName {
    override fun dataTestName(): String = name
}