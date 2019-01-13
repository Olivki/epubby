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

import java.awt.print.Book
import java.nio.file.Path

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
public abstract class Resource(internal val book: Book, public val name: String, public val type: Type, file: Path) {
    
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
        
        PAGES("Text/", "XHTML", "HTML"),
        STYLES("Styles/", "CSS"),
        IMAGES("Images/", "JPG", "JPEG", "PNG", "GIF", "SVG"),
        FONTS("Fonts/", "TTF", "OTF"),
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

public class ResourceRepository(internal val book: Book) {

}

