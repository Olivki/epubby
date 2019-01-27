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

package moe.kanon.epubby

import net.swiftzer.semver.SemVer

/**
 * Thrown whenever a problem is encountered within epubby.
 *
 * Any `exception` thrown *explicitly* by epubby will most likely be a child of this `exception` in some way.
 */
public open class BookException : Exception {
    
    constructor(message: String, cause: Throwable) : super(message, cause)
    
    constructor(message: String) : super(message)
    
    constructor(cause: Throwable) : super(cause)
    
}

public open class BookVersionException : BookException {
    
    constructor(version: SemVer, message: String = "\"EPUB $version\" is not supported!") : super(message)
    
    constructor(version: SemVer, message: String = "\"EPUB $version\" is not supported!", cause: Throwable) : super(
        message,
        cause
    )
}

/**
 * Thrown whenever a problem with reading/parsing the epub is encountered.
 *
 * More specific `exceptions` that handle parsing/reading in epubby will be children of this.
 */
public open class ReadBookException : BookException {
    
    constructor(message: String, cause: Throwable) : super(
        "Something went wrong when trying to read the book: [$message]",
        cause
    )
    
    constructor(message: String) : super("Something went wrong when trying to read the book: [$message]")
    
    constructor(cause: Throwable) : super(cause)
    
}

/**
 * Thrown whenever a problem with writing the epub is encountered.
 *
 * More specific `exceptions` that handle writing in epubby will be children of this.
 */
public open class WriteBookException : BookException {
    
    constructor(message: String, cause: Throwable) : super(
        "Something went wrong when writing to the book: [$message]",
        cause
    )
    
    constructor(message: String) : super("Something went wrong when writing to the book: [$message]")
    
    constructor(cause: Throwable) : super(cause)
    
}