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

package moe.kanon.epubby.metainf

import com.google.common.net.MediaType
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookVersion
import moe.kanon.epubby.internal.NewFeature
import moe.kanon.epubby.props.Relationship
import moe.kanon.epubby.utils.NonEmptyList
import java.nio.file.Path

class MetaInfContainer(
    val book: Book,
    val version: String,
    val rootFiles: NonEmptyList<RootFile>,
    val links: MutableList<Link>
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

    class RootFile(val book: Book, val fullPath: Path, val mediaType: MediaType) {
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is RootFile -> false
            book != other.book -> false
            fullPath != other.fullPath -> false
            mediaType != other.mediaType -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = book.hashCode()
            result = 31 * result + fullPath.hashCode()
            result = 31 * result + mediaType.hashCode()
            return result
        }

        override fun toString(): String = "RootFile(book=$book, fullPath='$fullPath', mediaType='$mediaType')"
    }

    class Link(
        val book: Book,
        val href: String,
        @NewFeature(since = BookVersion.EPUB_3_0)
        val relation: Relationship? = null,
        val mediaType: MediaType? = null
    ) {
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Link -> false
            book != other.book -> false
            href != other.href -> false
            relation != other.relation -> false
            mediaType != other.mediaType -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = book.hashCode()
            result = 31 * result + href.hashCode()
            result = 31 * result + (relation?.hashCode() ?: 0)
            result = 31 * result + (mediaType?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = buildString {
            append("Link(")
            append("book=$book")
            append("href='$href'")
            if (relation != null) append("relation='$relation'")
            if (mediaType != null) append("mediaType='$mediaType'")
            append(")")
        }
    }
}