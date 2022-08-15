/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.resources

import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.properties.Properties

class ExternalResource @JvmOverloads constructor(
    override val epub: Epub,
    override val identifier: String,
    // TODO: make this into a 'URL' or 'URI'?
    override var href: String,
    override val mediaType: MediaType,
    override var fallback: ManifestResource? = null,
    override var mediaOverlay: String? = null,
    // TODO: verify that the version isn't older than 3.0 if this is used at some point
    //       make sure to always add the remote-resource property to the model
    override val properties: Properties = Properties.empty(),
) : ManifestResource() {
    override val elementName: String
        get() = "PackageManifest.ExternalResource"

    /**
     * Returns the result of invoking the [visitRemote][ResourceVisitor.visitExternal] function of the given [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitExternal(this)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ExternalResource -> false
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
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + (fallback?.hashCode() ?: 0)
        result = 31 * result + (mediaOverlay?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()
        return result
    }

    override fun toString(): String =
        "ExternalResource(identifier='$identifier', href='$href', mediaType=$mediaType, fallback=$fallback, mediaOverlay=$mediaOverlay, properties=$properties)"
}