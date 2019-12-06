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

import com.google.common.net.MediaType
import moe.kanon.kommons.io.paths.deleteIfExists
import moe.kanon.kommons.io.paths.pathOf
import moe.kanon.kommons.writeOut

const val BASE_IN_PATH = "H:\\Programming\\JVM\\Kotlin\\Data\\epubby\\reader"
const val BASE_OUT_PATH = "H:\\Programming\\JVM\\Kotlin\\Data\\epubby\\writer"
const val FILE_NAME = "test_1.epub"
const val DIR = "!EPUB3"

fun main() {
    val inDirectory = pathOf(BASE_IN_PATH, DIR, FILE_NAME)
    val outDirectory = pathOf(BASE_OUT_PATH, DIR)
    outDirectory.resolve(FILE_NAME).deleteIfExists()
    readBook(inDirectory, outDirectory).use { book ->
        book.pages.transformers.registerInstalled()
        book.resources.moveToDesiredDirectories()
        book.`save all this shit to the place yo lol`()
        //writeOut("file-system: <${it.fileSystem}>")
    }
}