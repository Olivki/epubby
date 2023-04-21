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

package net.ormr.epubby.internal

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun identifierDelegate(
    initialValue: String?,
    onChange: ((oldIdentifier: String?, newIdentifier: String?) -> Unit)? = null,
): ReadWriteProperty<InternalOpfElement, String?> = IdentifierDelegate(initialValue, onChange)

private class IdentifierDelegate(
    initialValue: String?,
    private val onChange: ((oldIdentifier: String?, newIdentifier: String?) -> Unit)?,
) : ReadWriteProperty<InternalOpfElement, String?> {
    private var value: String? = initialValue

    override fun getValue(thisRef: InternalOpfElement, property: KProperty<*>): String? = value

    override fun setValue(thisRef: InternalOpfElement, property: KProperty<*>, value: String?) {
        val opf = thisRef.opf
        val currentValue = this.value
        if (currentValue != null) {
            opf?.removeElement(currentValue)
            opf?.putElement(value, thisRef)
        } else {
            opf?.putElement(value, thisRef)
        }
        onChange?.invoke(currentValue, value)
        this.value = value
    }
}