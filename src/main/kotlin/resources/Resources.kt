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

package moe.kanon.epubby.resources

import moe.kanon.kextensions.io.not
import java.awt.image.BufferedImage
import java.awt.print.Book
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO


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
public abstract class Resource(internal val book: Book, public val name: String, file: Path, public val type: Type) {
    
    /**
     * The actual [file][Path] instance linked to this resource.
     */
    public var file: Path = file
        internal set(value) {
            field = value
        }
    
    /**
     * This function is ran whenever this resource is actually created.
     */
    public abstract fun onCreation()
    
    /**
     * The function is ran whenever this resource has been marked for deletion.
     */
    public fun onDeletion() {}
    
    /**
     * Renames this resource to [name].
     *
     * This function also takes care of updating any and all references to this resource.
     */
    public fun renameTo(name: String) {
        TODO("Implement resource renaming.")
    }
    
    /**
     * This enum is used for categorizing all the different kind of resources used in an EPUB.
     *
     * @property location The directory where any resources that are of this [Type] should be stored.
     * @property extensions What kind of extensions (file types) this type supports.
     */
    public enum class Type(public val location: String = "", public vararg val extensions: String) {
        
        PAGE("Text/", "XHTML", "HTML"),
        STYLE("Styles/", "CSS"),
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

public class ImageResource(book: Book, name: String, file: Path) : Resource(book, name, file, Type.IMAGE) {
    
    public lateinit var image: BufferedImage
        private set
    
    public lateinit var extension: Extension
        private set
    
    override fun onCreation() {
        try {
            image = ImageIO.read(!file)
        } catch (e: IOException) {
            throw ResourceCreateException("Failed to read the image: (\"$file\")", e)
        }
    }
    
    public enum class Extension {
        PNG, JPG, GIF, SVG, UNKNOWN;
        
        companion object {
            public fun from(extension: String): Extension =
                values().asSequence().find { it.name == extension.toUpperCase() } ?: UNKNOWN
        }
    }
}

public class ResourceRepository(internal val book: Book) : Iterable<Resource> {
    
    private val resources: LinkedHashMap<String, Resource> = LinkedHashMap()
    
    public fun filter(type: Resource.Type): List<Resource> = resources.values.filter { it.type == type }
    
    public override fun iterator(): Iterator<Resource> = resources.values.toList().iterator()
    
    // Operators
    public operator fun get(key: String): Resource? = resources[key]
    
    public operator fun set(key: String, resource: Resource) {
        resources[key] = resource
    }
    
    public operator fun contains(href: String): Boolean = resources.containsKey(href)
    
    public operator fun contains(resource: Resource): Boolean = resources.containsValue(resource)
}

