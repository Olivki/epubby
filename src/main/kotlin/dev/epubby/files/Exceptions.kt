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

import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path

internal fun fileAlreadyExists(path: Path): Nothing =
    throw FileAlreadyExistsException(path.toString(), null, "Overwriting existing files is forbidden.")

internal fun fileAlreadyExists(origin: Path, other: Path): Nothing =
    throw FileAlreadyExistsException(origin.toString(), other.toString(), "Overwriting existing files is forbidden.")