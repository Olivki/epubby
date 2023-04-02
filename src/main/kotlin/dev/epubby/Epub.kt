/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.dublincore.DateEvent
import dev.epubby.dublincore.DublinCore
import dev.epubby.files.DirectoryFile
import dev.epubby.files.EpubFile
import dev.epubby.files.RegularFile
import dev.epubby.files.revamped.ConcreteEpubFileSystem
import dev.epubby.files.revamped.EpubFileSystem
import dev.epubby.internal.models.metainf.MetaInfModel
import dev.epubby.internal.models.packages.PackageDocumentModel
import dev.epubby.internal.verifiers.VerifierEpub
import dev.epubby.metainf.MetaInf
import dev.epubby.packages.PackageDocument
import dev.epubby.packages.PackageManifest
import dev.epubby.packages.PackageSpine
import dev.epubby.packages.metadata.Opf3Meta
import dev.epubby.packages.metadata.PackageMetadata
import dev.epubby.prefixes.PackagePrefix
import dev.epubby.properties.Property
import dev.epubby.properties.matches
import dev.epubby.resources.ResourceFileOrganizer.NameClashStrategy
import dev.epubby.toc.TableOfContents
import krautils.io.touch
import java.io.Closeable
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import kotlin.io.path.moveTo

// TODO: verify that the 'media-type' of a resource truly does match the media-type we get from probing the content
//       type of the resources file, remember to drop the parameters of both to make sure we're comparing them
//       equally
// TODO: verify that the schema of a outputted file, and the schema of a epub we're attempting to read is minimally
//       sound by verifying it against the schemas provided by adobe
//       https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#app-package-schema
//       http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#AppendixA
// TODO: replace all lists that are marked as 'stale' with Sequence<T>

/**
 * Represents an EPUB file.
 *
 * TODO: more documentation
 *
 * @property [version] The version of the EPUB specification that this epub adheres to.
 * @property [fileSystem] TODO
 */
