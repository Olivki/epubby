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

package moe.kanon.epubby.resources

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentHashMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.LegacyFeature
import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.packages.PackageManifest
import moe.kanon.epubby.structs.Identifier
import moe.kanon.epubby.structs.props.vocabs.ManifestVocabulary
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.collections.filterValuesIsInstance
import moe.kanon.kommons.collections.getOrThrow
import moe.kanon.kommons.io.paths.createDirectories
import moe.kanon.kommons.io.paths.delete
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.getOrCreateDirectory
import moe.kanon.kommons.io.paths.moveTo
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.requireThat
import java.io.IOException
import java.nio.file.CopyOption
import java.nio.file.FileSystem
import java.nio.file.Path

class Resources internal constructor(val book: Book) : Iterable<Resource> {
    private val resources: MutableMap<Identifier, Resource> = hashMapOf()

    /**
     * Returns a map of all the resources in the [book], mapped like `identifier::resource`.
     */
    val entries: ImmutableMap<Identifier, Resource> get() = resources.toPersistentHashMap()

    // TODO: Change names back to have the 'Resources' suffix?

    /**
     * Returns a map of all the [page-resources][PageResource] that the book has, mapped like `identifier::resource`.
     */
    val pages: ImmutableMap<Identifier, PageResource>
        get() = resources.filterValuesIsInstance<Identifier, PageResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [stylesheet-resources][StyleSheetResource] that the book has, mapped like
     * `identifier::resource`.
     */
    val styleSheets: ImmutableMap<Identifier, StyleSheetResource>
        get() = resources.filterValuesIsInstance<Identifier, StyleSheetResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [image-resources][ImageResource] that the book has, mapped like `identifier::resource`.
     */
    val images: ImmutableMap<Identifier, ImageResource>
        get() = resources.filterValuesIsInstance<Identifier, ImageResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [font-resources][FontResource] that the book has, mapped like `identifier::resource`.
     */
    val fonts: ImmutableMap<Identifier, FontResource>
        get() = resources.filterValuesIsInstance<Identifier, FontResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [audio-resources][AudioResource] that the book has, mapped like `identifier::resource`.
     */
    val audio: ImmutableMap<Identifier, AudioResource>
        get() = resources.filterValuesIsInstance<Identifier, AudioResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [script-resources][ScriptResource] that the book has, mapped like
     * `identifier::resource`.
     */
    val scripts: ImmutableMap<Identifier, ScriptResource>
        get() = resources.filterValuesIsInstance<Identifier, ScriptResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [video-resources][VideoResource] that the book has, mapped like `identifier::resource`.
     */
    val videos: ImmutableMap<Identifier, VideoResource>
        get() = resources.filterValuesIsInstance<Identifier, VideoResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [misc-resources][MiscResource] that the book has, mapped like `identifier::resource`.
     */
    val miscellaneous: ImmutableMap<Identifier, MiscResource>
        get() = resources.filterValuesIsInstance<Identifier, MiscResource>().toPersistentHashMap()

    /**
     * Returns a map of all the [ncx-resources][NcxResource]  that the book has [book], mapped like
     * `identifier::resource`.
     */
    val ncx: ImmutableMap<Identifier, NcxResource>
        get() = resources.filterValuesIsInstance<Identifier, NcxResource>().toPersistentHashMap()

    /**
     * Returns the [NcxResource] that represents the main ncx document used as the
     * [tableOfContents][Book.tableOfContents] for the book.
     *
     * Note that the `toc` attribute that this relies on is marked as a **LEGACY** feature as of
     * [EPUB 3.0][_BookVersion.EPUB_3_0], so there is no guarantee that this will return anything.
     */
    @LegacyFeature(since = "3.0")
    fun getTableOfContentsNcx(): NcxResource? =
        when (val item = book.spine.tableOfContents?.let { getResourceByFileOrNull(it.href) }) {
            null -> null
            !is NcxResource -> {
                logger.warn { "Book spine 'toc' attribute does NOT point towards a ncx-resource <$item>" }
                null
            }
            else -> item
        }

    // TODO: documentation
    @LegacyFeature(since = "3.0")
    fun setTableOfContentsNcx(resource: NcxResource) {
        book.spine.tableOfContents = resource.manifestItem
    }

