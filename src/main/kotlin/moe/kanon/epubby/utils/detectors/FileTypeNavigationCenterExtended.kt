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

package moe.kanon.epubby.utils.detectors

import com.google.auto.service.AutoService
import moe.kanon.kommons.io.paths.name
import java.nio.file.Path
import java.nio.file.spi.FileTypeDetector

/**
 * [Navigation Center eXtended](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.4.1)
 */
@AutoService(FileTypeDetector::class)
class FileTypeNavigationCenterExtended : FileTypeDetector() {
    override fun probeContentType(path: Path): String? =
        if (path.name.endsWith(".ncx")) "application/oebps-package+xml" else null
}