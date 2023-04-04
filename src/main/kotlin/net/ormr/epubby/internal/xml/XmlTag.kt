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

import kotlinx.serialization.descriptors.SerialDescriptor
import org.jdom2.Namespace

internal data class XmlTag(
    val name: String,
    val index: Int,
    val descriptor: SerialDescriptor,
    val listWrapperElementName: String?,
    val elementsName: String?,
    val namespace: Namespace?,
    val additionalNamespaces: List<Namespace>,
    val textValue: XmlTextValue?,
    val isAttributeOverflowTarget: Boolean,
    val shouldInheritNamespace: Boolean,
)