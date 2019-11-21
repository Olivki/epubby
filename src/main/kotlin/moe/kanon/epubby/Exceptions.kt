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

import moe.kanon.kommons.io.paths.name
import java.nio.file.Path

// TODO: Documentation

open class EpubbyException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

open class MalformedBookException(
    val container: Path,
    val currentFile: Path,
    message: String? = null,
    cause: Throwable? = null
) : EpubbyException(message, cause) {
    internal companion object {
        @JvmSynthetic
        internal fun withDebug(container: Path, message: String, cause: Throwable? = null): MalformedBookException {
            val detailedMessage = """
                |Encountered an error when traversing file "${container.name}" as an EPUB container.
                |---- Debug Details ----
                |Container: $container
                |Message: "$message"
                |Cause: ${if (cause != null) cause.message else "no cause"}
                |-----------------------
            """.trimMargin()
            return MalformedBookException(container, container, detailedMessage, cause)
        }

        @JvmSynthetic
        internal fun withDebug(
            container: Path,
            currentFile: Path,
            message: String,
            cause: Throwable? = null
        ): MalformedBookException {
            val detailedMessage = """
                |Encountered an error when traversing file "${container.relativize(currentFile)}" in container file "${container.name}"
                |---- Debug Details ----
                |Container: $container
                |Current File: $currentFile
                |Message: "$message"
                |Cause: ${if (cause != null) cause.message else "no cause"}
                |-----------------------
            """.trimMargin()
            return MalformedBookException(container, currentFile, detailedMessage, cause)
        }
    }
}

@PublishedApi
@JvmSynthetic
internal fun malformedFail(
    container: Path,
    currentFile: Path,
    message: String,
    cause: Throwable? = null
): Nothing = throw MalformedBookException.withDebug(container, currentFile, message, cause)