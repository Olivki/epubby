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

package dev.epubby.files.revamped

import arrow.core.Either
import dev.epubby.errors.FileError
import krautils.io.renameTo
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption.READ
import java.nio.file.attribute.FileTime
import kotlin.io.path.*

internal data class ConcreteFile(override val path: Path) : File {
    override val directory: Directory
        get() = Directory(path.parent ?: error("File '$this' should have a parent directory, EPUB may be corrupt."))

    override val isHidden: Boolean
        get() = delegatePath.isHidden()

    override val isReadable: Boolean
        get() = delegatePath.isReadable()

    override val isWritable: Boolean
        get() = delegatePath.isWritable()

    override val isExecutable: Boolean
        get() = delegatePath.isExecutable()

    override val fileSize: Either<FileError, Long>
        get() = wrapIOException { delegatePath.fileSize() }

    override fun isEmpty(): Either<FileError, Boolean> = fileSize.map { it <= 0 }

    override fun getLastModifiedTime(): Either<FileError, FileTime> = wrapIOException(delegatePath::getLastModifiedTime)

    override fun copyTo(target: ModifiableTarget, vararg options: CopyOption): Either<FileError, File> {
        TODO("Not yet implemented")
    }

    override fun byteChannel(): Either<FileError, ReadableByteChannel> =
        wrapIOException { NonWritableByteChannel(Files.newByteChannel(delegatePath)) }

    override fun inputStream(): Either<FileError, InputStream> = wrapIOException { delegatePath.inputStream(READ) }

    override fun readText(charset: Charset): Either<FileError, String> =
        wrapIOException { delegatePath.readText(charset) }

    override fun readLines(charset: Charset): Either<FileError, List<String>> =
        wrapIOException { delegatePath.readLines(charset) }

    override fun readBytes(): Either<FileError, ByteArray> = wrapIOException { delegatePath.readBytes() }

    override fun isSameAs(other: Resource): Either<FileError, Boolean> =
        wrapIOException { delegatePath.isSameFileAs(other.delegatePath) }

    override fun toString(): String = path.toString()
}

internal data class ConcreteUnprotectedFile(private val delegate: ConcreteFile) : File by delegate, UnprotectedFile {
    override fun setLastModifiedTime(time: FileTime): Either<FileError, UnprotectedFile> = wrapIOException {
        delegatePath.setLastModifiedTime(time)
        this
    }

    override fun delete(): Either<FileError, Nil> =
        wrapIOException(delegatePath::deleteExisting) { createNil() }

    // TODO: update LocalResource file property
    override fun moveTo(
        target: ModifiableTarget,
        vararg options: CopyOption,
    ): Either<FileError, File> = wrapIOException {
        if (target is Directory) {
            delegatePath.moveTo(target.delegatePath.resolve(path.name), *options).toEpubFile()
        } else {
            delegatePath.moveTo(target.delegatePath, *options).toEpubFile()
        }
    }

    override fun renameTo(name: String, overwrite: Boolean): Either<FileError, File> =
        wrapIOException { delegatePath.renameTo(name, overwrite).toEpubFile() }

    override fun writeText(text: CharSequence, charset: Charset): Either<FileError, Unit> =
        wrapIOException { delegatePath.writeText(text, charset) }

    override fun writeText(text: CharSequence, vararg options: OpenOption, charset: Charset): Either<FileError, Unit> =
        wrapIOException { delegatePath.writeText(text, charset, *options) }

    override fun appendText(text: CharSequence, charset: Charset): Either<FileError, Unit> =
        wrapIOException { delegatePath.appendText(text, charset) }

    override fun writeBytes(bytes: ByteArray): Either<FileError, Unit> =
        wrapIOException { delegatePath.writeBytes(bytes) }

    override fun appendBytes(bytes: ByteArray): Either<FileError, Unit> =
        wrapIOException { delegatePath.appendBytes(bytes) }

    override fun writeLines(lines: Iterable<CharSequence>, charset: Charset): Either<FileError, UnprotectedFile> =
        wrapIOException {
            delegatePath.writeLines(lines, charset)
            this
        }

    override fun writeLines(
        lines: Iterable<CharSequence>,
        vararg options: OpenOption,
        charset: Charset,
    ): Either<FileError, UnprotectedFile> = wrapIOException {
        delegatePath.writeLines(lines, charset, *options)
        this
    }

    override fun appendLines(lines: Iterable<CharSequence>, charset: Charset): Either<FileError, UnprotectedFile> =
        wrapIOException {
            delegatePath.appendLines(lines, charset)
            this
        }

    override fun byteChannel(): Either<FileError, SeekableByteChannel> =
        wrapIOException { Files.newByteChannel(delegatePath) }

    override fun byteChannel(vararg option: OpenOption): Either<FileError, SeekableByteChannel> =
        wrapIOException { Files.newByteChannel(delegatePath, *option) }

    override fun writeBytes(bytes: ByteArray, vararg options: OpenOption): Either<FileError, Unit> =
        wrapIOException { delegatePath.writeBytes(bytes, *options) }

    override fun inputStream(vararg options: OpenOption): Either<FileError, InputStream> =
        wrapIOException { delegatePath.inputStream(*options) }

    override fun outputStream(vararg options: OpenOption): Either<FileError, OutputStream> =
        wrapIOException { delegatePath.outputStream(*options) }

    override fun toString(): String = path.toString()

    private fun JPath.toEpubFile(): File = File(ConcretePath(this, this@ConcreteUnprotectedFile.fileSystem))
}

internal data class ConcreteDeletableFile(private val delegate: ConcreteUnprotectedFile) : DeletableFile by delegate {
    override fun toString(): String = path.toString()
}

internal data class ConcreteModifiableFile(private val delegate: ConcreteUnprotectedFile) : ModifiableFile by delegate {
    override fun toString(): String = path.toString()
}

internal fun File(path: Path): File {
    val file = ConcreteFile(path)
    return when {
        isMimeTypePath(path) || isOpfFilePath(path) || isMetaInfFilePath(path) -> file
        isLocalResourcePath(path) -> ConcreteModifiableFile(ConcreteUnprotectedFile(file))
        else -> ConcreteUnprotectedFile(file)
    }
}