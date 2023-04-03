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

package dev.epubby.version

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import dev.epubby.Epub31Feature
import dev.epubby.version.EpubVersion.EPUB_2_0
import dev.epubby.version.EpubVersion.EPUB_3_0
import dev.epubby.version.EpubVersion.EPUB_3_1
import dev.epubby.version.EpubVersion.EPUB_3_2
import dev.epubby.version.EpubVersionParseError.InvalidMajor
import dev.epubby.version.EpubVersionParseError.InvalidMinor
import dev.epubby.version.EpubVersionParseError.MissingSeparator
import dev.epubby.version.EpubVersionParseError.NoVersion
import dev.epubby.version.EpubVersionParseError.TooManySeparators
import dev.epubby.version.EpubVersionParseError.UnknownVersion
import net.ormr.epubby.internal.util.effect

@OptIn(Epub31Feature::class)
private val versions by lazy { listOf(EPUB_2_0, EPUB_3_0, EPUB_3_1, EPUB_3_2) }

internal fun parseEpubVersion(version: String): Result<EpubVersion, EpubVersionParseError> = effect {
    ensure(version.isNotBlank()) { NoVersion }
    ensure(version.count { it == '.' } > 0) { MissingSeparator }
    val parts = version.split('.')
    ensure(parts.size == 2) { TooManySeparators }
    val major = parseInt(parts[0], ::InvalidMajor).bind()
    val minor = parseInt(parts[1], ::InvalidMinor).bind()
    findVersion(major, minor).bind()
}

private inline fun parseInt(
    text: String,
    err: (String) -> EpubVersionParseError,
): Result<Int, EpubVersionParseError> = try {
    Ok(text.toInt())
} catch (e: NumberFormatException) {
    Err(err(text))
}

private fun findVersion(major: Int, minor: Int): Result<EpubVersion, EpubVersionParseError> =
    versions.find { it.major == major && it.minor == minor }.toResultOr { UnknownVersion(major, minor) }