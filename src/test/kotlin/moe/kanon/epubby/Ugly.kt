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

@file:Suppress("CheckedExceptionsKotlin")

package moe.kanon.epubby

import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.createTmpDirectory
import moe.kanon.kommons.io.paths.deleteIfExists
import moe.kanon.kommons.io.paths.pathOf
import java.nio.file.Path

const val FILE_NAME = "test_1.epub"
const val DIR = "!EPUB3"

private val input: Path = pathOf("H:", "Programming", "JVM", "Kotlin", "Data", "epubby", "reader")
    .resolve(DIR)
    .resolve(FILE_NAME)
private val output: Path = pathOf("H:", "Programming", "JVM", "Kotlin", "Data", "epubby", "writer")
    .resolve(DIR)
    .resolve(FILE_NAME)
private val writer = BookWriter()

// TODO: This library somehow corrupts/removes the metadata for the actual cover image of the epub, as Calibre can not
//       seem to load it, fix this

fun main() {
    output.deleteIfExists()

    val book = readBook(createBackup(input)).use {
        it.resources.moveToDesiredDirectories()
        return@use it
    }

    writer.writeToFile(book, output)

}

private fun createBackup(original: Path): Path =
    original.copyTo(createTmpDirectory("epubby"), keepName = true).apply { toFile().deleteOnExit() }