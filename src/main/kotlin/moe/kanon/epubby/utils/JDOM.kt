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

@file:JvmName("JDomUtils")

package moe.kanon.epubby.utils

import moe.kanon.epubby.MalformedBookException
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
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
import java.nio.file.StandardOpenOption

// -- PUBLIC -- \\
/**
 * Returns the first child with the given [name] and [namespace], or [None] if none is found.
 */
fun Element.getChildOrNone(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Option<Element> =
    Option(this.getChild(name, namespace))

/**
 * Returns the attribute with the given [name] and [namespace], or [None] if none is found.
 */
fun Element.getAttributeOrNone(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Option<Attribute> =
    Option(this.getAttribute(name, namespace))

/**
 * Returns the value of the attribute with the given [name] and [namespace], or [None] if none is found.
 */
fun Element.getAttributeValueOrNone(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Option<String> =
    Option(this.getAttributeValue(name, namespace))

/**
 * Serializes `this` document into a file stored in the specified [directory], with the specified [fileName], it is
 * output using the given [format].
 *
 * @param [directory] the directory in which the file will be saved
 * @param [fileName] the name of the file. *(This is the full file name, including the extension.)*
 * @param [format] the [Format] that should be used when writing this document to the file
 */
fun Document.writeTo(directory: Path, fileName: String, format: Format = Format.getPrettyFormat()): Path =
    directory.resolve(fileName).also { file ->
        file.newOutputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use {
            XMLOutputter(format).output(this, it)
        }
    }

/**
 * Serializes `this` document into the given [file], it is output using the given [format].
 *
 * @param [file] the file to which the document should be saved
 * @param [format] the [Format] that should be used when writing this document to the file
 */
fun Document.writeTo(file: Path, format: Format = Format.getPrettyFormat()): Path = file.also {
    it.newOutputStream().use { out ->
        XMLOutputter(format).output(this, out)
    }
}

fun Document.stringify(format: Format = Format.getPrettyFormat()): String = XMLOutputter(format).outputString(this)

fun Element.stringify(format: Format = Format.getPrettyFormat()): String = XMLOutputter(format).outputString(this)

// -- INTERNAL -- \\
@PublishedApi
@JvmSynthetic
internal inline fun <R> parseXmlFile(file: Path, scope: (doc: Document, root: Element) -> R): R =
    file.newInputStream().use { input ->
        val doc = SAXBuilder(XMLReaders.NONVALIDATING).build(input)
        scope(doc, doc.rootElement)
    }

@PublishedApi
@JvmSynthetic
internal fun Element.attr(name: String, epub: Path, current: Path): String =
    getAttributeValue(name) ?: throw MalformedBookException(
        epub,
        current,
        "Element '${this.name}' is missing required attribute '$name'"
    )

@PublishedApi
@JvmSynthetic
internal fun Element.child(
    name: String,
    epub: Path,
    current: Path,
    namespace: Namespace = this.namespace
): Element = getChild(name, namespace) ?: throw MalformedBookException.withDebug(
    epub,
    current,
    "Element '${this.name}' is missing required child element '$name'"
)

@PublishedApi
@JvmSynthetic
internal inline fun Document.docScope(block: Element.() -> Unit): Document = this.apply { rootElement.apply(block) }

@PublishedApi
@JvmSynthetic
internal inline fun <R> Document.scope(block: Element.() -> R): R = with(this) { rootElement.run(block) }

@PublishedApi
@JvmSynthetic
internal fun Element.toCompactString(): String = XMLOutputter(Format.getCompactFormat()).outputString(this)

