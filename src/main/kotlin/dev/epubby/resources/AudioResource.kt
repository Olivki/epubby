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

class AudioResource(
    epub: Epub,
    identifier: String,
    file: RegularFile,
    override val mediaType: MediaType
) : LocalResource(epub, identifier, file) {
    /**
     * Returns the result of invoking the [visitAudio][ResourceVisitor.visitAudio] function of the given [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitAudio(this)

    override fun toString(): String = "AudioResource(identifier='$identifier', mediaType=$mediaType, file='$file')"
}

@AutoService(LocalResourceLocator::class)
internal object AudioResourceLocator : LocalResourceLocator {
    private val TYPES: Set<MediaType> = persistentHashSetOf(MediaType.MPEG_AUDIO)
    private val FACTORY: LocalResourceFactory = ::AudioResource

    override fun findFactory(mediaType: MediaType): LocalResourceFactory? = FACTORY.takeIf { mediaType in TYPES }
}