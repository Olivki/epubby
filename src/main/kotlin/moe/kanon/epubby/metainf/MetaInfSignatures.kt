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

import java.nio.file.FileSystem

/**
 * Represents the [signatures.xml](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-ocf.html#sec-container-metainf-signatures.xml)
 * meta-inf file.
 */
class MetaInfSignatures private constructor() {
    @JvmSynthetic
    internal fun writeToFile(fileSystem: FileSystem) {
        TODO()
    }
}