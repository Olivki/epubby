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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentHashMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.packages.Manifest
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.props.vocabs.ManifestVocabulary
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.filterValuesIsInstance
import moe.kanon.kommons.collections.getValueOrThrow
import moe.kanon.kommons.collections.isEmpty
import moe.kanon.kommons.io.paths.PathVisitor
import moe.kanon.kommons.io.paths.delete
import moe.kanon.kommons.io.paths.entries
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.getOrCreateDirectory
import moe.kanon.kommons.io.paths.moveTo
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.walkFileTree
import moe.kanon.kommons.requireThat
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.reflect.KClass

class Resources internal constructor(val book: Book) : Iterable<Resource> {
    private val resources: MutableMap<Identifier, Resource> = hashMapOf()

    /**
     * Returns a map of all the resources in the [book], mapped like `identifier::resource`.
     */
    val entries: ImmutableMap<Identifier, Resource> get() = resources.toPersistentHashMap()

    // TODO: Documentation

    val tableOfContentResources: ImmutableMap<Identifier, TableOfContentsResource>
        get() = resources.filterValuesIsInstance<Identifier, TableOfContentsResource>().toPersistentHashMap()

    val pageResources: ImmutableMap<Identifier, PageResource>
        get() = resources.filterValuesIsInstance<Identifier, PageResource>().toPersistentHashMap()

    val styleSheetResources: ImmutableMap<Identifier, StyleSheetResource>
        get() = resources.filterValuesIsInstance<Identifier, StyleSheetResource>().toPersistentHashMap()

    val imageResources: ImmutableMap<Identifier, ImageResource>
        get() = resources.filterValuesIsInstance<Identifier, ImageResource>().toPersistentHashMap()

    val fontResources: ImmutableMap<Identifier, FontResource>
        get() = resources.filterValuesIsInstance<Identifier, FontResource>().toPersistentHashMap()

    val audioResources: ImmutableMap<Identifier, AudioResource>
        get() = resources.filterValuesIsInstance<Identifier, AudioResource>().toPersistentHashMap()

    val scriptResources: ImmutableMap<Identifier, ScriptResource>
        get() = resources.filterValuesIsInstance<Identifier, ScriptResource>().toPersistentHashMap()

    val videoResources: ImmutableMap<Identifier, VideoResource>
        get() = resources.filterValuesIsInstance<Identifier, VideoResource>().toPersistentHashMap()

    val miscResources: ImmutableMap<Identifier, MiscResource>
        get() = resources.filterValuesIsInstance<Identifier, MiscResource>().toPersistentHashMap()

    @JvmSynthetic
    internal fun populateFromManifest() {
        logger.info { "Creating book resources from book manifest.." }
        val localResources = book.manifest.localItems.values.asSequence()
        val knownResources = localResources
            .filter { it.mediaType != null }
            .filter { it.href.exists }
            .map {
                Resource.fromMediaType(it.mediaType!!, it.href, book, it.identifier).apply {
                    properties.addAll(it.properties.filterIsInstance<ManifestVocabulary>())
                }
            }
        val unknownResources = localResources
            .filter { it.mediaType == null }
            .filter { it.href.exists }
            .onEach { logger.warn { "Item <$it> does not have a 'mediaType', will be marked as a 'MiscResource'" } }
            .map {
                MiscResource(book, it.href, it.identifier).apply {
                    properties.addAll(it.properties.filterIsInstance<ManifestVocabulary>())
                }
            }
        val allResources = (knownResources + unknownResources).associateByTo(HashMap()) { it.identifier }
        resources.putAll(allResources)
        logger.info { "Successfully created all book resources from the book manifest." }
    }

