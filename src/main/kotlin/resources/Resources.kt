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
import moe.kanon.epubby.BookLoad
import moe.kanon.epubby.MalformedBookException
import moe.kanon.epubby.multiCatch
import moe.kanon.epubby.resources.Resource.Type
import moe.kanon.epubby.resources.Resource.Type.MISC
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.kextensions.io.*
import org.xml.sax.SAXException
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO
import javax.xml.parsers.ParserConfigurationException


/**
 * A class representation of a resource in the epub.
 *
 * @property book An internal reference to the main book container, only used accessing other parts of the book is
 * required.
 * @property name The name of this resource, this should be based on the `file` name.
 * @property type The [Type] of this resource, this is used for categorizing the resource into different folders in the
 * epub.
 *
 * @param book An internal reference to the main book container, only used accessing other parts of the book is
 * required.
 * @param name The name of this resource, this should be based on the `file` name.
 * @param file The [file][Path] that this resource is based on.
 * @param type The [Type] of this resource, this is used for categorizing the resource into different folders in the
 * epub.
 */
sealed class Resource(internal val book: Book, val name: String, file: Path, val type: Type) {
    
    /**
     * The actual [file][Path] instance linked to this resource.
     */
    var origin: Path = file
        private set
    
    /**
     * Returns a basic representation of the location of this resource in relation to it's [type].
     */
    val href: String get() = "${type.location}${origin.name}"
    
    /**
     * The manifest id of this resource.
     *
     * This might be migrated to it's own class or something.
     */
    lateinit var manifestId: String
    
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
     * A map containing all the occurrences of this `resource` inside the pages of the epub.
     */
    val pageOccurences: MutableMap<Path, Int> = LinkedHashMap()
    
    /**
     * A function that is ran whenever this `resource` is first created.
     *
     * This function generally handles the delegation of this `resource` to all of the different sub-systems that are
     * designed specifically for dealing with it.
     */
    @Throws(CreateResourceException::class)
    abstract fun onInitialization()
    
    /**
     * A function that is ran whenever this `resource` has been marked for deletion.
     *
     * Most resources don't have any "special" behaviour for deletion, so this isn't marked as `abstract`.
     */
    @Throws(DeleteResourceException::class)
    open fun onRemoval() {
    }
    
    /**
     * Renames this resource to [name].
     *
     * This function also takes care of updating any and all references to this resource.
     */
    fun renameTo(name: String) {
        TODO("Implement resource renaming.")
    }
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Resource -> false
        book != other.book -> false
        name != other.name -> false
        type != other.type -> false
        origin != other.origin -> false
        manifestId != other.manifestId -> false
        pageOccurences != other.pageOccurences -> false
        else -> true
    }
    
    override fun hashCode(): Int = Objects.hash(book, name, type, origin, manifestId, pageOccurences)
    
    override fun toString(): String =
        "Resource(book=$book, name='$name', type=$type, origin=$origin, manifestId='$manifestId')"
    
    /**
     * This enum is used for categorizing all the different kind of resources used in an EPUB.
     *
     * @property location The directory where any resources that are of this [Type] should be stored.
     * @property extensions An array of all the file extensions *(file types)* this `type` supports.
     */
    enum class Type(val location: String, vararg val extensions: String) {
        
        PAGE("Text/", "XHTML", "HTML"),
        STYLE_SHEET("Styles/", "CSS"),
        IMAGE("Images/", "JPG", "JPEG", "PNG", "GIF", "SVG"),
        FONT("Fonts/", "TTF", "OTF"),
        AUDIO("Audio/", "MP3", "MPEG", "WAV"),
        VIDEO("Video/", "WEBM", "MP4", "MKV"),
        MISC("Misc/"),
        OPF("", "OPF"),
        NCX("", "NCX");
        
        companion object {
            
            /**
             * Returns the [Type] that has an [extension][Type.extensions] that matches with the specified [extension],
             * if none is found then [MISC] will be returned.
             */
            fun from(extension: String): Type = values().find { extension.toUpperCase() in it.extensions } ?: MISC
            
            /**
             * Returns the [Type] that has an [extension][Type.extensions] that matches that of the given [file], if
             * none is found then [MISC] will be returned.
             */
            fun from(file: Path): Type = values().find { file.extension.toUpperCase() in it.extensions } ?: MISC
            
        }
        
    }
}

