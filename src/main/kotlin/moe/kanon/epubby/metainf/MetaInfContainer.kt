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

import com.vdurmont.semver4j.Semver
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.utils.SemVer
import moe.kanon.epubby.utils.SemVerType
import moe.kanon.epubby.utils.attr
import moe.kanon.epubby.utils.parseXmlFile
import java.nio.file.Path

class MetaInfContainer private constructor(
    val file: Path,
    val version: Semver,
    private val _rootFiles: MutableList<RootFile>,
    private val _links: MutableList<Link>
) {
    companion object {
        internal fun parse(
            epubFile: Path,
            containerFile: Path
        ): MetaInfContainer = parseXmlFile(containerFile) { doc, root ->
            val version = SemVer(root.attr("version", epubFile, containerFile), SemVerType.LOOSE)
            TODO()
        }
    }

    val rootFiles: ImmutableList<RootFile> get() = _rootFiles.toImmutableList()

    val links: ImmutableList<Link> get() = _links.toImmutableList()

    data class RootFile(val path: Path, val mediaType: String)

    data class Link(val href: Path, val relation: String, val mediaType: String?)
}