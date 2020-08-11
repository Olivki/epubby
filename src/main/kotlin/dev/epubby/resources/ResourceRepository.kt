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

package dev.epubby.resources

import dev.epubby.Book
import dev.epubby.BookElement
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.asUnmodifiableMap
import moe.kanon.kommons.require

class ResourceRepository(override val book: Book) : BookElement, Iterable<Resource> {
    private val resources: MutableMap<String, Resource> = hashMapOf()

    val entries: Map<String, Resource>
        get() = resources.asUnmodifiableMap()

    override val elementName: String
        get() = "ResourceRepository"

    fun add(resource: Resource) {
        require(resource.book == book, "resource.book == this.book")
        require(resource.identifier !in resources) { "Identifier '${resource.identifier}' is not unique" }
        resources[resource.identifier] = resource
    }

    operator fun get(identifier: String): Resource =
        getOrNull(identifier) ?: throw NoSuchElementException("'$identifier' is not a known resource identifier")

    fun getOrNull(identifier: String): Resource? = resources[identifier]

    operator fun contains(identifier: String): Boolean = identifier in resources

    operator fun contains(resource: Resource): Boolean = resources.containsValue(resource)

    @JvmSynthetic
    internal fun updateIdentifier(resource: Resource, oldIdentifier: String, newIdentifier: String) {
        resources -= oldIdentifier
        resources[newIdentifier] = resource
    }

    override fun iterator(): Iterator<Resource> = resources.values.iterator().asUnmodifiable()
}