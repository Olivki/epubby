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

import moe.kanon.epubby.multiCatch
import moe.kanon.kextensions.io.extension
import moe.kanon.kextensions.io.not
import org.xml.sax.SAXException
import java.awt.image.BufferedImage
import java.awt.print.Book
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.xml.parsers.ParserConfigurationException


/**
 * A class representation of a resource in the EPUB.
 *
 * Currently implemented resources are;
 * - Images (
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

class OpfResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.OPF) {
    
    override fun onCreation() { // This loads before pages and maybe something more, might cause issues.
        try {
            //ContentReader.readFile(getBook(), file)
        } catch (e: IOException) {
            throw CreateResourceException("Failed to read the OPF file.", e)
        }
        
    }
}

class PageResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.PAGE) {
    
    //lateinit var page: Page
    //    private set
    
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
    internal val book: Book
) : Iterable<Resource> {
    
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
    
    override fun iterator(): Iterator<Resource> = resources.values.toList().iterator()
    
    fun remove(href: String): Resource? {
        TODO("not implemented")
    }
    
    // Operators
    operator fun contains(href: String): Boolean = resources.containsKey(href)
    
    operator fun contains(resource: Resource): Boolean = resources.containsValue(resource)
}