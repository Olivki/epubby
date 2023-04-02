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

import dev.epubby.Epub
import kotlin.io.path.absolute

internal fun isMimeTypePath(path: Path): Boolean = comparePaths(path, path.epub.mimeType.delegate)

internal fun isOpfFilePath(path: Path): Boolean = comparePaths(path, path.epub.opfFile.delegate)

internal fun isMetaInfFilePath(path: Path): Boolean {
    val parentPath = path.parent ?: return false
    return isMetaInfDirectoryPath(parentPath) && path.name in metaInfFiles
}

internal fun isLocalResourcePath(path: Path): Boolean =
    path.absolute().toString() in path.epub.manifest.fileToLocalResource

internal fun isMetaInfDirectoryPath(path: Path): Boolean =
    comparePaths(path, path.epub.metaInf.directory.delegate)

internal fun isOpfDirectory(path: Path): Boolean = comparePaths(path, path.epub.opfDirectory.delegate)

// TODO: is this sound?
internal fun comparePaths(a: Path, b: JPath): Boolean =
    a.absolute().normalize().toString().equals(b.absolute().normalize().toString(), ignoreCase = true)

internal val Resource.epub: Epub get() = fileSystem.epub

internal val Path.epub: Epub get() = fileSystem.epub

@PublishedApi
internal val Resource.delegatePath: JPath get() = path.delegate

internal fun ExistingResource.createNil(): Nil = ConcreteNil(path)

internal val metaInfFiles = setOf(
    "container.xml",
    "encryption.xml",
    "manifest.xml",
    "metadata.xml",
    "rights.xml",
    "signatures.xml",
)