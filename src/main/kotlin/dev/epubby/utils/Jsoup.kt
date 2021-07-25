/*
 * Copyright 2019-2021 Oliver Berg
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

@file:JvmName("JsoupUtils")
@file:Suppress("NOTHING_TO_INLINE")

package dev.epubby.utils

// TODO: migrate to it's own library?

import kotlinx.html.*
import kotlinx.html.consumers.onFinalize
import kotlinx.html.stream.createHTML
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.parser.Parser
import org.jsoup.parser.Tag
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import org.jsoup.select.NodeFilter
import org.jsoup.select.NodeVisitor
import org.w3c.dom.events.Event
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*
import kotlin.io.path.inputStream
import kotlinx.html.Entities as KEntities
import kotlinx.html.Tag as KTag

// NODE
// TODO: remove these ugly things jesus
val Node.parent: Node?
    @JvmSynthetic
    get() = parent()

val Node.parentNode: Node?
    @JvmSynthetic
    get() = parentNode()

val Node.root: Node
    @JvmSynthetic
    get() = root()

val Node.ownerDocument: Document?
    @JvmSynthetic
    get() = ownerDocument()

val Node.siblingNodes: List<Node>
    @JvmSynthetic
    get() = siblingNodes()

val Node.previousSibling: Node?
    @JvmSynthetic
    get() = previousSibling()

val Node.nextSibling: Node?
    @JvmSynthetic
    get() = nextSibling()

val Node.attributes: Attributes
    @JvmSynthetic
    get() = attributes()

var Node.baseUri: String
    @JvmSynthetic
    get() = baseUri()
    @JvmSynthetic
    set(value) {
        setBaseUri(value)
    }

val Node.nodeName: String
    @JvmSynthetic
    get() = nodeName()

val Node.childNodes: List<Node>
    @JvmSynthetic
    get() = childNodes()

@JvmSynthetic
fun Node.toOuterHtml(): String = outerHtml()

inline fun Node.traverse(
    crossinline head: (node: Node, depth: Int) -> Unit,
    crossinline tail: (node: Node, depth: Int) -> Unit,
): Node = apply {
    traverse(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {
            head(node, depth)
        }

        override fun tail(node: Node, depth: Int) {
            tail(node, depth)
        }
    })
}

typealias NodeFilterResult = NodeFilter.FilterResult

inline fun Node.filter(
    crossinline head: (node: Node, depth: Int) -> NodeFilterResult,
    crossinline tail: (node: Node, depth: Int) -> NodeFilterResult,
): Node = apply {
    filter(object : NodeFilter {
        override fun head(node: Node, depth: Int): NodeFilterResult = head(node, depth)

        override fun tail(node: Node, depth: Int): NodeFilterResult = tail(node, depth)
    })
}

@JvmSynthetic
fun Node.setAttribute(name: String, value: String) {
    attr(name, value)
}

@JvmSynthetic
fun Node.setAttribute(name: String, value: Boolean) {
    attributes[name] = value
}

@JvmSynthetic
fun Node.hasAttribute(name: String): Boolean = hasAttr(name)

@JvmSynthetic
fun Node.removeAttribute(name: String): Boolean = when {
    hasAttribute(name) -> {
        removeAttr(name)
        true
    }
    else -> false
}

/**
 * Returns the value of the attribute with the given [name], or `null` if none can be found.
 *
 * @see [Node.attr]
 */
@JvmSynthetic
fun Node.getAttribute(name: String): String? = when {
    hasAttribute(name) -> attr(name)
    else -> null
}

// ELEMENT
var Element.tagName: String
    @JvmSynthetic
    get() = tagName()
    @JvmSynthetic
    set(value) {
        tagName(value)
    }

val Element.normalName: String
    @JvmSynthetic
    get() = normalName()

val Element.tag: Tag
    @JvmSynthetic
    get() = tag()

val Element.id: String
    @JvmSynthetic
    get() = id()

val Element.dataset: Map<String, String>
    @JvmSynthetic
    get() = dataset()

val Element.parent: Element?
    @JvmSynthetic
    get() = parent()

val Element.parents: Elements
    @JvmSynthetic
    get() = parents()

