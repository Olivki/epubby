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

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr
import dev.epubby.ReadingDirection
import net.ormr.epubby.internal.util.getOwnText
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.xpath.XPathHelper

internal abstract class ModelXmlSerializer<E> {
    protected abstract fun missingAttribute(name: String, path: String): E

    protected abstract fun missingElement(name: String, path: String): E

    protected abstract fun missingText(path: String): E

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