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

package moe.kanon.epubby.resources.toc.builder

import moe.kanon.epubby.resources.toc.NcxDocument
import moe.kanon.epubby.structs.Identifier
import java.net.URI

object NcxBuilders {
    @JvmStatic
    fun textBuilder(origin: NcxDocument.Text): TextBuilder = TextBuilder(origin)

    @JvmStatic
    fun textBuilder(): TextBuilder = TextBuilder(null)

    @JvmStatic
    fun imgBuilder(origin: NcxDocument.Img): ImgBuilder = ImgBuilder(origin)

    @JvmStatic
    fun imgBuilder(): ImgBuilder = ImgBuilder(null)

    @JvmStatic
    fun contentBuilder(origin: NcxDocument.Content): ContentBuilder = ContentBuilder(origin)

    @JvmStatic
    fun contentBuilder(): ContentBuilder = ContentBuilder(null)

    // TODO: Make error message look better
    private fun fail(name: String): Nothing =
        throw UnsupportedOperationException("Property '$name' needs to be set before it can be built")

    class HeadBuilder // TODO

    class TextBuilder internal constructor(origin: NcxDocument.Text?) {
        private var content: String? = origin?.content
        private var identifier: Identifier? = origin?.identifier
        private var clazz: String? = origin?.clazz

        fun content(content: String) = apply { this.content = content }

        fun identifier(identifier: Identifier?) = apply { this.identifier = identifier }

        fun clazz(clazz: String?) = apply { this.clazz = clazz }

        fun build(): NcxDocument.Text = NcxDocument.Text(content ?: fail("content"), identifier, clazz)
    }

    class ImgBuilder internal constructor(origin: NcxDocument.Img?) {
        private var source: URI? = origin?.source
        private var identifier: Identifier? = origin?.identifier
        private var clazz: String? = origin?.clazz

        fun source(source: URI) = apply { this.source = source }

        fun identifier(identifier: Identifier?) = apply { this.identifier = identifier }

        fun clazz(clazz: String?) = apply { this.clazz = clazz }

        fun build(): NcxDocument.Img = NcxDocument.Img(source ?: fail("source"), identifier, clazz)
    }

    class ContentBuilder internal constructor(origin: NcxDocument.Content?) {
        private var source: URI? = origin?.source
        private var identifier: Identifier? = origin?.identifier

        fun source(source: URI) = apply { this.source = source }

        fun identifier(identifier: Identifier?) = apply { this.identifier = identifier }

        fun build(): NcxDocument.Content = NcxDocument.Content(source ?: fail("source"), identifier)
    }

    // TODO: The rest
}