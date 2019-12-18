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

package moe.kanon.epubby.metainf

import moe.kanon.epubby.internal.logger
import moe.kanon.epubby.utils.parseXmlFile
import moe.kanon.epubby.utils.writeTo
import org.jdom2.Document
import java.nio.file.FileSystem
import java.nio.file.Path

/**
 * Represents the [rights.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-rights.xml)
 * meta-inf file.
 */
class MetaInfRights private constructor(val file: Path, private val document: Document) {
    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        document.writeTo(fileSystem.getPath("META-INF", "rights.xml"))
    }

    override fun toString(): String = "MetaInfRights(file='$file', document=$document)"

    internal companion object {
        @JvmSynthetic
        internal fun fromFile(
            epub: Path,
            container: Path,
            directory: Path
        ): MetaInfRights = parseXmlFile(container) { doc, _ ->
            // as stated in the EPUB specification for v3.2:
            // > This version of the OCF specification does not require a specific format for DRM information, but a
            // future version might.
            // this means that there's no proper "format" for us to traverse and digest, so we just save the parsed
            // document so we can properly write it back again when needed. as of epub 3.2 as long as the EPUB file
            // contains a 'rights.xml' file it means that it is governed by some sort of digital rights. (DRM)
            MetaInfRights(container, doc).also {
                logger.trace { "Constructed meta-inf-rights instance <$it>" }
            }
        }
    }
}