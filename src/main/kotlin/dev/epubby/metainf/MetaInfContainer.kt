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
import dev.epubby.Book
import dev.epubby.BookVersion
import dev.epubby.files.RegularFile
import dev.epubby.internal.IntroducedIn
import dev.epubby.properties.Relationship
import dev.epubby.utils.NonEmptyList

class MetaInfContainer @JvmOverloads constructor(
    val book: Book,
    val version: ContainerVersion,
    val rootFiles: NonEmptyList<RootFile>,
    val links: MutableList<Link> = mutableListOf()
) {
    val packageDocument: RootFile
        get() = rootFiles[0]

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

    class RootFile(val book: Book, val fullPath: RegularFile, val mediaType: MediaType) {
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

    class Link @JvmOverloads constructor(
        val book: Book,
        val href: String,
        @IntroducedIn(version = BookVersion.EPUB_3_0)
        val relation: Relationship? = null,
        val mediaType: MediaType? = null
    ) {
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