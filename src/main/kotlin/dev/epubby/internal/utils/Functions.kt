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

package dev.epubby.internal.utils

/**
 * Invokes the [action] function if `this` is not `null`, otherwise does nothing.
 *
 * This function is useful is cases where you are working with a nullable `var` property and want to do some action on
 * it when it's not `null`, as the Kotlin compiler is unable to smart cast in cases like `if (thing != null)` if `thing`
 * is a `var`. It is also more explicit in its purposes than using any of the other scoping functions, like [also] or
 * [let], as those store the result of invoking `action` as a local variable, this function does *not* do that.
 *
 * Invoking this function is basically the same as manually capturing a local variant and forcing it's type to be not
 * `null`.
 *
 * For example:
 *
 * ```kotlin
 *  class Example {
 *      var thing: String? = null
 *
 *      fun doThing() {
 *          thing.ifNotNull { safeThing ->
 *              // do something with 'safeThing'
 *          }
 *      }
 *  }
 * ```
 *
 * And:
 *
 * ```kotlin
 *  class Example {
 *      var thing: String? = null
 *
 *      fun doThing() {
 *          if (thing != null) {
 *              val safeThing = thing!!
 *              // do something with 'safeThing'
 *          }
 *      }
 *  }
 * ```
 *
 * Basically get compiled down into the same code.
 */
@JvmSynthetic
internal inline infix fun <T : Any> T?.ifNotNull(action: (T) -> Unit) {
    if (this != null) {
        action(this)
    }
}

@JvmSynthetic
@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> self(value: T): T = value