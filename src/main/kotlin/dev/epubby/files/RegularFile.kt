/*
 * Copyright 2020-2022 Oliver Berg
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

package dev.epubby.files

import dev.epubby.Epub
import krautils.io.copyTo
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.FileAttribute

class RegularFile private constructor(
    override val epub: Epub,
    @get:JvmSynthetic
    override val delegate: Path,
) : EpubFile() {
    val simpleName: String
        get() = name.substringBeforeLast('.')

    val extension: String?
        get() = when {
            '.' in name -> name.substringAfterLast('.')
            else -> null
        }

    /**
     * Throws an [UnsupportedOperationException] as converting a regular file into a directory is not supported.
     */
    @Deprecated(
        message = "Can't convert a regular file to a directory.",
        replaceWith = ReplaceWith("createFile(*attributes)"),
        level = DeprecationLevel.HIDDEN
    )
    @Throws(UnsupportedOperationException::class)
    override fun getOrCreateDirectory(vararg attributes: FileAttribute<*>): DirectoryFile =
        throw UnsupportedOperationException("Can't convert a regular file to a directory.")

    /**
     * Returns this regular file, and [creates a new file][Files.createFile] if one doesn't exist.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.createFile]
     */
    override fun getOrCreateFile(vararg attributes: FileAttribute<*>): RegularFile = apply {
        if (exists) {
            return this
        }

        checkCreationPermissions()

        Files.createFile(delegate, *attributes)
    }

    override fun moveTo(target: EpubFile): RegularFile {
        checkModificationPermissions()
        checkCreationPermissions(target)
        checkExistence(target.delegate)
        val result = Files.move(delegate, target.delegate, StandardCopyOption.COPY_ATTRIBUTES).toRegularFile()

        if (isResourceFile) {
            epub.manifest.getLocalResourceFromFile(this)?.file = result
        }

        return result
    }

    override fun moveTo(target: DirectoryFile): RegularFile = moveTo(target.resolve(name))

    override fun renameTo(name: String): RegularFile = moveTo(resolveSibling(name))

    override fun deleteIfExists(): Boolean = when {
        canBeDeleted -> Files.deleteIfExists(delegate)
        else -> throw UnsupportedOperationException("File at '$fullPath' can not be deleted.")
    }

    override fun normalize(): RegularFile = delegate.normalize().toRegularFile()

    /**
     * TODO
     *
     * @see [Files.newInputStream]
     */
    // TODO: throw exception if 'options' contains a modifying operation when modification permission is not allowed
    @JvmOverloads
    fun newInputStream(vararg options: OpenOption = arrayOf(StandardOpenOption.READ)): InputStream =
        Files.newInputStream(delegate, *options)

    /**
     * TODO
     *
     * @see [Files.newOutputStream]
     */
    @JvmOverloads
    fun newOutputStream(
        vararg options: OpenOption = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
    ): OutputStream {
        checkModificationPermissions()
        return Files.newOutputStream(delegate, *options)
    }

    /**
     * TODO
     *
     * @see [Files.newBufferedReader]
     */
    @JvmOverloads
    fun newBufferedReader(charset: Charset = StandardCharsets.UTF_8): BufferedReader =
        Files.newBufferedReader(delegate, charset)

    /**
     * TODO
     *
     * @see [Files.newBufferedWriter]
     */
    @JvmOverloads
    fun newBufferedWriter(
        vararg options: OpenOption = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
    ): BufferedWriter {
        checkModificationPermissions()
        return Files.newBufferedWriter(delegate, *options)
    }

    /**
     * TODO
     *
     * @see [Files.newBufferedWriter]
     */
    @JvmOverloads
    fun newBufferedWriter(
        charset: Charset,
        vararg options: OpenOption = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ),
    ): BufferedWriter {
        checkModificationPermissions()
        return Files.newBufferedWriter(delegate, charset, *options)
    }

    /**
     * TODO
     *
     * @see [Files.newByteChannel]
     */
    @JvmOverloads
    fun newByteChannel(
        vararg options: OpenOption = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
    ): SeekableByteChannel {
        val channel = Files.newByteChannel(delegate, *options)
        return when {
            canBeModified -> channel
            else -> NonWritableByteChannel(channel)
        }
    }

    /**
     * Writes the given [bytes] to this file.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.write]
     */
    @JvmOverloads
    fun writeBytes(
        bytes: ByteArray,
        vararg options: OpenOption = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ),
    ): RegularFile {
        checkModificationPermissions()
        return Files.write(delegate, bytes, *options).toRegularFile()
    }

    /**
     * Returns a byte array containing the contents of this file.
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.readAllBytes]
     */
    fun readBytes(): ByteArray = Files.readAllBytes(delegate)

    /**
     * Returns a list containing all the lines of this file, encoded in the given [charset].
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.readAllLines]
     */
    @JvmOverloads
    fun readLines(charset: Charset = StandardCharsets.UTF_8): List<String> =
        Files.readAllLines(delegate, charset)

    /**
     * Writes the given [lines] to this file.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [Files.write]
     */
    @JvmOverloads
    fun writeLines(
        lines: Iterable<CharSequence>,
        vararg options: OpenOption = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ),
    ): RegularFile {
        checkModificationPermissions()
        return Files.write(delegate, lines, *options).toRegularFile()
    }

    /**
     * Returns a string containing all the lines of this file, encoded in the given [charset].
     *
     * Note that this function should not be used for a file whose size exceeds that of 1 GB.
     *
     * @throws [IOException] if an I/O error occurs
     *
     * @see [readBytes]
     */
    @JvmOverloads
    fun readString(charset: Charset = StandardCharsets.UTF_8): String = String(Files.readAllBytes(delegate), charset)

    /**
     * Writes the given [string] to this file encoded using the given [charset].
     *
     * @throws [IOException] if an I/O error occurs
     */
    @JvmOverloads
    fun writeString(
        string: String,
        charset: Charset,
        vararg options: OpenOption = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ),
    ): RegularFile {
        checkModificationPermissions()
        return Files.write(delegate, string.toByteArray(charset), *options).toRegularFile()
    }

    /**
     * Writes the given [string] to this file encoded using the [UTF_8][StandardCharsets.UTF_8] charset.
     *
     * @throws [IOException] if an I/O error occurs
     */
    @JvmOverloads
    fun writeString(
        string: String,
        vararg options: OpenOption = arrayOf(
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ),
    ): RegularFile {
        checkModificationPermissions()
        return Files.write(delegate, string.toByteArray(StandardCharsets.UTF_8), *options).toRegularFile()
    }

    override fun toAbsoluteFile(): RegularFile = delegate.toAbsolutePath().toRegularFile()

    private class NonWritableByteChannel(private val delegate: SeekableByteChannel) : SeekableByteChannel by delegate {
        override fun write(src: ByteBuffer): Int =
            throw UnsupportedOperationException("Can not write to this byte-channel as it belongs to a non-modifiable file.")

        override fun truncate(size: Long): SeekableByteChannel =
            throw UnsupportedOperationException("Can not truncate this byte-channel as it belongs to a non-modifiable file.")
    }

    companion object {
        /**
         * Returns a new [RegularFile] located inside the given [parent] with the given [name].
         *
         * The returned `RegularFile` may, or may not, [exist][RegularFile.exists], if it does not, invoke
         * [getOrCreateFile][RegularFile.getOrCreateFile] to create it.
         */
        @JvmStatic
        fun of(parent: DirectoryFile, name: String): RegularFile {
            val target = parent.delegate.resolve(name)
            return RegularFile(parent.epub, target)
        }

        /**
         * TODO
         *
         * @throws [FileAlreadyExistsException] if a file already exists in the given [parent] with the given
         * [fileName]
         * @throws [IOException] if an I/O error occurs
         */
        @JvmStatic
        fun fromInputStream(input: InputStream, parent: DirectoryFile, fileName: String): RegularFile {
            val target = parent.delegate.resolve(fileName)

            if (Files.exists(target)) {
                fileAlreadyExists(parent.delegate)
            }

            input.copyTo(target)

            return RegularFile(parent.epub, target)
        }

        /**
         * TODO
         *
         * @throws [FileAlreadyExistsException] if a file already exists in the given [parent] with the given
         * [fileName]
         * @throws [IOException] if an I/O error occurs
         */
        @JvmStatic
        fun fromBytes(
            bytes: ByteArray,
            parent: DirectoryFile,
            name: String,
        ): RegularFile = fromInputStream(ByteArrayInputStream(bytes), parent, name)

        /**
         * TODO
         *
         * @throws [FileAlreadyExistsException] if a file already exists in the given [parent] with the same
         * [fileName][Path.getFileName] as the given [path]
         * @throws [IOException] if an I/O error occurs
         * @throws [IllegalArgumentException] if the given [path] is not a [regular file][Files.isRegularFile]
         */
        @JvmStatic
        fun fromPath(parent: DirectoryFile, path: Path): RegularFile = when {
            Files.isRegularFile(path) -> when (path.fileSystem) {
                parent.epub.fileSystem -> RegularFile(parent.epub, path)
                else -> {
                    val target = parent.delegate.resolve(path.fileName.toString())

                    if (Files.exists(target)) {
                        fileAlreadyExists(target)
                    }

                    RegularFile(parent.epub, Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES))
                }
            }

            else -> throw IllegalArgumentException("File at '$path' is not a regular file.")
        }

        @JvmSynthetic
        internal operator fun invoke(path: Path, epub: Epub): RegularFile = RegularFile(epub, path)
    }
}