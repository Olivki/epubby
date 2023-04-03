/*
 * Copyright 2019-2023 Oliver Berg
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

@file:Suppress("ClassName")

package dev.epubby.version

import dev.epubby.Epub31Feature

/**
 * Represents a supported version of the epub specification.
 */
public sealed class EpubVersion(public val major: Int, public val minor: Int) : Comparable<EpubVersion> {
    /**
     * Represents the [EPUB 2.0](http://www.idpf.org/epub/dir/#epub201) format.
     */
    public object EPUB_2_0 : StableEpubVersion(2, 0)

    /**
     * Represents the [EPUB 3.0](http://www.idpf.org/epub/dir/#epub301) format.
     */
    public object EPUB_3_0 : StableEpubVersion(3, 0)

    /**
     * Represents the [EPUB 3.1](http://www.idpf.org/epub/dir/#epub31) format.
     *
     * The EPUB 3.1 format is [officially discouraged](http://www.idpf.org/epub/dir/#epub31) from use, and as such
     * the format is explicitly ***not*** supported by epubby, and it should never be used.
     */
    @Epub31Feature
    public object EPUB_3_1 : EpubVersion(3, 1)

    /**
     * Represents the [EPUB 3.2](http://www.idpf.org/epub/dir/#epub32) format.
     */
    public object EPUB_3_2 : StableEpubVersion(3, 2)

    final override fun compareTo(other: EpubVersion): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        else -> 0
    }

    override fun toString(): String = "$major.$minor"
}