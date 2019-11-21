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

package moe.kanon.epubby.pack

import com.google.common.net.MediaType
import moe.kanon.epubby.Book
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.props.Properties
import moe.kanon.epubby.utils.Namespaces
import moe.kanon.kommons.func.Either
import org.jdom2.Element
import org.jdom2.Namespace
import java.net.URL
import java.nio.file.Path

class Manifest {
    // TODO: Check if this namespace is correct
    @JvmSynthetic
    internal fun toElement(namespace: Namespace = Namespaces.OPF): Element {
        TODO()
    }

    data class Item(
        val identifier: Identifier,
        val href: Either<Path, URL>,
        val mediaType: MediaType? = null,
        val properties: Properties = Properties.empty() // if the properties is empty, do not serialize as attribute on element
    )

    companion object {
        @JvmSynthetic
        internal fun fromElement(book: Book, element: Element, file: Path): Manifest = TODO()
    }
}