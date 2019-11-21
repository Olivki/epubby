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

package moe.kanon.epubby.packages

import moe.kanon.epubby.packages.Guide.Reference
import moe.kanon.epubby.packages.Guide.Type

// -- GUIDE -- \\
/**
 * Adds a new [reference][Reference] instance based on the given [type], [href] and [title] to this guide.
 *
 * Note that if a `reference` already exists under the given [type], then it will be overridden.
 *
 * @param [type] the `type` to store the element under
 * @param [href] the [Resource] to inherit the [href][Resource.href] of
 * @param [title] the *(optional)* title
 */
operator fun Guide.set(type: Type, href: String) {
    addReference(type, href)
}

/**
 * Removes the [reference][Reference] element stored under the specified [type].
 *
 * @param [type] the `type` to remove
 */
operator fun Guide.minusAssign(type: Type) {
    removeReference(type)
}

/**
 * Returns the [reference][Reference] stored under the given [type], or throws a [NoSuchElementException] if none
 * is found.
 */
operator fun Guide.get(type: Type): Reference = getReference(type)

/**
 * Returns `true` if this guide has a reference with the given [type], `false` otherwise.
 */
operator fun Guide.contains(type: Type): Boolean = hasType(type)

/**
 * Adds a new [reference][Reference] instance based on the given [customType], and [href] to this guide.
 *
 * Note that if a `reference` already exists under the given [customType], then it will be overridden.
 *
 * The [OPF][Package] specification states that;
 *
 * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
 * **must** begin with the string `'other.'`"
 *
 * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
 *
 * This means that if this function is invoked with `(customType = "tn")` the system will *not* store the created
 * `reference` under the key `"tn"`, instead it will store the `reference` under the key `"other.tn"`. This
 * behaviour is consistent across all functions that accept a `customType`.
 *
 * @param [customType] the custom type string
 * @param [href] the [Resource] to inherit the [href][Resource.href] of
 */
operator fun Guide.set(customType: String, href: String) {
    addCustomReference(customType, href)
}

/**
 * Removes the [reference][Reference] element stored under the specified [customType].
 *
 * The [OPF][Package] specification states that;
 *
 * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
 * **must** begin with the string `'other.'`"
 *
 * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
 *
 * This means that if this function is invoked with `("tn")` the system does *not* remove a `reference` stored
 * under the key `"tn"`, instead it removes a `reference` stored under the key `"other.tn"`. This behaviour is
 * consistent across all functions that accept a `customType`.
 *
 * @param [customType] the custom type string
 */
operator fun Guide.minusAssign(customType: String) {
    removeCustomReference(customType)
}

/**
 * Returns the [reference][Reference] stored under the given [customType], or throws a [NoSuchElementException] if
 * none is found.
 *
 * The [OPF][Package] specification states that;
 *
 * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
 * **must** begin with the string `'other.'`"
 *
 * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
 *
 * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
 * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
 * consistent across all functions that accept a `customType`.
 */
operator fun Guide.get(customType: String): Reference = getCustomReference(customType)

/**
 * Returns `true` if this guide has a reference with the given [customType], `false` otherwise.
 *
 * The [OPF][Package] specification states that;
 *
 * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
 * **must** begin with the string `'other.'`"
 *
 * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
 *
 * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
 * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
 * consistent across all functions that accept a `customType`.
 */
operator fun Guide.contains(customType: String): Boolean = hasCustomType(customType)

// TODO: Manifest operators