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

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.files.RegularFile
import dev.epubby.internal.utils.buildPersistentList
import dev.epubby.resources.*
import kotlinx.collections.immutable.PersistentList
import moe.kanon.kommons.collections.asUnmodifiableMap
import moe.kanon.kommons.collections.getOrThrow
import java.io.IOException
import java.util.function.Consumer

// TODO: only allow one 'NcxResource' instance to exist per 'PackageManifest' instance

class PackageManifest internal constructor(
    override val epub: Epub,
    var identifier: String? = null,
    private val _localResources: MutableMap<String, LocalResource> = hashMapOf(),
    // TODO: find a better name
    @get:JvmSynthetic
    internal val fileToLocalResource: MutableMap<String, LocalResource> = hashMapOf(),
    // TODO: we could probably make this one public?
    private val _externalResources: MutableMap<String, ExternalResource> = hashMapOf(),
) : EpubElement, Iterable<ManifestResource> {
    /**
     * Returns an unmodifiable view of all the [LocalResource] implementations stored in this manifest.
     */
    val localResources: Map<String, LocalResource>
        get() = _localResources.asUnmodifiableMap()

    /**
     * Returns an unmodifiable view of all the [ExternalResource] implementations stored in this manifest.
     */
    val externalResources: Map<String, ExternalResource>
        get() = _externalResources.asUnmodifiableMap()

    /**
     * The file organizer belonging to this manifest.
     */
    val fileOrganizer: ResourceFileOrganizer by lazy { ResourceFileOrganizer(epub) }

    // TODO: move 'ResourceRepository' to here
    override val elementName: String
        get() = "PackageManifest"

    fun addLocalResource(resource: LocalResource) {
        require(resource.epub == epub, "resource.epub == this.epub")
        require(resource !in this) { "Resource '$resource' already exists in this manifest." }
        require(resource.identifier !in this) { "Identifier '${resource.identifier}' must be unique." }
        // TODO: throw an error if 'identifier' is already registered here?
        _localResources.putIfAbsent(resource.identifier, resource)
        fileToLocalResource[resource.file.fullPath] = resource
    }

    fun getLocalResource(identifier: String): LocalResource =
        _localResources.getOrThrow(identifier) { "No local resource found with the identifier '$identifier'" }

    fun getLocalResourceOrNull(identifier: String): LocalResource? = _localResources[identifier]

    /**
     * Returns `true` if this manifest contains a [ManifestResource] that has the same
     * [identifier][ManifestResource.identifier] as the given [identifier], otherwise `false`.
     */
    @JvmName("hasResource")
    operator fun contains(identifier: String): Boolean = identifier in _localResources || identifier in _externalResources

    operator fun contains(resource: LocalResource): Boolean = _localResources.containsValue(resource)

    operator fun contains(resource: ExternalResource): Boolean = _externalResources.containsValue(resource)

    // TODO: document that this will *delete* the resource completely
    // TODO: rename to 'deleteLocalResource'?
    // TODO: remove it from the file cache too
    @JvmOverloads
    @Throws(IOException::class)
    fun removeLocalResource(identifier: String, deleteFile: Boolean = true) {

    }

    fun addExternalResource(resource: ExternalResource) {
        _externalResources[resource.identifier] = resource
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
    fun visitResources(
        visitor: ResourceVisitor<*>,
        filter: ResourceFilter = ResourceFilters.ALLOW_ALL
    ) {
        visitor.begin(this)

        for (resource in _externalResources.values) {
            if (resource.accept(filter)) {
                resource.accept(visitor)
            }
        }

        for (resource in _localResources.values) {
            if (resource.accept(filter)) {
                resource.accept(visitor)
            }
        }

        visitor.end(this)
    }

    /**
     * Invokes the given [collector] with the result of invoking the [accept][ManifestResource.accept] function of all
     * the resources in this `manifest` with the given [visitor].
     *
     * The `accept` function of each resource will only be invoked if the given [filter] returns `true` for that
     * resource.
     *
     * If the result is either `null` or [Unit] then it will be ignored.
     *
     * @param [visitor] the visitor to collect the results from
     * @param [filter] the filter to check before visiting the resource with [visitor],
     * [ALLOW_ALL][ResourceFilters.ALLOW_ALL] by default
     * @param [collector] the consumer to use to collect all the results
     *
     * @see [visitResources]
     */
    @JvmOverloads
    fun <R> collectResources(
        visitor: ResourceVisitor<R>,
        filter: ResourceFilter = ResourceFilters.ALLOW_ALL,
        collector: Consumer<R>,
    ) {
        visitor.begin(this)

        for (resource in _externalResources.values) {
            if (resource.accept(filter)) {
                val result = resource.accept(visitor)

                if (result != null && result != Unit) {
                    collector.accept(result)
                }
            }
        }

        for (resource in _localResources.values) {
            if (resource.accept(filter)) {
                val result = resource.accept(visitor)

                if (result != null && result != Unit) {
                    collector.accept(result)
                }
            }
        }

        visitor.end(this)
    }

    /**
     * Returns a list of the results of invoking the [accept][ManifestResource.accept] function of all the resources
     * in this `manifest` with the given [visitor].
     *
     * The `accept` function of each resource will only be invoked if the given [filter] returns `true` for that
     * resource.
     *
     * If the result is either `null` or [Unit] then it will be ignored.
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
    ): PersistentList<R> = buildPersistentList { collectResources(visitor, filter, this::add) }

    @JvmSynthetic
    internal fun updateLocalResourceIdentifier(resource: LocalResource, oldIdentifier: String, newIdentifier: String) {
        _localResources -= oldIdentifier
        _localResources[newIdentifier] = resource
    }

    @JvmSynthetic
    internal fun updateLocalResourceFile(oldFile: RegularFile, newFile: RegularFile) {
        if (oldFile != newFile) {
            val resource = fileToLocalResource[oldFile.fullPath]

            if (resource != null) {
                fileToLocalResource -= oldFile.fullPath
                fileToLocalResource[newFile.fullPath] =  resource
            } else {
                throw IllegalStateException("Given oldFile '$oldFile' does not represent a resource file.")
            }
        }
    }

    @JvmSynthetic
    internal fun getLocalResourceFromFile(file: RegularFile): LocalResource? = fileToLocalResource[file.fullPath]

    @JvmSynthetic
    internal fun writeResourcesToFile() {
        for ((_, resource) in _localResources) {
            resource.triggerWriteToFile()
        }
    }

    // TODO: verify that this iterator works
    override fun iterator(): Iterator<ManifestResource> = object : Iterator<ManifestResource> {
        private val localIterator = _localResources.values.iterator()
        private val remoteIterator = _externalResources.values.iterator()

        // we want to avoid short circuiting, so we're using 'and' and not '&&' here
        // TODO: verify that this works like it should
        override fun hasNext(): Boolean = localIterator.hasNext() || remoteIterator.hasNext()

        override fun next(): ManifestResource = when {
            localIterator.hasNext() -> localIterator.next()
            remoteIterator.hasNext() -> remoteIterator.next()
            else -> throw NoSuchElementException()
        }
    }

    override fun toString(): String =
        "PackageManifest(localResources=$_localResources, externalResources=$_externalResources)"
}