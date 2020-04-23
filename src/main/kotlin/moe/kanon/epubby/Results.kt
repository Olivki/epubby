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

package moe.kanon.epubby

import com.github.michaelbull.logging.InlineLogger
import moe.kanon.kommons.func.Try

// iterable
internal inline fun <T, R> Iterable<T>.tryMap(mapper: (T) -> R): List<Try<R>> = map { Try { mapper(it) } }

internal fun <T> Iterable<Try<T>>.mapSuccess(): List<T> = filterIsInstance<Try.Success<T>>().map { it.item }

internal fun <T : Any> Iterable<Try<T>>.mapToValues(
    logger: InlineLogger,
    strictness: ParseStrictness
): List<T> = mapNotNull { result ->
    when (strictness) {
        ParseStrictness.STRICT -> result.unwrap()
        ParseStrictness.LENIENT -> result.fold({ logger.error { it.localizedMessage }; null }, { it })
    }
}

// sequence
internal inline fun <T, R> Sequence<T>.tryMap(crossinline mapper: (T) -> R): Sequence<Try<R>> =
    map { Try { mapper(it) } }

internal fun <T> Sequence<Try<T>>.mapSuccess(): Sequence<T> = filterIsInstance<Try.Success<T>>().map { it.item }

internal fun <T : Any> Sequence<Try<T>>.mapToValues(
    logger: InlineLogger,
    strictness: ParseStrictness
): Sequence<T> = mapNotNull { result ->
    when (strictness) {
        ParseStrictness.STRICT -> result.unwrap()
        ParseStrictness.LENIENT -> result.fold({ logger.error { it.localizedMessage }; null }, { it })
    }
}