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

@file:JvmName("BookExceptions")

package moe.kanon.epubby

import java.io.IOException

// TODO: Provide properties for storing data in the exceptions

/**
 * Thrown to indicate that something went wrong during the serialization process of the epub.
 */
open class BookSerializationException : IOException {
    
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    constructor(message: String) : super(message)
    
    companion object {
        /**
         * Creates a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [info] Information regarding *why* the operation failed.
         */
        @JvmStatic
        fun create(book: Book, info: String): BookSerializationException =
            BookSerializationException("An error occurred when attempting to deserialize the book <$book>. ($info)")
        
        /**
         * Creates a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [info] Information regarding *why* the operation failed.
         * @param [cause] The cause of the exception.
         */
        @JvmStatic
        fun create(book: Book, info: String, cause: Throwable): BookSerializationException =
            BookSerializationException(
                "An error occurred when attempting to deserialize the book <$book>. ($info)",
                cause
            )
    }
}

/**
 * Thrown to indicate that something went wrong during the deserialization process of the epub.
 */
open class BookDeserializationException : IOException {
    
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    constructor(message: String) : super(message)
    
    companion object {
        /**
         * Creates a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [info] Information regarding *why* the operation failed.
         */
        @JvmStatic
        fun create(book: Book, info: String): BookDeserializationException =
            BookDeserializationException("An error occurred when attempting to deserialize the book <$book>. ($info)")
        
        /**
         * Creates a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [info] Information regarding *why* the operation failed.
         * @param [cause] The cause of the exception.
         */
        @JvmStatic
        fun create(book: Book, info: String, cause: Throwable): BookDeserializationException =
            BookDeserializationException(
                "An error occurred when attempting to deserialize the book <$book>. ($info)",
                cause
            )
    }
}

/**
 * Thrown to indicate that some part of a book is malformed.
 *
 * This exception may be thrown in scenarios where it's uncertain that the file that's being parsed is an actual epub.
 */
open class MalformedBookException : BookDeserializationException {
    
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    constructor(message: String) : super(message)
    
    companion object {
        /**
         * Creates a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [info] Information regarding *why* the operation failed.
         */
        @JvmStatic
        fun create(book: Book, info: String): MalformedBookException =
            MalformedBookException("The file <${book.file}> is either malformed, or not an epub. ($info)")
        
        /**
         * Creates a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [info] Information regarding *why* the operation failed.
         * @param [cause] The cause of the exception.
         */
        @JvmStatic
        fun create(book: Book, info: String, cause: Throwable): MalformedBookException =
            MalformedBookException(
                "The file <${book.file}> is either malformed, or not an epub. ($info)",
                cause
            )
    }
}

/**
 * Thrown to indicate that something went wrong when attempting to modify some part of the book.
 */
open class BookModificationException : IOException {
    
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    constructor(message: String) : super(message)
    
    companion object {
        /**
         * Creates a [BookModificationException] from the specified [book] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [info] Information regarding *why* the operation failed.
         */
        @JvmStatic
        fun create(book: Book, info: String): BookModificationException =
            BookModificationException("An error occurred when attempting to modify the book <$book>. ($info)")
        
        /**
         * Creates a [BookModificationException] from the specified [book] and [info] parameters.
         *
         * @param [book] The book that's being serialized.
         * @param [info] Information regarding *why* the operation failed.
         * @param [cause] The cause of the exception.
         */
        @JvmStatic
        fun create(book: Book, info: String, cause: Throwable): BookModificationException =
            BookModificationException(
                "An error occurred when attempting to modify the book <$book>. ($info)",
                cause
            )
    }
    
}