/**
 * The `class` representation for `PNG`, `JPG`, `JPEG`, `GIF`, & `SVG` files.
 */
// I have a burning hatred for the use of JPEG over JPG.
class ImageResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.IMAGE) {
    
    /**
     * The [BufferedImage] instance of this image resource.
     *
     * **Note:** This is generated during the creation process of this resource, trying to access it before that will
     * result in an `exception` being thrown.
     */
    lateinit var image: BufferedImage
        private set
    
    /**
     * What `type` of image this resource contains.
     *
     * **Note:** This is generated during the creation process of this resource, trying to access it before that will
     * result in an `exception` being thrown.
     */
    lateinit var imageType: ImageType
        private set
    
    @Throws(CreateResourceException::class)
    override fun onInitialization() {
        try {
            image = ImageIO.read(!origin)
        } catch (e: IOException) {
            throw CreateResourceException("Failed to read the image: (\"$origin\")", e)
        }
        
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
    
    /**
     * Contains all the supported image types.
     */
    enum class ImageType {
        PNG, JPG, GIF, SVG, UNKNOWN;
        
        companion object {
            
            /**
             * Returns the [ImageType] that has an [extension][Path.extension] that matches with the specified
             * [extension], if none is found then [UNKNOWN] will be returned.
             */
            @JvmStatic
            fun from(extension: String): ImageType =
                values().find { it.name.equals(extension, true) } ?: UNKNOWN
            
            /**
             * Returns the [ImageType] that matches the [extension][Path.extension] of the given [file], if none is found
             * then [MISC] will be returned.
             */
            @JvmStatic
            fun from(file: Path): ImageType =
                values().find { it.name.equals(file.extension, true) } ?: UNKNOWN
        }
    }
    
    
}

/**
 * The `class` representation for the `NCX` file.
 *
 * The `NCX` file represents the table of contents in epub files of format versions under v3.
 *
 * The v3 format introduced the `nav` page which replaced the `NCX` file, epubby will still generate an accurate ncx
 * file regardless of version.
 */
class NcxResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.NCX) {
    
    override fun onInitialization() {
        try {
            //TableOfContentsReader.readFile(getBook(), file)
        } catch (e: Exception) {
            e.multiCatch(ParserConfigurationException::class, IOException::class, SAXException::class) {
                throw CreateResourceException("Failed to read the NCX file.", e)
            }
        }
        
    }
}

/**
 * The `class` representation for the `OPF` file.
 */
class OpfResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.OPF) {
    
    override fun onInitialization() { // This loads before pages and maybe something more, might cause issues.
        try {
            //ContentReader.readFile(getBook(), file)
        } catch (e: IOException) {
            throw CreateResourceException("Failed to read the OPF file.", e)
        }
        
    }
}

/**
 * The `class` representation for `XHTML` & `HTML` files.
 */
class PageResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.PAGE) {
    
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
    
    override fun onInitialization() {
        try {
            //this.page = getBook().getPages().addPage(file)
        } catch (e: IOException) {
            throw CreateResourceException("Failed to read the (X)HTML file.", e)
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
}

/**
 * The `class` representation for `CSS` files.
 */
class StyleSheetResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.STYLE_SHEET) {
    
    /**
     * **Note:** This is generated during the creation process of this resource, trying to access it before that will
     * result in an `exception` being thrown.
     */
    //lateinit var reader: StyleSheetReader
    //    private set
    
    override fun onInitialization() {
        //styleSheetReader = getBook().getStyleSheets().registerReader(this)
    }
    
    override fun onRemoval() {
        //getBook().getPages().removeStyleSheet(getStyleSheetReader())
        //getBook().getStyleSheets().unregisterReader(this)
    }
}

/**
 * The `class` representation for any files that don't have explicit representations made.
 */
class MiscResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.MISC) {
    
