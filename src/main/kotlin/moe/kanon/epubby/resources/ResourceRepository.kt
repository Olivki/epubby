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

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableHashMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.logger
import moe.kanon.epubby.root.ManifestItem
import moe.kanon.epubby.root.PackageManifest
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.isEmpty
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.firstOrNone
import moe.kanon.kommons.func.getValueOrNone
import moe.kanon.kommons.io.paths.PathVisitor
import moe.kanon.kommons.io.paths.delete
import moe.kanon.kommons.io.paths.entries
import moe.kanon.kommons.io.paths.getOrCreateDirectory
import moe.kanon.kommons.io.paths.moveTo
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.walkFileTree
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class ResourceRepository(val book: Book) : Iterable<Resource> {
    private val _resources: MutableMap<String, Resource> = HashMap()

    /**
     * Returns a [ImmutableMap] containing all the resources of the [book], mapped in the following fashion;
     * `identifier:resource`.
     */
    val resources: ImmutableMap<String, Resource> get() = _resources.toImmutableHashMap()

    /**
     * Populates this repository with the entries from the [manifest][PackageManifest] of the [book].
     */
    @JvmSynthetic internal fun populateFromManifest() {
        logger.info { "Populating resource repository..." }
        val localResources = book.manifest.items.values
            .asSequence()
            .filterIsInstance<ManifestItem.Local>()
        val knownResources = localResources
            .filter { it.mediaType.isPresent }
            .map { Resource.fromMediaType(it.mediaType.value, it.href, book, it.identifier) }
        val unknownResources = localResources
            .filter { it.mediaType.isEmpty }
            .onEach { logger.warn { "Item <$it> does not have a 'mediaType', will be marked as a 'MiscResource'" } }
            .map { MiscResource(book, it.href, it.identifier) }
        val allResources = (knownResources + unknownResources).associateByTo(HashMap()) { it.identifier }
        _resources.putAll(allResources)
        logger.info { "Resource repository successfully populated!" }
    }

    /**
     * Attempts to move all resources in this repository to their [desired directories][Resource.desiredDirectory].
     *
     * @throws [IOException] if an i/o error occurs
     */
    @Throws(IOException::class)
    fun organizeResourceFiles() {
        val root = book.packageDocument.file.parent
        for (resource in this) {
            val file = resource.file
            val desiredDirectory = resource.desiredDirectory.getOrCreateDirectory()
            if (file.parent != desiredDirectory) {
                logger.debug { "Moving file <${file.name}> from <${file.parent}> to <$desiredDirectory>" }
                resource.file = resource.file.moveTo(desiredDirectory, keepName = true)
            }
        }
        // walk the file tree and delete any empty directories that are left after we've moved the resource files around
        root.walkFileTree(visitor = object : PathVisitor {
            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (dir.entries.isEmpty) {
                    logger.debug { "Directory <$dir> is empty, deleting..." }
                    dir.delete()
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = FileVisitResult.CONTINUE

            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult = FileVisitResult.CONTINUE

            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
                FileVisitResult.CONTINUE
        })
    }

    // -- RESOURCES -- \\
    /**
     * Adds the given [resource] to this repository, the `resource` will be stored under its
     * [identifier][Resource.identifier].
     *
     * Note that if `resource` already exists in this repository, then this function will **NOT** add it again.
     *
     * @return the given [resource]
     *
     * @see [addResourceFromFile]
     */
    fun <R : Resource> addResource(resource: R): R = resource.also {
        val id = it.identifier
        when {
            resource in this -> logger.warn { "This repository already contains the resource <$resource>" }
            resource in book.manifest && resource.identifier !in this -> {
                logger.debug { "Manifest has item with id <$id>, but no resource can be found with that id" }
                _resources[id] = resource
            }
            resource.identifier in this -> {
                logger.debug { "Overwriting already existing resource under id <$id>" }
                _resources[id] = resource
            }
            else -> {
                logger.debug { "Adding resource <$resource> to repository" }
                _resources[id] = resource
            }
        }
    }

    /**
     * Creates a new [Resource] instance wrapped around the given [file] using the [Resource.fromFile] function, and
     * then [adds it to this repository][addResource].
     *
     * @param [file] the file that the resource should be wrapping around
     * @param [id] the unique identifier that the resource should be stored under
     *
     * ([file.name][Path.name] by default)
     *
     * @see [Resource.fromFile]
     * @see [addResource]
     */
    @JvmOverloads
    fun addResourceFromFile(file: Path, id: String = file.name): Resource =
        addResource(Resource.fromFile(file, book, id))

    /**
     * Adds the given [resource] to this repository under the given [id], or replaces the existing entry under `id` if
     * one already exists.
     *
     * This function will also make sure that the [identifier][Resource.identifier] of `resource` is the same as the
     * given `id`.
     *
     * @return the given [resource]
     */
    fun <R : Resource> setResource(id: String, resource: R): R = resource.also {
        // TODO: This might be doing the same thing twice
        resource.identifier = id
        _resources[id] = resource
    }

    /**
     * Removes the [Resource] stored under the given [id] from this repository.
     */
    fun removeResource(id: String) {
        _resources -= id
    }

    /**
     * Returns the [Resource] stored under the given [id], or throws a [NoSuchElementException] if none is found.
     *
     * @see [getResourceOrNone]
     */
    fun getResource(id: String): Resource =
        _resources[id] ?: throw NoSuchElementException("No resource found with id <$id>")

    /**
     * Returns the [Resource] stored under the given [id], or [None] if none is found.
     *
     * @see [getResource]
     */
    fun getResourceOrNone(id: String): Option<Resource> = _resources.getValueOrNone(id)

    /**
     * Returns the first [Resource] that has a [href][Resource.href] that matches the given [href], or throws a
     * [NoSuchElementException] if none is found.
     */
    fun getResourceByHref(href: String): Resource = _resources.values.find { it.href == href }
        ?: throw NoSuchElementException("No resource found with href <$href>")

    /**
     * Returns the first [Resource] that has a [href][Resource.href] that matches the given [href], or [None] if none
     * is found.
     */
    fun getResourceByHrefOrNone(href: String): Option<Resource> = _resources.values.firstOrNone { it.href == href }

    /**
     * Returns whether or not this repository contains a [Resource] that has the same [id][Resource.identifier] as
     * the given [id].
     */
    @JvmName("hasResource")
    operator fun contains(id: String): Boolean = id in _resources

    /**
     * Returns whether or not this repository contains the given [resource].
     */
    @JvmName("hasResource")
    operator fun contains(resource: Resource): Boolean = _resources.containsValue(resource)

    // -- OVERRIDES -- \\
    override fun iterator(): Iterator<Resource> = _resources.values.iterator().asUnmodifiable()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ResourceRepository -> false
        book != other.book -> false
        _resources != other._resources -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + _resources.hashCode()
        return result
    }

    override fun toString(): String = "ResourceRepository(book=$book, resources=$_resources)"
}

