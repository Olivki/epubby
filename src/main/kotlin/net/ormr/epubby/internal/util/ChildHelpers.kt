/*
 * Copyright 2023 Oliver Berg
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

import net.ormr.epubby.internal.opf.InternalIdentifiableOpfElement
import net.ormr.epubby.internal.opf.InternalOpfElement
import net.ormr.epubby.internal.opf.OpfImpl

internal fun OpfImpl.adoptElement(element: InternalOpfElement) {
    // TODO: custom exception?
    require(element.opf == null || element.opf === this) { "Element already belongs to an Opf instance" }
    element.opf = this
    if (element is InternalIdentifiableOpfElement) {
        putElement(element.identifier, element)
    }
}

internal fun OpfImpl.disownElement(element: InternalOpfElement) {
    // TODO: raise exception otherwise?
    if (element.opf === this) {
        element.opf = null
    }
    if (element is InternalIdentifiableOpfElement) {
        removeElement(element.identifier)
    }
}