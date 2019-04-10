/*
 * Copyright 2019 Oliver Berg
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

package moe.kanon.epubby.resources

import arrow.core.Option
import moe.kanon.kommons.io.name

/**
 * Represents a `href` attribute with a optional [fragmentIdentifier].
 *
 * @property [resource] The [Resource] instance that `this` reference points to.
 * @property [fragmentIdentifier] The fragment-identifier attached to `this` reference.
 *
 * A fragment-identifier is optional, and thus there is no guarantee that one will exist.
 */
data class ResourceReference @JvmOverloads constructor(
    val resource: Resource,
    var fragmentIdentifier: Option<String> = Option.empty()
) {
    /**
     * Returns the local path to where the underlying [resource] is stored.
     */
    val path: String get() = "${resource.type.location}${resource.origin.name}"
    
    /**
     * Returns [toString].
     */
    fun get(): String = toString()
    
    /**
     * Returns [toString].
     */
    @JvmSynthetic
    operator fun invoke(): String = get()
    
    /**
     * Returns the [path] property.
     */
    operator fun component3(): String = path
    
    /**
     * Returns the [toString] function.
     */
    operator fun component4(): String = toString()
    
    /**
     * Returns whether or not `this` reference has a [fragmentIdentifier].
     */
    fun hasFragment(): Boolean = fragmentIdentifier.nonEmpty()
    
    /**
     * Returns the [href] of `this` reference with *(if [hasFragment] is `true`)* the [fragmentIdentifier] appended.
     *
     * The `href` and `fragmentIdentifier` is separated by a `#` character, as per the epub specification.
     */
    override fun toString(): String = "$path${if (hasFragment()) "#${fragmentIdentifier.orNull()}" else ""}"
}

typealias HREF = ResourceReference
typealias href = ResourceReference