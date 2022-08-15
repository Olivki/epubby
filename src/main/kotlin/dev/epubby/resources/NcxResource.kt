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

import com.google.auto.service.AutoService
import com.google.common.net.MediaType
import dev.epubby.Epub
import dev.epubby.files.RegularFile
import kotlinx.collections.immutable.persistentHashSetOf

// TODO: Change the 'file' of 'NcxDocument' if the file of this one gets changed, make sure to validate that this is the
//       one defined inside of the 'toc' attribute too
// TODO: Make sure that there's only ever *one* NcxResource per Epub instance

class NcxResource internal constructor(
    epub: Epub,
    identifier: String,
    file: RegularFile,
    override val mediaType: MediaType = MEDIA_TYPE
) : LocalResource(epub, identifier, file) {
    companion object {
        // TODO: unsure if the actual "proper" media-type is either 'x-dtbncx+xml' or 'oebps-package+xml'
        @JvmField
        val MEDIA_TYPE: MediaType = MediaType.create("application", "x-dtbncx+xml")
    }

    /**
     * Returns the result of invoking the [visitNcx][ResourceVisitor.visitNcx] function of the given [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitNcx(this)

    override fun toString(): String = "PageResource(identifier='$identifier', mediaType=$mediaType, file='$file')"
}

@AutoService(LocalResourceLocator::class)
internal object NcxResourceLocator : LocalResourceLocator {
    private val TYPES: Set<MediaType> = persistentHashSetOf(
        MediaType.create("application", "x-dtbncx+xml"),
        MediaType.create("application", "oebps-package+xml")
    )
    private val FACTORY: LocalResourceFactory = ::NcxResource

    override fun findFactory(mediaType: MediaType): LocalResourceFactory? = FACTORY.takeIf { mediaType in TYPES }
}