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

@file:Suppress("OPT_IN_USAGE")

package net.ormr.epubby.internal.xml

import kotlinx.serialization.InheritableSerialInfo
import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
internal annotation class XmlListWrapperElement(val name: String)

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
internal annotation class XmlElementsName(val name: String)

@InheritableSerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
internal annotation class XmlNamespace(val prefix: String, val uri: String)

@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
internal annotation class XmlAdditionalNamespaces(val namespaces: Array<XmlNamespace>)

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
internal annotation class XmlAttributeOverflow

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
internal annotation class XmlTextValue(val normalize: Boolean = true)