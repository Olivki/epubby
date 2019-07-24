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

package moe.kanon.epubby.resources.root

import moe.kanon.epubby.Book
import moe.kanon.epubby.ElementSerializer
import moe.kanon.epubby.SerializedName
import moe.kanon.epubby.resources.Resource
import moe.kanon.kommons.func.Option
import org.jdom2.Element
import java.net.URL
import java.nio.file.Path

/**
 * Represents the [manifest](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#sec-pkg-manifest)
 * element.
 *
 * The manifest provides an exhaustive list of the [resources][Resource] used by the [book].
 */
class PackageManifest private constructor(
    val book: Book,
    val identifier: Option<String>,
    private val _items: MutableMap<String, ManifestItem>
) : ElementSerializer {
    companion object {
        internal fun parse(book: Book, packageDocument: Path, element: Element): PackageManifest = with(element) {
            TODO()
        }
    }

    override fun toElement(): Element {
        TODO("not implemented")
    }
}

sealed class ManifestItem {
    /**
     * Represents the [item](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#elemdef-package-item)
     * element.
     */
    data class Local(
        @SerializedName("id") val identifier: String,
        val href: Path,
        val fallback: Option<String>,
        val mediaOverlay: Option<String>,
        val mediaType: Option<String>,
        val properties: Option<String>
    ) : ElementSerializer {
        override fun toElement(): Element {
            TODO("not implemented")
        }
    }

    data class External(
        @SerializedName("id") val identifier: String,
        val href: URL,
        val fallback: Option<String>,
        val mediaOverlay: Option<String>,
        val mediaType: Option<String>,
        val properties: Option<String>
    ) : ElementSerializer {
        override fun toElement(): Element {
            TODO("not implemented")
        }
    }
}