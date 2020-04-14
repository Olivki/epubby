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

package moe.kanon.epubby.internal

import kotlinx.serialization.ImplicitReflectionSerializer
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.newBufferedReader
import moe.kanon.kommons.io.paths.readString
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.serialization.XML
import org.jdom2.Document
import java.nio.file.Path

@OptIn(ImplicitReflectionSerializer::class)
internal inline fun <reified T : Any> XML.parse(input: Path): T =
    XmlStreaming.newReader(input.newBufferedReader()).use { parse(T::class, it) }

@OptIn(ImplicitReflectionSerializer::class)
internal inline fun <reified T : Any> XML.parseIfExists(input: Path): T? =
    input.takeIf(Path::exists)?.let { parse(T::class, input.readString()) }

@OptIn(ImplicitReflectionSerializer::class)
internal inline fun <reified T : Any> XML.toDocument(obj: T, prefix: String? = null): Document =
    documentOf(stringify(obj))