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

package dev.epubby.packages

import com.google.common.net.MediaType
import dev.epubby.Book
import dev.epubby.BookElement
import dev.epubby.properties.Properties

// TODO: make sure to redirect the user to the ResourceRepository for modifying pages.
class PackageManifest(override val book: Book) : BookElement {
    val remoteItems: MutableMap<String, RemoteItem> = hashMapOf()

    override val elementName: String
        get() = "PackageManifest"

    class RemoteItem @JvmOverloads constructor(
        override val book: Book,
        val identifier: String,
        // TODO: make this into a 'URL' or 'URI'?
        var href: String,
        var mediaType: MediaType? = null,
        var fallback: String? = null,
        var mediaOverlay: String? = null,
        // TODO: verify that the version isn't older than 3.0 if this is used at some point
        val properties: Properties = Properties.empty()
    ) : BookElement {
        override val elementName: String
            get() = "PackageManifest.RemoteItem"

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is RemoteItem -> false
            identifier != other.identifier -> false
            href != other.href -> false
            mediaType != other.mediaType -> false
            fallback != other.fallback -> false
            mediaOverlay != other.mediaOverlay -> false
            properties != other.properties -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = identifier.hashCode()
            result = 31 * result + href.hashCode()
            result = 31 * result + (mediaType?.hashCode() ?: 0)
            result = 31 * result + (fallback?.hashCode() ?: 0)
            result = 31 * result + (mediaOverlay?.hashCode() ?: 0)
            result = 31 * result + properties.hashCode()
            return result
        }

        override fun toString(): String =
            "RemoteItem(identifier='$identifier', href='$href', mediaType=$mediaType, fallback=$fallback, mediaOverlay=$mediaOverlay, properties=$properties)"
    }
}