    /**
     * Attempts to move all resources in this repository to their [desired directories][Resource.desiredDirectory].
     *
     * @throws [IOException] if an i/o error occurs
     */
    @Throws(IOException::class)
    fun moveToDesiredDirectories() {
        val root = book.packageRoot
        for (resource in this) {
            val file = resource.file
            val desiredDirectory = resource.desiredDirectory.getOrCreateDirectory()
            if (file.parent != desiredDirectory) {
                logger.trace { "Moving file <${file.name}> from <${file.parent}> to <$desiredDirectory>" }
                resource.file = resource.file.moveTo(desiredDirectory, true) // , StandardCopyOption.REPLACE_EXISTING
            }
        }
        // walk the file tree and delete any empty directories that are left after we've moved the resource files around
        root.walkFileTree(visitor = object : PathVisitor {
            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (dir.entries.isEmpty) {
                    logger.trace { "Directory <$dir> is empty, deleting..." }
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

    fun <R : Resource> addResource(resource: R): R {
        requireThat(resource.identifier !in resources) { "there already exists a resource with the identifier '${resource.identifier}'" }
        resources[resource.identifier] = resource
        if (!book.manifest.hasItemFor(resource)) {
            book.manifest.addItemForResource(resource, resource.properties)
        }
        return resource
    }

    /**
     * Returns a new [Resource] implementation that is appropriate for the [contentType][Path.contentType] of the
     * given [file], or [MiscResource] if none could be found. The returned `Resource` is also added to the resources
     * of the [book].
     *
     * @param [file] the [path][Path] pointing towards the file that the returned [Resource] should be wrapping
     * around, note that this **NEEDS** to point towards an [existing][Path.exists] file
     * @param [identifier] the unique identifier that the returned [Resource] should be using, this is used for
     * storing the resource inside the [manifest][Manifest] of the [book]
     *
     * @throws [IllegalArgumentException] if the [file-system][Path.getFileSystem] of the given [file] is not the same
     * as the [file-system][Book.fileSystem] of the [book]
     * @throws [IOException] if an i/o error occurred
     * @throws [EpubbyException] if something went wrong with the creation of the resource
     */
    @JvmOverloads
    @Throws(IOException::class, EpubbyException::class)
    fun addResourceFromFile(file: Path, identifier: Identifier = Identifier.fromFile(file)): Resource =
        addResource(Resource.fromFile(file, book, identifier))

    @JvmOverloads
    @Throws(IOException::class, EpubbyException::class)
    fun addResourceFromExternalFile(
        file: Path,
        targetDirectory: Path,
        identifier: Identifier = Identifier.fromFile(file)
    ): Resource = addResource(Resource.fromExternalFile(file, targetDirectory, book, identifier))

    fun removeResource(identifier: Identifier): Boolean {
        val resource = resources[identifier]
        resources -= identifier
        resource?.onDeletion()
        return resource != null
    }

    fun removeResource(resource: Resource): Boolean {
        val result = resource.identifier in resources
        resources -= resource.identifier
        if (result) {
            resource.onDeletion()
        }
        return result
    }

    fun getResource(identifier: Identifier): Resource =
        resources.getValueOrThrow(identifier) { "No resource found with the identifier '$identifier'" }

    fun getResourceOrNull(identifier: Identifier): Resource? = resources[identifier]

    /**
     * Returns `true` if there exists a resource with the given [identifier], `false` otherwise.
     */
    fun hasResource(identifier: Identifier): Boolean = identifier in resources

    /**
     * Returns `true` if the given [resource] is known, `false` otherwise.
     */
    fun hasResource(resource: Resource): Boolean = resources.containsValue(resource)

    // -- UTILS -- \\
    fun visitResources(visitor: ResourceVisitor) {
        for ((_, resource) in resources) {
            when (resource) {
                is TableOfContentsResource -> visitor.onTableOfContents(resource)
                is PageResource -> visitor.onPage(resource)
                is StyleSheetResource -> visitor.onStyleSheet(resource)
                is ImageResource -> visitor.onImage(resource)
                is FontResource -> visitor.onFont(resource)
                is AudioResource -> visitor.onAudio(resource)
                is ScriptResource -> visitor.onScript(resource)
                is VideoResource -> visitor.onVideo(resource)
                is MiscResource -> visitor.onMisc(resource)
            }
        }
    }

    /**
     * Returns a list of all the directories that the underlying [files][Resource.file] of the `resources` are stored
     * in. The list is ordered by frequency of directory, with the highest being first, and lowest being last.
     */
    // TODO: Does this work?
    fun <T : Resource> getDirectoriesUsedBy(filter: Class<T>): ImmutableList<Path> {
        val dirs = resources
            .values
            .asSequence()
            .filterIsInstance(filter)
            .map { it.file.parent }
        return dirs
            .distinct()
            .map { it to dirs.count { dir -> it == dir } }
            .sortedByDescending { it.second }
            .map { it.first }
            .asIterable()
            .toImmutableList()
    }

    /**
     * Returns a list of all the directories that the underlying [files][Resource.file] of the `resources` are stored
     * in. The list is ordered by frequency of directory, with the highest being first, and lowest being last.
     */
    @JvmSynthetic
    inline fun <reified T : Resource> getDirectoriesUsedBy(): ImmutableList<Path> = getDirectoriesUsedBy(T::class.java)

    override fun iterator(): Iterator<Resource> = resources.values.iterator().asUnmodifiable()
}