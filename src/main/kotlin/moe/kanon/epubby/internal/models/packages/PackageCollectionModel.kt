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

package moe.kanon.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import moe.kanon.epubby.Book
import moe.kanon.epubby.ParseStrictness
import moe.kanon.epubby.internal.elementOf
import moe.kanon.epubby.internal.models.SerialName
import moe.kanon.epubby.packages.PackageCollection
import moe.kanon.epubby.prefixes.Prefixes
import org.apache.logging.log4j.kotlin.loggerOf
import org.jdom2.Element
import moe.kanon.epubby.internal.Namespaces.OPF as NAMESPACE

@SerialName("collection")
internal data class PackageCollectionModel internal constructor(val d: Unit) {
    internal fun toElement(): Element = elementOf("collection", NAMESPACE) {
        TODO("'toElement' operation is not implemented.")
    }

    internal fun toPackageCollection(book: Book, prefixes: Prefixes): PackageCollection {
        TODO("'toPackageCollection' operation is not implemented yet.")
    }

    // TODO
    internal companion object {
        private val logger = InlineLogger(PackageCollectionModel::class)

        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageCollectionModel {
            TODO("'fromElement' operation is not implemented yet")
        }
    }
}