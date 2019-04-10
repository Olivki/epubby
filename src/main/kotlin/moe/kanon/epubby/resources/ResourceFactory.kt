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

@file:JvmName("ResourceFactory")

package moe.kanon.epubby.resources

import arrow.core.Try
import moe.kanon.epubby.Book
import moe.kanon.epubby.utils.toXHTML
import moe.kanon.kommons.io.contentType
import moe.kanon.kommons.io.createTemporaryFile
import moe.kanon.kommons.io.extension
import moe.kanon.kommons.io.name
import moe.kanon.kommons.io.writeLine
import org.jsoup.Jsoup
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Creates and returns a appropriate [Resource] instance from the specified [file], or a [MiscResource] if no concrete
 * implementation is available for that specific file type.
 *
 * @receiver the [Book] instance that the resulting `Resource` should be bound to
 *
 * @param [file] the file to create a `Resource` instance for
 *
 * @return the newly created `Resource` instance
 */
@JvmOverloads
@Throws(IOException::class, ResourceDeserializationException::class)
fun Book.createResource(file: Path, addToRepository: Boolean = true): Resource {
    val type = ResourceType.from(file.extension)
    val name = file.name
    
    return when (type) {
        ResourceType.PAGE -> PageResource.newInstance(this, name, file)
        ResourceType.STYLE_SHEET -> StyleSheetResource.newInstance(this, name, file)
        ResourceType.IMAGE -> ImageResource.newInstance(this, name, file)
        ResourceType.OPF -> OpfResource.newInstance(this, name, file)
        ResourceType.NCX -> NcxResource.newInstance(this, name, file)
        // If the file type is not *explicitly* supported, mark it as a MiscResource, this is so that we can still
        // have access to it and work on it, albeit in a very limited way.
        else -> MiscResource.newInstance(this, name, file)
    }.also {
        it.onCreation()
        
        if (addToRepository) this.resources += it
    }
}

@JvmOverloads
fun Book.tryCreateResource(file: Path, addToRepository: Boolean = true): Try<Resource> =
    Try { this.createResource(file) }

// TODO: Documentation

// images
@JvmOverloads
@JvmName("createImage")
fun Book.createImageResource(imageFile: Path, addToRepository: Boolean = true): ImageResource =
    this.createResource(imageFile) as ImageResource

// ncx
@JvmOverloads
@JvmName("createNcx")
fun Book.createNcxResource(ncxFile: Path, addToRepository: Boolean = true): NcxResource =
    this.createResource(ncxFile) as NcxResource

// opf
@JvmOverloads
@JvmName("createOpf")
fun Book.createOpfResource(opfFile: Path, addToRepository: Boolean = true): OpfResource =
    this.createResource(opfFile) as OpfResource

// pages
@JvmOverloads
@JvmName("createPage")
fun Book.createPageResource(pageFile: Path, addToRepository: Boolean = true): PageResource =
    this.createResource(pageFile) as PageResource

// TODO: This
/**
 * Creates a new [PageResource] from the specified [content].
 *
 * The specified `content` will be passed to [Jsoup] before being added to the [resource-repository][Book.resources] to
 * make sure that we're working with valid EPUB pages.
 *
 * Note that if [addToRepository] is `false`, the underlying file of the generated `Resource` will be created in the
 * temporary file directory of the users system, and be marked to [delete on close][StandardOpenOption.DELETE_ON_CLOSE].
 * Unless this is the wanted behaviour, it is recommended to leave `addToRepository` as `true`. As when
 * `addToRepository` is `true` the underlying file will be created directly in the currently active
 * [epub file][Book.file], and added to the [resource-repository][Book.resources] of `this` book, ensuring that the
 * created resource is properly recognized by the system.
 *
 * @param [name] the name of the `.xhtml` file that will be created for the resource
 * @param [content] the XHTML content to parse
 * @param [addToRepository] whether or not the created resource should automatically be added to the
 * [resource-repository][Book.resources] of `this` book.
 *
 * (`true` by default)
 *
 * @return the newly created [PageResource]
 */
@JvmOverloads
@JvmName("createPage")
fun Book.createPageResource(name: String, content: String, addToRepository: Boolean = true): PageResource {
    val book = this@createPageResource
    val doc = Jsoup.parse(content)
    val file = book.pathOf("")
    file.contentType
    val tempFile = createTemporaryFile(suffix = ".xhtml")
        .writeLine(
            doc.toXHTML(), options = *arrayOf(
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                // delete on close might delete the file earlier than we want
                StandardOpenOption.DELETE_ON_CLOSE
            )
        )
    //return this.createPageResource(tempFile)
    TODO()
}

// stylesheets
@JvmOverloads
@JvmName("createStyleSheet")
fun Book.createStyleSheetResource(styleSheetFile: Path, addToRepository: Boolean = true): StyleSheetResource =
    this.createResource(styleSheetFile) as StyleSheetResource

// misc
@JvmOverloads
@JvmName("createMisc")
fun Book.createMiscResource(miscFile: Path, addToRepository: Boolean = true): MiscResource =
    this.createResource(miscFile) as MiscResource