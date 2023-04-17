/*
 * Copyright 2022-2023 Oliver Berg
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

package net.ormr.zip

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.io.inputstream.ZipInputStream
import net.lingala.zip4j.model.FileHeader
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Path
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.absolutePathString

// TODO: move this to its own library

@JvmInline
public value class Zip internal constructor(internal val zipFile: ZipFile) {
    public var comment: String?
        get() = zipFile.comment?.ifEmpty { null }
        set(value) {
            zipFile.comment = value ?: ""
        }

    public val file: Path
        get() = zipFile.file.toPath()

    public var charset: Charset
        get() = zipFile.charset
        set(value) {
            zipFile.charset = value
        }

    public val isValidZipFile: Boolean
        get() = zipFile.isValidZipFile

    public val isEncrypted: Boolean
        get() = zipFile.isEncrypted

    public val isSplitArchive: Boolean
        get() = zipFile.isSplitArchive

    public val fileHeaders: List<FileHeader>
        get() = zipFile.fileHeaders

    public var bufferSize: Int
        get() = zipFile.bufferSize
        set(value) {
            zipFile.bufferSize = value
        }

    public val progressMonitor: ProgressMonitor
        get() = zipFile.progressMonitor

    public fun getFileHeader(fileName: String): FileHeader? = zipFile.getFileHeader(fileName)

    public fun importFile(file: Path, parameters: ZipParameters = DEFAULT_PARAMETERS) {
        zipFile.addFile(file.toFile(), parameters.asZip4jParameters())
    }

    public fun importFiles(files: List<Path>, parameters: ZipParameters = DEFAULT_PARAMETERS) {
        zipFile.addFiles(files.map(Path::toFile), parameters.asZip4jParameters())
    }

    public fun importDirectory(directory: Path, parameters: ZipParameters = DEFAULT_PARAMETERS) {
        zipFile.addFolder(directory.toFile(), parameters.asZip4jParameters())
    }

    public fun importStream(stream: InputStream, parameters: ZipParameters) {
        zipFile.addStream(stream, parameters.asZip4jParameters())
    }

    public fun extractAll(target: Path, parameters: ZipExtractParameters = DEFAULT_EXTRACT_PARAMETERS) {
        zipFile.extractAll(target.absolutePathString(), parameters.asUnzipParameters())
    }

    public fun extractFile(
        file: FileHeader,
        target: Path,
        newFileName: String? = null,
        parameters: ZipExtractParameters = DEFAULT_EXTRACT_PARAMETERS,
    ) {
        extractFile(file.fileName, target, newFileName, parameters)
    }

    public fun extractFile(
        fileName: String,
        target: Path,
        newFileName: String? = null,
        parameters: ZipExtractParameters = DEFAULT_EXTRACT_PARAMETERS,
    ) {
        zipFile.extractFile(fileName, target.absolutePathString(), newFileName, parameters.asUnzipParameters())
    }

    public fun deleteFile(file: FileHeader) {
        zipFile.removeFile(file)
    }

    public fun deleteFile(fileName: String) {
        zipFile.removeFile(fileName)
    }

    public fun deleteFiles(files: List<String>) {
        zipFile.removeFiles(files)
    }

    public fun renameFile(file: FileHeader, newName: String) {
        zipFile.renameFile(file, newName)
    }

    public fun renameFile(fileName: String, newFileName: String) {
        zipFile.renameFile(fileName, newFileName)
    }

    public fun renameFiles(fileNames: Map<String, String>) {
        zipFile.renameFiles(fileNames)
    }

    public fun mergeSplitFiles(targetZip: Path) {
        zipFile.mergeSplitFiles(targetZip.toFile())
    }

    public fun newInputStream(file: FileHeader): ZipInputStream = zipFile.getInputStream(file)

    public fun close() {
        zipFile.close()
    }
}

public fun Zip.newReader(file: FileHeader, charset: Charset = Charsets.UTF_8): InputStreamReader =
    InputStreamReader(newInputStream(file), charset)

public fun Zip.readText(file: FileHeader, charset: Charset = Charsets.UTF_8): String =
    newReader(file, charset).use { it.readText() }

private val DEFAULT_PARAMETERS = ZipParameters()
private val DEFAULT_EXTRACT_PARAMETERS = ZipExtractParameters()

// we're using 'String' instead of 'CharArray', because a CharArray is not really any safer than
// just using a String, for *numerous* reasons, all it does it convolute the API

public fun Zip(
    file: Path,
    password: String? = null,
    charset: Charset = Charsets.UTF_8,
    runInThread: Boolean = false,
): Zip {
    val zipFile = ZipFile(file.toFile(), password?.toCharArray())
    zipFile.charset = charset
    zipFile.isRunInThread = runInThread
    return Zip(zipFile)
}

public inline fun Zip(
    file: Path,
    password: String? = null,
    charset: Charset = Charsets.UTF_8,
    runInThread: Boolean = false,
    builder: (Zip) -> Unit,
): Zip {
    val zip = Zip(file, password, charset, runInThread)
    zip.use(builder)
    return zip
}

// TODO: copyToSplitZip which act like factory functions that create a new Zip instance
/*fun Path.copyTo(
    target: Zip,
    splitLength: Long,
    splitArchive: Boolean = true,
    parameters: ZipParameters = DEFAULT_PARAMETERS,
) {
    target.use {
        zipFile.createSplitZipFile(
            listOf(this@copyTo.toFile()),
            parameters.asZip4jParameters(),
            splitArchive,
            splitLength,
        )
    }
}

fun Path.copyDirectoryTo(
    target: Zip,
    splitLength: Long,
    splitArchive: Boolean = true,
    parameters: ZipParameters = DEFAULT_PARAMETERS,
) {
    target.use {
        zipFile.createSplitZipFileFromFolder(
            this@copyDirectoryTo.toFile(),
            parameters.asZip4jParameters(),
            splitArchive,
            splitLength,
        )
    }
}*/

// 'Zip' does not implement 'Closeable' as to avoid unnecessary boxing
public inline fun <R> Zip.use(block: (Zip) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when (val cause = exception) {
            null -> close()
            else -> try {
                close()
            } catch (closeException: Throwable) {
                cause.addSuppressed(closeException)
            }
        }
    }
}
