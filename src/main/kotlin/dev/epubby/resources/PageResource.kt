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
import dev.epubby.page.Page
import kotlinx.collections.immutable.persistentHashSetOf

class PageResource(
    epub: Epub,
    identifier: String,
    file: RegularFile,
    override val mediaType: MediaType
) : LocalResource(epub, identifier, file) {
    /**
     * Returns the [Page] of this page resource, or `null` if this resource has no `Page` associated with it.
     */
    fun getPageOrNull(): Page? = epub.spine.pages.firstOrNull { it.reference == this }

    /**
     * Returns the [Page] of this resource, or throws a [NoSuchElementException] if this resource no `Page` associated
     * with it.
     */
    fun getPage(): Page =
        getPageOrNull() ?: throw NoSuchElementException("No page associated with resource '$identifier'")

    /**
     * Returns the result of invoking the [visitPage][ResourceVisitor.visitPage] function of the given [visitor].
     */
    override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitPage(this)

    override fun toString(): String = "PageResource(identifier='$identifier', mediaType=$mediaType, file='$file')"
}

@AutoService(LocalResourceLocator::class)
internal object PageResourceLocator : LocalResourceLocator {
    private val TYPES: Set<MediaType> = persistentHashSetOf(
        MediaType.XHTML_UTF_8,
        MediaType.XHTML_UTF_8.withoutParameters()
    )
    private val FACTORY: LocalResourceFactory = ::PageResource

    override fun findFactory(mediaType: MediaType): LocalResourceFactory? = FACTORY.takeIf { mediaType in TYPES }
}