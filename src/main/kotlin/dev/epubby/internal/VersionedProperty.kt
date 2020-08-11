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

package dev.epubby.internal

import dev.epubby.BookElement
import dev.epubby.BookVersion
import dev.epubby.invalidVersion
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class VersionedProperty<T : Any>(
    private var value: T?,
    private val currentVersion: BookVersion,
    private val minVersion: BookVersion,
    private val containerName: String,
    private val featureName: String,
    private val setAction: ((T) -> Unit)?
) : ReadWriteProperty<BookElement, T?> {
    init {
        value ifNotNull {
            if (currentVersion.isOlder(minVersion)) {
                invalidVersion(currentVersion, minVersion, containerName, featureName)
            }

            setAction?.invoke(it)
        }
    }

    override fun getValue(thisRef: BookElement, property: KProperty<*>): T? = value

    override fun setValue(thisRef: BookElement, property: KProperty<*>, value: T?) {
        if (value != null) {
            if (currentVersion.isOlder(minVersion)) {
                invalidVersion(currentVersion, minVersion, containerName, featureName)
            } else {
                this.value = value
            }
        } else {
            this.value = null
        }
    }
}

internal fun <T : Any> BookElement.versioned(
    initial: T?,
    name: String,
    minVersion: BookVersion,
    action: ((T) -> Unit)? = null
): ReadWriteProperty<BookElement, T?> = VersionedProperty(initial, book.version, minVersion, elementName, name, action)