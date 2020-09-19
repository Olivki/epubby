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

package dev.epubby.metainf

import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.EpubVersion
import dev.epubby.files.RegularFile
import dev.epubby.internal.IntroducedIn
import dev.epubby.properties.Relationship
import dev.epubby.utils.NonEmptyList

class MetaInfContainer internal constructor(
    override val epub: Epub,
    val file: RegularFile,
    val version: String,
    val rootFiles: NonEmptyList<RootFile>,
    val links: MutableList<Link> = mutableListOf()
) : EpubElement {
    override val elementName: String
        get() = "container"

    val opf: RootFile
        get() = rootFiles[0]

    @JvmSynthetic
    internal fun updateOpfFile(newFile: RegularFile) {
        rootFiles[0] = RootFile(epub, newFile, opf.mediaType)
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is MetaInfContainer -> false
        version != other.version -> false
        rootFiles != other.rootFiles -> false
        links != other.links -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + rootFiles.hashCode()
        result = 31 * result + links.hashCode()
        return result
    }

    override fun toString(): String = "MetaInfContainer(version='$version', rootFiles=$rootFiles, links=$links)"

    class RootFile internal constructor(
        override val epub: Epub,
        val fullPath: RegularFile,
        val mediaType: MediaType,
    ) : EpubElement {
        override val elementName: String
            get() = "container/rootfile"

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is RootFile -> false
            fullPath != other.fullPath -> false
            mediaType != other.mediaType -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = fullPath.hashCode()
            result = 31 * result + mediaType.hashCode()
            return result
        }

        override fun toString(): String = "RootFile(fullPath='$fullPath', mediaType='$mediaType')"
    }

    class Link internal constructor(
        override val epub: Epub,
        val href: String,
        @IntroducedIn(version = EpubVersion.EPUB_3_0)
        val relation: Relationship? = null,
        val mediaType: MediaType? = null
    ) : EpubElement {
        override val elementName: String
            get() = "container/link"

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Link -> false
            href != other.href -> false
            relation != other.relation -> false
            mediaType != other.mediaType -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = href.hashCode()
            result = 31 * result + (relation?.hashCode() ?: 0)
            result = 31 * result + (mediaType?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("Link(")
            append("href='$href'")
            if (relation != null) append("relation='$relation'")
            if (mediaType != null) append("mediaType='$mediaType'")
            append(")")
        }
    }
}