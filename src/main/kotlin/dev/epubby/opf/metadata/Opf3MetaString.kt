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

package dev.epubby.opf.metadata

import dev.epubby.Epub3Feature
import dev.epubby.ReadingDirection
import dev.epubby.property.Property

@Epub3Feature
public data class Opf3MetaString(
    override var value: String,
    override var property: Property,
    override var scheme: Property? = null,
    override var refines: String? = null,
    override var identifier: String? = null,
    override var direction: ReadingDirection? = null,
    override var language: String? = null,
) : Opf3Meta<String> {
    override fun getValueAsString(): String = value
}