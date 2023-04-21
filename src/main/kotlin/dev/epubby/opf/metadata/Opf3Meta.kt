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
import dev.epubby.opf.IdentifiableOpfElement
import dev.epubby.property.Property

@Epub3Feature
public sealed interface Opf3Meta<T : Any> : OpfMeta, IdentifiableOpfElement {
    /**
     * The value of the `meta` element.
     */
    public var value: T
    public var property: Property // TODO: do checks when assigning this that the property is a known one
    public val scheme: Property?
    public var refines: String?
    override var identifier: String?
    public var direction: ReadingDirection?
    public var language: String?

    public fun getValueAsString(): String
}