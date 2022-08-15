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

@file:JvmName("BookFactory")

package dev.epubby

import dev.epubby.internal.models.metainf.MetaInfModel
import dev.epubby.internal.models.packages.PackageDocumentModel
import dev.epubby.internal.utils.documentFrom
import dev.epubby.internal.utils.getAttributeValueOrThrow
import dev.epubby.internal.utils.use
import org.jdom2.Document
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

@JvmOverloads
fun readEpub(epubFile: Path, mode: ParseMode = ParseMode.STRICT): Epub {
    val fileSystem = try {
        FileSystems.newFileSystem(epubFile, null as ClassLoader?)
    } catch (e: IOException) {
        throw IOException("Could not create a zip file-system for '$epubFile'.", e)
    }
    val root = fileSystem.getPath("/")
    val metaInfDirectory = root.resolve("META-INF")
    val mimeTypeFile = root.resolve("mimetype")
    verifyFile(mimeTypeFile, metaInfDirectory, fileSystem)
    val metaInfModel = MetaInfModel.fromDirectory(metaInfDirectory, mode)
    val opfFile = fileSystem.getPath(metaInfModel.container.rootFiles[0].fullPath)
    val (opfDocument, version) = getVersionAndDocument(opfFile)
    val epub = Epub(version, fileSystem, epubFile)
    epub.metaInf = metaInfModel.toMetaInf(epub)
    val packageDocumentModel = PackageDocumentModel.fromDocument(opfDocument, opfFile, mode)
    epub.packageDocument = packageDocumentModel.toPackageDocument(epub)
    // TODO: this
    /*val tableOfContents = when (version) {
        EPUB_2_0 -> {
            val file = epub.spine.tableOfContents.file.delegate
            NavigationCenterExtendedModel.fromFile(file, mode)
                .map { it.toTableOfContents(epub, mode) }
                .unwrap()
        }
        EPUB_3_0, EPUB_3_1, EPUB_3_2 -> TODO()
    }
    epub.tableOfContents = tableOfContents*/

    return epub
}

// Verifies that the EPUB is, at least, minimally sound.
private fun verifyFile(mimeType: Path, metaInf: Path, fileSystem: FileSystem) {
    // the 'META-INF' directory NEEDS to exist because the 'container.xml' NEEDS to exist inside of it
    if (metaInf.notExists() || !metaInf.isDirectory()) {
        invalidFile("'META-INF' directory is missing from root of epub", fileSystem)
    }

    val container = metaInf.resolve("container.xml")

    // the 'container.xml' file NEEDS to exist, as it is the document providing the location of the 'opf' document
    if (container.notExists() || !container.isRegularFile()) {
        invalidFile("'container.xml' file is missing from the '/META-INF/' directory", fileSystem)
    }

    // 'mimetype' NEEDS to exist in the root of the epub file
    if (mimeType.notExists() || !mimeType.isRegularFile()) {
        invalidFile("'mimetype' file is missing from root of epub", fileSystem)
    }

    // 'mimetype' NEEDS to contain the string "application/epub+zip" encoded in ASCII
    if (mimeType.exists()) {
        val contents = try {
            mimeType.readText(StandardCharsets.US_ASCII)
        } catch (e: IOException) {
            invalidFile("could not read 'mimetype' file with ASCII charset: ${e.message}", fileSystem, e)
        }

        if (contents != "application/epub+zip") {
            invalidFile("contents of 'mimetype' should be 'application/epub+zip', was '$contents'", fileSystem)
        }
    }
}

private fun invalidFile(message: String, fileSystem: FileSystem, cause: Throwable? = null): Nothing {
    fileSystem.close()
    throw MalformedBookException("Invalid EPUB file: $message", cause)
}

private fun getVersionAndDocument(opfFile: Path): Pair<Document, EpubVersion> {
    val document = documentFrom(opfFile)
    val version = document.use { _, root -> root.getAttributeValueOrThrow("version").let { EpubVersion.parse(it) } }
    return document to version
}