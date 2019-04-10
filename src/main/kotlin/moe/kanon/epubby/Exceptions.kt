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

/**
 * Thrown to indicate that something went wrong when working with the specified [book] instance.
 *
 * @property [book] The [Book] instance that `this` exception originates from.
 */
// TODO: Make the other exceptions inherit from this exception
open class BookException @JvmOverloads constructor(val book: Book, message: String, cause: Throwable? = null) :
    IOException(message, cause) {
    companion object {
        /**
         * Creates and returns a [BookException] from the specified [book] and [info] parameters.
         *
         * @param [book] the book that's being serialized
         * @param [info] information regarding *why* the operation failed
         * @param [cause] the cause of the exception
         */
        @JvmStatic
        @JvmOverloads
        fun create(book: Book, info: String, cause: Throwable? = null): BookException = BookException(
            book,
            "An error occurred when working with the book <$book>: $info",
            cause
        )
    }
}

/**
 * Thrown to indicate that something went wrong during the serialization *(saving)* process of the epub.
 */
open class BookSerializationException @JvmOverloads constructor(message: String, cause: Throwable? = null) :
    IOException(message, cause) {
    companion object {
        /**
         * Creates and returns a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] the book that's being serialized
         * @param [info] information regarding *why* the operation failed
         * @param [cause] the cause of the exception
         */
        @JvmStatic
        @JvmOverloads
        fun create(book: Book, info: String, cause: Throwable? = null): BookSerializationException =
            BookSerializationException(
                "An error occurred when attempting to deserialize the book <$book>: $info",
                cause
            )
    }
}

/**
 * Thrown to indicate that something went wrong during the deserialization *(loading)* process of the epub.
 */
open class BookDeserializationException @JvmOverloads constructor(message: String, cause: Throwable? = null) :
    IOException(message, cause) {
    companion object {
        /**
         * Creates and returns a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] the book that's being serialized
         * @param [info] information regarding *why* the operation failed
         * @param [cause] the cause of the exception
         */
        @JvmStatic
        @JvmOverloads
        fun create(book: Book, info: String, cause: Throwable? = null): BookDeserializationException =
            BookDeserializationException(
                "An error occurred when attempting to deserialize the book <$book>: $info",
                cause
            )
    }
}

/**
 * Thrown to indicate that some part of a book is malformed.
 *
 * This exception may be thrown in scenarios where it's uncertain that the file that's being parsed is an actual epub.
 */
open class MalformedBookException @JvmOverloads constructor(message: String, cause: Throwable? = null) :
    BookDeserializationException(message, cause) {
    companion object {
        /**
         * Creates and returns a [BookSerializationException] from the specified [book] and [info] parameters.
         *
         * @param [book] the book that's being serialized
         * @param [info] information regarding *why* the operation failed
         * @param [cause] the cause of the exception
         */
        @JvmStatic
        @JvmOverloads
        fun create(book: Book, info: String, cause: Throwable? = null): MalformedBookException =
            MalformedBookException(
                "The file <${book.file}> is either malformed, or not an epub: $info",
                cause
            )
    }
}

/**
 * Thrown to indicate that something went wrong when attempting to modify some part of the book.
 *
 * @property [book] The [Book] instance that `this` exception originates from.
 */
open class BookModificationException @JvmOverloads constructor(
    val book: Book,
    message: String,
    cause: Throwable? = null
) : IOException(message, cause) {
    companion object {
        /**
         * Creates and returns a [BookModificationException] from the specified [book] and [info] parameters.
         *
         * @param [book] the book that's being serialized
         * @param [info] information regarding *why* the operation failed
         * @param [cause] the cause of the exception
         */
        @JvmStatic
        @JvmOverloads
        fun create(book: Book, info: String, cause: Throwable? = null): BookModificationException =
            BookModificationException(
                book,
                "An error occurred when attempting to modify the book <$book>: $info",
                cause
            )
    }
    
}