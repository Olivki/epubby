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

@file:JvmName("ReferenceFactory")

package moe.kanon.epubby.resources

import arrow.core.Option
import moe.kanon.epubby.Book
import moe.kanon.epubby.utils.FakeKeywordMarker
import moe.kanon.epubby.utils.ifNotEmpty

/**
 * Represents a `href` attribute with a optional [fragmentIdentifier].
 *
 * @property [href] The string pointing towards a resource.
 *
 * The resource may, or may not actually exist, this class makes no promises regarding the actual existence of the
 * resource.
 * @property [fragmentIdentifier] The fragment-identifier attached to `this` reference.
 *
 * A fragment-identifier is optional, and thus there is no guarantee that one will exist.
 */
data class ResourceReference @JvmOverloads constructor(
    val href: String,
    val fragmentIdentifier: Option<String> = Option.empty()
) {
    /**
     * Returns the [Resource] located under the [href] of `this` reference.
     *
     * Any invocations of this property will result in an exception being thrown as long as this property has not been
     * set. To set this property to a `resource` use the [toResource] function. However it is important to note that
     * there's still no guarantee that this property has been initialized even after the `toResource` function has been
     * invoked. This is because this class has no way of guaranteeing that there is an actual `resource` located at the
     * location that the `href` of `this` reference points to.
     *
     * If you retrieved an instance of `this` reference from a `Resource` instance then it is safe to assume that this
     * property has already been set.
     */
    lateinit var resource: Resource
        @JvmSynthetic internal set
    
    /**
     * Returns whether or not the [resource] property has been initialized yet.
     */
    fun isResourceInitialized(): Boolean = this::resource.isInitialized
    
    /**
     * Returns whether or not `this` reference has a [fragmentIdentifier].
     */
    fun hasFragment(): Boolean = fragmentIdentifier.nonEmpty()
    
    /**
     * Returns the [Resource] stored under the location that the [href] of `this` reference points towards, or none if
     * no `resource` could be found.
     *
     * If a `Resource` instance is successfully located then this function will also set the [resource] property to the
     * found `Resource`.
     *
     * If [isResourceInitialized] is `true` then this will return the [resource] property wrapped in a [Option].
     *
     * @param [book] the [Book] instance to look for the `resource` in
     */
    fun toResource(book: Book): Option<Resource> = when (isResourceInitialized()) {
        true -> Option.just(resource)
        else -> book.resources.getOrNone<Resource>(href).also { r -> r.ifNotEmpty { resource = it } }
    }
    /**
     * Returns the [href] of `this` reference with *(if [hasFragment] is `true`)* the [fragmentIdentifier] appended.
     *
     * The `href` and `fragmentIdentifier` is separated by a `#` character, as per the epub specification.
     */
    override fun toString(): String = "$href${if (hasFragment()) "#${fragmentIdentifier.orNull()}" else ""}"
}

typealias HREF = ResourceReference

/**
 * Creates and returns a [HREF] instance based on the results of parsing the specified [input].
 *
 * @param [input] the input string to parse
 */
@JvmName("parse")
@FakeKeywordMarker
fun href(input: String): HREF = when {
    '#' in input -> {
        val href = input.split('#')
        HREF(href[0], Option.just(href[1]))
    }
    else -> HREF(input)
}