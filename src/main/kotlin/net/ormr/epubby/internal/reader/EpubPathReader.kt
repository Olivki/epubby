/*
 * Copyright 2019-2023 Oliver Berg
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

package net.ormr.epubby.internal.reader

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import dev.epubby.Epub
import dev.epubby.metainf.MetaInfReadError
import dev.epubby.reader.EpubReader
import dev.epubby.reader.EpubReaderError
import dev.epubby.reader.EpubReaderError.*
import net.ormr.epubby.internal.MediaTypes
import net.ormr.epubby.internal.models.content.PackageDocumentModelXml
import net.ormr.epubby.internal.models.metainf.MetaInfModel
import net.ormr.epubby.internal.models.metainf.MetaInfModelXml
import net.ormr.epubby.internal.util.effect
import net.ormr.epubby.internal.util.loadDocument
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Path
import java.nio.file.ProviderNotFoundException
import kotlin.io.path.*

internal class EpubPathReader(private val path: Path) : EpubReader<EpubReaderError> {
    override fun read(): Result<Epub, EpubReaderError> = effect {
        val fs = openFileSystem().bind().use { fs ->
            checkFiles(fs).bind()
            createFileSystem(fs.getPath("/"))
        }.bind()
        val root = fs.getPath("/")
        val metaInfModel = parseMetaInfModel(root).bind(::MetaInfError)
        // TODO: implement some strategy that a user can define for resolving issues where there are multiple
        //       'OEBPS_PACKAGE' package files available
        val rootFile = metaInfModel
            .container
            .rootFiles
            .find { it.mediaType == MediaTypes.OEBPS_PACKAGE } ?: shift(MissingOebpsRootFileElement)
        val opfFile = fs.getPath(rootFile.fullPath)
        ensure(opfFile.exists()) { MissingOpfFile(rootFile.fullPath) }
        // TODO: handle potential error from loadDocument
        val opfDocument = loadDocument(opfFile)
        val opf = PackageDocumentModelXml.read(opfDocument.rootElement)
        println(opf)
        TODO()
    }

    private fun parseMetaInfModel(root: Path): Result<MetaInfModel, MetaInfReadError> =
        MetaInfModelXml.readFiles(root / "META-INF")

    // copies all the files from the currently opened epub zip file to an in memory filesystem
    @OptIn(ExperimentalPathApi::class)
    private fun createFileSystem(root: Path): Result<FileSystem, FailedToCreateFileSystem> = try {
        val fs = Jimfs.newFileSystem("epubby", Configuration.unix())
        root.visitFileTree {
            onPreVisitDirectory { originalDirectory, _ ->
                // TODO: copy attributes
                val directory = fs.getPath(originalDirectory.pathString)
                directory.createDirectories()
                CONTINUE
            }

            onVisitFile { originalFile, _ ->
                // TODO: copy attributes
                val file = fs.getPath(originalFile.pathString)
                originalFile.copyTo(file)
                CONTINUE
            }
        }
        Ok(fs)
    } catch (e: IOException) {
        Err(FailedToCreateFileSystem(e))
    }

    private fun openFileSystem(): Result<FileSystem, EpubReaderError> = try {
        Ok(FileSystems.newFileSystem(path))
    } catch (e: IOException) {
        Err(FailedToOpenFile(e))
    } catch (e: ProviderNotFoundException) {
        Err(FailedToOpenFile(e))
    }

    // verifies that the epub is, at the very least, minimally sound.
    private fun checkFiles(fileSystem: FileSystem): Result<Unit, EpubReaderError> = effect {
        val root = fileSystem.getPath("/")
        val metaInf = root.resolve("META-INF")
        val mimeType = root.resolve("mimetype")
        ensure(metaInf.exists() && metaInf.isDirectory()) { MissingMetaInf }
        val container = metaInf.resolve("container.xml")
        ensure(container.exists() && container.isRegularFile()) { MissingMetaInfContainer }
        ensure(mimeType.exists() && mimeType.isRegularFile()) { MissingMimeType }
        val mimeTypeContent = try {
            mimeType.readText(StandardCharsets.US_ASCII)
        } catch (_: IOException) {
            shift(CorruptMimeType)
        }
        ensure(mimeTypeContent == MIME_TYPE_CONTENT) { MimeTypeContentMismatch(mimeTypeContent) }
    }

    private companion object {
        private const val MIME_TYPE_CONTENT = "application/epub+zip"
    }
}