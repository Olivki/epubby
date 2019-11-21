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

@file:JvmName("CSSUtils")

package moe.kanon.epubby.utils

import cz.vutbr.web.css.Rule
import moe.kanon.kommons.requireThat
import kotlin.reflect.typeOf

/**
 * Returns the element stored at the given [index] of `this` rule cast to [E].
 *
 * @throws [IllegalArgumentException] if the element stored at the given [index] is not a type/sub-type of [E]
 */
@UseExperimental(ExperimentalStdlibApi::class)
inline operator fun <reified E> Rule<*>.invoke(index: Int): E {
    val element = this[index]
    requireThat(element is E) { "element at index <$index> should be of type ${typeOf<E>()}" }
    return element
}