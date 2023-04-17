/*
 * Copyright 2019-2023 Oliver Berg
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

package net.ormr.epubby.internal.util

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Feature.FILE_CHANNEL
import com.google.common.jimfs.Feature.SECURE_DIRECTORY_STREAM
import com.google.common.jimfs.Jimfs
import com.google.common.jimfs.PathType
import java.nio.file.FileSystem
import java.util.*

// because 'unix' configuration sets working directory to 'work' which is not what we want for the structure of the epub
// see https://github.com/google/jimfs/issues/74
private val EPUBBY: Configuration = Configuration.builder(PathType.unix()).apply {
    setRoots("/")
    setWorkingDirectory("/")
    setAttributeViews("basic")
    // zip files don't *really* support symbolic links, so no point in enabling it here
    setSupportedFeatures(SECURE_DIRECTORY_STREAM, FILE_CHANNEL)
}.build()

internal fun createEpubbyFileSystem(): FileSystem = Jimfs.newFileSystem("epubby-${UUID.randomUUID()}", EPUBBY)