val Element.children: List<Element>
    @JvmSynthetic
    get() = children()

val Element.textNodes: List<TextNode>
    @JvmSynthetic
    get() = textNodes()

val Element.dataNodes: List<DataNode>
    @JvmSynthetic
    get() = dataNodes()

var Element.text: String
    @JvmSynthetic
    get() = text()
    @JvmSynthetic
    set(value) {
        text(value)
    }

val Element.wholeText: String
    @JvmSynthetic
    get() = wholeText()

val Element.ownText: String
    @JvmSynthetic
    get() = ownText()

val Element.data: String
    @JvmSynthetic
    get() = data()

var Element.className: String
    @JvmSynthetic
    get() = className()
    @JvmSynthetic
    set(value) {
        attributes["class"] = value.trim()
    }

var Element.classNames: Set<String>
    @JvmSynthetic
    get() = classNames()
    @JvmSynthetic
    set(value) {
        classNames(value)
    }

var Element.value: String
    @JvmSynthetic
    get() = `val`()
    @JvmSynthetic
    set(value) {
        `val`(value)
    }

var Element.html: String
    @JvmSynthetic
    get() = html()
    @JvmSynthetic
    set(value) {
        html(value)
    }

@JvmSynthetic
fun Element.filter(cssQuery: String): Elements = select(cssQuery)

fun Element.first(cssQuery: String): Element =
    firstOrNull(cssQuery) ?: throw NoSuchElementException("No element found with css query '$cssQuery'")

@JvmSynthetic
fun Element.firstOrNull(cssQuery: String): Element? = selectFirst(cssQuery)

@JvmSynthetic
fun Element.anyMatch(cssQuery: String): Boolean = `is`(cssQuery)

@JvmSynthetic
fun Element.anyMatch(evaluator: Evaluator): Boolean = `is`(evaluator)

@JvmSynthetic
fun Element.toCssSelector(): String = cssSelector()

/**
 * Returns `true` if `this` element has a `class` attribute defined, otherwise `false`.
 *
 * @see [Element.hasClass]
 */
val Element.isClassDefined: Boolean
    get() = attributes.hasKeyIgnoreCase("class")

/**
 * Returns `true` if `this` element has any children, otherwise `false`.
 */
fun Element.isParent(): Boolean = childNodes.isNotEmpty()

/**
 * Returns `true` if `this` element is the only child of its [parent][Element.parent], otherwise `false`.
 *
 * If the `parent` of `this` elements [own text][Element.ownText] is not blank, then `false` will be returned,
 * as text content is considered to be a child in this context, likewise, if `parent` is `null`, then `false` will also
 * be returned.
 */
fun Element.isOnlyChild(): Boolean = (parent?.children?.size ?: 0) <= 1 && parent?.ownText?.isBlank() ?: false

/**
 * Completely removes the `class` attribute of `this` element.
 *
 * @see [Element.removeClass]
 */
fun Element.removeClass() {
    attributes.removeIgnoreCase("class")
}

/**
 * Completely replaces the `class` attribute of `this` element with the specified [newClass].
 */
fun Element.replaceClass(newClass: String) {
    attributes["class"] = newClass
}

/**
 * Replaces the given [targetClass] with the given [newClass] in the [classNames] of `this` element.
 */
fun Element.replaceClass(targetClass: String, newClass: String) {
    removeClass(targetClass)
    addClass(newClass)
}

/**
 * Returns `true` if `this` element *only* contains the specified [cssQuery], ignoring any elements that match the
 * given [ignoredQuery].
 */
@JvmOverloads
fun Element.onlyContains(
    cssQuery: String,
    ignoredQuery: Array<String> = arrayOf("p", "span", "div", "section", "body"),
): Boolean = allElements
    .asSequence()
    .filter { it.anyMatch(cssQuery) }
    .any { element -> ignoredQuery.any { element.anyMatch(it) } && element.text.isBlank() }

/**
 * Returns `true` if `this` element has a child that matches the given [query], otherwise `false`.
 *
 * Note that `this` element does *not* get checked if it matches `query`, only it's children do.
 */
