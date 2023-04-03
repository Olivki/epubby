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

package net.ormr.epubby.internal.util

import net.pearx.kasechange.toPascalCase
import net.pearx.kasechange.toTitleCase
import org.jsoup.Jsoup
import java.net.URL

private data class MarcEntry(val code: String, val name: String, var description: String)

private val parenRegex = """\(([^)]+)\)""".toRegex()

internal fun createCreativeRole(): String {
    val document = Jsoup.parse(URL("https://www.loc.gov/marc/relators/relaterm.html"), 2_000)
    val entries = mutableListOf<MarcEntry>()
    val akaEntries = hashMapOf<String, MutableList<String>>()
    document
        .select("dl")
        .first { it.childrenSize() > 20 }
        .children()
        .chunked(2)
        .forEach { (nameElement, useNoteElement) ->
            val name = nameElement.selectFirst("span.authorized")?.ownText()
            if (name != null) {
                val code = nameElement.selectFirst("span.relator-code")!!
                    .ownText()
                    .substringAfter('[')
                    .substringBefore(']')
                val description = useNoteElement.ownText()
                val fixedName = name.substringBefore(',')
                entries += MarcEntry(code, fixedName, description)
            } else {
                val aka = nameElement.selectFirst("span.unauthorized")!!.ownText()
                val ref = useNoteElement.selectFirst("div.use-term > span.use-for-ref")!!.ownText()
                akaEntries.getOrPut(ref) { mutableListOf() }.add(aka)
            }
        }

    // replace references
    for (origin in entries) {
        val regex = """${origin.name} \[${origin.code}]""".toRegex()
        val className = origin.name.toPascalCase()
        for (target in entries) {
            target.description = target.description.replace(regex, "[$className]")
        }
    }

    // italicize
    for (entry in entries) {
        entry.description = entry.description.replace(parenRegex) { "*${it.value}*" }
    }

    return entries.joinToString(separator = "\n\n") { (code, name, description) ->
        val refs = akaEntries[name]
        if (refs == null) {
            """
            /**
             * $description${if (description.endsWith('.')) "" else "."}
             */
            public object ${name.toPascalCase()} : DefaultCreativeRole("$code", "$name")
            """.trimIndent()
        } else {
            """
            /**
             * $description${if (description.endsWith('.')) "" else "."}
             *
             * Also known as: ${refs.joinToString { "*${it.toTitleCase()}*" }}.
             */
            public object ${name.toPascalCase()} : DefaultCreativeRole("$code", "$name")
            """.trimIndent()
        }
    }
}