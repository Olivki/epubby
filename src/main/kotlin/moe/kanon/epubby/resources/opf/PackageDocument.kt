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

@file:Suppress("DataClassPrivateConstructor", "MemberVisibilityCanBePrivate")

package moe.kanon.epubby.resources.opf

import arrow.core.Option
import moe.kanon.epubby.Book
import moe.kanon.xml.parseAsDocument
import java.nio.file.Path

/**
 * Implementation of the [OPF](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.0) format.
 */
class PackageDocument private constructor(val book: Book) {
    // TODO: unique-identifier
    // TODO: version property
    
    /**
     * The meta-data of the underlying [book].
     */
    val metadata: PackageMetadata = PackageMetadata(this)
    
    /**
     * The [manifest](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-manifest-elem) of the underlying
     * [book].
     */
    val manifest: PackageManifest = PackageManifest(this)
    
    /**
     * The spine of the underlying [book].
     */
    val spine: PackageSpine = PackageSpine(this)
    
    /**
     * The guide of the underlying [book], if it has one.
     *
     * A `book` is *not* guaranteed to have a guide, as per the OPF format specification; "Within the package there
     * **may** be one `guide` element"[¹](http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.6)
     *
     * The specification also states that if a guide exists, it needs to contain *at least* one `reference` element,
     * due to this epubby will remove and *not* serialize any existing `guide`s that are empty, even if they were
     * explicitly created by the user.
     *
     * Due to the above reasons, this `guide` property will start out as an `empty` [Option], to create a `guide` for
     * a book, use the [createGuide] function. Note that the `createGuide` function will throw an exception if a
     * `guide` already exists.
     */
    var guide: Option<PackageGuide> = Option.empty()
        @JvmSynthetic internal set
    
    /**
     * Returns whether or not `this` OPF document has a [guide] defined.
     */
    fun hasGuide(): Boolean = guide.nonEmpty()
    
    /**
     * Attempts to create a new [guide] for `this` OPF document, throwing a [UnsupportedOperationException] if a
     * `guide` already exists.
     */
    fun createGuide() {
        if (hasGuide()) throw UnsupportedOperationException("OPF document already has a 'guide' defined")
        guide = Option.just(PackageGuide(this))
    }
    
    /**
     * Serializes all the contents of `this` OPF document into the [epub file][Book.file] of the underlying [book].
     *
     * Note that this function will replace any OPF files that already exist.
     */
    fun createFile() {
        TODO()
    }
    
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is PackageDocument -> false
        manifest != other.manifest -> false
        guide != other.guide -> false
        metadata != other.metadata -> false
        spine != other.spine -> false
        else -> true
    }
    
    override fun hashCode(): Int {
        var result = book.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + manifest.hashCode()
        result = 31 * result + spine.hashCode()
        result = 31 * result + guide.hashCode()
        return result
    }
    
    override fun toString(): String =
        "PackageDocument(metadata=$metadata, manifest=$manifest, spine=$spine${if (hasGuide()) ", guide=$guide)" else ")"}"
    
    companion object {
        /**
         * Creates and populates a [PackageDocument] instance from the specified [opfFile].
         */
        @JvmStatic
        fun from(book: Book, opfFile: Path): PackageDocument {
            val content = PackageDocument(book)
            
            opfFile.parseAsDocument {
                element("thing") {
                
                }
            }
            
            return content
        }
    }
}

typealias MetadataObject = PackageMetadata.Element
typealias ManifestItem = PackageManifest.Item
typealias GuideReference = PackageGuide.Reference