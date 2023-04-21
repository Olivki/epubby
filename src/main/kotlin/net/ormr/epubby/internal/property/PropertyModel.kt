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

package net.ormr.epubby.internal.property

import dev.epubby.Epub3Feature
import dev.epubby.property.Property

internal data class PropertyModel(val prefix: String?, val reference: String) {
    fun asString(): String = when (prefix) {
        null -> reference
        else -> "$prefix:$reference"
    }
}

@OptIn(Epub3Feature::class)
internal fun Property.toPropertyModel(): PropertyModel = PropertyModel(prefix?.name, reference)