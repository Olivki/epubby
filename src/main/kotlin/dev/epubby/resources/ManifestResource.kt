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

import com.google.common.net.MediaType
import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.BookVersion.EPUB_3_0
import dev.epubby.files.RegularFile
import dev.epubby.internal.IntroducedIn
import dev.epubby.packages.PackageDocument
import dev.epubby.packages.PackageManifest
import dev.epubby.packages.metadata.Opf2Meta
import dev.epubby.properties.Properties
import dev.epubby.properties.vocabularies.ManifestVocabulary
import dev.epubby.utils.verifyFile
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import moe.kanon.kommons.io.paths.*
import moe.kanon.kommons.reflection.KServiceLoader
import moe.kanon.kommons.reflection.loadServices
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.properties.Delegates

// TODO: documentation

sealed class ManifestResource : BookElement {
    abstract override val book: Book

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
     * For [LocalResource] instances this will be pointing towards a local file, while a [RemoteResource] will be
     * pointing towards an URI.
     */
    abstract val href: String

    /**
     * The [MediaType] of this resource, or `null` if none is defined.
     */
    abstract val mediaType: MediaType?

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
     * The returned list will become stale the moment any `meta` element is added or removed from the book, or when the
     * [content][Opf2Meta.Name.content] property of the `meta` element is changed, therefore it is not recommended to
     * cache the returned list, instead one should retrieve a new one when needed.
     */
    val additionalMetadata: PersistentList<Opf2Meta.Name>
        get() = book.metadata
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
    override val book: Book,
    identifier: String,
    file: Path,
) : ManifestResource() {
    companion object {
        private val LOCATORS: KServiceLoader<LocalResourceLocator> = loadServices()

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun fromFile(
            file: Path,
            book: Book,
            identifier: String = "x_${file.name}",
        ): LocalResource {
            require(identifier !in book.manifest) { "Identifier '$identifier' must be unique." }
            verifyFile(book, file)
            val mediaType =
                requireNotNull(file.contentType?.let(MediaType::parse)) { "Could not determine media type of file '$file'." }
            return LOCATORS.asSequence()
                .map { it.findFactory(mediaType) }
                .filterNotNull()
                .firstOrNull()
                ?.invoke(book, identifier, file, mediaType) ?: MiscResource(book, identifier, file, mediaType)
        }
    }

    final override val elementName: String
        get() = "PackageManifest.LocalResource"

    /**
     * The [MediaType] of `this` resource.
     *
     * TODO: more documentation
     */
    abstract override val mediaType: MediaType?

    /**
     * The file that this resource is wrapping around.
     *
     * It is ***HIGHLY DISCOURAGED*** to do any operations on this file outside of the functions provided *([renameTo],
     * [moveTo])*, as the ability to change this property is *internal only*, any outside operations, like [Files.move]
     * and the like will leave this property pointing to a non-existent file, which will result in a
     * [IllegalStateException] being thrown at some point.
     *
     * If one wants to delete the `file` of a resource in a *safe* manner, see [PackageManifest.removeLocalResource].
     */
    var file: Path = file
        get() {
            checkExistence()
            return field
        }
        @JvmSynthetic
        internal set(value) {
            checkExistence()
            require(value.isRegularFile) { "value must not be a directory" }
            val oldFile = field
            verifyFile(book, file)
            require(isValidDirectoryTarget(value.parent)) { "'${value.parent}' is not an allowed directory for resources" }
            field = value
            updateReferences(value)
            onFileChanged(oldFile, value)
        }

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var isDeleted: Boolean = false

    /**
     * Returns a path that's relative to the [OPF file][PackageDocument.file] of the [book].
     */
    val relativeFile: RegularFile
        get() = book.packageDocument.file.relativize(file)

    override val href: String
        get() = book.packageDocument.directory.relativize(file).fullPath

    // TODO: replace any 'relativeHref.substringAfter("../")' with a function that just uses the index instead for
    //       faster speed? or maybe make a property/function specifically for returning a value like that?
    val relativeHref: String
        get() = relativeFile.toString()

    override val properties: Properties = Properties.empty()

    override var fallback: ManifestResource? = null

    override var identifier: String by Delegates.observable(identifier) { _, old, new ->
        book.manifest.updateLocalResourceIdentifier(this, old, new)

        book.metadata
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
        get() = book.spine.getDocumentReferencesOf(this)

    override var mediaOverlay: String? = null

    private fun checkExistence() {
        check(file.exists) {
            if (isDeleted) {
                "Resource '$this' does not exist anymore as it has been deleted, this resource should not be used anymore."
            } else {
                "The file for resource '$this' does not exist anymore, illegal user operations have most likely been done."
            }
        }
    }

    private fun isValidDirectoryTarget(directory: Path): Boolean = when {
        directory.name.equals("META-INF", ignoreCase = true) -> false
        directory isSameAs book.root -> false
        else -> true
    }

    private fun updateReferences(newFile: Path) {
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
        val realHref = href.substringAfter('#')

        return realHref.equals(this.href, ignoreCase) ||
            realHref.equals(relativeHref, ignoreCase) ||
            realHref.equals(relativeHref.substringAfter("../"), ignoreCase)
    }

    // TODO: document these two

    @JvmOverloads
    fun renameTo(simpleName: String, replaceExisting: Boolean = false) {
        val options = if (replaceExisting) arrayOf(COPY_ATTRIBUTES, REPLACE_EXISTING) else arrayOf(COPY_ATTRIBUTES)
        file = file.renameTo(file.extension?.let { "$simpleName.$it" } ?: simpleName, *options)
    }

    @JvmOverloads
    fun moveTo(directory: Path, replaceExisting: Boolean = false) {
        require(isValidDirectoryTarget(directory)) { "'$directory' is not an allowed directory for resources" }
        val options = if (replaceExisting) arrayOf(COPY_ATTRIBUTES, REPLACE_EXISTING) else arrayOf(COPY_ATTRIBUTES)
        file = file.moveTo(directory, keepName = true, *options)
    }

    protected open fun onFileChanged(oldFile: Path, newFile: Path) {}

    abstract override fun <R> accept(visitor: ResourceVisitor<R>): R

    // TODO: document and make sure to mention that 'book' is not used for equality checks
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

    // TODO: document and make sure to mention that 'book' is not used for hashCode generation
    final override fun hashCode(): Int {
        var result = mediaType.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + (fallback?.hashCode() ?: 0)
        result = 31 * result + (mediaOverlay?.hashCode() ?: 0)
        result = 31 * result + href.hashCode()
        return result
    }
}

class RemoteResource @JvmOverloads constructor(
    override val book: Book,
    override val identifier: String,
    // TODO: make this into a 'URL' or 'URI'?
    override var href: String,
    override val mediaType: MediaType? = null,
    override var fallback: ManifestResource? = null,
    override var mediaOverlay: String? = null,
    // TODO: verify that the version isn't older than 3.0 if this is used at some point
    //       make sure to always add the remote-resource property to the model
    override val properties: Properties = Properties.empty(),
) : ManifestResource() {
    override val elementName: String
        get() = "PackageManifest.RemoteResource"

    /**
     * Returns the result of invoking the [visitRemote][ResourceVisitor.visitRemote] function of the given [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitRemote(this)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is RemoteResource -> false
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
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (fallback?.hashCode() ?: 0)
        result = 31 * result + (mediaOverlay?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()
        return result
    }

    override fun toString(): String =
        "RemoteResource(identifier='$identifier', href='$href', mediaType=$mediaType, fallback=$fallback, mediaOverlay=$mediaOverlay, properties=$properties)"
}