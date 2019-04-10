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

@file:Suppress("MemberVisibilityCanBePrivate")

package moe.kanon.epubby.resources

import moe.kanon.epubby.Book
import moe.kanon.epubby.BookListener
import moe.kanon.epubby.MalformedBookException
import moe.kanon.epubby.resources.opf.PackageDocument
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.epubby.utils.toUnmodifiableSet
import moe.kanon.kommons.io.name
import moe.kanon.kommons.io.notExists
import moe.kanon.kommons.misc.multiCatch
import org.xml.sax.SAXException
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.xml.parsers.ParserConfigurationException

// TODO: This
class ResourceDirectory(val book: Book, val type: ResourceType) : Iterable<Resource> {
    /**
     * All the resources stored under `this` resource-directory.
     */
    private val resources: List<Resource> = ArrayList()
    
    /**
     *
     */
    val directory: Path = book.pathOf(type.location).also {
        if (it.notExists) throw MalformedBookException.create(book, "Resource directory <'$it'> is missing")
    }
    
    override fun iterator(): Iterator<Resource> = resources.toList().iterator()
}

/**
 * A abstract implementation of a resource that is used in the specified [book] instance.
 *
 * @constructor Creates a new [Resource] from the arguments.
 *
 * @param [book] a call-back reference to the [Book] instance that the resource is bound to
 * @param [name] the name of the resource, this should be based on the `file` name
 * @param [file] the [file][Path] that the resource is based on
 * @param [type] the [ResourceType] of the resource, this is used for categorizing the resource into different folders
 * in the epub
 *
 * @property [book] A call-back reference to the [Book] instance that `this` resource is bound to.
 * @property [name] The name of `this` resource, this should be based on the `file` name.
 * @property [type] The [ResourceType] of `this` resource, this is used for categorizing the resource into different
 * folders in the epub.
 */
sealed class Resource(val book: Book, val name: String, file: Path, val type: ResourceType) : BookListener {
    /**
     * The underlying file that `this` resource represents.
     */
    var origin: Path = file
        @JvmSynthetic internal set
    
    /**
     * Returns a [HREF] pointing to the location of `this` resources' [origin] file.
     *
     * Note that this property will create a new `HREF` instance on every call. This is done to make sure that the
     * returned `HREF` always points to the correct location, as the `origin` file might have been moved or renamed
     * since the the creation of `this` resource.
     */
    // TODO: Might be able to just get this from the relative path of the origin file
    //val href: String get() = "${type.location}${origin.name}"
    val href: HREF = HREF(this)
    
    /**
     * The manifest id of this resource.
     *
     * This might be migrated to it's own class or something.
     */
    lateinit var manifestId: String
        @JvmSynthetic internal set
    
    /**
     * Returns a basic representation of the location of this resource in relation to it's [type].
     *
     * What makes this different from [href] is that this has "`../`" appended at the start, this is generally used
     * when working with files inside of the epub to mark relative paths.
     *
     * TODO: This might not even be needed what with how the Path system works with zip files.
     */
    val relativeHref: String get() = "../$href"
    
    /**
     * A [Set] containing all the resources that reference `this` resource in some way.
     */
    private val refs: MutableSet<Resource> = HashSet()
    
    /**
     * Returns an unmodifiable view of the [refs] property of `this` resource.
     *
     * @see refs
     */
    val references: Set<Resource> get() = refs.toUnmodifiableSet()
    
    /**
     * Invoked by the underlying [book] of `this` resource when all the systems have been initialized.
     */
    override fun onBookInitialization() {
        TODO("not implemented")
    }
    
    /**
     * Invoked when `this` resource is first created.
     */
    @Throws(ResourceDeserializationException::class)
    abstract fun onCreation()
    
    /**
     * Invoked when `this` resource has been marked for removal.
     */
    @Throws(ResourceDeletionException::class)
    open fun onRemoval() {
    }
    
    /**
     * Returns whether or not the [origin] file of `this` resource is located inside of the directory of the specified
     * [type].
     *
     * This currently does a very naïve equality check.
     */
    @JvmName("isInDirectoryOf")
    operator fun contains(type: ResourceType): Boolean = origin.parent.name == type.location.substringBefore('/')
    
    /**
     * Attempts to rename this resource to the specified [name].
     *
     * Renaming a resource this way also updates any and all references made to this resource in the epub.
     *
     * This only changes the actual name part, the `extension` of the resource stays the same.
     *
     * Example:
     *
     * ```kotlin
     *      val resource = ... // Current file name is "page.xhtml".
     *      resource.renameTo("big_page") // New file name is "big_page.xhtml".
     * ```
     *
     * Changing the `extension` of the [origin] file is *not* supported in this form, and is very much not recommended
     * to actually ever do.
     */
    @Throws(ResourceModificationException::class)
    infix fun renameTo(name: String) {
        val oldFile = origin
        
        //origin = origin.renameTo()
    }
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Resource -> false
        book != other.book -> false
        name != other.name -> false
        type != other.type -> false
        origin != other.origin -> false
        manifestId != other.manifestId -> false
        refs != other.refs -> false
        else -> true
    }
    
    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + origin.hashCode()
        result = 31 * result + manifestId.hashCode()
        result = 31 * result + refs.hashCode()
        return result
    }
    
    override fun toString(): String =
        "Resource(type=$type, name='$name', origin='$origin', manifestId='$manifestId', book=$book)"
    
}

/**
 * An `image-resource` is any image file with the following extensions; {png, jpg, jpeg, gif and svg}. No other image
 * files are supported as those are the only image types that are supported by the epub standard itself.
 */
