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

package dev.epubby.dublincore

import dev.epubby.opf.IdentifiableOpfElement

public sealed interface DublinCore : IdentifiableOpfElement {
    /**
     * The identifier of the dublin-core element, or `null` if no identifier has been defined.
     */
    override var identifier: String?

    /**
     * The contents of the dublin-core element.
     */
    public var content: String?
}