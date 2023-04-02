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

package dev.epubby.errors

import dev.epubby.files.revamped.ExistingResource
import java.io.IOException

sealed interface FileError : EpubbyError {
    data class NoSuchResource(val path: String?) : FileError
    data class DirectoryNotEmpty(val path: String?) : FileError
    data class ResourceAlreadyExists(val origin: String?, val other: String?) : FileError
    data class NonDirectoryRootResource(val resource: ExistingResource) : FileError
    // TODO: make sure to do thoroughly check this, so we don't just check the first parent and then be like
    //       'yeah it's ok', 'path.startsWith' might be worth checking into?
    data class InvalidMetaInfResource(val resource: ExistingResource) : FileError
    data class NotFile(val path: String?) : FileError
    data class NotModifiable(val path: String) : FileError
    data class NotDeletable(val path: String) : FileError
    data class NotDirectory(val path: String?) : FileError
    data class InvalidGlobPattern(val pattern: String) : FileError
    data class Unknown(val cause: IOException) : FileError
}