    fun <R : Resource> addResource(resource: R): R {
        // TODO: unsure if there really can only be one ncx per book, as the 'toc' attribute seems to be there to
        //       indicate which one to actually use, otherwise the 'toc' attribute shouldn't be needed, so this is
        //       removed for now
        //requireThat(resource !is NcxResource) { "there can only be one ncx-resource per book" }
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
     * storing the resource inside the [manifest][PackageManifest] of the [book]
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
        resources.getOrThrow(identifier) { "No resource found with the identifier '$identifier'" }

    fun getResourceOrNull(identifier: Identifier): Resource? = resources[identifier]

    fun getResourceByFile(file: Path): Resource =
        getResourceByFileOrNull(file) ?: throw NoSuchElementException("No resource found with the file '$file'")

    fun getResourceByFileOrNull(file: Path): Resource? = resources.values.firstOrNull { it.file == file }

    @JvmOverloads
    fun getResourceByHref(href: String, ignoreCase: Boolean = false): Resource =
        getResourceByHrefOrNull(href, ignoreCase)
            ?: throw NoSuchElementException("No resource found with the given href '$href'")

    @JvmOverloads
    fun getResourceByHrefOrNull(href: String, ignoreCase: Boolean = false): Resource? =
        resources.values.firstOrNull { it.isHrefEqual(href, ignoreCase) }

    /**
     * Returns `true` if there exists a resource with the given [identifier], `false` otherwise.
     */
    fun hasResource(identifier: Identifier): Boolean = identifier in resources

    /**
     * Returns `true` if the given [resource] is known, `false` otherwise.
     */
    fun hasResource(resource: Resource): Boolean = resources.containsValue(resource)

    // -- UTILS -- \\
    fun <R> visitResources(visitor: Resource.Visitor<R>): ImmutableList<R> = resources.values
        .map { it.accept(visitor) }
        .toImmutableList()

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
    // TODO: Remove?
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

    fun <T : Resource> getResources(filter: Class<T>): ImmutableList<T> = entries.values
        .filterIsInstance(filter)
        .toImmutableList()

    @JvmSynthetic
    inline fun <reified T : Resource> getResources(): ImmutableList<T> = getResources(T::class.java)

    /**
     * Returns the [desired directory][Resource.desiredDirectory] of the given [resource].
     */
    fun <T : Resource> getDesiredDirectoryOf(resource: Class<T>): Path = when (resource) {
        NcxResource::class.java -> book.packageRoot
        PageResource::class.java -> book.packageRoot.resolve("Text/")
        StyleSheetResource::class.java -> book.packageRoot.resolve("Styles/")
        ImageResource::class.java -> book.packageRoot.resolve("Images/")
        FontResource::class.java -> book.packageRoot.resolve("Fonts/")
        AudioResource::class.java -> book.packageRoot.resolve("Audio/")
        ScriptResource::class.java -> book.packageRoot.resolve("Scripts/")
        VideoResource::class.java -> book.packageRoot.resolve("Video/")
        MiscResource::class.java -> book.packageRoot.resolve("Misc/")
        else -> throw IllegalArgumentException("Unknown resource class <$resource>")
    }

    /**
     * Returns the [desired directory][Resource.desiredDirectory] of the given [resource type][T].
     */
    @JvmSynthetic
    inline fun <reified T : Resource> getDesiredDirectoryOf(): Path = getDesiredDirectoryOf(T::class.java)

    /**
     * Returns a list of all the directories that the underlying [files][Resource.file] of the `resources` are stored
     * in. The list is ordered by frequency of directory, with the highest being first, and lowest being last.
     *
     * If there are no resources of the given [type] available in the `book`, then a list containing only the
     * [desired directory][Resource.desiredDirectory] of the resource type will be returned.
     */
    // TODO: Does this work?
    fun <T : Resource> getDirectoriesUsedBy(resource: Class<T>): ImmutableList<Path> {
        val allDirectories = resources
            .values
            .asSequence()
            .filterIsInstance(resource)
            .map { it.file.parent }
        val sortedDirectories = allDirectories
            .distinct()
            .map { it to allDirectories.count { dir -> it == dir } }
            .sortedByDescending { it.second }
            .map { it.first }
            .asIterable()
            .toImmutableList()
        return if (sortedDirectories.isEmpty()) {
            persistentListOf(getDesiredDirectoryOf(resource).createDirectories())
        } else {
            sortedDirectories
        }
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
    internal fun populateFromManifest(manifest: PackageManifest) {
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

    @JvmSynthetic
    internal fun writeResourcesToFile(fileSystem: FileSystem) {
        logger.debug { "Writing all style-sheets to their respective files.." }
        book.transformers.transformStyleSheets()
        for ((_, resource) in styleSheets) {
            resource.writeToFile(fileSystem)
        }

        logger.debug { "Writing all images to their respective files.." }
        for ((_, resource) in images) {
            resource.writeToFile(fileSystem)
        }
    }
}