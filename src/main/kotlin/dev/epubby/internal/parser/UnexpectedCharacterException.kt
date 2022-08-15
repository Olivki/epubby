/*
 * Copyright 2019-2022 Oliver Berg
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

internal class UnexpectedCharacterException(
    line: Int,
    column: Int,
    val expected: CharArray,
    val got: Char
) : ParseException(line, column, createMessage(line, column, expected, got))

private fun createMessage(line: Int, column: Int, expected: CharArray, got: Char): String = buildString {
    append('[')
    append(line)
    append(", ")
    append(column)
    append("]: Expected character")

    if (expected.size == 1) {
        append(" '")
        append(expected[0])
        append('\'')
    } else {
        append("s ")
        expected.joinTo(this, " | ", "(", ")") { "'$it'" }
    }

    append(", got character '")
    append(got)
    append("'.")
}