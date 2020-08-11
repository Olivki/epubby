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

package dev.epubby.internal

import dev.epubby.MalformedBookException
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.paths.newOutputStream
import org.jdom2.Attribute
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.nio.file.Path

private val defaultFormat: Format = Format.getPrettyFormat().setIndent("    ")

internal fun Document.stringify(format: Format = defaultFormat): String = XMLOutputter(format).outputString(this)

internal fun Element.stringify(format: Format = defaultFormat): String = XMLOutputter(format).outputString(this)

internal fun Element.getChildOrThrow(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Element =
    getChild(name, namespace) ?: throw MalformedBookException("Element '$name' is missing required child '$name'.")

internal fun Element.getAttributeOrThrow(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Attribute =
    getAttribute(name, namespace)
        ?: throw MalformedBookException("Element '$name' is missing required attribute '$name'.")

internal fun Element.getAttributeValueOrThrow(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): String =
    getAttributeOrThrow(name, namespace).value

internal fun documentFrom(input: String): Document =
    input.reader().use { SAXBuilder(XMLReaders.NONVALIDATING).build(it) }

internal fun documentFrom(input: Path): Document =
    input.newInputStream().use { SAXBuilder(XMLReaders.NONVALIDATING).build(it) }

internal fun Document.writeTo(file: Path, format: Format = Format.getPrettyFormat()): Path {
    file.newOutputStream().use { XMLOutputter(format).output(this, it) }
    return file
}

internal inline fun <T> Document.use(scope: (doc: Document, root: Element) -> T): T = run { scope(this, rootElement) }

internal inline fun documentOf(
    rootName: String,
    rootNamespace: Namespace,
    scope: (doc: Document, root: Element) -> Unit
): Document = Document(Element(rootName, rootNamespace)).apply { scope(this, rootElement) }

internal inline fun elementOf(
    name: String,
    namespace: Namespace,
    scope: (Element) -> Unit
): Element = Element(name, namespace).apply { scope(this) }