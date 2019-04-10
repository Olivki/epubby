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

import moe.kanon.epubby.Book
import moe.kanon.epubby.BookDeserializationException
import moe.kanon.epubby.BookModificationException
import java.nio.file.Path

/**
 * Thrown to indicate that something went wrong when attempting to deserialize a file into a [Resource].
 */
open class ResourceDeserializationException : BookDeserializationException {
    
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    constructor(message: String) : super(message)
    
    companion object {
        
        /**
         * Creates a [ResourceDeserializationException] from the specified [book], [file] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [file] The file that the resource is being created from.
         * @param [info] Information regarding *why* the operation failed.
         */
        @JvmStatic
        fun create(book: Book, file: Path, info: String): ResourceDeserializationException =
            ResourceDeserializationException(
                "An error occurred in book <$book> when attempting to create a resource from file <$file>. ($info)"
            )
        
        /**
         * Creates a [ResourceDeserializationException] from the specified [book], [file] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [file] The file that the resource is being created from.
         * @param [info] Information regarding *why* the operation failed.
         * @param [cause] The cause of the exception.
         */
        @JvmStatic
        fun create(book: Book, file: Path, info: String, cause: Throwable): ResourceDeserializationException =
            ResourceDeserializationException(
                "An error occurred in book <$book> when attempting to create a resource from file <$file>. ($info)",
                cause
            )
        
    }
}

/**
 * Thrown to indicate that something went wrong when attempting to delete a [Resource].
 */
open class ResourceDeletionException : BookDeserializationException {
    
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    constructor(message: String) : super(message)
    
    companion object {
        
        /**
         * Creates a [ResourceDeserializationException] from the specified [book], [resource] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [resource] The resource that's being deleted.
         * @param [info] Information regarding *why* the operation failed.
         */
        @JvmStatic
        fun create(book: Book, resource: Resource, info: String): ResourceDeletionException =
            ResourceDeletionException(
                "An error occurred in book <$book> when attempting to delete the resource <$resource>. ($info)"
            )
        
        /**
         * Creates a [ResourceDeserializationException] from the specified [book], [resource] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [resource] The resource that's being deleted.
         * @param [info] Information regarding *why* the operation failed.
         * @param [cause] The cause of the exception.
         */
        @JvmStatic
        fun create(book: Book, resource: Resource, info: String, cause: Throwable): ResourceDeletionException =
            ResourceDeletionException(
                "An error occurred in book <$book> when attempting to delete the resource <$resource>. ($info)",
                cause
            )
        
    }
    
}

/**
 * Thrown to indicate that something went wrong when attempting to modify a [Resource].
 */
open class ResourceModificationException : BookModificationException {
    
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    constructor(message: String) : super(message)
    
    companion object {
        
        /**
         * Creates a [ResourceModificationException] from the specified [book], [resource] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [resource] The resource that's being deleted.
         * @param [info] Information regarding *why* the operation failed.
         */
        @JvmStatic
        fun create(book: Book, resource: Resource, info: String): ResourceModificationException =
            ResourceModificationException(
                "An error occurred in book <$book> when attempting to modify the resource <$resource>. ($info)"
            )
        
        /**
         * Creates a [ResourceModificationException] from the specified [book], [resource] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [resource] The resource that's being deleted.
         * @param [info] Information regarding *why* the operation failed.
         * @param [cause] The cause of the exception.
         */
        @JvmStatic
        fun create(book: Book, resource: Resource, info: String, cause: Throwable): ResourceModificationException =
            ResourceModificationException(
                "An error occurred in book <$book> when attempting to modify the resource <$resource>. ($info)",
                cause
            )
        
    }
    
}