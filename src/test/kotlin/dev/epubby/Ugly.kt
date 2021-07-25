/*
 * Copyright 2019-2021 Oliver Berg
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

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.resources.ImageResource
import dev.epubby.resources.ResourceFilters
import dev.epubby.resources.ResourceVisitorUnit
import dev.epubby.utils.ImageDimension
import dev.epubby.utils.ImageOrientation
import dev.epubby.utils.ImageOrientation.*
import dev.epubby.utils.dimension
import krautils.scalr.ImageResizeMode
import java.awt.image.BufferedImage
import java.nio.file.Path
import kotlin.io.path.*

const val FILE_NAME = "test_1.epub"
const val DIR = "!EPUB2"

private val INPUT: Path = Path("H:", "Programming", "JVM", "Kotlin", "Data", "epubby", "reader")
    .resolve(DIR)
    .resolve(FILE_NAME)
private val OUTPUT: Path = Path("H:", "Programming", "JVM", "Kotlin", "Data", "epubby", "writer")
    .resolve(DIR)
    .resolve(FILE_NAME)

// TODO: make sure that a 'opf' file can never be added to the manifest as a resource as that is disallowed by
//       by the EPUB spec
// TODO: move any resources that are outside of the opf directory into it by default.
// TODO: remove any files that are not inside of the manifest except for those inside of /META-INF/?
// TODO: verify tableOfContents once that has been properly implemented
// TODO: introduce the jetbrains annotations library as an API dependency

fun main() {
    OUTPUT.deleteIfExists()

    val file = INPUT.copyTo(OUTPUT)

    readEpub(file).use { epub ->
        epub.manifest.visitResources(ImageResizer, ResourceFilters.ONLY_IMAGES)
        //println(epub.tableOfContents.entries.joinToString(LINE_SEPARATOR))
    }
}

private object ImageResizer : ResourceVisitorUnit {
    private val LOGGER: InlineLogger = InlineLogger(ImageResizer::class)

    private val PORTRAIT_DIMENSION = ImageDimension(820, 1200)

    private val LANDSCAPE_DIMENSION = ImageDimension(1584, 1200)

    override fun visitImage(resource: ImageResource) {
        val image = resource.getOrLoadImage()
        val orientation = ImageOrientation.fromBufferedImage(image)

        if (shouldResize(image, orientation)) {
            val targetDimension = when (orientation) {
                PORTRAIT -> PORTRAIT_DIMENSION
                LANDSCAPE -> LANDSCAPE_DIMENSION
                RECTANGLE -> ImageDimension(image.width, image.height) / 2
            }

            resource.resizeTo(targetDimension, resizeMode = ImageResizeMode.AUTOMATIC)

            LOGGER.info { "'${resource.file.name}' ${image.dimension} -> ${resource.getOrLoadImage().dimension}." }
        }
    }

    private fun shouldResize(image: BufferedImage, orientation: ImageOrientation): Boolean = when (orientation) {
        PORTRAIT -> image.width > PORTRAIT_DIMENSION.width && image.height > PORTRAIT_DIMENSION.height
        LANDSCAPE -> image.width > LANDSCAPE_DIMENSION.width && image.height > LANDSCAPE_DIMENSION.height
        RECTANGLE -> image.width > 1500
    }
}

private fun createBackup(original: Path): Path =
    original.copyTo(createTempDirectory("epubby").resolve(original.name)).apply { toFile().deleteOnExit() }