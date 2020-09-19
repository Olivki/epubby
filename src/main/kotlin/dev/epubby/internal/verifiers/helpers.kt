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

@file:Suppress("NOTHING_TO_INLINE")

package dev.epubby.internal.verifiers

import dev.epubby.EpubVersion
import dev.epubby.invalidVersion
import kotlin.reflect.KProperty0

@DslMarker
private annotation class HelperMarker

@HelperMarker
internal class VersionChecker(val name: String, val currentVersion: EpubVersion) {
    @HelperMarker
    internal inline fun raise(min: EpubVersion, feature: String): Nothing =
        invalidVersion(currentVersion, min, name, feature)

    @HelperMarker
    internal inline fun <T> verify(min: EpubVersion, property: KProperty0<T?>) {
        if (property.get() != null && currentVersion isOlder min) {
            raise(min, property.name)
        }
    }
}

@HelperMarker
internal inline fun checkVersion(name: String, version: EpubVersion, body: VersionChecker.() -> Unit) {
    VersionChecker(name, version).apply(body)
}