/**
 * Adds the given [resource] to this repository, the `resource` will be stored under its
 * [identifier][Resource.identifier].
 *
 * Note that if `resource` already exists in this repository, then this function will **NOT** add it again.
 */
operator fun ResourceRepository.plusAssign(resource: Resource) {
    this.addResource(resource)
}

/**
 * Creates a new [Resource] instance wrapped around the given [file] using the [Resource.fromFile] function, and
 * then [adds it to this repository][ResourceRepository.addResource].
 *
 * @param [file] the file that the resource should be wrapping around
 *
 * @see [Resource.fromFile]
 * @see [ResourceRepository.addResource]
 */
operator fun ResourceRepository.plusAssign(file: Path) {
    this.addResourceFromFile(file)
}

/**
 * Adds the given [resource] to this repository under the given [id], or replaces the existing entry under `id` if
 * one already exists.
 *
 * This function will also make sure that the [identifier][Resource.identifier] of `resource` is the same as the
 * given `id`.
 */
operator fun ResourceRepository.set(id: String, resource: Resource) {
    this.setResource(id, resource)
}

/**
 * Removes the [Resource] stored under the given [id] from this repository.
 */
operator fun ResourceRepository.minusAssign(id: String) {
    this.removeResource(id)
}

/**
 * Returns the [Resource] stored under the given [id], or throws a [NoSuchElementException] if none is found.
 *
 * @see [ResourceRepository.getResourceOrNone]
 */
operator fun ResourceRepository.get(id: String): Resource = this.getResource(id)