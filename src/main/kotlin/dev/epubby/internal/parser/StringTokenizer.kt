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

package dev.epubby.internal.parser

internal class StringTokenizer internal constructor(
    private val source: String,
    private val skippableCharacters: CharArray
) {
    var currentLine: Int = 1
        private set

    var currentColumn: Int = 1
        private set

    val isAtEnd: Boolean
        get() = index >= source.length

    private var index: Int = 0

    private var _currentChar: Char? = null

    private val iterator: CharIterator = source.iterator()

    private var count: Int = -1

    // TODO: different name?
    fun peek(): Char {
        while (count < index) {
            if (!iterator.hasNext()) {
                unexpectedEnd()
            }

            val char = iterator.nextChar()
            count++
            _currentChar = char
        }

        return requireNotNull(_currentChar)
    }

    fun moveOne() {
        when (peek()) {
            '\n' -> {
                currentLine++
                currentColumn = 1
            }
            '\r' -> currentColumn = 1
            else -> currentColumn++
        }

        index++
    }

    fun eat(char: Char): Char {
        val result = peek()

        if (result != char) {
            unexpectedChar(char)
        }

        moveOne()
        return result
    }

    fun eat(characters: CharArray): Char {
        val result = peek()

        if (result !in characters) {
            unexpectedChar(characters)
        }

        moveOne()
        return result
    }

    fun eatChar(): Char {
        val char = peek()
        moveOne()
        return char
    }

    fun eatChars(amount: Int): String {
        require(amount > 0) { "'amount' must not be negative" }
        require(isInRange(amount)) { "'amount' is larger than the remaining amount of characters" }
        return buildString {
            repeat(amount) {
                append(eatChar())
            }
        }
    }

    fun eatSkippable() {
        while (!isAtEnd && isSkippable(peek())) {
            moveOne()
        }
    }

    fun eatSkippableUntil(char: Char): Char {
        eatSkippable()
        return eat(char)
    }

    fun check(chars: CharArray): Boolean = when {
        isAtEnd -> false
        peek() !in chars -> false
        else -> true
    }

    fun check(char: Char): Boolean = when {
        isAtEnd -> false
        peek() != char -> false
        else -> true
    }

    fun isSkippable(char: Char): Boolean = char in skippableCharacters

    fun isInRange(amount: Int): Boolean = (index + amount) < source.length

    fun isOutOfRange(amount: Int): Boolean = (index + amount) >= source.length

    private fun unexpectedEnd(): Nothing = exception("Unexpected end of input.")

    private fun unexpectedChar(expected: Char): Nothing = unexpectedChar(charArrayOf(expected))

    private fun unexpectedChar(expected: CharArray): Nothing =
        throw UnexpectedCharacterException(currentLine, currentColumn, expected, peek())

    internal fun exception(message: String, cause: Throwable? = null): Nothing =
        throw ParseException(currentLine, currentColumn, "[$currentLine, $currentColumn]: $message", cause)
}