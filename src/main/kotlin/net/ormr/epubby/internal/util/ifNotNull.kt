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

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Invokes the [action] function if [value] is not `null`, otherwise does nothing.
 *
 * This function is useful in cases where the Kotlin compiler is unable to smart cast properties, like for open
 * properties or `var` properties.
 *
 * It can be seen as a side-effect only variant of [let] or [also].
 *
 * Invoking this function is basically the same as manually capturing a local variable of a property and checking that
 * it's not `null`.
 *
 * For example:
 *
 * ```kotlin
 *  var foo: String? = ...
 *
 *  fun bar() {
 *      ifNotNull(foo) { localFoo ->
 *          // do something with 'localFoo'
 *      }
 *  }
 * ```
 *
 * And:
 *
 * ```kotlin
 *  var foo: String? = ...
 *
 *  fun bar() {
 *      val localFoo = foo
 *      if (localFoo != null) {
 *          // do something with 'localFoo'
 *      }
 *  }
 * ```
 *
 * Are equivalent in what they accomplish, but the `ifNotNull` variant does it in less lines.
 */
internal inline fun <T : Any> ifNotNull(value: T?, action: (T) -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }

    if (value != null) {
        action(value)
    }
}