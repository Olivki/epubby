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
import moe.kanon.epubby.readBookCopy
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.props.vocabs.ManifestVocabulary
import moe.kanon.epubby.utils.internal.logger
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.filterValuesIsInstance
import moe.kanon.kommons.collections.getValueOrThrow
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.firstOrNone
import moe.kanon.kommons.func.getValueOrNone
import moe.kanon.kommons.io.paths.delete
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.getOrCreateDirectory
import moe.kanon.kommons.io.paths.moveTo
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.requireThat
import java.io.IOException
import java.nio.file.CopyOption
import java.nio.file.Path

class Resources internal constructor(val book: Book) : Iterable<Resource> {
    private val resources: MutableMap<Identifier, Resource> = hashMapOf()

    /**
     * Returns a map of all the resources in the [book], mapped like `identifier::resource`.
     */
    val entries: ImmutableMap<Identifier, Resource> get() = resources.toPersistentHashMap()

    /**
     * Returns a map of all the [ncx-resources][NcxResource] that the book has, mapped like `identifier::resource`.
     *
     * A well-formed EPUB file should *at most* contain `1` `ncx-resource`.
     */
    val ncxResources: ImmutableMap<Identifier, NcxResource>
        get() = resources.filterValuesIsInstance<Identifier, NcxResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [page-resources][PageResource] that the book has, mapped like `identifier::resource`.
     */
    val pageResources: ImmutableMap<Identifier, PageResource>
        get() = resources.filterValuesIsInstance<Identifier, PageResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [stylesheet-resources][StyleSheetResource] that the book has, mapped like
     * `identifier::resource`.
     */
    val styleSheetResources: ImmutableMap<Identifier, StyleSheetResource>
        get() = resources.filterValuesIsInstance<Identifier, StyleSheetResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [image-resources][ImageResource] that the book has, mapped like `identifier::resource`.
     */
    val imageResources: ImmutableMap<Identifier, ImageResource>
        get() = resources.filterValuesIsInstance<Identifier, ImageResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [font-resources][FontResource] that the book has, mapped like `identifier::resource`.
     */
    val fontResources: ImmutableMap<Identifier, FontResource>
        get() = resources.filterValuesIsInstance<Identifier, FontResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [audio-resources][AudioResource] that the book has, mapped like `identifier::resource`.
     */
    val audioResources: ImmutableMap<Identifier, AudioResource>
        get() = resources.filterValuesIsInstance<Identifier, AudioResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [script-resources][ScriptResource] that the book has, mapped like
     * `identifier::resource`.
     */
    val scriptResources: ImmutableMap<Identifier, ScriptResource>
        get() = resources.filterValuesIsInstance<Identifier, ScriptResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [video-resources][VideoResource] that the book has, mapped like `identifier::resource`.
     */
    val videoResources: ImmutableMap<Identifier, VideoResource>
        get() = resources.filterValuesIsInstance<Identifier, VideoResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [misc-resources][MiscResource] that the book has, mapped like `identifier::resource`.
     */
    val miscResources: ImmutableMap<Identifier, MiscResource>
        get() = resources.filterValuesIsInstance<Identifier, MiscResource>().toPersistentHashMap()

    fun <R : Resource> addResource(resource: R): R {
        // TODO: Is this too extreme?
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

    // TODO: Document that removing a resource will also *delete* it, or maybe make this optional behaviour?

    fun removeResource(identifier: Identifier): Boolean {
        val resource = resources[identifier]
        resources -= identifier
        resource?.onDeletion()
        resource?.file?.delete()
        return resource != null
    }

    fun removeResource(resource: Resource): Boolean {
        val result = resource.identifier in resources
        resources -= resource.identifier
        if (result) {
            resource.onDeletion()
            resource.file.delete()
        }
        return result
    }

    fun getResource(identifier: Identifier): Resource =
        resources.getValueOrThrow(identifier) { "No resource found with the identifier '$identifier'" }

    fun getResourceOrNone(identifier: Identifier): Option<Resource> = resources.getValueOrNone(identifier)

    fun getResourceOrNull(identifier: Identifier): Resource? = resources[identifier]

    fun getResourceByFile(file: Path): Resource =
        getResourceByFileOrNull(file) ?: throw NoSuchElementException("No resource found with the file '$file'")

    fun getResourceByFileOrNone(file: Path): Option<Resource> = resources.values.firstOrNone { it.file == file }

    fun getResourceByFileOrNull(file: Path): Resource? = resources.values.firstOrNull { it.file == file }

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
                is NcxResource -> visitor.onTableOfContents(resource)
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
     * Attempts to move all resources in this repository to their [desired directories][Resource.desiredDirectory].
     *
     * **NOTE:** This function will *directly* modify the [Book.file] of the [book], meaning that if one is not using
     * a backed-up/copied version of the original file, then this will directly modify the source EPUB file. This
     * behaviour may not be what one wants, and if that is the case, make sure that you are working with a copy of the
     * original file, and not the actual original. If the `book` was created by [readBookCopy] or by building your own
     * `Book` instance then there should be no worries.
     *
     * @param [options] TODO
     *
     * @throws [IOException] if an i/o error occurs
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun moveToDesiredDirectories(vararg options: CopyOption = arrayOf()) {
        for (resource in this) {
            val file = resource.file
            val desiredDirectory = resource.desiredDirectory.getOrCreateDirectory()
            if (file.parent != desiredDirectory) {
                logger.trace { "Moving file <${file.name}> from <${file.parent}> to <$desiredDirectory>" }
                resource.file =
                    resource.file.moveTo(desiredDirectory, true, *options) // , StandardCopyOption.REPLACE_EXISTING
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

    // -- INTERNAL -- \\
    @JvmSynthetic
    internal fun populateFromManifest(manifest: Manifest) {
        logger.debug { "Creating resource instances for the book from the manifest.." }
        val localResources = manifest.localItems.values.asSequence()
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
    }

    @JvmSynthetic
    internal fun updateResourceIdentifier(resource: Resource, oldIdentifier: Identifier, newIdentifier: Identifier) {
        book.manifest.updateManifestItemIdentifier(oldIdentifier, newIdentifier)
        resources -= oldIdentifier
        resources[newIdentifier] = resource
    }
}