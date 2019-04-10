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

@file:Suppress("NOTHING_TO_INLINE")

package moe.kanon.epubby.resources

import arrow.core.Option
import arrow.core.Try
import moe.kanon.epubby.Book
import moe.kanon.epubby.BookListener
import moe.kanon.epubby.MalformedBookException
import moe.kanon.kommons.io.copyTo
import moe.kanon.kommons.io.deleteIfExists
import moe.kanon.kommons.io.extension
import moe.kanon.kommons.io.isSameFile
import moe.kanon.kommons.io.sameFile
import moe.kanon.kommons.misc.multiCatch
import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.LinkedHashMap

/**
 * A repository containing all the currently loaded resources used by the specified [book].
 */
@Suppress("UNCHECKED_CAST")
class ResourceRepository(val book: Book) : Iterable<Resource>, BookListener {
    
    private val delegate: MutableMap<String, Resource> = LinkedHashMap()
    
    override fun onBookInitialization() {
        val opfResource: OpfResource =
            findAs { it.origin.extension.equals("opf", ignoreCase = true) }
                ?: throw MalformedBookException.create(book, "Missing OPF file.")
        
        // Why is this done again?
        delegate -= opfResource.href
        delegate[opfResource.href] = opfResource
        
        for ((_, resource) in delegate) resource.onBookInitialization()
    }
    
    /**
     * Returns the first [Resource] that has a [href][Resource.href] that matches the specified [href], or it will
     * throw a [NoSuchElementException] if none is found.
     *
     * @throws [NoSuchElementException] if no `resource` could be found under the specified [href]
     */
    operator fun <R : Resource> get(href: String): R = getOr<R>(href).fold(
        { throw NoSuchElementException("No resource found that matches the href <$href>") },
        { it }
    )
    
    /**
     * Returns the first [Resource] that has a [href][Resource.href] that matches the specified [href], or it will
     * throw a [NoSuchElementException] if none is found.
     *
     * ```kotlin
     *  val book: Book = ...
     *  val resources = book.resources
     *  // this function enables the use of a syntax which
     *  // is very close to that of the 'get' operator syntax,
     *  // while still allowing one to pass a generic type
     *  val page = resources<PageResource>("...")
     * ```
     *
     * @throws [NoSuchElementException] if no `resource` could be found under the specified [href]
     */
    @JvmSynthetic
    operator fun <R : Resource> invoke(href: String): R = this[href]
    
    /**
     * Returns the first [Resource] that has a `href` that matches the specified [href] wrapped as a [Option].
     */
    fun <R : Resource> getOr(href: String): Option<R> = Option.fromNullable(delegate[href] as R?)
    
    /**
     * Returns the first [Resource] that has an [origin][Resource.origin] file that matches the specified [file], or
     * throws a [NoSuchElementException] if none is found.
     */
    operator fun <R : Resource> get(file: Path): R = getOr<R>(file).fold(
        { throw NoSuchElementException("No resource found with an origin file that matches the file <$file>") },
        { it }
    )
    
    /**
     * Returns the first [Resource] that has a `origin` that matches the specified [file] wrapped as a [Option].
     */
    fun <R : Resource> getOr(file: Path): Option<R> =
        Option.fromNullable(delegate.values.firstOrNull { it.origin.isSameFile(file) } as R?)
    