class Epub internal constructor(
    val version: EpubVersion,
    @get:JvmSynthetic
    internal val fileSystem: FileSystem,
    @get:JvmSynthetic
    internal val epubFile: Path,
) : Closeable {
    val epubFileSystem: EpubFileSystem = ConcreteEpubFileSystem(this, fileSystem)

    /**
     * The root directory of the epub.
     *
     * Normally only the `mimetype` file and the `META-INF` and `OEBPS` *(may not always be named `OEBPS`)* directories
     * should be located at the root of a epub. Any *direct* changes *(i.e; [Files.delete], [Files.move])* to any of
     * these files is ***highly discouraged***, as that can, and most likely will, cause severe issues for the system.
     */
    // TODO: will this work correctly?
    // TODO: instead of providing file stuff directly on this instance, provide some sort of Virtual File System container
    //       like 'EpubFileSystem' that uses our custom File types
    val root: DirectoryFile = DirectoryFile(fileSystem.getPath("/"), this)

    /**
     * The `mimetype` file required to exist in every EPUB file.
     *
     * This file can *not* be modified, moved, or deleted.
     */
    val mimeType: RegularFile = root.resolveFile("mimetype")

    @set:JvmSynthetic
    lateinit var metaInf: MetaInf
        internal set

    val opfFile: RegularFile
        get() = metaInf.container.opf.fullPath

    val opfDirectory: DirectoryFile
        get() = opfFile.parent ?: throw IllegalStateException("Opf file should always have a parent!")

    /**
     * The [PackageDocument] of this EPUB.
     */
    @set:JvmSynthetic
    lateinit var packageDocument: PackageDocument
        internal set

    /**
     * The [PackageMetadata] of this EPUB.
     */
    val metadata: PackageMetadata
        get() = packageDocument.metadata

    /**
     * The [PackageManifest] of this EPUB.
     */
    val manifest: PackageManifest
        get() = packageDocument.manifest

    /**
     * The [PackageSpine] of this EPUB.
     */
    val spine: PackageSpine
        get() = packageDocument.spine

    /**
     * The [TableOfContents] of this EPUB.
     */
    @set:JvmSynthetic
    lateinit var tableOfContents: TableOfContents
        internal set

    /**
     * The primary title of the epub.
     *
     * @see [PackageMetadata.primaryTitle]
     */
    var title: String
        get() = metadata.primaryTitle.value
        set(value) {
            metadata.primaryTitle.value = value
        }

    /**
     * The primary author of the epub, or `null` if no primary author is defined.
     *
     * @see [PackageMetadata.primaryAuthor]
     */
    var author: String?
        get() = metadata.primaryAuthor?.value
        set(value) {
            if (value != null) {
                metadata.primaryAuthor?.value = value
            } else {
                metadata.primaryAuthor = null
            }
        }

    /**
     * The primary language of the epub.
     *
     * @see [PackageMetadata.primaryLanguage]
     */
    var language: String
        get() = metadata.primaryLanguage.value
        set(value) {
            metadata.primaryLanguage.value = value
        }

    // TODO: documentation
    // TODO: move this to a like 'EpubFiles' class that contains properties for all the known and important epub files
    @JvmOverloads
    fun organizeFiles(nameClashStrategy: NameClashStrategy = NameClashStrategy.THROW_EXCEPTION) {
        val desiredOpfDirectory = root.resolveDirectory("OEBPS/")

        // TODO: does this work like we want it to?
        if (opfDirectory != desiredOpfDirectory) {
            desiredOpfDirectory.getOrCreateDirectory()
            val opfPath = opfFile.delegate.moveTo(desiredOpfDirectory.delegate.resolve(opfFile.name))
            val newOpfFile = RegularFile(opfPath, this)
            metaInf.container.updateOpfFile(newOpfFile)
        }

        manifest.fileOrganizer.organizeFiles(nameClashStrategy)
    }

    /**
     * Updates the `last-modified` date of this EPUB to the given [date], formatted according to
     * [ISO_LOCAL_DATE_TIME][DateTimeFormatter.ISO_LOCAL_DATE_TIME].
     *
     * This will create a new `last-modified` meta entry if none can be found.
     *
     * The resulting text will use the given [date], with its offset set to [UTC][ZoneOffset.UTC], and formatted with
     * the pattern `"CCYY-MM-DDThh:mm:ssZ"`, as per the EPUB specification.
     */
    @JvmOverloads
    fun setLastModifiedDate(date: LocalDateTime = LocalDateTime.now()) {
        val currentDateTime = date.atOffset(UTC).format(ISO_LOCAL_DATE_TIME)
        LOGGER.debug { "Updating last-modified date of $this to '$currentDateTime'.." }

        when {
            version isOlderThan EPUB_3_0 -> {
                val dublinCore = metadata.dublinCoreEntries
                    .filterIsInstance<DublinCore.Date>()
                    .firstOrNull { it.event == DateEvent.MODIFICATION }

                if (dublinCore != null) {
                    dublinCore.value = currentDateTime
                } else {
                    val entry = DublinCore.Date(this, currentDateTime, event = DateEvent.MODIFICATION)
                    metadata.addDublinCore(entry)
                }
            }

            else -> {
                val property = Property.of(PackagePrefix.DC_TERMS, "modified")
                val meta = metadata.opf3MetaEntries.firstOrNull { property matches it.property }

                if (meta != null) {
                    meta.value = currentDateTime
                } else {
                    metadata.opf3MetaEntries += Opf3Meta(this, currentDateTime, property)
                }
            }
        }
    }

    /**
     * Returns a new [EpubFile] that belongs to this epub.
     *
     * @see [FileSystem.getPath]
     */
    // TODO: rename to something better?
    fun getFile(first: String, vararg more: String): EpubFile =
        root.resolve(EpubFile(fileSystem.getPath(first, *more), this))

    /**
     * Closes the [fileSystem] belonging to this epub, and writes all modifications done to this EPUB back to the
     * appropriate files.
     */
    // TODO: 'close' shouldn't be the thing that writes everything back ideally
    //       we probably want a function that does write everything but also saves the files, but with a clearer name
    //       because 'close' does not make it clear that this function will actually modify the epub.
    //       a normal 'close' should just not save any changes done.
    override fun close() {
        VerifierEpub.verify(this)
        LOGGER.debug { "Closing filesystem of $this and writing contents.." }
        writeFiles()
        fileSystem.close()
    }

    private fun writeFiles() {
        setLastModifiedDate()

        val metaInfModel = MetaInfModel.fromMetaInf(metaInf)
        val packageDocumentModel = PackageDocumentModel.fromPackageDocument(packageDocument)
        metaInfModel.writeToDirectory(fileSystem)
        packageDocumentModel.writeToFile()

        spine.writePagesToFile()
        manifest.writeResourcesToFile()

        //epub.tableOfContents.writeToFile(it)

        epubFile.touch()
    }

    override fun toString(): String = when (val author = this.author) {
        null -> "Epub(title='$title', language=$language)"
        else -> "Epub(title='$title', author='$author', language=$language)"
    }

    private companion object {
        private val LOGGER: InlineLogger = InlineLogger(Epub::class)
    }
}