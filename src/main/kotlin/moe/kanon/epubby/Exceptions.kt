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

import moe.kanon.epubby.utils.combineWith
import java.nio.file.Path

/**
 * Thrown to indicate that something went wrong when attempting to do something with a [Book] instance.
 *
 * @property [epub] The path to the file that caused this exception to be thrown.
 */
open class EpubbyException @JvmOverloads constructor(
    val epub: Path,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown to indicate that the specified [epub] is pointing to a malformed EPUB.
 *
 * Note that this exception may be raised in scenarios where the `file` is not an EPUB file at all, ***and*** scenarios
 * where the `file` might be a mostly valid EPUB file. So do *not* assume that a file is an EPUB file *at all* just
 * because this exception was raised.
 */
open class MalformedBookException @JvmOverloads constructor(
    epub: Path,
    message: String? = null,
    cause: Throwable? = null
) : EpubbyException(epub, message, cause)

@PublishedApi internal fun raiseMalformedError(epub: Path, file: Path, reason: String): Nothing =
    throw MalformedBookException(epub, "<${epub.combineWith(file)}> is malformed; $reason")