class ImageResource private constructor(book: Book, name: String, file: Path) :
    Resource(book, name, file, ResourceType.IMAGE) {
    
    /**
     * The [BufferedImage] instance of this image resource.
     *
     * This is generated lazily to prevent unneeded overhead during the deserialization of the book.
     */
    @get:Throws(ResourceDeserializationException::class)
    val image: BufferedImage by lazy {
        return@lazy try {
            ImageIO.read(origin.toFile())
        } catch (e: IOException) {
            throw ResourceDeserializationException.create(
                book,
                origin,
                "Failed to read the image. <${e.message}>",
                e
            )
        }
    }
    
    /**
     * What `type` of image this resource contains.
     *
     * This is generated during the creation process of this resource, trying to access it before that will
     * result in an `exception` being thrown.
     */
    lateinit var imageType: ImageType
        private set
    
    @Throws(ResourceDeserializationException::class)
    override fun onCreation() {
        imageType = ImageType.from(origin)
    }
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ImageResource -> false
        !super.equals(other) -> false
        image != other.image -> false
        imageType != other.imageType -> false
        else -> true
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + imageType.hashCode()
        return result
    }
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book, name: String, file: Path): ImageResource = ImageResource(book, name, file)
    }
}

/**
 * The `class` representation for the `NCX` file.
 *
 * `NCX` stands for **N**avigation **C**enter e**X**tended
 *
 * The `NCX` file represents the table of contents in epub files of format versions under v3.
 *
 * The v3 format introduced the `nav` page which replaced the `NCX` file, epubby will still generate an accurate ncx
 * file regardless of version.
 */
class NcxResource private constructor(book: Book, name: String, file: Path) :
    Resource(book, name, file, ResourceType.NCX) {
    
    @Throws(ResourceDeserializationException::class)
    override fun onCreation() {
        try {
            //TableOfContentsReader.readFile(getBook(), file)
        } catch (e: Exception) {
            e.multiCatch(ParserConfigurationException::class, IOException::class, SAXException::class) {
                throw ResourceDeserializationException.create(book, origin, e.message.toString(), e)
            }
        }
    }
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book, name: String, file: Path): NcxResource = NcxResource(book, name, file)
    }
}

/**
 * Representation of the [OPF][PackageDocument] class in resource form.
 *
 * `OPF` stands for **O**pen **P**ackaging **F**ormat.
 */
class OpfResource private constructor(book: Book, name: String, file: Path) :
    Resource(book, name, file, ResourceType.OPF) {
    
    @Throws(ResourceDeserializationException::class)
    override fun onCreation() { // This loads before pages and maybe something more, might cause issues.
        try {
            //ContentReader.readFile(getBook(), file)
        } catch (e: IOException) {
            throw ResourceDeserializationException.create(book, origin, e.message.toString(), e)
        }
        
    }
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book, name: String, file: Path): OpfResource = OpfResource(book, name, file)
    }
}

/**
 * A resource that represents `XHTML` & `HTML` files.
 */
class PageResource private constructor(book: Book, name: String, file: Path) :
    Resource(book, name, file, ResourceType.PAGE) {
    
    /**
     * The [Page] instance of this `resource`.
     *
     * This is what should be used if you want to do some actual work on a page.
     *
     * **Note:** This is generated during the creation process of this resource, trying to access it before that will
     * result in an `exception` being thrown.
     */
    lateinit var page: Page
        private set
    
    @Throws(ResourceDeserializationException::class)
    override fun onCreation() {
        try {
            //this.page = getBook().getPages().addPage(file)
        } catch (e: IOException) {
            throw ResourceDeserializationException("Failed to read the (X)HTML file.", e)
        }
        
    }
    
    override fun onRemoval() {
        //getBook().getPages().removePage(getPage())
    }
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PageResource -> false
        !super.equals(other) -> false
        page != other.page -> false
        else -> true
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + page.hashCode()
        return result
    }
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book, name: String, file: Path): PageResource = PageResource(book, name, file)
    }
}

/**
 * The `class` representation for `CSS` files.
 */
class StyleSheetResource private constructor(book: Book, name: String, file: Path) :
    Resource(book, name, file, ResourceType.STYLE_SHEET) {
    
    /**
     * **Note:** This is generated during the creation process of this resource, trying to access it before that will
     * result in an `exception` being thrown.
     */
    //lateinit var reader: StyleSheetReader
    //    private set
    
    @Throws(ResourceDeserializationException::class)
    override fun onCreation() {
        //styleSheetReader = getBook().getStyleSheets().registerReader(this)
    }
    
    override fun onRemoval() {
        //getBook().getPages().removeStyleSheet(getStyleSheetReader())
        //getBook().getStyleSheets().unregisterReader(this)
    }
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book, name: String, file: Path): StyleSheetResource =
            StyleSheetResource(book, name, file)
    }
}

/**
 * Represents a resource for which there is no concrete implementation of.
 *
 * This class is here to provide *(albeit limited)* support for working with resources that the epub may use that do
 * not currently have concrete implementations in the epubby system.
 */
class MiscResource private constructor(book: Book, name: String, file: Path) :
    Resource(book, name, file, ResourceType.MISC) {
    
    @Throws(ResourceDeserializationException::class)
    override fun onCreation() {
        book.logger.warn { "\"${origin.name}\" has been marked as a MiscResource." }
    }
    
    companion object {
        @JvmSynthetic
        internal fun newInstance(book: Book, name: String, file: Path): MiscResource = MiscResource(book, name, file)
    }
}