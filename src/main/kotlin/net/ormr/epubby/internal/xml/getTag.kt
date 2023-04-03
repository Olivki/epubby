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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import net.ormr.epubby.internal.util.firstInstanceOrNull
import org.jdom2.Namespace

@OptIn(ExperimentalSerializationApi::class)
internal fun getXmlTag(descriptor: SerialDescriptor, index: Int): XmlTag = getXmlTag(
    descriptor = descriptor.getElementDescriptor(index),
    index = index,
    elementName = descriptor.getElementName(index),
    annotations = descriptor.getElementAnnotations(index),
)

@OptIn(ExperimentalSerializationApi::class)
internal fun SerialDescriptor.toXmlTag(): XmlTag = getXmlTag(this, 0, serialName, annotations)

internal fun getXmlTag(
    descriptor: SerialDescriptor,
    index: Int,
    elementName: String,
    annotations: List<Annotation>,
): XmlTag = XmlTag(
    name = elementName,
    index = index,
    descriptor = descriptor,
    listWrapperElementName = annotations.firstInstanceOrNull<XmlListWrapperElement>()?.name,
    elementsName = annotations.firstInstanceOrNull<XmlElementsName>()?.name,
    namespace = annotations.firstInstanceOrNull<XmlNamespace>()?.toNamespace(),
    additionalNamespaces = annotations.firstInstanceOrNull<XmlAdditionalNamespaces>()?.namespaces?.map {
        it.toNamespace()
    } ?: emptyList(),
    textValue = annotations.firstInstanceOrNull<XmlTextValue>(),
)

private fun XmlNamespace.toNamespace(): Namespace = Namespace.getNamespace(prefix, uri)