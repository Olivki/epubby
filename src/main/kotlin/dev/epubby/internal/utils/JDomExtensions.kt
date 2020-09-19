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

@file:JvmName("JDomUtils")

package dev.epubby.internal.utils

import dev.epubby.MalformedBookException
import moe.kanon.kommons.io.paths.newInputStream
import moe.kanon.kommons.io.paths.newOutputStream
import org.jdom2.*
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.nio.file.Path

//@get:JvmName("getPrettyFormat")
internal val defaultXmlFormat: Format by lazy { Format.getPrettyFormat().setIndent("  ") }

//@get:JvmName("getPrettyOutputter")
internal val defaultXmlOutputter: XMLOutputter by lazy { XMLOutputter(defaultXmlFormat) }

//@get:JvmName("getCompactOutputter")
internal val compactXmlOutputter: XMLOutputter by lazy { XMLOutputter(Format.getCompactFormat()) }

internal fun Document.encodeToString(outputter: XMLOutputter = defaultXmlOutputter): String = outputter.outputString(this)

internal fun Element.encodeToString(outputter: XMLOutputter = defaultXmlOutputter): String = outputter.outputString(this)

internal fun Element.getChildOrThrow(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Element =
    getChild(name, namespace) ?: throw MalformedBookException("Element ${this.encodeToString(compactXmlOutputter)} is missing required child '$name'.")

internal fun Element.getAttributeOrThrow(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Attribute =
    getAttribute(name, namespace)
        ?: throw MalformedBookException("Element '${this.encodeToString(compactXmlOutputter)}' is missing required attribute '$name'.")

internal fun Element.getAttributeValueOrThrow(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): String =
    getAttributeOrThrow(name, namespace).value

internal fun documentFrom(input: String): Document =
    input.reader().use { SAXBuilder(XMLReaders.NONVALIDATING).build(it) }

internal fun documentFrom(input: Path): Document =
    input.newInputStream().use { SAXBuilder(XMLReaders.NONVALIDATING).build(it) }

internal fun Document.writeTo(file: Path, outputter: XMLOutputter = defaultXmlOutputter): Path {
    file.newOutputStream().use { outputter.output(this, it) }
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