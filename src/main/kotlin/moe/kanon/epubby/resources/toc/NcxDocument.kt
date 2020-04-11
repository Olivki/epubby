/*
 * Copyright 2019-2020 Oliver Berg
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

package moe.kanon.epubby.resources.toc

import moe.kanon.epubby.Book
import moe.kanon.epubby.internal.Namespaces
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.structs.Direction
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.NonEmptyList
import moe.kanon.epubby.structs.nonEmptyListOf
import moe.kanon.epubby.structs.toNonEmptyList
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.child
import moe.kanon.epubby.utils.docScope
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.epubby.utils.writeTo
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.Locale

/**
 * Handles the serialization/deserialization of the [TableOfContents] class to the
 * [NCX](http://www.daisy.org/z3986/2005/Z3986-2005.html#NCX) file format.
 *
 * [EPUB specification entry](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.4.1)
 *
 * Note that since `EPUB 3.0` a new format is used, but for backwards compatibility sake a `ncx` file should also be
 * generated.
 */
class NcxDocument private constructor(
    var file: Path,
    val version: String, // should be "2005-1"
    var language: Locale? = null,
    var direction: Direction? = null,
    var head: Head,
    var title: DocTitle,
    val authors: MutableList<DocAuthor>,
    var navMap: NavMap,
    var pageList: PageList? = null,
    var navLists: MutableList<NavList> = mutableListOf()
) {
    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        val target = fileSystem.getPath(file.toString())
        logger.debug { "Writing ncx-document to file '$target'.." }
        toDocument().writeTo(target)
    }

    @JvmSynthetic
    internal fun toDocument(): Document = Document(Element("ncx", Namespaces.DAISY_NCX)).docScope {
        setAttribute("version", version)
        language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }
        direction?.also { setAttribute(it.toAttribute()) }

        addContent(title.toElement())
        for (author in authors) addContent(author.toElement())
        addContent(navMap.toElement())
        pageList?.also { addContent(it.toElement()) }
        for (navList in navLists) addContent(navList.toElement())
    }

    /*
    * The list of required metadata provided in http://www.niso.org/workrooms/daisy/Z39-86-2005.html#NavMeta does
    * not apply to EPUB; the only required meta is that which contains a content reference to the OPF unique ID.
    * For backwards compatibility reasons, the value of the name of that meta remains dtb:id.
    */
    class Head(val meta: NonEmptyList<Meta>) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("head", namespace).apply {
                for (meta in meta) addContent(meta.toElement())
            }

        /*
        * The list of required metadata provided in http://www.niso.org/workrooms/daisy/Z39-86-2005.html#NavMeta does
        * not apply to EPUB; the only required meta is that which contains a content reference to the OPF unique ID.
        * For backwards compatibility reasons, the value of the name of that meta remains dtb:id.
        */
        data class Meta(val name: String, var content: String, var scheme: String?) {
            @JvmSynthetic
            internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
                Element("meta", namespace).apply {
                    setAttribute("name", this@Meta.name)
                    setAttribute("content", this@Meta.content)
                    scheme?.also { setAttribute("scheme", it) }
                }
        }
    }

    data class Text(var content: String, var identifier: Identifier? = null, var clazz: String? = null) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("text", namespace).apply {
                identifier?.also { setAttribute(it.toAttribute()) }
                clazz?.also { setAttribute("class", it) }
                text = this@Text.content
            }
    }

    // TODO: narrow 'source' to 'ImageResource'?
    data class Img(var source: URI, var identifier: Identifier? = null, var clazz: String? = null) {
        fun getPath(book: Book): Path = book.packageRoot.resolve(source.path)

        fun toResource(book: Book): Resource = book.resources.getResourceByFile(getPath(book))

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element = Element("img", namespace).apply {
            setAttribute("src", source.toString())
            identifier?.also { setAttribute(it.toAttribute()) }
            clazz?.also { setAttribute("class", it) }
        }
    }

    data class Content(var source: URI, var identifier: Identifier? = null) {
        fun getPath(book: Book): Path = book.packageRoot.resolve(source.path)

        fun toResource(book: Book): Resource = book.resources.getResourceByFile(getPath(book))

        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("content", namespace).apply {
                setAttribute("src", source.toString())
                identifier?.also { setAttribute(it.toAttribute()) }
            }
    }

    data class DocTitle(var text: Text, var image: Img? = null, var identifier: Identifier? = null) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("docTitle", namespace).apply {
                identifier?.also { setAttribute(it.toAttribute()) }
                addContent(this@DocTitle.text.toElement())
                image?.also { addContent(it.toElement()) }
            }
    }

    data class DocAuthor(var text: Text, var image: Img? = null, var identifier: Identifier? = null) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("docAuthor", namespace).apply {
                identifier?.also { setAttribute(it.toAttribute()) }
                addContent(this@DocAuthor.text.toElement())
                image?.also { addContent(it.toElement()) }
            }
    }

    class NavMap(
        val info: MutableList<NavInfo>,
        val labels: MutableList<NavLabel>,
        // this needs to contain AT LEAST one entry
        val points: NonEmptyList<NavPoint>,
        var identifier: Identifier? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("navMap", namespace).apply {
                identifier?.also { setAttribute(it.toAttribute()) }
                for (info in info) addContent(info.toElement())
                for (label in labels) addContent(label.toElement())
                for (point in points) addContent(point.toElement())
            }
    }

    data class NavPoint(
        var identifier: Identifier,
        var content: Content,
        // this needs to contain AT LEAST one entry
        val labels: NonEmptyList<NavLabel>,
        var clazz: String? = null,
        // the 'playOrder' attribute is not required for the NCX document used for EPUBs
        var playOrder: Int? = null,
        val children: MutableList<NavPoint>
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("navPoint", namespace).apply {
                setAttribute(identifier.toAttribute())
                clazz?.also { setAttribute("class", it) }
                playOrder?.also { setAttribute("playOrder", it.toString()) }
                for (label in labels) addContent(label.toElement())
                addContent(this@NavPoint.content.toElement())
                // "<navPoint>s may be nested to represent the hierarchical structure of a document"
                for (point in this@NavPoint.children) addContent(point.toElement())
            }
    }

    data class NavLabel(
        var text: Text,
        var image: Img? = null,
        var language: Locale? = null,
        var direction: Direction? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("navLabel", namespace).apply {
                language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }
                direction?.also { setAttribute(it.toAttribute()) }
                addContent(this@NavLabel.text.toElement())
                image?.also { addContent(it.toElement()) }
            }
    }

    // valid inside of NavMap, PageList and NavList
    data class NavInfo(
        var text: Text,
        var image: Img? = null,
        var language: Locale? = null,
        var direction: Direction? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("navInfo", namespace).apply {
                language?.also { setAttribute("lang", it.toLanguageTag(), Namespace.XML_NAMESPACE) }
                direction?.also { setAttribute(it.toAttribute()) }
                addContent(this@NavInfo.text.toElement())
                image?.also { addContent(it.toElement()) }
            }
    }

    // valid inside NcxDocument
    class PageList(
        val info: MutableList<NavInfo>,
        val labels: MutableList<NavLabel>,
        // this needs to contain AT LEAST one entry
        val targets: NonEmptyList<PageTarget>,
        var identifier: Identifier? = null,
        var clazz: String? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("pageList", namespace).apply {
                identifier?.also { setAttribute(it.toAttribute()) }
                clazz?.also { setAttribute("class", it) }
                for (info in info) addContent(info.toElement())
                for (label in labels) addContent(label.toElement())
                for (target in targets) addContent(target.toElement())
            }
    }

    // valid inside PageList
    data class PageTarget(
        var identifier: Identifier,
        var type: Type,
        // this needs to contain AT LEAST one entry
        val labels: NonEmptyList<NavLabel>,
        val content: Content,
        var value: Int? = null,
        var clazz: String? = null,
        // the 'playOrder' attribute is not required for the NCX document used for EPUBs
        var playOrder: Int? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("pageTarget", namespace).apply {
                setAttribute(identifier.toAttribute())
                this@PageTarget.value?.also { setAttribute("value", it.toString()) }
                setAttribute("type", type.attributeName)
                clazz?.also { setAttribute("class", it) }
                playOrder?.also { setAttribute("playOrder", it.toString()) }
                for (label in labels) addContent(label.toElement())
                addContent(this@PageTarget.content.toElement())
            }

        enum class Type(val attributeName: String) {
            FRONT("front"),
            NORMAL("normal"),
            SPECIAL("special");

            companion object {
                @JvmStatic
                fun of(name: String): Type =
                    values().firstOrNull { it.attributeName == name }
                        ?: throw NoSuchElementException("No type found with the name '$name'")
            }
        }
    }

    // valid inside NcxDocument
    class NavList(
        val info: MutableList<NavInfo>,
        // this needs to contain AT LEAST one entry
        val labels: NonEmptyList<NavLabel>,
        // this needs to contain AT LEAST one entry
        val targets: NonEmptyList<NavTarget>,
        var identifier: Identifier? = null,
        var clazz: String? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("navList", namespace).apply {
                identifier?.also { setAttribute(it.toAttribute()) }
                clazz?.also { setAttribute("class", it) }
                for (info in info) addContent(info.toElement())
                for (label in labels) addContent(label.toElement())
                for (target in targets) addContent(target.toElement())
            }
    }

    // valid inside NavList
    data class NavTarget(
        val identifier: Identifier,
        // this needs to contain AT LEAST one entry
        val labels: NonEmptyList<NavLabel>,
        val content: Content,
        var value: Int? = null,
        var clazz: String? = null,
        // the 'playOrder' attribute is not required for the NCX document used for EPUBs
        var playOrder: Int? = null
    ) {
        @JvmSynthetic
        internal fun toElement(namespace: Namespace = Namespaces.DAISY_NCX): Element =
            Element("navTarget", namespace).apply {
                setAttribute(identifier.toAttribute())
                this@NavTarget.value?.also { setAttribute("value", it.toString()) }
                clazz?.also { setAttribute("class", it) }
                playOrder?.also { setAttribute("playOrder", it.toString()) }
                for (label in labels) addContent(label.toElement())
                addContent(this@NavTarget.content.toElement())
            }
    }

    internal companion object {
        @JvmSynthetic
        internal fun create(book: Book, file: Path, points: NonEmptyList<NavPoint>): NcxDocument {
            val head = Head(nonEmptyListOf(Head.Meta("dtb:id", book.packageDocument.uniqueIdentifier.toString(), null)))
            val title = DocTitle(Text(book.title))
            val authors = book.metadata.authors.mapTo(mutableListOf()) { DocAuthor(Text(it.content)) }
            val navMap = NavMap(mutableListOf(), mutableListOf(), points)
            return NcxDocument(file, "2005-1", head = head, title = title, authors = authors, navMap = navMap)
        }

        @JvmSynthetic
        internal fun fromFile(epub: Path, file: Path): NcxDocument = parseXmlFile(file) { _, root ->
            val namespace = root.namespace

            val version = root.attr("version", epub, file)
            val language = root.getAttributeValue("lang", Namespace.XML_NAMESPACE)?.let(Locale::forLanguageTag)
            val direction = root.getAttributeValue("dir")?.let(Direction.Companion::of)

            val head = createHead(root.child("head", epub, file), epub, file)
            val title = createDocTitle(root.child("docTitle", epub, file), epub, file)
            val authors = root.getChildren("docAuthor", namespace)
                .asSequence()
                .map { createDocAuthor(it, epub, file) }
                .filterNotNullTo(ArrayList())
            val navMap = createNavMap(root.child("navMap", epub, file), epub, file)
            val pageList = root.getChild("pageList", namespace)?.let { createPageList(it, epub, file) }
            val navList = root.getChildren("navList", namespace)
                .asSequence()
                .map { createNavList(it, epub, file) }
                .filterNotNullTo(ArrayList())

            return NcxDocument(file, version, language, direction, head, title, authors, navMap, pageList, navList)
        }

        private fun createHead(element: Element, epub: Path, container: Path): Head {
            val meta = element.getChildren("meta", element.namespace)
                .asSequence()
                .map { createHeadMeta(it, epub, container) }
                .filterNotNull()
                .ifEmpty { malformed(epub, container, "ncx-document 'head' needs to contain at least one entry") }
                .toNonEmptyList()
            return Head(meta)
        }

        private fun createHeadMeta(element: Element, epub: Path, container: Path): Head.Meta {
            val name = element.attr("name", epub, container)
            val content = element.attr("content", epub, container)
            val scheme = element.getAttributeValue("scheme")
            return Head.Meta(name, content, scheme)
        }

        private fun createText(element: Element): Text {
            val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
            val clazz = element.getAttributeValue("class")
            val content = element.textNormalize
            return Text(content, identifier, clazz)
        }

        private fun createImg(element: Element, epub: Path, container: Path): Img {
            val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
            val clazz = element.getAttributeValue("class")
            val source = element.attr("src", epub, container).let(::URI)
            return Img(source, identifier, clazz)
        }

        private fun createContent(element: Element, epub: Path, container: Path): Content {
            val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
            val source = URI(element.attr("src", epub, container))
            return Content(source, identifier)
        }

        private fun createDocTitle(element: Element, epub: Path, container: Path): DocTitle {
            val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
            val text = createText(element.child("text", epub, container))
            val image = element.getChild("img", element.namespace)?.let { createImg(it, epub, container) }
            return DocTitle(text, image, identifier)
        }

        private fun createDocAuthor(element: Element, epub: Path, container: Path): DocAuthor {
            val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
            val text = createText(element.child("text", epub, container))
            val image = element.getChild("img", element.namespace)?.let { createImg(it, epub, container) }
            return DocAuthor(text, image, identifier)
        }

        private fun createNavMap(element: Element, epub: Path, container: Path): NavMap {
            val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
            val info = element
                .getChildren("navInfo", element.namespace)
                .asSequence()
                .map { createNavInfo(it, epub, container) }
                .filterNotNullTo(ArrayList())
            val labels = element
                .getChildren("navLabel", element.namespace)
                .asSequence()
                .map { createNavLabel(it, epub, container) }
                .filterNotNullTo(ArrayList())
            val points = element
                .getChildren("navPoint", element.namespace)
                .asSequence()
                .map { createNavPoint(it, epub, container) }
                .filterNotNull()
                .ifEmpty { malformed(epub, container, "ncx-document 'navMap' needs to contain at least one nav-point") }
                .toNonEmptyList()
            return NavMap(info, labels, points, identifier)
        }

        private fun createNavPoint(element: Element, epub: Path, container: Path): NavPoint {
            val identifier = Identifier.fromElement(element, epub, container)
            val clazz = element.getAttributeValue("class")
            val playOrder = element.getAttributeValue("playOrder")?.toInt()
            val labels = element
                .getChildren("navLabel", element.namespace)
                .asSequence()
                .map { createNavLabel(it, epub, container) }
                .filterNotNull()
                .ifEmpty { malformed(epub, container, "nav-point needs to have at least one nav-label") }
                .toNonEmptyList()
            val content = createContent(element.child("content", epub, container), epub, container)
            val points = element
                .getChildren("navPoint", element.namespace)
                .asSequence()
                .map { createNavPoint(it, epub, container) }
                .filterNotNullTo(ArrayList())
            return NavPoint(identifier, content, labels, clazz, playOrder, points)
        }

        private fun createNavLabel(element: Element, epub: Path, container: Path): NavLabel {
            val language = element.getAttributeValue("lang", Namespace.XML_NAMESPACE)?.let(Locale::forLanguageTag)
            val direction = element.getAttributeValue("dir")?.let(Direction.Companion::of)
            val text = createText(element.child("text", epub, container))
            val image = element.getChild("img", element.namespace)?.let { createImg(it, epub, container) }
            return NavLabel(text, image, language, direction)
        }

        private fun createNavInfo(element: Element, epub: Path, container: Path): NavInfo {
            val language = element.getAttributeValue("lang", Namespace.XML_NAMESPACE)?.let(Locale::forLanguageTag)
            val direction = element.getAttributeValue("dir")?.let(Direction.Companion::of)
            val text = createText(element.child("text", epub, container))
            val image = element.getChild("img", element.namespace)?.let { createImg(it, epub, container) }
            return NavInfo(text, image, language, direction)
        }

        private fun createPageList(element: Element, epub: Path, container: Path): PageList {
            val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
            val clazz = element.getAttributeValue("class")
            val info = element
                .getChildren("navInfo", element.namespace)
                .asSequence()
                .map { createNavInfo(it, epub, container) }
                .filterNotNullTo(ArrayList())
            val labels = element
                .getChildren("navLabel", element.namespace)
                .asSequence()
                .map { createNavLabel(it, epub, container) }
                .filterNotNullTo(ArrayList())
            val targets = element
                .getChildren("pageTarget", element.namespace)
                .asSequence()
                .map { createPageTarget(it, epub, container) }
                .filterNotNull()
                .ifEmpty {
                    malformed(epub, container, "ncx-document 'pageList' needs to contain at least one page-target")
                }
                .toNonEmptyList()
            return PageList(info, labels, targets, identifier, clazz)
        }

        private fun createPageTarget(element: Element, epub: Path, container: Path): PageTarget {
            val identifier = Identifier.fromElement(element, epub, container)
            val value = element.getAttributeValue("value")?.toInt()
            val type = PageTarget.Type.of(element.attr("type", epub, container))
            val clazz = element.getAttributeValue("class")
            val playOrder = element.getAttributeValue("playOrder")?.toInt()
            val labels = element
                .getChildren("navLabel", element.namespace)
                .asSequence()
                .map { createNavLabel(it, epub, container) }
                .filterNotNull()
                .ifEmpty { malformed(epub, container, "page-target needs to have at least one nav-label") }
                .toNonEmptyList()
            val content = createContent(element.child("content", epub, container), epub, container)
            return PageTarget(identifier, type, labels, content, value, clazz, playOrder)
        }

        private fun createNavList(element: Element, epub: Path, container: Path): NavList {
            val identifier = element.getAttributeValue("id")?.let(Identifier.Companion::of)
            val clazz = element.getAttributeValue("class")
            val info = element
                .getChildren("navInfo", element.namespace)
                .asSequence()
                .map { createNavInfo(it, epub, container) }
                .filterNotNullTo(ArrayList())
            val labels = element
                .getChildren("navLabel", element.namespace)
                .asSequence()
                .map { createNavLabel(it, epub, container) }
                .filterNotNull()
                .ifEmpty { malformed(epub, container, "nav-list needs to contain at least one nav-label") }
                .toNonEmptyList()
            val targets = element
                .getChildren("navTarget", element.namespace)
                .asSequence()
                .map { createNavTarget(it, epub, container) }
                .filterNotNull()
                .ifEmpty { malformed(epub, container, "nav-list needs to contain at least one nav-target") }
                .toNonEmptyList()
            return NavList(info, labels, targets, identifier, clazz)
        }

        private fun createNavTarget(element: Element, epub: Path, container: Path): NavTarget {
            val identifier = Identifier.fromElement(element, epub, container)
            val value = element.getAttributeValue("value")?.toInt()
            val clazz = element.getAttributeValue("class")
            val playOrder = element.getAttributeValue("playOrder")?.toInt()
            val labels = element
                .getChildren("navLabel", element.namespace)
                .asSequence()
                .map { createNavLabel(it, epub, container) }
                .filterNotNull()
                .ifEmpty { malformed(epub, container, "page-target needs to have at least one nav-label") }
                .toNonEmptyList()
            val content = createContent(element.child("content", epub, container), epub, container)
            return NavTarget(identifier, labels, content, value, clazz, playOrder)
        }
    }
}