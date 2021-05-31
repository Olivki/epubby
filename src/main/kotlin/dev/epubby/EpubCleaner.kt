/*
 * Copyright 2019-2020 Oliver Berg
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

@file:JvmName("EpubCleaner")

package dev.epubby

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.files.AbstractEpubFileVisitor
import dev.epubby.files.DirectoryFile
import dev.epubby.files.RegularFile
import moe.kanon.kommons.io.paths.deleteIfExists
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes

/**
 * Cleans the general structure of this EPUB.
 *
 * Invoking this function will ensure that no empty directories and that no files that aren't used by the system
 * is left in this EPUB.
 */
// TODO: name? or maybe make it not be an extension function?
fun Epub.clean() {
    root.walkFileTree(FileCleanerVisitor)
}

private val RegularFile.isUseless: Boolean
    get() = !isMetaInfFile && !isMimeType && !isResourceFile && !isOpfFile

private object FileCleanerVisitor : AbstractEpubFileVisitor() {
    private val LOGGER: InlineLogger = InlineLogger(FileCleanerVisitor::class)

    override fun visitFile(file: RegularFile, attributes: BasicFileAttributes): FileVisitResult {
        if (file.isUseless && file.canBeDeleted) {
            LOGGER.trace { "Deleting useless file: $file" }
            file.delegate.deleteIfExists()
        }

        return FileVisitResult.CONTINUE
    }

    override fun postVisitDirectory(directory: DirectoryFile, exception: IOException?): FileVisitResult {
        if (directory.isEmpty() && directory.canBeDeleted) {
            LOGGER.trace { "Deleting empty directory: $directory" }
            directory.delegate.deleteIfExists()
        }

        return super.postVisitDirectory(directory, exception)
    }
}