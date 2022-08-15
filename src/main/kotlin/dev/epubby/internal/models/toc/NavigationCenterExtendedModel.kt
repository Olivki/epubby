/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.internal.models.toc

import arrow.core.Either
import arrow.core.left
import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Epub
import dev.epubby.ParseMode
import dev.epubby.files.RegularFile
import dev.epubby.internal.Namespaces.DAISY_NCX
import dev.epubby.internal.models.SerializedName
import dev.epubby.internal.utils.*
import dev.epubby.resources.PageResource
import dev.epubby.toc.TableOfContents
import dev.epubby.toc.TableOfContents.Entry
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace.XML_NAMESPACE
import java.nio.file.Path

internal data class NavigationCenterExtendedModel internal constructor(
    private val file: Path,
    internal val version: String, // should always be "2005-1"
    internal val language: String?,
    internal val direction: String?,
    internal val head: HeadModel,
    internal val title: DocTitleModel,
    internal val authors: PersistentList<DocAuthorModel>,
    internal val navMap: NavMapModel,
    internal val pageList: PageListModel?,
    internal val navLists: PersistentList<NavListModel>,
) {
    internal fun toDocument(): Document = documentOf("ncx", DAISY_NCX) { _, root ->
        root.setAttribute("version", version)
        if (language != null) root.setAttribute("lang", language, XML_NAMESPACE)
        if (direction != null) root.setAttribute("dir", direction)

        root.addContent(title.toElement())

        for (author in authors) {
            root.addContent(author.toElement())
        }

        root.addContent(navMap.toElement())

        if (pageList != null) root.addContent(pageList.toElement())

        for (navList in navLists) {
            root.addContent(navList.toElement())
        }
    }

    internal fun writeToFile() {
        toDocument().writeTo(file)
    }

    internal fun toTableOfContents(epub: Epub, mode: ParseMode): TableOfContents {
        val tableOfContents = TableOfContents(epub)
        val entries = navMap.points
            .asSequence()
            .map { it.toEntry(epub, tableOfContents, mode = mode) }
            .flatMapFailure()
            .filterFailure(LOGGER, mode)
            .toList()

        tableOfContents.entries.addAll(entries)

        return tableOfContents
    }

    /*
    * The list of required metadata provided in http://www.niso.org/workrooms/daisy/Z39-86-2005.html#NavMeta does
    * not apply to EPUB; the only required meta is that which contains a content reference to the OPF unique ID.
    * For backwards compatibility reasons, the value of the name of that meta remains dtb:id.
    */
    @SerializedName("head")
    internal data class HeadModel internal constructor(
        @SerializedName("meta")
        internal val metaEntries: PersistentList<MetaModel> = persistentListOf(),
    ) {
        internal fun toElement(): Element = elementOf("head", DAISY_NCX) {
            for (meta in metaEntries) {
                it.addContent(meta.toElement())
            }
        }

        /*
        * The list of required metadata provided in http://www.niso.org/workrooms/daisy/Z39-86-2005.html#NavMeta does
        * not apply to EPUB; the only required meta is that which contains a content reference to the OPF unique ID.
        * For backwards compatibility reasons, the value of the name of that meta remains dtb:id.
        */
        @SerializedName("meta")
        internal data class MetaModel internal constructor(
            internal val name: String,
            internal val content: String,
            internal val scheme: String? = null,
        ) {
            internal fun toElement(): Element = elementOf("meta", DAISY_NCX) {
                it.setAttribute("name", name)
                it.setAttribute("content", content)
                if (scheme != null) it.setAttribute("scheme", scheme)
            }

            internal companion object {
                internal fun fromElement(element: Element): Try<MetaModel> {
                    val name = element.getAttributeValue("name") ?: return missingAttribute(element, "name")
                    val content = element.getAttributeValue("content") ?: return missingAttribute(element, "content")
                    val scheme: String? = element.getAttributeValue("scheme")

                    return Either.Right(MetaModel(name, content, scheme))
                }
            }
        }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(HeadModel::class)

            internal fun fromElement(element: Element, mode: ParseMode): Try<HeadModel> {
                val metaEntries = element.getChildren("meta", element.namespace)
                    .asSequence()
                    .map { MetaModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                    .ifEmpty { return missingChild(element, "meta") }

                return Either.Right(HeadModel(metaEntries))
            }
        }
    }

    @SerializedName("text")
    internal data class TextModel internal constructor(
        internal val content: String,
        @SerializedName("id")
        internal val identifier: String? = null,
        @SerializedName("class")
        internal val clazz: String? = null,
    ) {
        internal fun toElement(): Element = elementOf("text", DAISY_NCX) {
            if (identifier != null) it.setAttribute("id", identifier)
            if (clazz != null) it.setAttribute("class", clazz)

            it.text = content
        }

        internal companion object {
            internal fun fromElement(element: Element): TextModel {
                val content = element.textNormalize
                val identifier: String? = element.getAttributeValue("id")
                val clazz: String? = element.getAttributeValue("class")

                return TextModel(content, identifier, clazz)
            }
        }
    }

    @SerializedName("img")
    internal data class ImageModel internal constructor(
        @SerializedName("src")
        internal val source: String,
        @SerializedName("id")
        internal val identifier: String? = null,
        @SerializedName("class")
        internal val clazz: String? = null,
    ) {
        internal fun toElement(): Element = elementOf("img", DAISY_NCX) {
            it.setAttribute("src", source)
            if (identifier != null) it.setAttribute("id", identifier)
            if (clazz != null) it.setAttribute("class", clazz)
        }

        internal companion object {
            internal fun fromElement(element: Element): Try<ImageModel> {
                val source = element.getAttributeValue("src") ?: return missingAttribute(element, "src")
                val identifier: String? = element.getAttributeValue("id")
                val clazz: String? = element.getAttributeValue("class")

                return Either.Right(ImageModel(source, identifier, clazz))
            }
        }
    }

    @SerializedName("content")
    internal data class ContentModel internal constructor(
        @SerializedName("src")
        internal val source: String,
        @SerializedName("id")
        internal val identifier: String? = null,
    ) {
        internal fun toElement(): Element = elementOf("content", DAISY_NCX) {
            it.setAttribute("src", source)
            if (identifier != null) it.setAttribute("id", identifier)
        }

        internal companion object {
            internal fun fromElement(element: Element): Try<ContentModel> {
                val source = element.getAttributeValue("src") ?: return missingAttribute(element, "src")
                val identifier: String? = element.getAttributeValue("id")

                return Either.Right(ContentModel(source, identifier))
            }
        }
    }

    @SerializedName("docTitle")
    internal data class DocTitleModel internal constructor(
        @SerializedName("id")
        internal val identifier: String? = null,
        internal val text: TextModel,
        @SerializedName("img")
        internal val image: ImageModel? = null,
    ) {
        internal fun toElement(): Element = elementOf("docTitle", DAISY_NCX) {
            if (identifier != null) it.setAttribute("id", identifier)

            it.addContent(text.toElement())

            if (image != null) it.addContent(image.toElement())
        }

        internal companion object {
            internal fun fromElement(element: Element): Try<DocTitleModel> {
                val identifier = element.getAttributeValue("id")
                val text = element.getChild("text", element.namespace)?.let {
                    TextModel.fromElement(it)
                } ?: return missingChild(element, "text")
                val image = element.getChild("img", element.namespace)?.let {
                    ImageModel.fromElement(it).fold({ e -> return e.left() }, ::self)
                }

                return Either.Right(DocTitleModel(identifier, text, image))
            }
        }
    }

    @SerializedName("docAuthor")
    internal data class DocAuthorModel internal constructor(
        @SerializedName("id")
        internal val identifier: String? = null,
        internal val text: TextModel,
        @SerializedName("img")
        internal val image: ImageModel? = null,
    ) {
        internal fun toElement(): Element = elementOf("docAuthor", DAISY_NCX) {
            if (identifier != null) it.setAttribute("id", identifier)

            it.addContent(text.toElement())

            if (image != null) it.addContent(image.toElement())
        }

        internal companion object {
            internal fun fromElement(element: Element): Try<DocAuthorModel> {
                val identifier: String? = element.getAttributeValue("id")
                val text = element.getChild("text", element.namespace)?.let {
                    TextModel.fromElement(it)
                } ?: return missingChild(element, "text")
                val image = element.getChild("img", element.namespace)?.let {
                    ImageModel.fromElement(it).fold({ e -> return e.left() }, ::self)
                }

                return Either.Right(DocAuthorModel(identifier, text, image))
            }
        }
    }

    @SerializedName("navMap")
    internal data class NavMapModel internal constructor(
        @SerializedName("id")
        internal val identifier: String? = null,
        @SerializedName("navInfo")
        internal val infoEntries: PersistentList<NavInfoModel> = persistentListOf(),
        @SerializedName("navLabel")
        internal val labels: PersistentList<NavLabelModel> = persistentListOf(),
        // this needs to contain AT LEAST one entry
        @SerializedName("navPoint")
        internal val points: PersistentList<NavPointModel>,
    ) {
        internal fun toElement(): Element = elementOf("navMap", DAISY_NCX) {
            if (identifier != null) it.setAttribute("id", identifier)

            for (info in infoEntries) {
                it.addContent(info.toElement())
            }

            for (label in labels) {
                it.addContent(label.toElement())
            }

            for (point in points) {
                it.addContent(point.toElement())
            }
        }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(NavMapModel::class)

            internal fun fromElement(element: Element, mode: ParseMode): Try<NavMapModel> {
                val identifier: String? = element.getAttributeValue("id")
                val infoEntries = element.getChildren("navInfo", element.namespace)
                    .asSequence()
                    .map { NavInfoModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                val labels = element.getChildren("navLabel", element.namespace)
                    .asSequence()
                    .map { NavLabelModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                val points = element.getChildren("navPoint", element.namespace)
                    .asSequence()
                    .map { NavPointModel.fromElement(it, mode) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                    .ifEmpty { return missingChild(element, "navPoint") }

                return Either.Right(NavMapModel(identifier, infoEntries, labels, points))
            }
        }
    }

    @SerializedName("navPoint")
    internal data class NavPointModel internal constructor(
        @SerializedName("id")
        internal val identifier: String,
        @SerializedName("class")
        internal val clazz: String? = null,
        // the 'playOrder' attribute is not required for the NCX document used for EPUBs
        internal val playOrder: Int? = null,
        // this needs to contain AT LEAST one entry
        @SerializedName("navLabel")
        internal val labels: PersistentList<NavLabelModel>,
        internal val content: ContentModel,
        @SerializedName("navPoint")
        internal val children: PersistentList<NavPointModel> = persistentListOf(),
    ) {
        internal fun toElement(): Element = elementOf("navPoint", DAISY_NCX) {
            it.setAttribute("id", identifier)
            if (clazz != null) it.setAttribute("class", clazz)
            if (playOrder != null) it.setAttribute("playOrder", playOrder.toString())

            for (label in labels) {
                it.addContent(label.toElement())
            }

            it.addContent(content.toElement())

            // "<navPoint>s may be nested to represent the hierarchical structure of a document"
            for (point in children) {
                it.addContent(point.toElement())
            }
        }

        internal fun toEntry(
            epub: Epub,
            container: TableOfContents,
            parent: Entry? = null,
            mode: ParseMode,
        ): Try<Entry> {
            val title = labels.first().text.content
            val resource = getResource(epub).fold({ return it.left() }, ::self)
            val fragment = run {
                val source = content.source
                if ('#' in source) source.substringAfterLast('#') else null
            }
            val entry = Entry(container, parent, resource, title, identifier, fragment)
            val children = this.children.asSequence()
                .map { it.toEntry(epub, container, entry, mode) }
                .flatMapFailure()
                .filterFailure(LOGGER, mode)
                .toPersistentList()

            entry.children.addAll(children)

            return Either.Right(entry)
        }

        private fun getResource(epub: Epub): Try<PageResource> {
            // TODO: will this always be sound?
            val fixedSource = content.source.substringBeforeLast('#')
            val file = when (val temp = epub.opfDirectory.resolve(fixedSource)) {
                is RegularFile -> temp
                else -> return malformedFailure("'content/source' ($fixedSource) of nav-point '$identifier' points towards a non existent file.")
            }
            val resource = file.resource
                ?: return malformedFailure("'content/source' ($fixedSource) of nav-point '$identifier' points towards a non resource file.")

            return when (resource) {
                is PageResource -> Either.Right(resource)
                else -> malformedFailure("'content/source' ($fixedSource) of nav-point '$identifier' points towards a non page resource file.")
            }
        }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(NavPointModel::class)

            internal fun fromElement(element: Element, mode: ParseMode): Try<NavPointModel> {
                val identifier = element.getAttributeValue("id") ?: return missingAttribute(element, "id")
                val clazz: String? = element.getAttributeValue("class")
                val playOrder = element.getAttributeValue("playOrder")?.toInt()
                val labels = element.getChildren("navLabel", element.namespace)
                    .asSequence()
                    .map { NavLabelModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                    .ifEmpty { return missingChild(element, "navLabel") }
                val content = element.getChild("content", element.namespace)?.let {
                    ContentModel.fromElement(it).fold({ e -> return e.left() }, ::self)
                } ?: return missingChild(element, "content")
                val children = element.getChildren("navPoint", element.namespace)
                    .asSequence()
                    .map { fromElement(it, mode) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()

                return Either.Right(NavPointModel(identifier, clazz, playOrder, labels, content, children))
            }

            internal fun fromEntry(entry: Entry): NavPointModel {
                val title = persistentListOf(NavLabelModel(text = TextModel(entry.title)))
                val href = entry.resource.href
                val content = ContentModel(entry.fragment?.let { "$href#$it" } ?: href)
                val children = entry.children.mapNotNull { fromEntry(entry) }.toPersistentList()

                return NavPointModel(entry.identifier, labels = title, content = content, children = children)
            }
        }
    }

    @SerializedName("navLabel")
    internal data class NavLabelModel internal constructor(
        @SerializedName("xml:lang")
        internal val language: String? = null,
        @SerializedName("dir")
        internal val direction: String? = null,
        internal val text: TextModel,
        @SerializedName("img")
        internal val image: ImageModel? = null,
    ) {
        internal fun toElement(): Element = elementOf("navLabel", DAISY_NCX) {
            if (language != null) it.setAttribute("lang", language, XML_NAMESPACE)
            if (direction != null) it.setAttribute("dir", direction)

            it.addContent(text.toElement())

            if (image != null) it.addContent(image.toElement())
        }

        internal companion object {
            internal fun fromElement(element: Element): Try<NavLabelModel> {
                val language: String? = element.getAttributeValue("lang", XML_NAMESPACE)
                val direction: String? = element.getAttributeValue("dir")
                val text = element.getChild("text", element.namespace)?.let {
                    TextModel.fromElement(it)
                } ?: return missingChild(element, "text")
                val image = element.getChild("img", element.namespace)?.let {
                    ImageModel.fromElement(it).fold({ e -> return e.left() }, ::self)
                }

                return Either.Right(NavLabelModel(language, direction, text, image))
            }
        }
    }

    // valid inside of NavMap, PageList and NavList
    @SerializedName("navInfo")
    internal data class NavInfoModel internal constructor(
        @SerializedName("xml:lang")
        internal val language: String? = null,
        @SerializedName("dir")
        internal val direction: String? = null,
        internal val text: TextModel,
        @SerializedName("img")
        internal val image: ImageModel? = null,
    ) {
        internal fun toElement(): Element = elementOf("navInfo", DAISY_NCX) {
            if (language != null) it.setAttribute("lang", language, XML_NAMESPACE)
            if (direction != null) it.setAttribute("dir", direction)

            it.addContent(text.toElement())

            if (image != null) it.addContent(image.toElement())
        }

        internal companion object {
            internal fun fromElement(element: Element): Try<NavInfoModel> {
                val language: String? = element.getAttributeValue("lang", XML_NAMESPACE)
                val direction: String? = element.getAttributeValue("dir")
                val text = element.getChild("text", element.namespace)?.let {
                    TextModel.fromElement(it)
                } ?: return missingChild(element, "text")
                val image = element.getChild("img", element.namespace)?.let {
                    ImageModel.fromElement(it).fold({ e -> return e.left() }, ::self)
                }

                return Either.Right(NavInfoModel(language, direction, text, image))
            }
        }
    }

    // valid inside NcxDocument
    @SerializedName("pageList")
    internal data class PageListModel internal constructor(
        @SerializedName("id")
        internal val identifier: String? = null,
        @SerializedName("class")
        internal val clazz: String? = null,
        @SerializedName("navInfo")
        internal val infoEntries: PersistentList<NavInfoModel> = persistentListOf(),
        @SerializedName("navLabel")
        internal val labels: PersistentList<NavLabelModel> = persistentListOf(),
        // this needs to contain AT LEAST one entry
        @SerializedName("pageTarget")
        internal val targets: PersistentList<PageTargetModel>,
    ) {
        internal fun toElement(): Element = elementOf("pageList", DAISY_NCX) {
            if (identifier != null) it.setAttribute("id", identifier)
            if (clazz != null) it.setAttribute("class", clazz)

            for (info in infoEntries) {
                it.addContent(info.toElement())
            }

            for (label in labels) {
                it.addContent(label.toElement())
            }

            for (target in targets) {
                it.addContent(target.toElement())
            }
        }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(PageListModel::class)

            internal fun fromElement(element: Element, mode: ParseMode): Try<PageListModel> {
                val identifier: String? = element.getAttributeValue("id")
                val clazz: String? = element.getAttributeValue("class")
                val infoEntries = element.getChildren("navInfo", element.namespace)
                    .asSequence()
                    .map { NavInfoModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                val labels = element.getChildren("navLabel", element.namespace)
                    .asSequence()
                    .map { NavLabelModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                val targets = element.getChildren("pageTarget", element.namespace)
                    .asSequence()
                    .map { PageTargetModel.fromElement(it, mode) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                    .ifEmpty { return missingChild(element, "pageTarget") }

                return Either.Right(PageListModel(identifier, clazz, infoEntries, labels, targets))
            }
        }
    }

    // valid inside PageList
    @SerializedName("pageTarget")
    internal data class PageTargetModel internal constructor(
        @SerializedName("id")
        internal val identifier: String,
        // "front" | "normal" | "special"
        internal val type: String,
        internal val value: Int? = null,
        @SerializedName("class")
        internal val clazz: String? = null,
        // the 'playOrder' attribute is not required for the NCX document used for EPUBs
        internal val playOrder: Int? = null,
        // this needs to contain AT LEAST one entry
        @SerializedName("navLabel")
        internal val labels: PersistentList<NavLabelModel>,
        internal val content: ContentModel,
    ) {
        internal fun toElement(): Element = elementOf("pageTarget", DAISY_NCX) {
            it.setAttribute("id", identifier)
            it.setAttribute("type", type)
            if (value != null) it.setAttribute("value", value.toString())
            if (clazz != null) it.setAttribute("class", clazz)
            if (playOrder != null) it.setAttribute("playOrder", playOrder.toString())

            for (label in labels) {
                it.addContent(label.toElement())
            }

            it.addContent(content.toElement())
        }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(PageTargetModel::class)

            internal fun fromElement(element: Element, mode: ParseMode): Try<PageTargetModel> {
                val identifier = element.getAttributeValue("id") ?: return missingAttribute(element, "id")
                val type = element.getAttributeValue("type") ?: return missingAttribute(element, "type")
                val value = element.getAttributeValue("value")?.toInt()
                val clazz: String? = element.getAttributeValue("class")
                val playOrder = element.getAttributeValue("playOrder")?.toInt()
                val labels = element.getChildren("navLabel", element.namespace)
                    .asSequence()
                    .map { NavLabelModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                val content = element.getChild("content")?.let {
                    ContentModel.fromElement(it).fold({ e -> return e.left() }, ::self)
                } ?: return missingChild(element, "content")

                return Either.Right(PageTargetModel(identifier, type, value, clazz, playOrder, labels, content))
            }
        }
    }

    // valid inside NcxDocument
    @SerializedName("navList")
    internal data class NavListModel internal constructor(
        @SerializedName("id")
        internal val identifier: String? = null,
        @SerializedName("class")
        internal val clazz: String? = null,
        @SerializedName("navInfo")
        internal val infoEntries: PersistentList<NavInfoModel> = persistentListOf(),
        // this needs to contain AT LEAST one entry
        @SerializedName("navLabel")
        internal val labels: PersistentList<NavLabelModel>,
        // this needs to contain AT LEAST one entry
        @SerializedName("navTarget")
        internal val targets: PersistentList<NavTargetModel>,
    ) {
        internal fun toElement(): Element = elementOf("navList", DAISY_NCX) {
            if (identifier != null) it.setAttribute("id", identifier)
            if (clazz != null) it.setAttribute("class", clazz)

            for (info in infoEntries) {
                it.addContent(info.toElement())
            }

            for (label in labels) {
                it.addContent(label.toElement())
            }

            for (target in targets) {
                it.addContent(target.toElement())
            }
        }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(NavListModel::class)

            internal fun fromElement(element: Element, mode: ParseMode): Try<NavListModel> {
                val identifier: String? = element.getAttributeValue("id")
                val clazz: String? = element.getAttributeValue("class")
                val infoEntries = element.getChildren("navInfo", element.namespace)
                    .asSequence()
                    .map { NavInfoModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                val labels = element.getChildren("navLabel", element.namespace)
                    .asSequence()
                    .map { NavLabelModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                    .ifEmpty { return missingChild(element, "navLabel") }
                val targets = element.getChildren("navTarget", element.namespace)
                    .asSequence()
                    .map { NavTargetModel.fromElement(it, mode) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                    .ifEmpty { return missingChild(element, "navTarget") }

                return Either.Right(NavListModel(identifier, clazz, infoEntries, labels, targets))
            }
        }
    }

    // valid inside NavList
    @SerializedName("navTarget")
    internal data class NavTargetModel internal constructor(
        @SerializedName("id")
        internal val identifier: String,
        internal val value: Int? = null,
        @SerializedName("class")
        internal val clazz: String? = null,
        // the 'playOrder' attribute is not required for the NCX document used for EPUBs
        internal val playOrder: Int? = null,
        // this needs to contain AT LEAST one entry
        @SerializedName("navLabel")
        internal val labels: PersistentList<NavLabelModel>,
        internal val content: ContentModel,
    ) {
        internal fun toElement(): Element = elementOf("navTarget", DAISY_NCX) {
            it.setAttribute("id", identifier)
            if (value != null) it.setAttribute("value", value.toString())
            if (clazz != null) it.setAttribute("class", clazz)
            if (playOrder != null) it.setAttribute("playOrder", playOrder.toString())

            for (label in labels) {
                it.addContent(label.toElement())
            }

            it.addContent(content.toElement())
        }

        internal companion object {
            private val LOGGER: InlineLogger = InlineLogger(NavTargetModel::class)

            internal fun fromElement(element: Element, mode: ParseMode): Try<NavTargetModel> {
                val identifier = element.getAttributeValue("id") ?: return missingAttribute(element, "id")
                val value = element.getAttributeValue("value")?.toInt()
                val clazz: String? = element.getAttributeValue("class")
                val playOrder = element.getAttributeValue("playOrder")?.toInt()
                val labels = element.getChildren("navLabel", element.namespace)
                    .asSequence()
                    .map { NavLabelModel.fromElement(it) }
                    .flatMapFailure()
                    .filterFailure(LOGGER, mode)
                    .toPersistentList()
                    .ifEmpty { return missingChild(element, "navLabel") }
                val content = element.getChild("content", element.namespace)?.let {
                    ContentModel.fromElement(it).fold({ e -> return e.left() }, ::self)
                } ?: return missingChild(element, "content")

                return Either.Right(NavTargetModel(identifier, value, clazz, playOrder, labels, content))
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(NavigationCenterExtendedModel::class)

        internal fun fromFile(
            ncx: Path,
            mode: ParseMode
        ): Try<NavigationCenterExtendedModel> = documentFrom(ncx).use { _, root ->
            val version = root.getAttributeValue("version") ?: return missingAttribute(root, "version")
            val language: String? = root.getAttributeValue("lang", XML_NAMESPACE)
            val direction: String? = root.getAttributeValue("dir")

            val head = root.getChild("head", root.namespace)?.let {
                HeadModel.fromElement(it, mode).fold({ e -> return e.left() }, ::self)
            } ?: return missingChild(root, "head")
            val title = root.getChild("docTitle", root.namespace)?.let {
                DocTitleModel.fromElement(it).fold({ e -> return e.left() }, ::self)
            } ?: return missingChild(root, "docTitle")
            val authors = root.getChildren("docAuthor", root.namespace)
                .asSequence()
                .map { DocAuthorModel.fromElement(it) }
                .flatMapFailure()
                .filterFailure(LOGGER, mode)
                .toPersistentList()
            val navMap = root.getChild("navMap", root.namespace)?.let {
                NavMapModel.fromElement(it, mode).fold({ e -> return e.left() }, ::self)
            } ?: return missingChild(root, "navMap")
            val pageList = root.getChild("pageList", root.namespace)?.let {
                PageListModel.fromElement(it, mode).fold({ e -> return e.left() }, ::self)
            }
            val navLists = root.getChildren("navList", root.namespace)
                .asSequence()
                .map { NavListModel.fromElement(it, mode) }
                .flatMapFailure()
                .filterFailure(LOGGER, mode)
                .toPersistentList()

            return@use Either.Right(
                NavigationCenterExtendedModel(
                    ncx,
                    version,
                    language,
                    direction,
                    head,
                    title,
                    authors,
                    navMap,
                    pageList,
                    navLists
                )
            )
        }
    }
}