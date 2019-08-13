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

package moe.kanon.epubby.toc

import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.utils.Direction
import moe.kanon.epubby.utils.Namespaces
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.func.Option
import org.jdom2.Element
import java.util.*

/**
 * Represents a [NCX](http://www.daisy.org/z3986/2005/Z3986-2005.html#NCX) document.
 */
class NavigationCenterX private constructor(
    val version: String,
    @SerializedName("xml:lang") val language: Locale,
    @SerializedName("dir") val direction: Option<Direction>,
    val head: Option<Head>,
    val docTitle: Option<DocTitle>,
    val docAuthor: Option<DocAuthor>
) {
    data class Head internal constructor(private val elements: List<Meta>) : Iterable<Head.Meta>, ElementSerializer {
        data class Meta(val content: String, val name: String) : ElementSerializer {
            override fun toElement(): Element = Element("meta", Namespaces.DAISY_NCX).apply {
                setAttribute("content", this@Meta.content)
                setAttribute("name", name)
            }
        }

        override fun toElement(): Element = Element("head", Namespaces.DAISY_NCX).apply {
            for (meta in elements) addContent(meta.toElement())
        }

        override fun iterator(): Iterator<Meta> = elements.iterator().asUnmodifiable()
    }

    data class DocTitle internal constructor(val text: Text, val identifier: Option<String>) : ElementSerializer {
        override fun toElement(): Element = Element("docTitle", Namespaces.DAISY_NCX).apply {
            identifier.ifPresent { setAttribute("id", it) }
            addContent(this@DocTitle.text.toElement())
        }
    }

    data class DocAuthor internal constructor(val text: Text, val identifier: Option<String>) : ElementSerializer {
        override fun toElement(): Element = Element("docAuthor", Namespaces.DAISY_NCX).apply {
            identifier.ifPresent { setAttribute("id", it) }
            addContent(this@DocAuthor.text.toElement())
        }
    }

    data class Text internal constructor(
        val content: String,
        val identifier: Option<String>,
        val clazz: Option<String>
    ) : ElementSerializer {
        override fun toElement(): Element = Element("text", Namespaces.DAISY_NCX).setText(content).apply {
            identifier.ifPresent { setAttribute("id", it) }
            clazz.ifPresent { setAttribute("class", it) }
        }
    }

    /**
     * Contains a pointer to graphical content associated with a `docTitle` or `docAuthor`, or of a `navLabel` or
     * `navInfo`.
     */
    data class Img internal constructor(val source: String, val identifier: Option<String>, val clazz: Option<String>) :
        ElementSerializer {
        override fun toElement(): Element = Element("text", Namespaces.DAISY_NCX).apply {
            identifier.ifPresent { setAttribute("id", it) }
            clazz.ifPresent { setAttribute("class", it) }
            setAttribute("src", source)
        }
    }

    //data class NavMap internal constructor(val entries: List<>)
}