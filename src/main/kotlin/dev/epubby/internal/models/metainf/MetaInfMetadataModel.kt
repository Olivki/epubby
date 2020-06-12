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

package dev.epubby.internal.models.metainf

import dev.epubby.Book
import dev.epubby.internal.documentFrom
import dev.epubby.internal.writeTo
import dev.epubby.metainf.MetaInfMetadata
import org.jdom2.Document
import java.nio.file.FileSystem
import java.nio.file.Path

internal data class MetaInfMetadataModel internal constructor(internal val document: Document) {
    internal fun writeToFile(fileSystem: FileSystem) {
        document.writeTo(fileSystem.getPath("/META-INF/metadata.xml"))
    }

    internal fun toMetaInfMetadata(book: Book): MetaInfMetadata = MetaInfMetadata(book, document)

    internal companion object {
        internal fun fromFile(file: Path): MetaInfMetadataModel = MetaInfMetadataModel(documentFrom(file))

        internal fun fromMetaInfMetadata(origin: MetaInfMetadata): MetaInfMetadataModel =
            MetaInfMetadataModel(origin.document)
    }
}