fun Element.anyChild(query: String): Boolean = children.any { it.anyMatch(query) }

// ELEMENTS
var Elements.value: String
    @JvmSynthetic
    get() = `val`()
    @JvmSynthetic
    set(value) {
        `val`(value)
    }

var Elements.html: String
    @JvmSynthetic
    get() = html()
    @JvmSynthetic
    set(value) {
        html(value)
    }

val Elements.texts: List<String>
    @JvmSynthetic
    get() = eachText()

@JvmSynthetic
fun Elements.toOuterHtml(): String = outerHtml()

@JvmSynthetic
operator fun Elements.get(cssQuery: String): Elements = select(cssQuery)

@JvmSynthetic
fun Elements.filter(cssQuery: String): Elements = select(cssQuery)

@JvmSynthetic
fun Elements.filterNot(cssQuery: String): Elements = not(cssQuery)

fun Elements.first(cssQuery: String): Element =
    firstOrNull(cssQuery) ?: throw NoSuchElementException("No element found with css query '$cssQuery'")

@JvmSynthetic
fun Elements.firstOrNull(cssQuery: String): Element? = filter(cssQuery).firstOrNull()

@JvmSynthetic
fun Elements.anyMatch(cssQuery: String): Boolean = `is`(cssQuery)

@JvmSynthetic
operator fun Elements.contains(cssQuery: String): Boolean = `is`(cssQuery)

@JvmSynthetic
operator fun Elements.inc(): Elements = next()

@JvmSynthetic
operator fun Elements.dec(): Elements = prev()

@JvmSynthetic
inline fun Elements.prepend(block: TagConsumer<Element>.() -> Unit): Elements = apply {
    for (ogElement in this) {
        ElementDomBuilder(ogElement.ownerDocumentSafe).onFinalize { element, partial ->
            if (!partial) {
                ogElement.prependChild(element)
            }
        }.apply(block)
    }
}

@JvmSynthetic
inline fun Elements.append(block: TagConsumer<Element>.() -> Unit): Elements = apply {
    for (ogElement in this) {
        ElementDomBuilder(ogElement.ownerDocumentSafe).onFinalize { element, partial ->
            if (!partial) {
                ogElement.appendChild(element)
            }
        }.apply(block)
    }
}

@JvmSynthetic
inline fun Elements.before(block: TagConsumer<Element>.() -> Unit): Elements = apply {
    for (ogElement in this) {
        ElementDomBuilder(ogElement.ownerDocumentSafe).onFinalize { element, partial ->
            if (!partial) {
                ogElement.before(element)
            }
        }.apply(block)
    }
}

@JvmSynthetic
inline fun Elements.after(block: TagConsumer<Element>.() -> Unit): Elements = apply {
    for (ogElement in this) {
        ElementDomBuilder(ogElement.ownerDocumentSafe).onFinalize { element, partial ->
            if (!partial) {
                ogElement.after(element)
            }
        }.apply(block)
    }
}

@JvmSynthetic
inline fun Elements.wrap(block: TagConsumer<Element>.() -> Unit): Elements = apply {
    for (ogElement in this) {
        ElementDomBuilder(ogElement.ownerDocumentSafe).onFinalize { element, partial ->
            if (!partial) {
                ogElement.wrap(element.toOuterHtml())
            }
        }.apply(block)
    }
}

/**
 * Completely removes the `class` attribute of all the elements contained in `this`.
 */
fun Elements.removeClasses() {
    for (element in this) {
        element.removeClass()
    }
}

/**
 * Completely replaces the `class` attribute of `this` element with the specified [newClass].
 */
fun Elements.replaceClass(newClass: String) {
    for (element in this) {
        element.replaceClass(newClass)
    }
}

/**
 * Replaces the given [targetClass] with the given [newClass] in the [classNames] of `this` element.
 */
fun Elements.replaceClass(targetClass: String, newClass: String) {
    for (element in this) {
        element.replaceClass(targetClass, newClass)
    }
}

// DOCUMENT
var Document.title: String
    @JvmSynthetic
    get() = title()
    @JvmSynthetic
    set(value) {
        title(value)
    }

