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

import dev.epubby.EpubElement
import dev.epubby.EpubVersion
import dev.epubby.invalidVersion
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class VersionedProperty<T : Any>(
    private var value: T?,
    private val currentVersion: EpubVersion,
    private val minVersion: EpubVersion,
    private val containerName: String,
    private val featureName: String,
    private val setAction: ((T) -> Unit)?
) : ReadWriteProperty<EpubElement, T?> {
    init {
        value ifNotNull {
            if (currentVersion.isOlder(minVersion)) {
                invalidVersion(currentVersion, minVersion, containerName, featureName)
            }

            setAction?.invoke(it)
        }
    }

    override fun getValue(thisRef: EpubElement, property: KProperty<*>): T? = value

    override fun setValue(thisRef: EpubElement, property: KProperty<*>, value: T?) {
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

internal fun <T : Any> EpubElement.versioned(
    initial: T?,
    name: String,
    minVersion: EpubVersion,
    action: ((T) -> Unit)? = null
): ReadWriteProperty<EpubElement, T?> = VersionedProperty(initial, epub.version, minVersion, elementName, name, action)