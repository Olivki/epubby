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

package net.ormr.epubby.internal.util


// based on functions from XMLUtils in owlapi
// TODO: include the apache 2.0 license header for owl api

private fun Int.isXmlNameStartChar(): Boolean =
    this == ':'.code || this in 'A'.code..'Z'.code || this == '_'.code || this in 'a'.code..'z'.code || isXmlNameStartCodepoint()

private fun Int.isXmlNameStartCodepoint(): Boolean = this in 0xC0..0xD6
        || this in 0xD8..0xF6
        || this in 0xF8..0x2FF
        || this in 0x370..0x37D
        || this in 0x37F..0x1FFF
        || this in 0x200C..0x200D
        || this in 0x2070..0x218F
        || this in 0x2C00..0x2FEF
        || this in 0x3001..0xD7FF
        || this in 0xF900..0xFDCF
        || this in 0xFDF0..0xFFFD
        || this in 0x10000..0xEFFFF

private fun Int.isXmlNameChar(): Boolean =
    isXmlNameStartChar() || this == '-'.code || this == '.'.code || this in '0'.code..'9'.code || isXmlNameCodepoint()

private fun Int.isXmlNameCodepoint(): Boolean = this == 0xB7 || this in 0x0300..0x036F || this in 0x203F..0x2040

private fun Int.isNCNameStartChar(): Boolean = this != ':'.code && isXmlNameStartChar()

private fun Int.isNCNameChar(): Boolean = this != ':'.code && isXmlNameChar()

internal fun CharSequence.isNCName(): Boolean {
    if (isEmpty()) return false
    val firstCodePoint = Character.codePointAt(this, 0)
    if (!firstCodePoint.isNCNameStartChar()) return false
    var i = Character.charCount(firstCodePoint)
    while (i < length) {
        val codePoint = Character.codePointAt(this, i)
        if (!codePoint.isNCNameChar()) return false
        i += Character.charCount(codePoint)
    }
    return true
}

internal fun CharSequence.firstNCName(): String? {
    val ncName = buildString {
        val firstCodePoint = Character.codePointAt(this@firstNCName, 0)
        if (!firstCodePoint.isNCNameStartChar()) return null
        appendCodePoint(firstCodePoint)
        var i = Character.charCount(firstCodePoint)
        while (i < length) {
            val codePoint = Character.codePointAt(this@firstNCName, i)
            if (!codePoint.isNCNameChar()) break
            appendCodePoint(codePoint)
            i += Character.charCount(codePoint)
        }
    }
    return ncName
}