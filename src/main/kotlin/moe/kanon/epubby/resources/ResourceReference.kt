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

package moe.kanon.epubby.resources

import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.asSome
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Element
import java.nio.file.Path

/**
 * Used for returning references of a resource.
 *
 * @property [element] The element that referenced the resource.
 * @property [attr] The [Attribute] that stores the actual reference.
 * @property [fragmentIdentifier] An optional `fragment-identifier`, this will generally only contain a value if the
 * resource is a [PageResource].
 */
data class ResourceReference(
    val element: Element,
    val attr: Attribute,
    val fragmentIdentifier: Option<String> = ('#' in attr.value).asSome { attr.value.split('#')[1] }
) {
    @JvmSynthetic internal fun updateReferenceTo(resource: Resource, file: Path) {
        val relativeFile = resource.book.packageDocument.file.relativize(file)
        val location = relativeFile.toString() + fragmentIdentifier.fold({ "" }) { "#$it" }
        element.attr(attr.key, location)
    }
}