var Document.charset: Charset
    @JvmSynthetic
    get() = charset()
    @JvmSynthetic
    set(value) {
        charset(value)
    }

var Document.outputSettings: Document.OutputSettings
    @JvmSynthetic
    get() = outputSettings()
    @JvmSynthetic
    set(value) {
        outputSettings(value)
    }

var Document.parser: Parser
    @JvmSynthetic
    get() = parser()
    @JvmSynthetic
    set(value) {
        parser(value)
    }

typealias QuirksMode = Document.QuirksMode

var Document.quirksMode: QuirksMode
    @JvmSynthetic
    get() = quirksMode()
    @JvmSynthetic
    set(value) {
        quirksMode(value)
    }

val Document.location: String
    @JvmSynthetic
    get() = location()

val Document.head: Element
    @JvmSynthetic
    get() = head() ?: throw IllegalStateException("'head' should not be null")

val Document.body: Element
    @JvmSynthetic
    get() = body() ?: throw IllegalStateException("'body' should not be null")


// TODO: Move all the stuff below into a separate library

@JvmSynthetic
@HtmlTagMarker
inline fun buildDocument(namespace: String? = null, crossinline body: HTML.() -> Unit): Document {
    val content = createHTML(prettyPrint = false, xhtmlCompatible = true).html(namespace) { apply(body) }
    return Jsoup.parse(content)
}

// adapted from the 'HTMLDOMBuilder' from 'kotlinx.html'
@PublishedApi
internal class ElementDomBuilder(val document: Document) : TagConsumer<Element> {
    private val elements: Deque<Element> = LinkedList()
    private var lastElement: Element? = null

    override fun onTagStart(tag: KTag) {
        val element = document.createElement(tag.tagName)

        for ((key, value) in tag.attributesEntries) {
            element.attributes[key] = value
        }

        if (elements.isNotEmpty()) {
            elements.last.appendChild(element)
        }

        elements.add(element)
    }

    override fun onTagAttributeChange(tag: KTag, attribute: String, value: String?) {
        if (elements.isEmpty()) {
            throw IllegalStateException("No current tag")
        }

        val element = elements.peekLast()

        if (element != null && value != null) {
            element.attributes[attribute] = value
        } else {
            element.attributes -= attribute
        }
    }

    override fun onTagEvent(tag: KTag, event: String, value: (Event) -> Unit) {
        throw UnsupportedOperationException()
    }

    override fun onTagEnd(tag: KTag) {
        if (elements.isEmpty() || elements.last().tagName.lowercase() != tag.tagName.lowercase()) {
            throw IllegalStateException("Tag '${tag.tagName}' has not been entered, but an attempt to leave it was made.")
        }

        val element = elements.removeLast()
        lastElement = element
    }

    override fun onTagContent(content: CharSequence) {
        if (elements.isEmpty()) {
            throw IllegalStateException("No current element available")
        }

        elements.last.appendText(content.toString())
    }

    override fun onTagComment(content: CharSequence) {
        if (elements.isEmpty()) {
            throw IllegalStateException("No current element available")
        }

        elements.last.appendChild(Comment(content.toString()))
    }

    override fun onTagContentEntity(entity: KEntities) {
        if (elements.isEmpty()) {
            throw IllegalStateException("No current element available")
        }

        elements.last.appendText(Entities.getByName(entity.name))
    }

    override fun onTagContentUnsafe(block: Unsafe.() -> Unit) {
        unsafe.apply(block)
    }

    private val unsafe: Unsafe = object : Unsafe {
        override operator fun String.unaryPlus() {
            val last = elements.last()
            last.append(this)
        }
    }

    override fun finalize(): Element = lastElement ?: throw IllegalStateException("No elements were created")
}

@PublishedApi
internal val Element.ownerDocumentSafe: Document
    get() = when (this) {
        is Document -> this
        else -> ownerDocument ?: throw IllegalStateException("'$this' does not belong to a document")
    }

@JvmSynthetic
inline fun <T : Element> T.prepend(block: TagConsumer<Element>.() -> Unit): T = apply {
    ElementDomBuilder(ownerDocumentSafe).onFinalize { element, partial ->
        if (!partial) {
            prependChild(element)
        }
    }.apply(block)
}

