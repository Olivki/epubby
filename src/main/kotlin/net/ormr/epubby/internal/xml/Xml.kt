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

package net.ormr.epubby.internal.xml

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.ormr.epubby.internal.util.defaultXmlOutputter
import net.ormr.epubby.internal.util.loadDocument
import net.ormr.epubby.internal.xml.decoder.XmlDocumentDecoder
import net.ormr.epubby.internal.xml.encoder.XmlDocumentEncoder
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.output.XMLOutputter
import java.nio.file.Path

internal sealed class Xml(
    val outputter: XMLOutputter,
    override val serializersModule: SerializersModule,
) : StringFormat {
    companion object Default : Xml(
        outputter = defaultXmlOutputter,
        serializersModule = EmptySerializersModule()
    )

    fun <T> encodeToDocument(serializer: SerializationStrategy<T>, value: T): Document {
        val encoder = XmlDocumentEncoder(serializersModule)
        encoder.encodeSerializableValue(serializer, value)
        return encoder.document
    }

    fun <T> encodeToElement(serializer: SerializationStrategy<T>, value: T): Element =
        encodeToDocument(serializer, value).detachRootElement()

    final override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        val document = encodeToDocument(serializer, value)
        return outputter.outputString(document)
    }

    fun <T> decodeFromDocument(deserializer: DeserializationStrategy<T>, document: Document): T {
        val decoder = XmlDocumentDecoder(document, serializersModule)
        return decoder.decodeSerializableValue(deserializer)
    }

    fun <T> decodeFromElement(deserializer: DeserializationStrategy<T>, element: Element): T =
        decodeFromDocument(deserializer, Document(element.detach()))

    final override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T =
        decodeFromDocument(deserializer, loadDocument(string))

    fun <T> decodeFromFile(deserializer: DeserializationStrategy<T>, file: Path): T {
        val document = loadDocument(file)
        return decodeFromDocument(deserializer, document)
    }
}