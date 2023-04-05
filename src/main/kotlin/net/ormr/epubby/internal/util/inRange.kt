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

// used to coerce the Kotlin compiler into doing cheap range cheap
// because doing something like 'value in EPUB_3_0..EPUB_3_2' will cause Kotlin to complain
// about type mismatches due the different types, but this function coerces it into accept the
// lowest common type
@Suppress("NOTHING_TO_INLINE")
internal inline fun <T : Comparable<T>> inRange(value: T, start: T, end: T): Boolean = value in start..end