    override fun onInitialization() {
        book.logger.warn { "\"${origin.name}\" has been marked as a MiscResource." }
    }
    
}

/**
 * A repository used for working with all the different kinds of `resources` in the epub.
 */
@Suppress("UNCHECKED_CAST")
class ResourceRepository(
    @PublishedApi internal val book: Book
) : Iterable<Resource>, BookLoad {
    
    /**
     * This is **only** for internal use by this class.
     */
    // This needs to be a LinkedHashMap to actually retain the original order of the resources.
    private val resources = LinkedHashMap<String, Resource>()
    
    override fun onBookInitialization() {
        val opfResource: OpfResource =
            find { it.origin.extension.equals("opf", true) }
                ?: throw MalformedBookException("This book contains no OPF file.")
        
        // Why is this done again?
        resources -= opfResource.href
        resources[opfResource.href] = opfResource
    
        for ((_, resource) in resources) resource.onInitialization()
    }
    
    /**
     * Returns a list containing only `resources` that match the given [type].
     */
    fun filterByType(type: Resource.Type): List<Resource> = filter { it.type == type }
    
    /**
     * Returns a list containing only `resources` that have the specified [extension].
     */
    fun filterByExtension(extension: String): List<Resource> = filter { it.origin.extension.equals(extension, true) }
    
    /**
     * Returns a list containing only `resources` that match the given [predicate].
     */
    fun filter(predicate: (Resource) -> Boolean): List<Resource> = iterator().asSequence().filter(predicate).toList()
    
    /**
     * Returns `true` if at least one element matches the given [predicate].
     */
    inline fun any(predicate: (Resource) -> Boolean): Boolean = iterator().asSequence().any(predicate)
    
    /**
     * Returns the first [Resource] matching the given [predicate].
     * @throws [NoSuchElementException] If no such `Resource` is found.
     *
     * The operation is _terminal_.
     *
     * @see find
     */
    inline fun <reified R : Resource> first(predicate: (Resource) -> Boolean): R =
        iterator().asSequence().first(predicate) as R
    
    /**
     * Returns the first [Resource] matching the given [predicate], or `null` if no such `Resource` was found.
     *
     * The operation is _terminal_.
     *
     * @see first
     */
    inline fun <reified R : Resource> find(predicate: (Resource) -> Boolean): R? =
        iterator().asSequence().find(predicate) as R
    
    override fun iterator(): Iterator<Resource> = resources.values.toList().iterator()
    
    // Not sure if everything will work like it should with these casts after removing the reified types.
    // But having to use reified for these things kind of doesn't make much sense.
    // And using reified for something like the "add" function would result in *very* large chunks of generated code.
    
    /**
     * Returns a [Resource] that matches with the specified [href], otherwise it will throw a
     * [ResourceNotFoundException].
     *
     * TODO: Implement so this searches for both the normal href and the ../href. (location href)
     */
    @Throws(ResourceNotFoundException::class)
    operator fun <R : Resource> get(href: String): R =
        resources[href] as R? ?: throw ResourceNotFoundException("with the href of \"$href\".")
    
    /**
     * Returns a [Resource] that matches with the specified [href], or `null` if none is found.
     */
    fun <R : Resource> getOrNull(href: String): R? = resources[href] as R?
    
    /**
     * Returns a [Resource] that has a origin [file][Resource.origin] that matches with the given [file], otherwise it
     * will throw a [ResourceNotFoundException].
     */
    // This intentionally does a very weak check for equality by only checking that the file name is the same.
    // If you want to do a more accurate equality check, you can use the "any" function.
    @Throws(ResourceNotFoundException::class)
    operator fun <R : Resource> get(file: Path): R =
        resources.values.firstOrNull { it.origin.name == file.name } as R?
            ?: throw ResourceNotFoundException("with a origin file that matches \"$file\".")
    
    /**
     * Returns a [Resource] that has a origin [file][Resource.origin] that matches with the given [file], or `null` if
     * none is found.
     */
    // This intentionally does a very weak check for equality by only checking that the file name is the same.
    // If you want to do a more accurate equality check, you can use the "any" function.
    fun <R : Resource> getOrNull(file: Path): R? =
        resources.values.firstOrNull { it.origin.name == file.name } as R?
    
    /**
     * Adds and serializes the given [resource] file to this repository.
     */
    @Throws(ResourceException::class)
    fun <R : Resource> add(resource: Path): R {
        val type = Resource.Type.from(resource.extension)
        val href = "${type.location}${resource.name}"
        lateinit var tempResource: R
        
        when (type) {
            Resource.Type.PAGE -> resources[href] = PageResource(book, resource.name, resource)
            Resource.Type.STYLE_SHEET -> resources[href] = StyleSheetResource(book, resource.name, resource)
            Resource.Type.IMAGE -> resources[href] = ImageResource(book, resource.name, resource)
            Resource.Type.OPF -> resources[href] = OpfResource(book, resource.name, resource)
            Resource.Type.NCX -> resources[href] = NcxResource(book, resource.name, resource)
            // If the file type is not *explicitly* supported, mark it as a MiscResource, this is so that we can still
            // have access to it and work on it, albeit in a very limited way.
            else -> resources[href] = MiscResource(book, resource.name, resource)
        }
        
        tempResource = this[href]
        
        if (tempResource !is OpfResource) {
            // TODO: Add to manifest.
        }
        
        book.logger.debug { "The \"${tempResource.href}\" resource has been added." }
        
        return tempResource
    }
    
    /**
     * Adds and serializes the given [resource] file to this repository.
     */
    @JvmSynthetic
    @Throws(ResourceException::class)
    operator fun plusAssign(resource: Path) {
        add<Resource>(resource)
    }
    
    /**
     * Removes the [Resource] located under the given [href] and calls the [onRemoval][Resource.onRemoval] function, it
     * then tries to delete the [origin][Resource.origin] file.
     */
    @Throws(DeleteResourceException::class)
    fun remove(href: String) {
        val resource: Resource = this[href]
        
        if (href !in this) {
            book.logger.error { "An attempt to remove a non-existent resource was made. \"$resource\"." }
            return
        }
        
        resource.onRemoval()
        resources.remove(resource.href)
        // TODO: Remove from manifest
        
        try {
            resource.origin.deleteIfExists()
        } catch (e: Exception) {
            e.multiCatch(IOException::class, DirectoryNotEmptyException::class, SecurityException::class) {
                throw DeleteResourceException("$resource could not be deleted.", e)
            }
        }
        
        book.logger.debug { "The \"${resource.href}\" resource has been removed." }
    }
    
    /**
     * Removes the [Resource] located under the given [href] and calls the [onRemoval][Resource.onRemoval] function, it
     * then tries to delete the [origin][Resource.origin] file.
     */
    @JvmSynthetic
    @Throws(DeleteResourceException::class)
    operator fun minusAssign(href: String) {
        remove(href)
    }
    
    /**
     * Removes the specified [resource] and calls the [onRemoval][Resource.onRemoval] function, it then tries to delete
     * the [origin][Resource.origin] file.
     */
    @Throws(DeleteResourceException::class)
    fun remove(resource: Resource) = remove(resource.href)
    
    /**
     * Removes the specified [resource] and calls the [onRemoval][Resource.onRemoval] function, it then tries to delete
     * the [origin][Resource.origin] file.
     */
    @JvmSynthetic
    @Throws(DeleteResourceException::class)
    operator fun minusAssign(resource: Resource) {
        remove(resource)
    }
    
    /**
     * Returns whether or not this epub has a `resource` at the specified [href] location.
     */
    operator fun contains(href: String): Boolean = resources.containsKey(href)
    
    /**
     * Returns whether or not the specified [resource] is from this epub.
     */
    operator fun contains(resource: Resource): Boolean = resources.containsValue(resource)
    
    /**
     * Returns whether or not the given [file] is the origin file for any `resources` in this epub.
     */
    operator fun contains(file: Path): Boolean = any { it.origin sameFile file }
}