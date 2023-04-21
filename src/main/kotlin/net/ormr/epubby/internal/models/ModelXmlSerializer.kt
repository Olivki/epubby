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

package net.ormr.epubby.internal.models

import cc.ekblad.konbini.ParserResult
import cc.ekblad.konbini.parseToEnd
import com.github.michaelbull.result.*
import dev.epubby.ReadingDirection
import net.ormr.epubby.internal.property.PropertyModel
import net.ormr.epubby.internal.property.PropertyModelList
import net.ormr.epubby.internal.property.propertyListParser
import net.ormr.epubby.internal.property.propertyParser
import net.ormr.epubby.internal.util.getOwnText
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.xpath.XPathHelper

internal abstract class ModelXmlSerializer<E> {
    protected open fun missingAttribute(name: String, path: String): E =
        error("'missingAttribute' should never be used")

    protected open fun missingElement(name: String, path: String): E = error("'missingElement' should never be used")

    protected open fun missingText(path: String): E = error("'missingText' should never be used")

    protected open fun invalidProperty(value: String, cause: ParserResult.Error): E =
        error("'invalidProperty' should never be used")

    private fun createMissingAttributeError(element: Element, name: String, namespace: Namespace): E {
        val path = XPathHelper.getAbsolutePath(element)
        return missingAttribute(fixName(name, namespace), path)
    }

    private fun createMissingElementError(element: Element, name: String, namespace: Namespace): E {
        val path = XPathHelper.getAbsolutePath(element)
        return missingElement(fixName(name, namespace), path)
    }

    private fun createMissingTextError(element: Element): E {
        val path = XPathHelper.getAbsolutePath(element)
        return missingText(path)
    }

    private fun fixName(name: String, namespace: Namespace): String =
        namespace.prefix?.ifEmpty { null }?.let { "$it:$name" } ?: name

    protected fun parseReadingDirection(value: String): Result<ReadingDirection, String> =
        ReadingDirection.fromValue(value)

    protected fun parseProperty(value: String): Result<PropertyModel, E> =
        when (val result = propertyParser.parseToEnd(value)) {
            is ParserResult.Ok -> Ok(result.result)
            is ParserResult.Error -> Err(invalidProperty(value, result))
        }

    protected fun parsePropertyList(value: String): Result<PropertyModelList, E> =
        when (val result = propertyListParser.parseToEnd(value)) {
            is ParserResult.Ok -> Ok(result.result)
            is ParserResult.Error -> Err(invalidProperty(value, result))
        }

    protected fun Element.ownText(normalize: Boolean = false): Result<String, E> =
        getOwnText(normalize).toResultOr { createMissingTextError(this) }

    protected fun Element.optionalOwnText(normalize: Boolean = false): String? = getOwnText(normalize)

    protected fun Element.child(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Result<Element, E> =
        getChild(name, namespace).toResultOr { createMissingElementError(this, name, namespace) }

    protected fun Element.optionalChild(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Element? =
        getChild(name, namespace)

    protected fun Element.children(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): List<Element> =
        getChildren(name, namespace)

    protected fun Element.childrenWrapper(
        wrapperName: String,
        childName: String,
        wrapperNamespace: Namespace = Namespace.NO_NAMESPACE,
        childNamespace: Namespace = wrapperNamespace,
    ): Result<List<Element>, E> = child(wrapperName, wrapperNamespace).map { it.children(childName, childNamespace) }

    protected fun Element.optionalChildrenWrapper(
        wrapperName: String,
        childName: String,
        wrapperNamespace: Namespace = Namespace.NO_NAMESPACE,
        childNamespace: Namespace = wrapperNamespace,
    ): List<Element>? = optionalChild(wrapperName, wrapperNamespace)?.children(childName, childNamespace)

    protected fun Element.attr(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): Result<String, E> =
        getAttributeValue(name, namespace).toResultOr { createMissingAttributeError(this, name, namespace) }

    protected fun Element.optionalAttr(name: String, namespace: Namespace = Namespace.NO_NAMESPACE): String? =
        getAttributeValue(name, namespace)

    protected fun Element.addChild(child: Element?) {
        if (child != null) {
            addContent(child)
        }
    }

    protected inline fun <T> Element.addChildren(
        children: Iterable<T>,
        mapper: (T) -> Element,
    ) {
        for (child in children) {
            val element = mapper(child)
            addContent(element)
        }
    }

    protected inline fun <T> Element.addChildrenWithWrapper(
        wrapperName: String,
        wrapperNamespace: Namespace = Namespace.NO_NAMESPACE,
        children: Iterable<T>,
        mapper: (T) -> Element,
    ) {
        addElement(wrapperName, wrapperNamespace) {
            for (child in children) {
                val element = mapper(child)
                addContent(element)
            }
        }
    }

    protected inline fun Element.addElement(
        name: String,
        namespace: Namespace? = null,
        builder: Element.() -> Unit,
    ) {
        val element = (namespace?.let { Element(name, it) } ?: Element(name)).apply(builder)
        addContent(element)
    }

    protected operator fun Element.set(name: String, value: String?) {
        if (value == null) {
            removeAttribute(name)
        } else {
            setAttribute(name, value)
        }
    }

    protected operator fun Element.set(name: String, namespace: Namespace, value: String?) {
        if (value == null) {
            removeAttribute(name, namespace)
        } else {
            setAttribute(name, value, namespace)
        }
    }
}