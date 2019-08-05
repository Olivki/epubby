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

import com.helger.css.decl.CascadingStyleSheet
import com.helger.css.reader.CSSReader
import com.helger.css.reader.CSSReaderSettings
import com.helger.css.reader.errorhandler.ThrowingCSSParseErrorHandler
import com.helger.css.writer.CSSWriter
import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.resources.StyleSheetResource
import moe.kanon.kommons.io.paths.newBufferedReader
import moe.kanon.kommons.io.paths.newBufferedWriter
import java.io.IOException
import java.nio.file.StandardOpenOption

/**
 * A simple wrapper around a [CascadingStyleSheet] to make it work nicer within our system.
 *
 * @property [book] The [Book] instance that `this` stylesheet is tied to.
 * @property [resource] The [Resource] instance created for the file that [css] is referencing.
 * @property [css] The underlying [CascadingStyleSheet] that `this` class is wrapped around.
 */
data class StyleSheet private constructor(
    val book: Book,
    val resource: StyleSheetResource,
    val css: CascadingStyleSheet
) {
    companion object {
        @Throws(EpubbyException::class)
        @JvmStatic fun fromResource(resource: StyleSheetResource): StyleSheet {
            val css = resource.file.newBufferedReader().use {
                val settings = CSSReaderSettings()
                    .setCustomErrorHandler(ThrowingCSSParseErrorHandler())
                CSSReader.readFromReader({ it }, settings)
            } ?: throw EpubbyException(resource.book.file, "Failed to parse CSS file <${resource.file}>")
            return StyleSheet(resource.book, resource, css)
        }
    }

    @Throws(IOException::class)
    fun saveStyleSheet() {
        resource.file.newBufferedWriter(
            StandardOpenOption.WRITE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { CSSWriter().writeCSS(css, it) }
    }
}