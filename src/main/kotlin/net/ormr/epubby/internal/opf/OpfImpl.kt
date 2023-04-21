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

package net.ormr.epubby.internal.opf

import dev.epubby.NonUniqueIdentifierException
import dev.epubby.opf.Opf
import dev.epubby.opf.OpfElement
import net.ormr.epubby.internal.InternalOpfElement
import net.ormr.epubby.internal.opf.metadata.MetadataImpl
import net.ormr.epubby.internal.util.ifNotNull
import kotlin.properties.Delegates

internal class OpfImpl : Opf {
    private val elements: MutableMap<String, InternalOpfElement> = hashMapOf()
    override var metadata: MetadataImpl by Delegates.notNull()

    override fun findElement(identifier: String): OpfElement? = elements[identifier]

    override fun hasElement(identifier: String): Boolean = identifier in elements

    internal fun putElement(identifier: String?, element: InternalOpfElement) {
        ifNotNull(identifier) {
            if (hasElement(it)) {
                throw NonUniqueIdentifierException("An element with identifier '$it' already exists")
            }
            elements[it] = element
        }
    }

    internal fun removeElement(identifier: String?) {
        ifNotNull(identifier) {
            elements -= it
        }
    }
}