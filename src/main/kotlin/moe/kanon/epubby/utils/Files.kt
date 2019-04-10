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

package moe.kanon.epubby.utils

import moe.kanon.kommons.io.createDirectory
import moe.kanon.kommons.io.createFile
import moe.kanon.kommons.io.exists
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute

/**
 * Creates and returns a [FileSystem] based on `this` file, with the specified [env] variables and the specified
 * [classLoader].
 */
@JvmOverloads
fun Path.createFileSystem(env: Map<String, String> = emptyMap(), classLoader: ClassLoader? = null): FileSystem =
    FileSystems.newFileSystem(toUri(), env, classLoader)

/**
 * Returns `this` directory if it exists, otherwise creates a new directory and returns that.
 */
fun Path.getOrCreateDirectory(vararg attributes: FileAttribute<*>): Path =
    if (this.exists) this else this.createDirectory(*attributes)

/**
 * Returns `this` file if it exists, otherwise creates a new directory and returns that.
 */
fun Path.getOrCreateFile(vararg attributes: FileAttribute<*>): Path =
    if (this.exists) this else this.createFile(*attributes)