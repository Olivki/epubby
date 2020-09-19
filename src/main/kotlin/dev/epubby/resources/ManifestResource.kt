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

import com.github.michaelbull.logging.InlineLogger
import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.files.DirectoryFile
import dev.epubby.files.RegularFile
import dev.epubby.internal.IntroducedIn
import dev.epubby.packages.PackageDocument
import dev.epubby.packages.PackageManifest
import dev.epubby.packages.metadata.Opf2Meta
import dev.epubby.properties.Properties
import dev.epubby.properties.vocabularies.ManifestVocabulary
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import moe.kanon.kommons.reflection.loadServices
import java.io.IOException
import kotlin.properties.Delegates

// TODO: documentation

sealed class ManifestResource : EpubElement {
    abstract override val epub: Epub

    /**
     * The identifier of this resource.
     *
     * The identifier of a resource is what it's generally used throughout the system to refer to a specific resource,
     * however, some parts of the system may use the [href] of a resource to refer to it.
     */
    abstract val identifier: String

    /**
     * A URI pointing to the location of the file that this resource is wrapping around.
     *
     * For [LocalResource] instances this will be pointing towards a local file, while a [ExternalResource] will be
     * pointing towards an URI.
     */
    abstract val href: String

    /**
     * The [MediaType] of this resource.
     */
    abstract val mediaType: MediaType

    /**
     * The resource that a reading system should use instead if it can't properly display this resource.
     */
    abstract var fallback: ManifestResource?

    abstract val mediaOverlay: String?

    /**
     * The properties belonging to this resource.
     *
     * See [ManifestVocabulary] for a list of properties that are supported by default in an EPUB.
     */
    @IntroducedIn(version = EPUB_3_0)
    abstract val properties: Properties

    /**
     * Returns a list of [Opf2Meta.Name] instances that provide additional metadata for this resource.
     *
     * The returned list will become stale the moment any `meta` element is added or removed from the epub, or when the
     * [content][Opf2Meta.Name.content] property of the `meta` element is changed, therefore it is not recommended to
     * cache the returned list, instead one should retrieve a new one when needed.
     */
    val additionalMetadata: PersistentList<Opf2Meta.Name>
        get() = epub.metadata
            .opf2MetaEntries
            .asSequence()
            .filterIsInstance<Opf2Meta.Name>()
            .filter { it.isFor(this) }
            .asIterable()
            .toPersistentList()

    /**
     * Returns the result of invoking the appropriate `visitXXX` function of the given [visitor].
     */
    abstract fun <R> accept(visitor: ResourceVisitor<R>): R
}

