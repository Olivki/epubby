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

@file:Suppress("MemberVisibilityCanBePrivate")

package moe.kanon.epubby

import moe.kanon.kextensions.io.KPath
import net.swiftzer.semver.SemVer
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.logger
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

// TODO: Add DSL builder for book settings
// TODO: Add DSL builder for creating new epubs from nothing, builder will be for initial settings like name and author.

public data class Book(public val file: Path) {
    
    /**
     * The [logger][KotlinLogger] instance used for any and all logging done by epubby.
     */
    @PublishedApi internal val logger: KotlinLogger = logger("epubby")
    
    /**
     * The format version used by this EPUB.
     */
    public var version: Version = Version.UNKNOWN
        internal set(value) = when (value.format) {
            EpubFormat.NOT_SUPPORTED -> throw BookVersionException(value.semantic)
            else -> field = value
        }
    
    public fun saveTo(directory: Path) {
        TODO("Implement saving feature.")
    }
    
    companion object {
        
        /*
        @Throws(Throwable::class)
@JvmStatic
fun main(args: Array<String>) {
    val env = HashMap<String, String>()
    env["create"] = "true"
    // locate file system by using the syntax
    // defined in java.net.JarURLConnection
    val uri = URI.create("jar:file:/codeSamples/zipfs/zipfstest.zip")
    
    FileSystems.newFileSystem(uri, env).use { zipfs ->
        val externalTxtFile = Paths.get("/codeSamples/zipfs/SomeTextFile.txt")
        val pathInZipfile = zipfs.getPath("/SomeTextFile.txt")
        // copy a file into the zip file
        Files.copy(
            externalTxtFile, pathInZipfile,
            StandardCopyOption.REPLACE_EXISTING
        )
    }
}
         */
        
        @JvmStatic
        @Throws(IOException::class, ReadBookException::class)
        public fun from(file: Path): Book {
            TODO("Implement factory method.")
        }
        
        @JvmStatic
        @Throws(IOException::class, ReadBookException::class)
        public fun from(path: String): Book = from(KPath(path))
        
        @JvmStatic
        @Throws(IOException::class, ReadBookException::class)
        public fun from(stream: InputStream): Book {
            TODO("Implement factory method.")
        }
    }
    
    /**
     * A data class holding information about the version of this EPUB.
     */
    public class Version(_semVer: String) {
        
        internal constructor(format: EpubFormat) : this(format.version.toString())
        
        /**
         * The [semantic version][SemVer] instance.
         */
        public val semantic: SemVer = SemVer.parse(_semVer)
        
        /**
         * The closest matching [version format][EpubFormat].
         */
        public val format: EpubFormat = EpubFormat.from(semantic)
        
        /**
         * Compares this [version][Version] to the [other] version and returns which one is the bigger.
         */
        public operator fun compareTo(other: Version): Int = semantic.compareTo(other.semantic)
        
        companion object {
            
            /**
             * Represents an unknown version.
             */
            @JvmStatic public val UNKNOWN: Version = Version(EpubFormat.UNKNOWN)
        }
    }
}

// TODO: Remove this once kextensions reaches a favourable state with v0.6.0.
/**
 * Simulates the `multi-catch` feature from Java in Kotlin.
 *
 * **Usage:**
 *
 * ```kotlin
 *  try {
 *      ...
 *  } catch (e: Exception) {
 *      e.multiCatch(ExceptionOne::class, ExceptionTwo::class) {
 *          ...
 *      }
 *  }
 * ```
 *
 * @param classes Which classes this `multi-catch` block should actually catch.
 *
 * If an `exception` gets thrown where this function is used, and it's not listed in the `classes` vararg, then the
 * exception will just get re-thrown.
 *
 * @author carleslc
 */
@PublishedApi
internal inline fun Throwable.multiCatch(vararg classes: KClass<*>, catchBlock: () -> Unit) =
    if (classes.any { this::class.isSubclassOf(it) }) catchBlock() else throw this