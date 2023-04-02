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
import arrow.core.left
import arrow.core.right
import dev.epubby.errors.FileError
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.attribute.BasicFileAttributes

interface ExistingResourceVisitor {
    fun preVisitDirectory(directory: Directory, attributes: BasicFileAttributes): Either<FileError, FileVisitResult> =
        CONTINUE.right()

    fun visitFile(file: File, attributes: BasicFileAttributes): Either<FileError, FileVisitResult> =
        CONTINUE.right()

    fun visitFileFailed(file: File, exception: IOException): Either<FileError, FileVisitResult> =
        exception.toFileError().left()

    fun postVisitDirectory(directory: Directory, exception: IOException?): Either<FileError, FileVisitResult> =
        exception?.toFileError()?.left() ?: CONTINUE.right()

    fun visitNil(nil: Nil, caller: NilCaller): Either<FileError, FileVisitResult> = when (caller) {
        is NilCaller.VisitFileFailed -> caller.exception.toFileError().left()
        is NilCaller.PostVisitDirectory -> caller.exception?.toFileError()?.left() ?: CONTINUE.right()
        else -> CONTINUE.right()
    }

    sealed interface NilCaller {
        data class PreVisitDirectory(val attributes: BasicFileAttributes) : NilCaller
        data class VisitFile(val attributes: BasicFileAttributes) : NilCaller
        data class VisitFileFailed(val exception: IOException) : NilCaller
        data class PostVisitDirectory(val exception: IOException?) : NilCaller
    }
}

@DslMarker
internal annotation class ResourceVisitorDslMarker

@ResourceVisitorDslMarker
sealed interface ExistingResourceVisitorDsl {
    @ResourceVisitorDslMarker
    fun onPreDirectory(block: (directory: Directory, attributes: BasicFileAttributes) -> Either<FileError, FileVisitResult>)

    @ResourceVisitorDslMarker
    fun onFile(block: (file: File, attributes: BasicFileAttributes) -> Either<FileError, FileVisitResult>)

    @ResourceVisitorDslMarker
    fun onFileVisitFailed(block: (file: File, exception: IOException) -> Either<FileError, FileVisitResult>)

    @ResourceVisitorDslMarker
    fun onPostDirectory(block: (directory: Directory, exception: IOException?) -> Either<FileError, FileVisitResult>)

    @ResourceVisitorDslMarker
    fun onNil(block: (nil: Nil, caller: ExistingResourceVisitor.NilCaller) -> Either<FileError, FileVisitResult>)
}