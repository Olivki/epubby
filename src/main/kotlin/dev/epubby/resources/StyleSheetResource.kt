/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.resources

import com.google.auto.service.AutoService
import com.google.common.net.MediaType
import com.helger.css.decl.CascadingStyleSheet
import com.helger.css.reader.CSSReader
import com.helger.css.reader.CSSReaderSettings
import com.helger.css.reader.errorhandler.ThrowingCSSParseErrorHandler
import com.helger.css.writer.CSSWriter
import com.helger.css.writer.CSSWriterSettings
import dev.epubby.Epub
import dev.epubby.MalformedBookException
import dev.epubby.files.RegularFile
import kotlinx.collections.immutable.persistentHashSetOf

class StyleSheetResource internal constructor(
    epub: Epub,
    identifier: String,
    file: RegularFile,
    override val mediaType: MediaType,
) : LocalResource(epub, identifier, file) {
    private var isStyleSheetLoaded: Boolean = false

    // TODO: should we switch from this library to jStyleParser instead? That library has a much nicer java like
    //       API to work with rather than the absolute mess that is this library
    //       however this library is very lenient and can fix faulty stuff and it has nullability annotations

    val styleSheet: CascadingStyleSheet by lazy {
        file.newBufferedReader().use { reader ->
            val result = CSSReader.readFromReader({ reader }, READER_SETTINGS)
            isStyleSheetLoaded = result != null
            result
        } ?: throw MalformedBookException("Could not read '$file' to a CascadingStyleSheet")
    }

    override fun writeToFile() {
        if (isStyleSheetLoaded) {
            file.newBufferedWriter().use { writer ->
                CSSWriter(WRITER_SETTINGS).writeCSS(styleSheet, writer)
            }
        }
    }

    /**
     * Returns the result of invoking the [visitStyleSheet][ResourceVisitor.visitStyleSheet] function of the given
     * [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitStyleSheet(this)

    override fun toString(): String = "StyleSheetResource(identifier='$identifier', mediaType=$mediaType, file='$file')"

    private companion object {
        private val READER_SETTINGS: CSSReaderSettings =
            CSSReaderSettings().setCustomErrorHandler(ThrowingCSSParseErrorHandler())

        private val WRITER_SETTINGS: CSSWriterSettings = CSSWriterSettings().setRemoveUnnecessaryCode(true)
    }
}

@AutoService(LocalResourceLocator::class)
internal object StyleSheetResourceLocator : LocalResourceLocator {
    private val TYPES: Set<MediaType> = persistentHashSetOf(
        MediaType.CSS_UTF_8,
        MediaType.CSS_UTF_8.withoutParameters()
    )
    private val FACTORY: LocalResourceFactory = ::StyleSheetResource

    override fun findFactory(mediaType: MediaType): LocalResourceFactory? = FACTORY.takeIf { mediaType in TYPES }
}