@JvmSynthetic
inline fun <T : Element> T.append(block: TagConsumer<Element>.() -> Unit): T = apply {
    ElementDomBuilder(ownerDocumentSafe).onFinalize { element, partial ->
        if (!partial) {
            appendChild(element)
        }
    }.apply(block)
}

@JvmSynthetic
inline fun <T : Element> T.before(block: TagConsumer<Element>.() -> Unit): T = apply {
    ElementDomBuilder(ownerDocumentSafe).onFinalize { element, partial ->
        if (!partial) {
            before(element)
        }
    }.apply(block)
}

@JvmSynthetic
inline fun <T : Element> T.after(block: TagConsumer<Element>.() -> Unit): T = apply {
    ElementDomBuilder(ownerDocumentSafe).onFinalize { element, partial ->
        if (!partial) {
            after(element)
        }
    }.apply(block)
}

@JvmSynthetic
inline fun <T : Element> T.wrap(block: TagConsumer<Element>.() -> Unit): T = apply {
    ElementDomBuilder(ownerDocumentSafe).onFinalize { element, partial ->
        if (!partial) {
            wrap(element.toOuterHtml())
        }
    }.apply(block)
}

// ATTRIBUTES
val Attributes.size: Int
    @JvmSynthetic
    get() = size()

@JvmSynthetic
internal fun Attributes.isEmpty(): Boolean = size <= 0

@JvmSynthetic
internal fun Attributes.isNotEmpty(): Boolean = size > 0

@JvmSynthetic
operator fun Attributes.set(key: String, value: String) {
    put(key.lowercase(), value)
}

@JvmSynthetic
operator fun Attributes.set(key: String, value: Boolean) {
    put(key.lowercase(), value)
}

/**
 * Returns the value stored by attribute with the given [key], or `null` if none can be found, or if the found
 * attribute is a [BooleanAttribute].
 */
fun Attributes.getOrNull(key: String): String? = when (key) {
    in this -> getIgnoreCase(key)
    else -> null
}

@JvmSynthetic
operator fun Attributes.plusAssign(attribute: Attribute) {
    put(attribute)
}

@JvmSynthetic
operator fun Attributes.minusAssign(key: String) {
    remove(key)
}

@JvmSynthetic
operator fun Attributes.contains(key: String): Boolean = hasKeyIgnoreCase(key)

@JvmSynthetic
fun Attributes.toHtml(): String = html()

/**
 * Adds all the entries stored in the given [attributes] to this collection.
 */
fun Attributes.addAll(attributes: Iterable<Attribute>) {
    for (attribute in attributes) {
        this += attribute
    }
}

/**
 * Removes all the entries of this collection.
 */
fun Attributes.clear() {
    val iterator = iterator()

    while (iterator.hasNext()) {
        iterator.next()
        iterator.remove()
    }
}

private val DEFAULT_PARSER: Parser = Parser.xmlParser()

// TODO: make use of this yo
internal fun documentShell(baseUri: String, isEpub3: Boolean): Document = Document.createShell(baseUri).also {
    setupSettings(it)
    it.prepend("""<!DOCTYPE html>""")
    it.prepend("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    val htmlTag = it.getElementsByTag("html").first()
    htmlTag.setAttribute("xmlns", "http://www.w3.org/1999/xhtml")
    if (isEpub3) htmlTag.setAttribute("xmlns:epub", "http://www.idpf.org/2007/ops")
}

internal fun documentFrom(file: Path, parser: Parser = DEFAULT_PARSER): Document =
    file.inputStream().use { Jsoup.parse(it, "UTF-8", file.toUri().toString(), parser) }

internal fun documentFrom(html: String, baseUri: String, parser: Parser = DEFAULT_PARSER): Document =
    Jsoup.parse(html, baseUri, parser)

internal fun setupSettings(document: Document): Document = document.apply {
    document.outputSettings().apply {
        prettyPrint(true)
        syntax(Document.OutputSettings.Syntax.xml)
        escapeMode(Entities.EscapeMode.xhtml)
        indentAmount(2)
    }
    parser(DEFAULT_PARSER)
}