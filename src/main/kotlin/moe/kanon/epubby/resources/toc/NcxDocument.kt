/*
 * Copyright 2019 Oliver Berg
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
import org.jdom2.Element
import org.jdom2.Namespace
import java.nio.file.Path

/**
 * Handles the serialization/deserialization of the [TableOfContents] class to the
 * [NCX](http://www.daisy.org/z3986/2005/Z3986-2005.html#NCX) file format.
 *
 * [EPUB specification entry](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.4.1)
 *
 * Note that since `EPUB 3.0` a new format is used, but for backwards compatibility sake a `ncx` file should also be
 * generated.
 */
internal class NcxDocument private constructor(
    val book: Book,
    val version: String,
    val headElement: Element,
    var title: String,
    var author: String?
) {
    data class NavPoint(val identifier: String, val playOrder: String, val title: String, val href: Path) {
        @JvmSynthetic
        internal fun toElement(book: Book, namespace: Namespace): Element = Element("navPoint", namespace).apply {
            addContent(Element("navLabel", namespace).apply {
                addContent(Element("text", namespace).setText(title))
            })
            val path = book.packageFile.relativize(href).toString().substringAfter("../")
            addContent(Element("content", namespace).setAttribute("src", path))
        }
    }
    //data class Head()
}