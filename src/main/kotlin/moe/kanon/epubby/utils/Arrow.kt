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

@file:JvmName("ArrowUtils")

package moe.kanon.epubby.utils

import arrow.core.None
import arrow.core.Option
import arrow.core.Some

/**
 * Executes the specified [action] if, and *only* if, `this` option [is not empty][Option.nonEmpty].
 *
 * @receiver the [Option] instance to check against
 *
 * @param [action] the action to execute
 */
inline infix fun <A> Option<A>.ifNotEmpty(action: (A) -> Unit) {
    if (this is Some<A>) action(this.t)
}

/**
 * Executes the specified [action] if, and *only* if, `this` option [not empty][Option.isEmpty].
 *
 * @receiver the [Option] instance to check against
 *
 * @param [action] the action to execute
 */
inline infix fun <A> Option<A>.ifEmpty(action: () -> Unit) {
    if (this is None) action()
}

/**
 * Returns the value of `this` [Option], or throws the specified [exception] if `this` option is empty.
 *
 * @receiver the [Option] instance to retrieve the value of
 *
 * @param [exception] the [Exception] to throw if `this` option is empty
 */
inline infix fun <A> Option<A>.orElseThrow(exception: () -> Exception): A = when (this) {
    is Some<A> -> this.t
    is None -> throw exception()
}