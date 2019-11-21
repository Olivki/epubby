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

package moe.kanon.epubby.utils.internal

import moe.kanon.epubby.EpubbyException
import moe.kanon.epubby.MalformedBookException
import moe.kanon.kommons.func.Failure
import moe.kanon.kommons.func.Try
import java.nio.file.Path

typealias BookResult<T> = Try<T>

@PublishedApi
internal fun fail(message: String, cause: Throwable? = null): Nothing = throw EpubbyException(message, cause)

@PublishedApi
internal fun malformed(container: Path, message: String, cause: Throwable? = null): Nothing =
    throw MalformedBookException(container, container, message, cause)

@PublishedApi
internal fun malformed(container: Path, currentFile: Path, message: String, cause: Throwable? = null): Nothing =
    throw MalformedBookException(container, currentFile, message, cause)

@PublishedApi
internal fun <T> failure(message: String, cause: Throwable?): Try<T> = Failure(EpubbyException(message, cause))