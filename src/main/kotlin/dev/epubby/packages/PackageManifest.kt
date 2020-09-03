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

package dev.epubby.packages

import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.internal.buildPersistentList
import dev.epubby.resources.*
import kotlinx.collections.immutable.PersistentList
import moe.kanon.kommons.collections.asUnmodifiableMap
import moe.kanon.kommons.collections.getOrThrow
import moe.kanon.kommons.require
import java.io.IOException

// TODO: make sure to redirect the user to the ResourceRepository for modifying pages.
class PackageManifest(
    override val book: Book,
    private val _localResources: MutableMap<String, LocalResource> = hashMapOf(),
    // TODO: we could probably make this one public?
    private val _remoteResources: MutableMap<String, RemoteResource> = hashMapOf(),
) : BookElement, Iterable<ManifestResource> {
    /**
     * Returns an unmodifiable view of all the [LocalResource] implementations stored in this manifest.
     */
    val localResources: Map<String, LocalResource>
        get() = _localResources.asUnmodifiableMap()

    // TODO: find a better name
    @get:JvmSynthetic
    internal val fileToLocalResource: MutableMap<String, LocalResource> = hashMapOf()

    /**
     * Returns an unmodifiable view of all the [RemoteResource] implementations stored in this manifest.
     */
    val remoteResources: Map<String, RemoteResource>
        get() = _remoteResources.asUnmodifiableMap()

    // TODO: move 'ResourceRepository' to here
    override val elementName: String
        get() = "PackageManifest"

    fun addLocalResource(resource: LocalResource) {
        require(resource.book == book, "resource.book == this.book")
        require(resource !in this) { "Resource '$resource' already exists in this manifest." }
        require(resource.identifier !in this) { "Identifier '${resource.identifier}' must be unique." }
        _localResources[resource.identifier] = resource
        fileToLocalResource[resource.file.toString()] = resource
    }

    fun getLocalResource(identifier: String): LocalResource =
        _localResources.getOrThrow(identifier) { "No local resource found with the identifier '$identifier'" }

    fun getLocalResourceOrNull(identifier: String): LocalResource? = _localResources[identifier]

    /**
     * Returns `true` if this manifest contains a [ManifestResource] that has the same
     * [identifier][ManifestResource.identifier] as the given [identifier], otherwise `false`.
     */
    @JvmName("hasResource")
    operator fun contains(identifier: String): Boolean = identifier in _localResources || identifier in _remoteResources

    operator fun contains(resource: LocalResource): Boolean = _localResources.containsValue(resource)

    operator fun contains(resource: RemoteResource): Boolean = _remoteResources.containsValue(resource)

    // TODO: document that this will *delete* the resource completely
    // TODO: rename to 'deleteLocalResource'?
    // TODO: remove it from the file cache too
    @JvmOverloads
    @Throws(IOException::class)
    fun removeLocalResource(identifier: String, deleteFile: Boolean = true) {

    }

    /**
     * Invokes the [accept][ManifestResource.accept] function of all the resources in this `manifest` with the given
     * [visitor].
     *
     * The `accept` function of each resource will only be invoked if the given [filter] returns `true` for that
     * resource.
     *
     * @param [visitor] the visitor to collect the results from
     * @param [filter] the filter to check before visiting the resource with [visitor],
     * [ALLOW_ALL][ResourceFilters.ALLOW_ALL] by default
     *
     * @see [collectResources]
     */
    @JvmOverloads
    fun visitResources(visitor: ResourceVisitor<*>, filter: ResourceFilter = ResourceFilters.ALLOW_ALL) {
        for (resource in _remoteResources.values) {
            if (!(resource.accept(filter))) {
                continue
            }

            resource.accept(visitor)
        }

        for (resource in _localResources.values) {
            if (!(resource.accept(filter))) {
                continue
            }

            resource.accept(visitor)
        }
    }

    /**
     * Returns a list of the results of invoking the [accept][ManifestResource.accept] function of all the resources
     * in this `manifest` with the given [visitor].
     *
     * The `accept` function of each resource will only be invoked if the given [filter] returns `true` for that
     * resource.
     *
     * @param [visitor] the visitor to collect the results from
     * @param [filter] the filter to check before visiting the resource with [visitor],
     * [ALLOW_ALL][ResourceFilters.ALLOW_ALL] by default
     *
     * @see [visitResources]
     */
    @JvmOverloads
    fun <R> collectResources(
        visitor: ResourceVisitor<R>,
        filter: ResourceFilter = ResourceFilters.ALLOW_ALL,
    ): PersistentList<R> = buildPersistentList {
        for (resource in _remoteResources.values) {
            if (!(resource.accept(filter))) {
                continue
            }

            val result = resource.accept(visitor)

            if (result != null && result != Unit) {
                add(result)
            }
        }

        for (resource in _localResources.values) {
            if (!(resource.accept(filter))) {
                continue
            }

            val result = resource.accept(visitor)

            if (result != null && result != Unit) {
                add(result)
            }
        }
    }

    @JvmSynthetic
    internal fun updateLocalResourceIdentifier(resource: LocalResource, oldIdentifier: String, newIdentifier: String) {
        _localResources -= oldIdentifier
        _localResources[newIdentifier] = resource
    }

    // TODO: verify that this iterator works
    override fun iterator(): Iterator<ManifestResource> = object : Iterator<ManifestResource> {
        private val localIterator = _localResources.values.iterator()
        private val remoteIterator = _remoteResources.values.iterator()

        // we want to avoid short circuiting, so we're using 'and' and not '&&' here
        // TODO: verify that the logic we've used for using 'and' here is actually correct
        override fun hasNext(): Boolean = localIterator.hasNext() and remoteIterator.hasNext()

        override fun next(): ManifestResource = when {
            localIterator.hasNext() -> localIterator.next()
            remoteIterator.hasNext() -> remoteIterator.next()
            else -> throw NoSuchElementException()
        }
    }

    override fun toString(): String =
        "PackageManifest(localResources=$_localResources, remoteResources=$_remoteResources)"
}