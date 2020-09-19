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

package dev.epubby

import dev.epubby.resources.ImageResource
import dev.epubby.resources.ResourceVisitorUnit
import moe.kanon.kommons.io.ImageResizeMode
import moe.kanon.kommons.io.paths.*
import java.nio.file.Path

const val FILE_NAME = "test_2.epub"
const val DIR = "!EPUB3"

private val INPUT: Path = pathOf("H:", "Programming", "JVM", "Kotlin", "Data", "epubby", "reader")
    .resolve(DIR)
    .resolve(FILE_NAME)
private val OUTPUT: Path = pathOf("H:", "Programming", "JVM", "Kotlin", "Data", "epubby", "writer")
    .resolve(DIR)
    .resolve(FILE_NAME)

// TODO: move any resources that are outside of the opf directory into it by default.
// TODO: remove any files that are not inside of the manifest except for those inside of /META-INF/?
// TODO: verify tableOfContents once that has been properly implemented

fun main() {
    OUTPUT.deleteIfExists()

    val file = INPUT.copyTo(OUTPUT)

    readEpub(file).use { epub ->
        epub.organizeFiles()
        epub.clean()

        /*val ncxResource = epub.manifest.localResources.values.filterIsInstance<NcxResource>().first()
        val toc = NavigationCenterExtendedModel
            .fromFile(ncxResource.file.delegate, ParseMode.STRICT)
            .unwrap()

        println(toc)
        println(toc.toDocument().encodeToString())*/
    }
}

private object ImageResizer : ResourceVisitorUnit {
    override fun visitImage(image: ImageResource) {
        val bufferedImage = image.getOrLoadImage()

        image.resizeTo(820, 1200, resizeMode = ImageResizeMode.AUTOMATIC)
    }
}

private fun createBackup(original: Path): Path =
    original.copyTo(createTmpDirectory("epubby"), keepName = true).apply { toFile().deleteOnExit() }