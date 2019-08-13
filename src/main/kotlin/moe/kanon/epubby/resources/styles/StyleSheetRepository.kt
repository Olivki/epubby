/*
 * Copyright 2019 Oliver Berg
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

@file:Suppress("DataClassPrivateConstructor")

package moe.kanon.epubby.resources.styles

import moe.kanon.epubby.Book
import moe.kanon.epubby.logger
import moe.kanon.epubby.resources.StyleSheetResource
import moe.kanon.kommons.collections.asUnmodifiable
import java.io.IOException
import java.nio.file.Path

/**
 * A repository containing all the [StyleSheet] instances used by the given [book].
 */
class StyleSheetRepository(val book: Book) : Iterable<StyleSheet> {
    @JvmSynthetic internal val styleSheets: MutableSet<StyleSheet> = HashSet()

    @JvmSynthetic internal fun populateRepository() {
        for (resource in book.resources.asSequence().filterIsInstance<StyleSheetResource>()) {
            styleSheets += StyleSheet.fromResource(resource)
        }
    }

    // TODO: Documentation and more functions

    fun addStyleSheet(sheet: StyleSheet): StyleSheet = sheet.also {
        styleSheets += it
    }

    @JvmName("hasStyleSheet")
    operator fun contains(id: String): Boolean = styleSheets.any { it.resource.identifier == id }

    @JvmName("hasStyleSheet")
    operator fun contains(resource: StyleSheetResource): Boolean = styleSheets.any { it.resource == resource }

    @JvmName("hasStyleSheet")
    operator fun contains(file: Path): Boolean = styleSheets.any { it.resource.file == file }

    @Throws(IOException::class)
    fun saveStyleSheets() {
        logger.debug { "Saving all style-sheets.." }
        for (sheet in styleSheets) sheet.saveStyleSheet()
    }

    override fun iterator(): Iterator<StyleSheet> = styleSheets.iterator().asUnmodifiable()
}

