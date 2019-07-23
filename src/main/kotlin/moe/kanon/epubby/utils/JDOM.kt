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

package moe.kanon.epubby.utils

import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.io.paths.newInputStream
import org.jdom2.Attribute
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@PublishedApi internal inline fun <R> parseFile(file: Path, scope: Element.() -> R): R =
    file.newInputStream().use { input -> with(SAXBuilder(XMLReaders.NONVALIDATING).build(input).rootElement, scope) }

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
fun Document.saveTo(directory: Path, fileName: String, format: Format = Format.getPrettyFormat()): Path {
    val file = directory.resolve(fileName)
    val writer = XMLOutputter(format)

    writer.output(
        this,
        Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    )

    return file
}

fun Document.stringify(format: Format = Format.getPrettyFormat()): String = XMLOutputter(format).outputString(document)