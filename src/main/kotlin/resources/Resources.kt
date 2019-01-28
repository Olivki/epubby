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
import moe.kanon.epubby.multiCatch
import moe.kanon.epubby.resources.pages.Page
import moe.kanon.kextensions.io.extension
import moe.kanon.kextensions.io.name
import moe.kanon.kextensions.io.not
import moe.kanon.kextensions.io.sameFile
import org.xml.sax.SAXException
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.xml.parsers.ParserConfigurationException


/**
 * A class representation of a resource in the epub.
 *
 * @property book An internal reference to be used when needed to access the parent book.
 * @property name The name of the resource, this is based on the file name.
 * @property type The type of the resource.
 */
sealed class Resource(internal val book: Book, val name: String, file: Path, val type: Type) {
    
    /**
     * The actual [file][Path] instance linked to this resource.
     */
    var file: Path = file
        internal set(value) {
            field = value
        }
    
    /**
     * Returns a basic representation of the location of this resource in relation to it's [type].
     */
    val href: String get() = "${type.location}${file.name}"
    
    /**
     * Returns a basic representation of the location of this resource in relation to it's [type].
     *
     * What makes this different from [href] is that this has "`../`" appended at the start, this is generally used
     * when working with files inside of the epub to mark relative paths.
     *
     * TODO: This might not even be needed what with how
     */
    val relativeHref: String get() = "../$href"
    
    /**
     * A map containing all the occurrences of this `resource` inside the pages of the epub.
     */
    val pageOccurences: MutableMap<Path, Int> = LinkedHashMap()
    
    /**
     * This function is ran whenever this resource is actually created.
     */
    @Throws(CreateResourceException::class)
    abstract fun onCreation()
    
    /**
     * The function is ran whenever this resource has been marked for deletion.
     */
    @Throws(DeleteResourceException::class)
    open fun onDeletion() {
    }
    
    /**
     * Renames this resource to [name].
     *
     * This function also takes care of updating any and all references to this resource.
     */
    fun renameTo(name: String) {
        TODO("Implement resource renaming.")
    }
    
    /**
     * This enum is used for categorizing all the different kind of resources used in an EPUB.
     *
     * @property location The directory where any resources that are of this [Type] should be stored.
     * @property extensions What kind of extensions (file types) this type supports.
     */
    enum class Type(public val location: String = "", public vararg val extensions: String) {
        
        PAGE("Text/", "XHTML", "HTML"),
        STYLE_SHEET("Styles/", "CSS"),
        IMAGE("Images/", "JPG", "JPEG", "PNG", "GIF", "SVG"),
        FONT("Fonts/", "TTF", "OTF"),
        AUDIO("Audio/", "MP3", "MPEG", "WAV"),
        VIDEO("Video/", "WEBM", "MP4", "MKV"),
        MISC("Misc/"),
        OPF(extensions = *arrayOf("OPF")),
        NCX(extensions = *arrayOf("NCX"));
        
        companion object {
            
            /**
             * Searches for a [Type] that contains [extension], if none is found then [MISC] is returned.
             */
            public fun from(extension: String): Type =
                values().asSequence().find { extension.toUpperCase() in it.extensions } ?: MISC
        }
        
    }
}

/**
 * The `class` representation for `PNG`, `JPG`, `GIF`, & `SVG` files.
 *
 * **Note:** This class also supports `JPEG` extensions, *however*, it does so by forcefully changing any instances of
 * a `JPEG` extension to a `JPG` one.
 */
class ImageResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.IMAGE) {
    
    lateinit var image: BufferedImage
        private set
    
    lateinit var imageType: ImageType
        private set
    
    @Throws(CreateResourceException::class)
    override fun onCreation() {
        try {
            image = ImageIO.read(!file)
        } catch (e: IOException) {
            throw CreateResourceException("Failed to read the image: (\"$file\")", e)
        }
    }
    
    enum class ImageType {
        PNG, JPG, GIF, SVG, UNKNOWN;
        
        companion object {
            
            @JvmStatic
            public fun from(extension: String): ImageType =
                values().find { it.name == extension.toUpperCase() } ?: UNKNOWN
        }
    }
}

/**
 * The `class` representation for the `NCX` file.
 *
 * The `NCX` file represents the table of contents in epub files of format versions under v3.
 *
 * The v3 format introduced the `nav` page which replaced the `NCX` file, it's still required to have one in the
 * archive for it to be a valid epub however.
 */
class NcxResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.NCX) {
    
