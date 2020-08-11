/*
 * Copyright 2019-2020 Oliver Berg
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

package dev.epubby.html

import moe.kanon.kommons.io.paths.newInputStream
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Path

interface HtmlDocumentFactory {
    fun fromString(value: String, baseUri: URI): HtmlDocument

    fun fromInputStream(input: InputStream, charset: Charset, baseUri: URI): HtmlDocument

    @JvmDefault
    fun fromFile(file: Path, charset: Charset): HtmlDocument =
        file.newInputStream().use { fromInputStream(it, charset, file.toUri()) }
}