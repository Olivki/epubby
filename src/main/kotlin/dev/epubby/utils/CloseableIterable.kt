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

package dev.epubby.utils

import moe.kanon.kommons.CloseableSequence
import java.io.Closeable

/**
 * An [Iterable] type that can be [closed][Closeable.close].
 *
 * Generally used for cases where a `Iterable` needs to be closed after being iterated.
 */
interface CloseableIterable<out T> : Iterable<T>, Closeable

/**
 * Returns a [CloseableIterable] wrapping around [this] sequence.
 *
 * Invoking [close][CloseableIterable.close] on the returned instance will close `this` sequence.
 */
fun <T> CloseableSequence<T>.asCloseableIterable(): CloseableIterable<T> = object : CloseableIterable<T> {
    override fun iterator(): Iterator<T> = this@asCloseableIterable.iterator()

    override fun close() {
        this@asCloseableIterable.close()
    }
}