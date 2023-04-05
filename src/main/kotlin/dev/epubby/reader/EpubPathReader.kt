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

package dev.epubby.reader

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.epubby.Epub
import dev.epubby.reader.EpubReaderError.CorruptMimeType
import dev.epubby.reader.EpubReaderError.FailedToReadAsZip
import dev.epubby.reader.EpubReaderError.MimeTypeContentMismatch
import dev.epubby.reader.EpubReaderError.MissingMetaInf
import dev.epubby.reader.EpubReaderError.MissingMetaInfContainer
import dev.epubby.reader.EpubReaderError.MissingMimeType
import net.ormr.epubby.internal.util.effect
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.ProviderNotFoundException
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

internal class EpubPathReader(private val path: Path) : EpubReader {
    override fun read(): Result<Epub, EpubReaderError> = effect {
        openFileSystem().bind().use { fileSystem ->
            checkFiles(fileSystem).bind()
            TODO("Not yet implemented")
        }
    }

    private fun openFileSystem(): Result<FileSystem, EpubReaderError> = try {
        Ok(FileSystems.newFileSystem(path))
    } catch (e: IOException) {
        Err(FailedToReadAsZip(e))
    } catch (e: ProviderNotFoundException) {
        Err(FailedToReadAsZip(e))
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
        ensure(mimeTypeContent == MIME_TYPE_CONTENT) { MimeTypeContentMismatch }
    }

    private companion object {
        private const val MIME_TYPE_CONTENT = "application/epub+zip"
    }
}