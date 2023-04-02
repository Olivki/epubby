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
import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.nio.file.NotDirectoryException
import java.util.regex.PatternSyntaxException

@PublishedApi
internal inline fun <T> wrapIOException(block: () -> T): Either<FileError, T> = try {
    Either.Right(block())
} catch (e: IOException) {
    Either.Left(e.toFileError())
} catch (e: PatternSyntaxException) {
    Either.Left(e.toFileError())
}

internal inline fun <T, R> wrapIOException(block: () -> T, mapper: (T) -> R): Either<FileError, R> = try {
    Either.Right(mapper(block()))
} catch (e: IOException) {
    Either.Left(e.toFileError())
} catch (e: PatternSyntaxException) {
    Either.Left(e.toFileError())
}

fun IOException.toFileError(): FileError = when (this) {
    is NoSuchFileException -> FileError.NoSuchResource(file)
    is DirectoryNotEmptyException -> FileError.DirectoryNotEmpty(file)
    is FileAlreadyExistsException -> FileError.ResourceAlreadyExists(file, otherFile)
    is NotDirectoryException -> FileError.NotDirectory(file)
    else -> FileError.Unknown(this)
}

@PublishedApi
internal fun PatternSyntaxException.toFileError(): FileError = FileError.InvalidGlobPattern(pattern)