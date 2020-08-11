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

package dev.epubby.html.jsoup

import dev.epubby.html.HtmlAttribute
import dev.epubby.html.HtmlAttributes
import dev.epubby.html.HtmlElement
import dev.epubby.html.HtmlTag
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag

internal fun HtmlAttribute.toJsoup(): Attribute = when (this) {
    is JsoupHtmlAttributeImpl -> delegate
    else -> Attribute(key, value)
}

internal fun HtmlAttributes.toJsoup(): Attributes = when (this) {
    is JsoupHtmlAttributesImpl -> delegate
    else -> Attributes().also {
        for (attribute in this) {
            it.put(attribute.toJsoup())
        }
    }
}

internal fun HtmlElement.toJsoup(): Element = when (this) {
    is JsoupHtmlElementImpl -> delegate
    else -> Element(tag.toJsoup(), baseUri, attributes.toJsoup()).also {
        for ()
    }
}

internal fun HtmlTag.toJsoup(): Tag = when (this) {
    is JsoupHtmlTagImpl -> delegate
    else -> Tag.valueOf(normalizedName)
}