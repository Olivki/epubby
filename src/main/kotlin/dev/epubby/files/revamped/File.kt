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
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.attribute.FileTime

sealed interface File : ExistingResource {
    val directory: Directory

    val isHidden: Boolean

    val isReadable: Boolean

    val isWritable: Boolean

    val isExecutable: Boolean

    /**
     * The size of `this` file in bytes.
     *
     * @see [Files.size]
     */
    val fileSize: Either<FileError, Long>

    fun byteChannel(): Either<FileError, ReadableByteChannel>

    fun copyTo(target: ModifiableTarget, vararg options: CopyOption): Either<FileError, File>

    fun inputStream(): Either<FileError, InputStream>

    fun readText(charset: Charset = Charsets.UTF_8): Either<FileError, String>

    fun readLines(charset: Charset = Charsets.UTF_8): Either<FileError, List<String>>

    fun readBytes(): Either<FileError, ByteArray>
}

sealed interface DeletableFile : File, Deletable

sealed interface ModifiableFile : File, Modifiable, `ModifiableFile | Nil` {
    override fun byteChannel(): Either<FileError, SeekableByteChannel>

    override fun setLastModifiedTime(time: FileTime): Either<FileError, ModifiableFile>

    fun moveTo(target: ModifiableTarget, vararg options: CopyOption): Either<FileError, File>

    override fun renameTo(name: String, overwrite: Boolean): Either<FileError, File>

    fun writeText(text: CharSequence, charset: Charset = Charsets.UTF_8): Either<FileError, Unit>

    fun appendText(text: CharSequence, charset: Charset = Charsets.UTF_8): Either<FileError, Unit>

    fun writeBytes(bytes: ByteArray): Either<FileError, Unit>

    fun appendBytes(bytes: ByteArray): Either<FileError, Unit>

    fun writeLines(lines: Iterable<CharSequence>, charset: Charset = Charsets.UTF_8): Either<FileError, ModifiableFile>

    fun appendLines(lines: Iterable<CharSequence>, charset: Charset = Charsets.UTF_8): Either<FileError, ModifiableFile>
}

sealed interface UnprotectedFile : Unprotected, File, DeletableFile, ModifiableFile {
    override fun setLastModifiedTime(time: FileTime): Either<FileError, UnprotectedFile>

    fun writeText(
        text: CharSequence,
        vararg options: OpenOption,
        charset: Charset = Charsets.UTF_8,
    ): Either<FileError, Unit>

    fun writeBytes(bytes: ByteArray, vararg options: OpenOption): Either<FileError, Unit>

    override fun writeLines(lines: Iterable<CharSequence>, charset: Charset): Either<FileError, UnprotectedFile>

    fun writeLines(
        lines: Iterable<CharSequence>,
        vararg options: OpenOption,
        charset: Charset = Charsets.UTF_8,
    ): Either<FileError, UnprotectedFile>

    override fun appendLines(lines: Iterable<CharSequence>, charset: Charset): Either<FileError, UnprotectedFile>

    fun byteChannel(vararg option: OpenOption): Either<FileError, SeekableByteChannel>

    fun inputStream(vararg options: OpenOption): Either<FileError, InputStream>

    fun outputStream(vararg options: OpenOption): Either<FileError, OutputStream>
}