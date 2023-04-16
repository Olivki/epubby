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

package net.ormr.epubby.internal

import dev.epubby.Epub
import dev.epubby.EpubFiles
import dev.epubby.version.EpubVersion

// https://idpf.org/epub/20/spec/OCF_2.0.1_draft.doc
// https://www.w3.org/publishing/epub3/epub-ocf.html
internal class EpubImpl(override val version: EpubVersion, override val files: EpubFiles) : Epub {
    override fun close() {
        files.fileSystem.close()
    }
}