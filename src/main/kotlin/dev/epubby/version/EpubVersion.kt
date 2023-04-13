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
import net.ormr.epubby.internal.util.inRange

/**
 * Represents a supported version of the epub specification.
 */
// TODO: Epub 3.3
public sealed class EpubVersion(public val major: Int, public val minor: Int) : Comparable<EpubVersion> {
    /**
     * The [EPUB 2.0](https://idpf.org/epub/201) format.
     *
     * The spec this refers to is actually EPUB 2.0.1, but epub documents do not serialize patch versions.
     */
    public object EPUB_2_0 : StableEpubVersion(2, 0)

    /**
     * The [EPUB 3.0](https://idpf.org/epub/30/) format.
     *
     * [OPF Spec](http://idpf.org/epub/301/spec/epub-publications-20140626.html).
     */
    public object EPUB_3_0 : StableEpubVersion(3, 0)

    /**
     * The [EPUB 3.1](https://idpf.org/epub/31/) format.
     *
     * The EPUB 3.1 format is [officially discouraged](http://www.idpf.org/epub/dir/#epub31) from use, and as such
     * the format is explicitly ***not*** supported by epubby, and it should never be used.
     */
    @Epub31Feature
    public object EPUB_3_1 : EpubVersion(3, 1)

    /**
     * The [EPUB 3.2](https://www.w3.org/publishing/epub3/epub-spec.html) format.
     */
    public object EPUB_3_2 : StableEpubVersion(3, 2)

    public fun isEpub2(): Boolean = EPUB_2_0 == this

    public fun isEpub3(): Boolean = inRange(this, EPUB_3_0, EPUB_3_2)

    final override fun compareTo(other: EpubVersion): Int = when {
        major > other.major -> 1
        major < other.major -> -1
        minor > other.minor -> 1
        minor < other.minor -> -1
        else -> 0
    }

    override fun toString(): String = "$major.$minor"
}