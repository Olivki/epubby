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

package net.ormr.epubby.internal.xml.encoder

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.ormr.epubby.internal.util.addAdditionalNamespaces
import net.ormr.epubby.internal.xml.toXmlTag
import org.jdom2.Document
import org.jdom2.Element

@OptIn(ExperimentalSerializationApi::class)
internal class XmlDocumentEncoder(
    override val serializersModule: SerializersModule,
) : XmlEncoder() {
    val document = Document()

    // TODO: handle nullables

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = when (descriptor.kind) {
        StructureKind.CLASS -> {
            val tag = descriptor.toXmlTag()
            val element = Element(descriptor.serialName, tag.namespace)
            element.addAdditionalNamespaces(tag.additionalNamespaces)
            document.rootElement = element
            XmlElementEncoder(element, serializersModule)
        }
        else -> throw SerializationException("Can't encode kind ${descriptor.kind} (${descriptor.serialName}) as root element for document")
    }
}