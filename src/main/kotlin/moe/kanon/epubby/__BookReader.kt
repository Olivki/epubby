/*
 * Copyright 2019 Oliver Berg
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

package moe.kanon.epubby

import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.internal.malformed
import moe.kanon.epubby.metainf.MetaInf
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.createTmpDirectory
import moe.kanon.kommons.io.paths.deleteIfExists
import moe.kanon.kommons.io.paths.exists
import moe.kanon.kommons.io.paths.isDirectory
import moe.kanon.kommons.io.paths.isRegularFile
import moe.kanon.kommons.io.paths.name
import moe.kanon.kommons.io.paths.notExists
import moe.kanon.kommons.io.paths.readString
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Path

// TODO: This
/**
 * TODO
 *
 * @property [mode] Determines how the system should act when encountering faulty/unknown elements when reading a
 * file into a [Book] instance.
 *
 * Note that this has no bearing on whether or not the system will throw a exception when doing the initial
 * validation that the given file *actually* is a EPUB.
 * @property [shouldBackupFile] Whether or not the reader should back the file its reading up to the
 * [temp-dir][createTmpDirectory] of the OS.
 * @property [shouldDeleteFileOnExit] Whether or not the reader should tell the system to delete the file its reading
 * when the JVM shuts down.
 */
class __BookReader private constructor(
    val mode: Mode,
    val shouldBackupFile: Boolean,
    val shouldDeleteFileOnExit: Boolean
) {
    @Throws(IOException::class, EpubbyException::class)
    fun read(epubFile: Path): Book {
        logger.info { "Reading file '$epubFile' as an EPUB container.." }

        val file = when {
            shouldBackupFile -> epubFile.copyTo(createTmpDirectory("epubby"), keepName = true)
            else -> epubFile
        }

        if (shouldDeleteFileOnExit) {
            file.toFile().deleteOnExit()
        }

        val fileSystem = try {
            FileSystems.newFileSystem(file, null).also { validateContainer(file, it.getPath("/")) }
        } catch (e: IOException) {
            // something went wrong when trying to create the new file-system, so we want to rethrow the exception
            // wrapped in an epubby-exception, this is to notify the user that *we* know that this happened
            if (shouldBackupFile) file.deleteIfExists()
            malformed(epubFile, "Could not create a file-system for '${epubFile.name}'", e)
        } catch (e: EpubbyException) {
            // the validation failed, so we want to just re-throw the exception
            if (shouldBackupFile) file.deleteIfExists()
            throw e
        }
        val root = fileSystem.getPath("/")
        val metaInf = MetaInf.fromDirectory(epubFile, root.resolve("META-INF"), root)

        TODO: Implement the leniency system and move the creation of the book when parsing to the package-document again

        return Book(metaInf, epubFile, fileSystem, root)
    }

    // validates that the required parts of an EPUB is available in the epub file
    private fun validateContainer(epub: Path, root: Path) {
        val metaInf = root.resolve("META-INF")
        val mimeType = root.resolve("mimetype")
        when {
            // none of the epub formats have changed the behaviour of requiring the '!META-INF' directory, so it's
            // safe *(for now)* to assume that to be a valid epub file, no matter the format version, a '!META-INF'
            // directory containing a 'container.xml' is REQUIRED
            metaInf.notExists -> {
                root.fileSystem.close()
                malformed(epub, root, "'META-INF' directory is missing from root of epub")
            }
            metaInf.isRegularFile -> {
                root.fileSystem.close()
                malformed(epub, metaInf, "'META-INF' should be a directory, was a file")
                // 'mimetype' also needs to exist in the root of the epub file, and it NEEDS to contain the ASCII
            }
            // string "application/epub+zip"
            mimeType.notExists -> {
                root.fileSystem.close()
                malformed(epub, root, "'mimetype' file is missing from root of epub")
            }
            mimeType.isDirectory -> {
                root.fileSystem.close()
                malformed(epub, mimeType, "'mimetype' should be a file, was a directory")
            }
            mimeType.exists -> {
                // the mimeType is supposed to be encoded in ASCII format, so we make sure to read it that way
                val contents = mimeType.readString(StandardCharsets.US_ASCII)
                if (contents != "application/epub+zip") {
                    root.fileSystem.close()
                    malformed(epub, mimeType, "contents of 'mimetype' should be 'application/epub+zip', was '$contents'")
                }
            }
        }
    }

    /**
     * A builder for [__BookReader].
     *
     * @property [mode] Determines how the system should act when encountering faulty/unknown elements when reading a
     * file into a [Book] instance.
     *
     * Note that this has no bearing on whether or not the system will throw a exception when doing the initial
     * validation that the given file *actually* is a EPUB.
     * @property [shouldBackupFile] Whether or not the reader should back the file its reading up to the
     * [temp-dir][createTmpDirectory] of the OS.
     * @property [shouldDeleteFileOnExit] Whether or not the reader should tell the system to delete the file its reading
     * when the JVM shuts down.
     */
    class Builder internal constructor(
        @set:JvmSynthetic
        @get:JvmSynthetic
        var mode: Mode = Mode.STRICT,
        @set:JvmSynthetic
        @get:JvmSynthetic
        var shouldBackupFile: Boolean = true,
        @set:JvmSynthetic
        @get:JvmSynthetic
        var shouldDeleteFileOnExit: Boolean = shouldBackupFile
    ) {
        /**
         * Determines how the system should act when encountering faulty/unknown elements when reading a
         * file into a [Book] instance.
         *
         * Note that this has no bearing on whether or not the system will throw a exception when doing the initial
         * validation that the given file *actually* is a EPUB.
         *
         * By default this is set to [STRICT][Mode.STRICT].
         */
        fun mode(mode: Mode) = apply {
            this.mode = mode
        }

        /**
         * Whether or not the reader should back the file its reading up to the [temp-dir][createTmpDirectory] of the
         * OS.
         *
         * By default this is set to `true`.
         */
        fun shouldBackupFile(shouldBackupFile: Boolean) = apply {
            this.shouldBackupFile = shouldBackupFile
        }

        /**
         * Whether or not the reader should tell the system to delete the file its reading when the JVM shuts down.
         *
         * By default this is set to `true`.
         */
        fun shouldDeleteFileOnExit(shouldDeleteFileOnExit: Boolean) = apply {
            this.shouldDeleteFileOnExit = shouldDeleteFileOnExit
        }

        fun build(): __BookReader = __BookReader(mode, shouldBackupFile, shouldDeleteFileOnExit)
    }

    enum class Mode {
        /**
         * The system will throw exceptions when it encounters faulty/unknown elements.
         */
        STRICT,
        /**
         * The system will try it's best to recover from any faulty/unknown elements, and will log the errors rather
         * than throwing them as exceptions.
         *
         * Note that if this mode is used, then epubby can no longer make any guarantees that the result of writing the
         * book to a file will produce a valid EPUB file.
         */
        LENIENT;
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()

        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(): __BookReader = builder().build()

        @JvmSynthetic
        inline operator fun invoke(scope: Builder.() -> Unit): __BookReader = builder().apply(scope).build()
    }
}