    /**
     * Creates a appropriate [Resource] instance for the specified [file], or a [MiscResource] instance of there exists
     * no concrete `Resource` implementation for that specific file type, and then adds the newly created `Resource` to
     * `this` repository.
     *
     * **NOTE:** It is recommended to keep [copyFile] as `true`, as it is very likely that a faulty epub will be
     * created when invoking [Book.saveTo] if a `resource` has been added to `this` repository with it set as `false`.
     *
     * @param [file] the file to create a `Resource` instance for
     * @param [copyFile] whether or not the specified [file] should be copied into the current `.epub` file
     *
     * altering this parameter may lead to faulty epubs
     *
     * (`true` by default)
     *
     * @return the newly created `Resource` instance
     */
    @Throws(IOException::class, ResourceDeserializationException::class)
    fun add(file: Path): Resource {
        // TODO: This
        // determine what type of resource the specified 'file' is from the extension
        val dir = ResourceType.from(file).getDirectory(book)
        // check whether the specified 'file' does not match any of the files inside of the resource 'dir'
        if (dir.none { it.isSameFile(file) }) {
            if (file.fileSystem == book.fileSystem) {
                // if the file system of the 'file' is the same as the books 'fileSystem' then the 'file' is already
                // stored somewhere in the book, but most likely under the wrong directory. and as such we want to move
                // it to the correct dire
                
            } else {
            
            }
        }
        val newFile =
            file.copyTo(book.file, true, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
        
        return add(book.createResource(newFile))
    }
    
    /**
     * Creates a appropriate [Resource] instance for the specified [file], or a [MiscResource] instance of there exists
     * no concrete `Resource` implementation for that specific file type, and then adds the newly created `Resource` to
     * `this` repository.
     *
     * **NOTE:** It is recommended to keep [copyFile] as `true`, as it is very likely that a faulty epub will be
     * created when invoking [Book.saveTo] if a `resource` has been added to `this` repository with it set as `false`.
     *
     * @param [file] the file to create a `Resource` instance for
     * @param [copyFile] whether or not the specified [file] should be copied into the current `.epub` file
     *
     * altering this parameter may lead to faulty epubs
     *
     * (`true` by default)
     *
     * @return the newly created `Resource` instance
     */
    fun tryAdd(file: Path): Try<Resource> = Try { add(file) }
    
    /**
     * Adds the specified [resource] to `this` repository.
     *
     * The specified `resource` is stored under its `href` property.
     *
     * @param [resource] the [Resource] instance to add to `this` repository
     *
     * @return the specified [resource]
     */
    fun <R : Resource> add(resource: R): R {
        delegate[resource.href] = resource
        
        if (resource !is OpfResource) {
            // TODO: Add to manifest.
        }
        
        book.logger.debug { "Added the resource <$resource> to the resource repository." }
        return resource
    }
    
    /**
     * Serializes the specified [file] into a appropriate [Resource] instance and then adds it to `this` repository.
     */
    @JvmSynthetic
    operator fun plusAssign(file: Path) {
        add(file)
    }
    
    /**
     * Adds the specified [resource] to `this` repository.
     */
    @JvmSynthetic
    operator fun plusAssign(resource: Resource) {
        add(resource)
    }
    
    /**
     * Removes the [Resource] located under the given [href] and calls the [onRemoval][Resource.onRemoval] function, it
     * then tries to delete the [origin][Resource.origin] file.
     *
     * @throws [NoSuchElementException] if no `resource` could be found under the specified [href]
     * @throws [ResourceDeletionException] if an error was encountered while attempting to delete the resource
     */
    @Throws(ResourceDeletionException::class)
    fun remove(href: String) {
        // this will throw a 'NoSuchElementException' if no resource is stored under the specified 'href'
        val resource: Resource = this[href]
        
        resource.onRemoval()
        delegate -= resource.href
        // TODO: Remove from manifest
        
        try {
            resource.origin.deleteIfExists()
        } catch (e: Exception) {
            e.multiCatch(IOException::class, DirectoryNotEmptyException::class, SecurityException::class) {
                throw ResourceDeletionException.create(book, resource, "<${e.message}>", e)
            }
        }
        
        book.logger.debug { "Removed and deleted the resource <$resource> from the repository." }
    }
    
    /**
     * Returns a list containing only `resources` that match the given [type].
     */
    fun filterByType(type: ResourceType): List<Resource> = filter { it.type == type }
    
    /**
     * Returns a list containing only `resources` that have the specified [extension].
     */
    fun filterByExtension(extension: String): List<Resource> = filter { it.origin.extension.equals(extension, true) }
    
    /**
     * Returns the first resource that matches the specified [predicate] casted to the specified [R].
     */
    inline fun <R : Resource> findAs(predicate: (R) -> Boolean): R? = find { predicate(it as R) } as R?
    
    override fun iterator(): Iterator<Resource> = delegate.values.toList().iterator()
    
    /**
     * Removes the specified [resource] from `this` repository.
     *
     * **NOTE:** This function will also attempt to delete the underlying [origin][Resource.origin] file of the
     * specified [resource] from the file system.
     *
     * @param [resource] the resource to remove from `this` repository
     */
    @Throws(ResourceDeletionException::class)
    fun remove(resource: Resource) {
    
    }
    
    /**
     * Removes the [Resource] located under the given [href] and calls the [onRemoval][Resource.onRemoval] function, it
     * then tries to delete the [origin][Resource.origin] file.
     */
    @JvmSynthetic
    operator fun minusAssign(href: String) {
        remove(href)
    }
    
    /**
     * Removes the specified [resource] from `this` repository.
     *
     * **NOTE:** This function will also attempt to delete the underlying [origin][Resource.origin] file of the
     * specified [resource] from the file system.
     */
    @JvmSynthetic
    operator fun minusAssign(resource: Resource) {
        remove(resource)
    }
    
    /**
     * Returns whether or not there's a resource stored in `this` repository under the specified [href].
     */
    operator fun contains(href: String): Boolean = delegate.containsKey(href)
    
    /**
     * Returns whether or not the specified [resource] is stored in `this` repository.
     *
     * This function does a [containsValue][Map.containsValue] check, which means that this function may return `false`
     * even if there is a resource stored in this repository under the same `href` as the specified `resource`.
     */
    operator fun contains(resource: Resource): Boolean = delegate.containsValue(resource)
    
    /**
     * Returns whether or not the specified [file] is the [origin][Resource.origin] file for any of the resources
     * stored in `this` repository.
     */
    operator fun contains(file: Path): Boolean = any { it.origin sameFile file }
}