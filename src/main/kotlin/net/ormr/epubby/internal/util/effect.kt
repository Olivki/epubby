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

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// custom implementation of binding from kotlin-result to support some more stuff, based on monad comprehensions
// as implemented in arrow

@Suppress("UNCHECKED_CAST")
internal inline fun <V, E> effect(crossinline block: BindingScope<E>.() -> V): Result<V, E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val scope = BindingScope<E>()
    return try {
        with(scope) { Ok(block()) }
    } catch (e: ShiftRequest) {
        e.error as Err<E>
    }
}

internal class BindingScope<E> {
    fun shift(error: E): Nothing = shiftErr(Err(error))

    private fun <V> shiftErr(err: Err<E>): V = throw ShiftRequest(err)

    fun <V> Result<V, E>.bind(): V = when (this) {
        is Ok -> value
        is Err -> shiftErr(this)
    }

    inline fun <V, E1> Result<V, E1>.bind(mapper: (E1) -> E): V = when (this) {
        is Ok -> value
        is Err -> shift(mapper(error))
    }

    inline fun ensure(condition: Boolean, shift: () -> E) {
        contract {
            callsInPlace(shift, InvocationKind.AT_MOST_ONCE)
            returns() implies condition
        }

        if (!condition) shift(shift())
    }

    inline fun <T> ensureNotNull(value: T?, shift: () -> E): T {
        contract {
            callsInPlace(shift, InvocationKind.AT_MOST_ONCE)
            returns() implies (value != null)
        }

        if (value == null) shift(shift())
        
        return value
    }
}

@PublishedApi
internal class ShiftRequest(val error: Err<Any?>) : Exception(null, null, false, false)