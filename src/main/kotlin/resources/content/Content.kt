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

package moe.kanon.epubby.resources.content

import moe.kanon.epubby.Book
import moe.kanon.xml.parseAsDocument
import java.nio.file.Path
import java.util.*

/**
 * A container class for all of the "content" in the epub.
 *
 * @param book Internal use only.
 */
class Content(book: Book) {
    
    /**
     * The manifest for this epub.
     */
    val manifest: Manifest = Manifest(book)
    
    /**
     * The guide for this epub, if it has one.
     */
    val guide: Guide = Guide(book)
    
    /**
     * The meta-data for this epub.
     */
    val metaData: MetaData = MetaData(book)
    
    /**
     * The spine for this epub.
     */
    val spine: Spine = Spine(book)
    
    /**
     * Serializes all of the sub-classes of this content container into XML and then saves it as the `OPF` file of this
     * epub.
     */
    fun save() {
        TODO()
    }
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Content -> false
        manifest != other.manifest -> false
        guide != other.guide -> false
        metaData != other.metaData -> false
        spine != other.spine -> false
        else -> true
    }
    
    override fun hashCode(): Int = Objects.hash(manifest, guide, metaData, spine)
    
    override fun toString(): String = "Content(manifest=$manifest, guide=$guide, metaData=$metaData, spine=$spine)"
    
    companion object {
    
        /**
         * Creates and populates a [Content] instance from the specified [opfFile].
         */
        @JvmStatic
        fun from(book: Book, opfFile: Path): Content {
            opfFile.parseAsDocument {
            
            }
            
            TODO()
        }
    }
}

class Manifest(internal val book: Book)

class Guide(internal val book: Book)

class MetaData(internal val book: Book)

class Spine(internal val book: Book)