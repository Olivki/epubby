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
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path

// TODO: documentation
open class BookFileIOException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

class BookFileAlreadyExistsException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null,
) : BookFileIOException(message, cause)

internal fun newBookFileAlreadyExistsException(path: Path): BookFileAlreadyExistsException =
    BookFileAlreadyExistsException("A file already exists at '$path', and overwriting other files is forbidden.")

internal fun newBookFileAlreadyExistsException(path: BookFile): BookFileAlreadyExistsException =
    newBookFileAlreadyExistsException(path.delegate)

@PublishedApi
internal inline fun <T> wrapIO(block: () -> T): T = try {
    block()
}  catch (e: BookFileIOException) {
    throw e
} catch (e: FileAlreadyExistsException) {
    throw BookFileAlreadyExistsException(
        "A file already exists at this location, and overwriting other files is forbidden${e.safeMessage}",
        e
    )
} catch (e: IOException) {
    throw BookFileIOException(e.message, e)
}

@PublishedApi
internal inline fun <T> wrapIO(message: String, block: () -> T): T = try {
    block()
} catch (e: IOException) {
    throw BookFileIOException("$message${e.safeMessage}", e)
}

@PublishedApi
internal val Exception.safeMessage: String
    get() = message?.let { ": $it" } ?: ""