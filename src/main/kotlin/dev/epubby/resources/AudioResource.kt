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

package dev.epubby.resources

import com.google.auto.service.AutoService
import com.google.common.net.MediaType
import dev.epubby.Book
import kotlinx.collections.immutable.persistentHashSetOf
import java.nio.file.Path

class AudioResource(
    book: Book,
    identifier: String,
    file: Path,
    override val mediaType: MediaType
) : Resource(book, identifier, file) {
    override fun <R : Any> accept(visitor: ResourceVisitor<R>): R = visitor.visitAudio(this)

    override fun toString(): String = "AudioResource(identifier='$identifier', mediaType=$mediaType, file='$file')"
}

@AutoService(ResourceLocator::class)
internal object AudioResourceLocator : ResourceLocator {
    private val TYPES: Set<MediaType> = persistentHashSetOf(MediaType.MPEG_AUDIO)
    private val FACTORY: ResourceFactory = ::AudioResource

    override fun findFactory(mediaType: MediaType): ResourceFactory? = FACTORY.takeIf { mediaType in TYPES }
}