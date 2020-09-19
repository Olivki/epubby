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

package dev.epubby.internal.utils

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.MalformedBookException
import dev.epubby.MalformedBookExceptionList
import dev.epubby.ParseMode
import dev.epubby.ParseMode.LENIENT
import dev.epubby.ParseMode.STRICT
import kotlinx.collections.immutable.PersistentList
import moe.kanon.kommons.func.Try
import org.jdom2.Element

internal fun throwMalformed(message: String, cause: Throwable? = null): Nothing =
    throw MalformedBookException(message, cause)

internal fun throwMissingChild(element: Element, name: String): Nothing =
    throw MalformedBookException("Element ${element.encodeToString(compactXmlOutputter)} is missing required child(ren) '$name'.")

internal fun <T> malformedFailure(message: String, cause: Throwable? = null): Try<T> =
    Try.failure(MalformedBookException(message, cause))

internal fun <T> missingAttribute(element: Element, attributeName: String, cause: Throwable? = null): Try<T> =
    malformedFailure(
        "Element ${element.encodeToString(compactXmlOutputter)} is missing required attribute '$attributeName'.",
        cause,
    )

internal fun <T> missingChild(element: Element, childName: String, cause: Throwable? = null): Try<T> =
    malformedFailure(
        "Element ${element.encodeToString(compactXmlOutputter)} is missing required child '$childName'.",
        cause,
    )

internal fun <T> Sequence<Try<T>>.flatMapFailure(): Sequence<Try<T>> {
    val successes = this.filterIsInstance<Try.Success<T>>()
    // TODO: we are currently just ignoring any 'failures' that contain a non 'MalformedBookException', should we do
    //       something in the case they contain a non 'MalformedBookException'? That case shouldn't actually ever
    //       happen, as we will be manually invoking this function on places were we only use 'MalformedBookException'
    //       but we might change this in the feature.
    return this.filterIsInstance<Try.Failure>()
        .map { it.cause }
        .filterIsInstance<MalformedBookException>()
        .toPersistentList()
        .ifEmpty { null }
        ?.flatMapToFailure()
        ?.let { sequenceOf(sequenceOf(it), successes).flatten() } ?: successes
}

internal fun <T> Sequence<Try<T>>.filterFailure(
    logger: InlineLogger,
    mode: ParseMode,
): Sequence<T> = when (mode) {
    STRICT -> this.map { it.unwrap() }
    LENIENT -> this.mapNotNull {
        it.fold(
            { e ->
                logger.error(e) { e.message }
                null
            },
            ::self
        )
    }
}

private fun PersistentList<MalformedBookException>.flatMapToFailure(): Try.Failure {
    check(this.isNotEmpty()) { "list should never be empty for this function" }
    var exceptions = this

    for (exception in exceptions) {
        if (exception is MalformedBookExceptionList) {
            // flatMaps any 'MalformedBookExceptionList' elements into the 'causes' that they are carrying
            exceptions = exceptions.remove(exception).addAll(exception.causes)
        }
    }

    return when (exceptions.size) {
        1 -> Try.Failure(exceptions.first())
        else -> Try.Failure(MalformedBookExceptionList(exceptions))
    }
}

// TODO: replace the functions below with the new functions defined above

// iterable
internal inline fun <T, R> Iterable<T>.tryMap(mapper: (T) -> R): List<Try<R>> = map { Try { mapper(it) } }

internal fun <T : Any> Iterable<Try<T>>.mapToValues(
    logger: InlineLogger,
    mode: ParseMode,
): List<T> = mapNotNull { result ->
    when (mode) {
        STRICT -> result.unwrap()
        LENIENT -> result.fold(
            {
                logger.error(it) { it.message }
                null
            },
            { it }
        )
    }
}

// sequence
internal inline fun <T, R> Sequence<T>.tryMap(crossinline mapper: (T) -> R): Sequence<Try<R>> =
    map { Try { mapper(it) } }

internal fun <T : Any> Sequence<Try<T>>.mapToValues(
    logger: InlineLogger,
    mode: ParseMode,
): Sequence<T> = mapNotNull { result ->
    when (mode) {
        STRICT -> result.unwrap()
        LENIENT -> result.fold(
            {
                logger.error(it) { it.message }
                null
            },
            { it }
        )
    }
}