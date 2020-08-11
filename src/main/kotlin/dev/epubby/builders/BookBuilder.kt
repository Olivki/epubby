/*
 * Copyright 2019-2020 Oliver Berg
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

package dev.epubby.builders

import dev.epubby.Book
import dev.epubby.BookVersion
import dev.epubby.InvalidBookVersionException
import dev.epubby.MalformedBookException
import dev.epubby.internal.models.metainf.MetaInfModel
import dev.epubby.internal.models.packages.PackageDocumentModel
import dev.epubby.internal.verifiers.PackageDocumentVerifier
import dev.epubby.prefixes.Prefixes
import java.io.IOException
import java.nio.file.FileSystem
import java.nio.file.Path

/**
 * A builder for [Book] instances.
 */
class BookBuilder internal constructor() : AbstractBuilder<Book>(Book::class.java) {
    // TODO: documentation

    private var version: BookVersion? = null

    @RequiredValue
    fun version(version: BookVersion): BookBuilder = apply {
        require(version != BookVersion.EPUB_3_1) { "Creation of EPUBs of version 3.1 is not allowed." }
        this.version = version
    }

    private var fileSystem: FileSystem? = null

    /**
     * TODO
     *
     * @throws [IOException] if an i/o error occurs
     */
    @RequiredValue
    fun fileSystem(directory: Path, name: String): BookBuilder = apply {
        TODO("implement the creation of a new file-system from a directory and a name")
    }

    private var metaInf: MetaInfModel? = null

    @RequiredValue
    fun metaInf(metaInf: MetaInfModel): BookBuilder = apply {
        this.metaInf = metaInf
    }

    private var packageDocument: PackageDocumentModel? = null

    @RequiredValue
    fun packageDocument(packageDocument: PackageDocumentModel): BookBuilder = apply {
        this.packageDocument = packageDocument
    }

    override fun build(): Book {
        val version = verify<BookVersion>(this::version)
        val fileSystem = verify<FileSystem>(this::fileSystem)
        val book = Book(version, fileSystem)
        val metaInf = verify<MetaInfModel>(this::metaInf).toMetaInf(book, Prefixes.empty())
        val packageDocument = verify<PackageDocumentModel>(this::packageDocument).toPackageDocument(book)

        try {
            PackageDocumentVerifier.verify(book, packageDocument)
        } catch (e: MalformedBookException) {
            rethrowWrapped<InvalidBookVersionException>(e) { "packageDocument" }
        }

        book.metaInf = metaInf
        book.packageDocument = packageDocument

        return book
    }
}