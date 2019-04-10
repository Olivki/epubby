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

@file:Suppress("NOTHING_TO_INLINE")

package moe.kanon.epubby.utils

import java.util.Collections
import java.util.RandomAccess

/**
 * Returns an unmodifiable view of `this` collection.
 *
 * This function allows modules to provide users with "read-only" access to internal collections.
 *
 * Query operations on the returned collection "read through" to the specified collection, and attempts to modify the
 * returned collection, whether direct or via its iterator, result in an [UnsupportedOperationException].
 *
 * The returned collection does *not* pass the hashCode and equals operations through to the backing collection, but
 * relies on `Any`'s `equals` and `hashCode` function.  This is necessary to preserve the contracts of these operations
 * in the case that the backing collection is a [Set] or a [List].
 *
 * The returned collection will be serializable if the specified collection is serializable.
 *
 * @receiver the collection for which an unmodifiable view is to be returned
 *
 * @param [V] the type that `this` collection stores
 *
 * @return an unmodifiable view of `this` collection
 */
internal inline fun <V> Collection<V>.toUnmodifiableCollection(): Collection<V> = Collections.unmodifiableCollection(this)

/**
 * Returns an unmodifiable view of `this` list.
 *
 * This function allows modules to provide users with "read-only" access to internal lists.
 *
 * Query operations on the returned list "read through" to the specified list, and attempts to modify the returned list,
 * whether direct or via its iterator, result in an [UnsupportedOperationException].
 *
 * The returned list will be serializable if the specified list is serializable. Similarly, the returned list will
 * implement [RandomAccess] if the specified list does.
 *
 * @receiver the list for which an unmodifiable view is to be returned
 *
 * @param [V] the type that `this` list stores
 *
 * @return an unmodifiable view of `this` list
 */
internal inline fun <V> List<V>.toUnmodifiableList(): List<V> = Collections.unmodifiableList(this)

/**
 * Returns an unmodifiable view of `this` set.
 *
 * This function allows modules to provide users with "read-only" access to internal sets.
 *
 * Query operations on the returned list "read through" to the specified set, and attempts to modify the returned set,
 * whether direct or via its iterator, result in an [UnsupportedOperationException].
 *
 * The returned list will be serializable if the specified set is serializable. Similarly, the returned set will
 * implement [RandomAccess] if the specified set does.
 *
 * @receiver the set for which an unmodifiable view is to be returned
 *
 * @param [V] the type that `this` set stores
 *
 * @return an unmodifiable view of `this` set
 */
internal inline fun <V> Set<V>.toUnmodifiableSet(): Set<V> = Collections.unmodifiableSet(this)