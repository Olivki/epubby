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

package dev.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Book
import dev.epubby.ParseStrictness
import dev.epubby.internal.elementOf
import dev.epubby.internal.models.SerializedName
import dev.epubby.packages.PackageCollection
import dev.epubby.prefixes.Prefixes
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("collection")
data class PackageCollectionModel internal constructor(val d: Unit) {
    init {
        TODO("Implement 'PackageCollectionModel'")
    }

    @JvmSynthetic
    internal fun toElement(): Element = elementOf("collection", NAMESPACE) {
        TODO("'toElement' operation is not implemented.")
    }

    @JvmSynthetic
    internal fun toPackageCollection(book: Book, prefixes: Prefixes): PackageCollection {
        TODO("'toPackageCollection' operation is not implemented yet.")
    }

    // TODO
    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageCollectionModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageCollectionModel {
            TODO("'fromElement' operation is not implemented yet")
        }
    }
}