abstract class LocalResource internal constructor(
    override val epub: Epub,
    identifier: String,
    file: RegularFile,
) : ManifestResource() {
    final override val elementName: String
        get() = "PackageManifest.LocalResource"

    /**
     * The [MediaType] of `this` resource.
     *
     * TODO: more documentation
     */
    abstract override val mediaType: MediaType

    /**
     * The file that this resource is wrapping around.
     *
     * If one wants to delete the `file` of a resource in a *safe* manner, see [PackageManifest.removeLocalResource].
     */
    var file: RegularFile = file
        get() {
            check(field.exists) {
                if (isDeleted) {
                    "Resource '$identifier' does not exist anymore as it has been deleted, this resource should not be used anymore."
                } else {
                    "The file for resource '$identifier' does not exist anymore, illegal user operations have most likely been done."
                }
            }
            return field
        }
        @JvmSynthetic
        internal set(newFile) {
            val oldFile = field
            LOGGER.debug { "Changing file of resource '$identifier' from '$oldFile' to '$newFile'" }
            field = newFile
            epub.manifest.updateLocalResourceFile(oldFile, newFile)
            updateReferences(newFile)
            onFileSet(oldFile, newFile)
        }

    /**
     * Returns the directory that this resource would like to belong to.
     *
     * The desired directory of a resource is determined by the [fileOrganizer][PackageManifest.fileOrganizer] of the
     * `manifest` of the book that the resource belongs to.
     */
    val desiredDirectory: DirectoryFile
        get() = epub.manifest.fileOrganizer.getDirectory(javaClass)

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var isDeleted: Boolean = false

    /**
     * Returns a path that's relative to the [OPF file][PackageDocument.file] of the [epub].
     */
    val relativeFile: RegularFile
        get() = epub.opfFile.relativizeFile(file)

    override val href: String
        get() = epub.opfDirectory.relativize(file).toString()

    // TODO: replace any 'relativeHref.substringAfter("../")' with a function that just uses the index instead for
    //       faster speed? or maybe make a property/function specifically for returning a value like that?
    val relativeHref: String
        get() = relativeFile.toString()

    override val properties: Properties = Properties.empty()

    override var fallback: ManifestResource? = null

    override var identifier: String by Delegates.observable(identifier) { _, old, new ->
        epub.manifest.updateLocalResourceIdentifier(this, old, new)

        epub.metadata
            .opf2MetaEntries
            .asSequence()
            .filterIsInstance<Opf2Meta.Name>()
            .filter { it.content == old }
            .forEach { it.content = identifier }
    }

    /**
     * Returns a list of HTML entities that are referencing this resource in some manner.
     *
     * The returned list will become stale the moment any change is done to the [file] of this resource, or if the
     * document the reference heralds from gets changed in any manner, therefore it is not recommended to cache the
     * returned list, instead one should retrieve a new one when needed.
     */
    val documentReferences: PersistentList<ResourceDocumentReference>
        get() = epub.spine.getDocumentReferencesOf(this)

    override var mediaOverlay: String? = null

    private fun updateReferences(newFile: RegularFile) {
        for (reference in documentReferences) {
            reference.updateReferenceTo(this, newFile)
        }
    }

    /**
     * Returns whether or not the given [href] is equal to this resources [href][ManifestResource.href].
     *
     * This function also takes care of cases where an `href` attribute might contain a fragment-identifier *(`#`)*.
     */
    @JvmOverloads
    fun isHrefEqual(href: String, ignoreCase: Boolean = false): Boolean {
        // TODO: is this correct?
        val realHref = when {
            href.startsWith('#') -> href.drop(1).substringBefore('#').substringAfter("../")
            else -> href.substringBefore('#').substringAfter("../")
        }

        return realHref.equals(this.href, ignoreCase)
    }

    /**
     * Gets invoked when this resource is being removed from the [epub].
     */
    protected open fun onRemoval() {}

    /**
     * Gets invoked when the [file] of this resource has been changed.
     */
    protected open fun onFileSet(oldFile: RegularFile, newFile: RegularFile) {}

    /**
     * Gets invoked when the [file] of this resource has been modified in some manner.
     *
     * For example, invoking [RegularFile.writeLines] on `file` would result in this function being invoked.
     */
    protected open fun onFileModified() {}

    /**
     * Invoked when the [epub] that this resource belongs to is being saved.
     */
    protected open fun writeToFile() {}

    abstract override fun <R> accept(visitor: ResourceVisitor<R>): R

    // TODO: document and make sure to mention that 'epub' is not used for equality checks
    final override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is LocalResource -> false
        mediaType != other.mediaType -> false
        identifier != other.identifier -> false
        properties != other.properties -> false
        fallback != other.fallback -> false
        mediaOverlay != other.mediaOverlay -> false
        href != other.href -> false
        else -> true
    }

    // TODO: document and make sure to mention that 'epub' is not used for hashCode generation
    final override fun hashCode(): Int {
        var result = mediaType.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + (fallback?.hashCode() ?: 0)
        result = 31 * result + (mediaOverlay?.hashCode() ?: 0)
        result = 31 * result + href.hashCode()
        return result
    }

    @JvmSynthetic
    internal fun triggerRemoval() {
        onRemoval()
    }

    @JvmSynthetic
    internal fun triggerFileModified() {
        onFileModified()
    }

    @JvmSynthetic
    internal fun triggerWriteToFile() {
        writeToFile()
    }

    companion object {
        private val LOGGER: InlineLogger = InlineLogger(LocalResource::class)
        private val LOCATORS: List<LocalResourceLocator> by lazy { loadServices<LocalResourceLocator>().toPersistentList() }
        private val FACTORY_CACHE: MutableMap<MediaType, LocalResourceFactory> = hashMapOf()

        private fun getFactory(mediaType: MediaType): LocalResourceFactory? = when (mediaType) {
            in FACTORY_CACHE -> FACTORY_CACHE.getValue(mediaType)
            else -> LOCATORS.asSequence()
                // TODO: .map { it.findFactory(mediaType.withoutParameters()) } ?
                .map { it.findFactory(mediaType) }
                .filterNotNull()
                .firstOrNull()
                ?.also { FACTORY_CACHE.putIfAbsent(mediaType, it) }
        }

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun create(
            file: RegularFile,
            epub: Epub,
            identifier: String = "x_${file.name}",
            mediaType: MediaType = getMediaType(file),
        ): LocalResource = getFactory(mediaType)?.invoke(epub, identifier, file, mediaType)
            ?: MiscResource(epub, identifier, file, mediaType)

        private fun getMediaType(file: RegularFile): MediaType =
            file.mediaType ?: throw IllegalArgumentException("Could not determine the media-type of file '$file'")
    }
}

class ExternalResource @JvmOverloads constructor(
    override val epub: Epub,
    override val identifier: String,
    // TODO: make this into a 'URL' or 'URI'?
    override var href: String,
    override val mediaType: MediaType,
    override var fallback: ManifestResource? = null,
    override var mediaOverlay: String? = null,
    // TODO: verify that the version isn't older than 3.0 if this is used at some point
    //       make sure to always add the remote-resource property to the model
    override val properties: Properties = Properties.empty(),
) : ManifestResource() {
    override val elementName: String
        get() = "PackageManifest.ExternalResource"

    /**
     * Returns the result of invoking the [visitRemote][ResourceVisitor.visitExternal] function of the given [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitExternal(this)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ExternalResource -> false
        identifier != other.identifier -> false
        href != other.href -> false
        mediaType != other.mediaType -> false
        fallback != other.fallback -> false
        mediaOverlay != other.mediaOverlay -> false
        properties != other.properties -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + href.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + (fallback?.hashCode() ?: 0)
        result = 31 * result + (mediaOverlay?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()
        return result
    }

    override fun toString(): String =
        "ExternalResource(identifier='$identifier', href='$href', mediaType=$mediaType, fallback=$fallback, mediaOverlay=$mediaOverlay, properties=$properties)"
}