    override fun onCreation() {
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
    
    override fun onCreation() { // This loads before pages and maybe something more, might cause issues.
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
    
    lateinit var page: Page
        private set
    
    override fun onCreation() {
        try {
            //this.page = getBook().getPages().addPage(file)
        } catch (e: IOException) {
            throw CreateResourceException("Failed to read the (X)HTML file.", e)
        }
        
    }
    
    override fun onDeletion() {
        //getBook().getPages().removePage(getPage())
    }
    
}

/**
 * The `class` representation for `CSS` files.
 */
class StyleSheetResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.STYLE_SHEET) {
    
    //lateinit var reader: StyleSheetReader
    //    private set
    
    override fun onCreation() {
        //styleSheetReader = getBook().getStyleSheets().registerReader(this)
    }
    
    override fun onDeletion() {
        //getBook().getPages().removeStyleSheet(getStyleSheetReader())
        //getBook().getStyleSheets().unregisterReader(this)
    }
}

/**
 * A repository used for working with all the different kinds of `resources` in the epub.
 */
class ResourceRepository(
    @PublishedApi internal val book: Book
) : Iterable<Resource> {
    
    // IntelliJ *really* does not like the @JvmSynthetic annotation.
    @PublishedApi @JvmSynthetic internal val resources = LinkedHashMap<String, Resource>()
    
    /**
     * Returns a list containing only `resources` that match the given [type].
     */
    fun filter(type: Resource.Type): List<Resource> = filter { it.type == type }
    
    /**
     * Returns a list containing only `resources` that have the specified [extension].
     */
    fun filter(href: String): List<Resource> = filter { it.file.extension.equals(href, true) }
    
    /**
     * Returns a list containing only `resources` that match the given [predicate].
     */
    inline fun filter(predicate: (Resource) -> Boolean): List<Resource> = resources.values.filter(predicate)
    
    /**
     * Returns `true` if at least one element matches the given [predicate].
     */
    inline fun any(predicate: (Resource) -> Boolean): Boolean = resources.values.any(predicate)
    
    override fun iterator(): Iterator<Resource> = resources.values.toList().iterator()
    
    /**
     * Returns a [Resource] that matches with the specified [href], otherwise it will throw a
     * [ResourceNotFoundException].
     *
     * TODO: Implement so this searches for both the normal href and the ../href. (location href)
     */
    @Throws(ResourceNotFoundException::class)
    inline operator fun <reified R : Resource> get(href: String): R =
        resources[href] as R? ?: throw ResourceNotFoundException("with the href of \"$href\".")
    
    /**
     * Returns a [Resource] that matches with the specified [href], or `null` if none is found.
     */
    inline fun <reified R : Resource> getOrNull(href: String): R? = resources[href] as R?
    
    /**
     * Returns a [Resource] that has a origin [file][Resource.file] that matches with the given [file], otherwise it
     * will throw a [ResourceNotFoundException].
     */
    @Throws(ResourceNotFoundException::class)
    inline operator fun <reified R : Resource> get(file: Path): R =
        resources.values.firstOrNull { it.file sameFile file } as R?
            ?: throw ResourceNotFoundException("with a origin file that matches \"$file\".")
    
    /**
     * Returns a [Resource] that has a origin [file][Resource.file] that matches with the given [file], or `null` if
     * none is found.
     */
    inline fun <reified R : Resource> getOrNull(file: Path): R? =
        resources.values.firstOrNull { it.file sameFile file } as R?
    
    /**
     * Attempts to add and serialize the specified [resource] to the repository.
     */
    @Throws(ResourceException::class)
    inline fun <reified R : Resource> add(resource: Path): R {
        val type = Resource.Type.from(resource.extension)
        val href = "${type.location}${resource.name}"
        lateinit var tempResource: Resource
        
        when (type) {
            Resource.Type.PAGE -> resources[href] = PageResource(book, resource.name, resource)
            Resource.Type.STYLE_SHEET -> resources[href] = StyleSheetResource(book, resource.name, resource)
            Resource.Type.IMAGE -> resources[href] = ImageResource(book, resource.name, resource)
            Resource.Type.OPF -> resources[href] = OpfResource(book, resource.name, resource)
            Resource.Type.NCX -> resources[href] = NcxResource(book, resource.name, resource)
            else -> book.logger.warn("No supported resource class found for \"$resource\".")
        }
        
        tempResource = this[href] // Does this work with the reified stuff and what have you?
        
        if (tempResource !is OpfResource) {
            // TODO: Add to manifest.
        }
        
        book.logger.debug { "Resource has been added to the repository: $tempResource." }
        
        return tempResource as R
    }
    
    fun remove(href: String): Resource? {
        TODO("not implemented")
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
    operator fun contains(file: Path): Boolean = any { it.file sameFile file }
}