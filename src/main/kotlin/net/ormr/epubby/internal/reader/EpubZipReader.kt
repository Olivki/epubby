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
import dev.epubby.Epub
import dev.epubby.EpubConfig
import dev.epubby.metainf.MetaInfReadError
import dev.epubby.reader.EpubReader
import dev.epubby.reader.EpubReaderError
import dev.epubby.reader.EpubReaderError.*
import net.lingala.zip4j.model.FileHeader
import net.ormr.epubby.internal.MediaTypes
import net.ormr.epubby.internal.models.metainf.MetaInfContainerModel.RootFileModel
import net.ormr.epubby.internal.models.metainf.MetaInfContainerModelConverter.toRootFile
import net.ormr.epubby.internal.models.metainf.MetaInfContainerModelConverter.toRootFileModel
import net.ormr.epubby.internal.models.metainf.MetaInfModel
import net.ormr.epubby.internal.models.metainf.MetaInfModelXml
import net.ormr.epubby.internal.models.opf.OpfModelXml
import net.ormr.epubby.internal.util.createEpubbyFileSystem
import net.ormr.epubby.internal.util.effect
import net.ormr.epubby.internal.util.loadDocument
import net.ormr.zip.Zip
import net.ormr.zip.readText
import net.ormr.zip.use
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.WRITE
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.outputStream

// https://www.w3.org/publishing/epub3/epub-ocf.html#sec-zip-container-zipreqs
internal class EpubZipReader(private val path: Path, private val config: EpubConfig) : EpubReader<EpubReaderError> {
    override fun read(): Result<Epub, EpubReaderError> = effect {
        val fs = Zip(path).use { zip ->
            checkFiles(zip).bind()
            createFileSystem(zip, zip.fileHeaders)
        }.bind()
        val root = fs.getPath("/")
        val metaInfModel = parseMetaInfModel(root).bind(::MetaInfError)
        val rootFile = findRootFile(metaInfModel).bind()
        val opfFile = root.resolve(rootFile.fullPath)
        ensure(opfFile.exists()) { MissingOpfFile(rootFile.fullPath) }
        // TODO: handle potential error from loadDocument
        val opfDocument = loadDocument(opfFile)
        val opf = OpfModelXml.read(opfDocument.rootElement)
        println(opf)
        TODO()
    }

    private fun findRootFile(metaInf: MetaInfModel): Result<RootFileModel, MissingOebpsRootFileElement> {
        val rootFiles = metaInf
            .container
            .rootFiles
            .filter { it.mediaType == MediaTypes.OEBPS_PACKAGE }
            .ifEmpty { return Err(MissingOebpsRootFileElement) }
        val rootFile = when (rootFiles.size) {
            1 -> rootFiles.first()
            else -> config.multipleOebpsFileResolver(rootFiles.map { it.toRootFile() }).toRootFileModel()
        }
        return Ok(rootFile)
    }

    private fun parseMetaInfModel(root: Path): Result<MetaInfModel, MetaInfReadError> =
        MetaInfModelXml.readFiles(root / "META-INF")

    // copies all the files from the currently opened epub zip file to an in memory filesystem
    private fun createFileSystem(
        zip: Zip,
        files: List<FileHeader>,
    ): Result<FileSystem, FailedToCreateFileSystem> = try {
        val fs = createEpubbyFileSystem()
        for (file in files) {
            val fileName = file.fileName
            val target = fs.getPath(fileName)
            target.parent?.createDirectories()
            if (file.isDirectory) continue
            zip.newInputStream(file).use { input ->
                // 'CREATE_NEW' should logically never fail as there shouldn't be any duplicate paths
                // but in case it does, we want it to be vocal about it, as that's unexpected behavior,
                // instead of just silently writing to the file as 'CREATE' would do
                target.outputStream(CREATE_NEW, WRITE).use { output ->
                    input.copyTo(output)
                }
            }
            // TODO: copy over attributes
        }
        Ok(fs)
    } catch (e: IOException) {
        Err(FailedToCreateFileSystem(e))
    }

    // verifies that the epub is, at the very least, minimally sound.
    private fun checkFiles(zip: Zip): Result<Unit, EpubReaderError> = effect {
        val metaInfContainer = zip.getFileHeader("META-INF/container.xml")
        val mimeType = zip.getFileHeader("mimetype")
        ensure(metaInfContainer != null && !metaInfContainer.isDirectory) { MissingMetaInfContainer }
        ensure(mimeType != null && !mimeType.isDirectory) { MissingMimeType }
        val mimeTypeContent = try {
            zip.readText(mimeType, Charsets.US_ASCII)
        } catch (e: IOException) {
            shift(CorruptMimeType(e))
        }
        ensure(mimeTypeContent == MIME_TYPE_CONTENT) { MimeTypeContentMismatch(mimeTypeContent) }
    }

    private companion object {
        private const val MIME_TYPE_CONTENT = "application/epub+zip"
    }
}