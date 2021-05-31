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

package dev.epubby.files

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes

abstract class AbstractEpubFileVisitor : EpubFileVisitor {
    @Throws(IOException::class)
    override fun preVisitDirectory(
        directory: DirectoryFile,
        attributes: BasicFileAttributes,
    ): FileVisitResult = FileVisitResult.CONTINUE

    @Throws(IOException::class)
    override fun visitFile(
        file: RegularFile,
        attributes: BasicFileAttributes,
    ): FileVisitResult = FileVisitResult.CONTINUE

    @Throws(IOException::class)
    override fun visitFileFailed(file: RegularFile, exception: IOException): FileVisitResult {
        throw exception
    }

    @Throws(IOException::class)
    override fun postVisitDirectory(directory: DirectoryFile, exception: IOException?): FileVisitResult {
        if (exception != null) {
            throw exception
        }

        return FileVisitResult.CONTINUE
    }
}