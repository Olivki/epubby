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

import moe.kanon.epubby.packages.PackageGuide.Reference
import moe.kanon.epubby.packages.PackageGuide.Type
import moe.kanon.epubby.packages.PackageManifest.Item
import moe.kanon.epubby.packages.PackageSpine.ItemReference
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.structs.Identifier

/**
 * Returns the [item][Item] stored under the given [identifier], or throws a [NoSuchElementException] if none is
 * found.
 */
operator fun PackageManifest.get(identifier: Identifier): Item<*> = getItem(identifier)

/**
 * Returns `true` if this manifest has an [item][Item] with the given [identifier], `false` otherwise.
 */
operator fun PackageManifest.contains(identifier: Identifier): Boolean = hasItem(identifier)

/**
 * Returns `true` if this manifest contains the given [item], `false` otherwise.
 */
operator fun PackageManifest.contains(item: Item<*>): Boolean = hasItem(item)

/**
 * Returns `true` if this manifest contains a [local item][Item.Local] that points towards the given [resource],
 * `false` otherwise.
 */
operator fun PackageManifest.contains(resource: Resource): Boolean = hasItemFor(resource)

// -- SPINE -- \\
/**
 * Returns the [itemref][ItemReference] at the given [index].
 *
 * @throws [IndexOutOfBoundsException] if the given [index] is out of range
 */
operator fun PackageSpine.get(index: Int): ItemReference = getReference(index)

/**
 * Returns the first [itemref][ItemReference] that references an [item][ItemReference] that has an
 * [id][PackageManifest.Item.identifier] that matches the given [resource], or throws a [NoSuchElementException] if none
 * is found.
 */
operator fun PackageSpine.get(resource: PageResource): ItemReference = getReferenceOf(resource)

/**
 * Returns the first [itemref][ItemReference] that references the given [item], or throws [NoSuchElementException]
 * if none is found.
 */
operator fun PackageSpine.get(item: Item<*>): ItemReference = getReferenceOf(item)

/**
 * Returns whether or not this `spine` element contains any [itemref][ItemReference] elements that reference
 * an [item][PackageManifest.Item] that has a [id][PackageManifest.Item.identifier] that matches the given [resource].
 */
operator fun PackageSpine.contains(resource: PageResource): Boolean = hasReferenceOf(resource)

/**
 * Returns whether or not this `spine` element contains any [itemref][ItemReference] elements that reference the
 * given [item].
 */
operator fun PackageSpine.contains(item: Item<*>): Boolean = hasReferenceOf(item)

// -- GUIDE -- \\
/**
 * Adds a new [reference][Reference] instance based on the given [type], [reference] and [title] to this guide.
 *
 * Note that if a `reference` already exists under the given [type], then it will be overridden.
 *
 * @param [type] the `type` to store the element under
 * @param [reference] the [Resource] to inherit the [href][Resource.href] of
 * @param [title] the *(optional)* title
 */
operator fun PackageGuide.set(type: Type, reference: PageResource) {
    addReference(type, reference)
}

/**
 * Removes the [reference][Reference] element stored under the specified [type].
 *
 * @param [type] the `type` to remove
 */
operator fun PackageGuide.minusAssign(type: Type) {
    removeReference(type)
}

/**
 * Returns the [reference][Reference] stored under the given [type], or throws a [NoSuchElementException] if none
 * is found.
 */
operator fun PackageGuide.get(type: Type): Reference = getReference(type)

/**
 * Returns `true` if this guide has a reference with the given [type], `false` otherwise.
 */
operator fun PackageGuide.contains(type: Type): Boolean = hasType(type)

/**
 * Adds a new [reference][Reference] instance based on the given [customType], and [href] to this guide.
 *
 * Note that if a `reference` already exists under the given [customType], then it will be overridden.
 *
 * The [OPF][PackageDocument] specification states that;
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
 * Note that as guide references are *case-insensitive* the casing of the given [customType] does not matter when
 * attempting to return it from [getCustomReference][PackageGuide.getCustomReference] or removing it via
 * [removeCustomReference][PackageGuide.removeCustomReference].
 *
 * @param [customType] the custom type string
 * @param [reference] the [Resource] to inherit the [href][Resource.href] of
 */
operator fun PackageGuide.set(customType: String, reference: PageResource) {
    addCustomReference(customType, reference)
}

/**
 * Removes the [reference][Reference] element stored under the specified [customType].
 *
 * The [OPF][PackageDocument] specification states that;
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
 * Note that as guide references are *case-insensitive* the casing of the given [customType] does not matter,
 * meaning that invoking this function with `customType` as `"deStROyeR"` will remove the same `reference` as if
 * invoking it with `customType` as `"destroyer"` or any other casing variation of the same string.
 *
 * @param [customType] the custom type string
 */
operator fun PackageGuide.minusAssign(customType: String) {
    removeCustomReference(customType)
}

/**
 * Returns the [reference][Reference] stored under the given [customType], or throws a [NoSuchElementException] if
 * none is found.
 *
 * The [OPF][PackageDocument] specification states that;
 *
 * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
 * **must** begin with the string `'other.'`"
 *
 * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
 *
 * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
 * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
 * consistent across all functions that accept a `customType`.
 *
 * Note that as guide references are *case-insensitive* the casing given to this function does not matter, meaning
 * that invoking this function with `"deStROyeR"` will return the same result as if invoking it with `"destroyer"`
 * or any other casing variation of the same string.
 */
operator fun PackageGuide.get(customType: String): Reference = getCustomReference(customType)

/**
 * Returns `true` if this guide has a reference with the given [customType], `false` otherwise.
 *
 * The [OPF][PackageDocument] specification states that;
 *
 * >".. Other types **may** be used when none of the [predefined types][Type] are applicable; their names
 * **must** begin with the string `'other.'`"
 *
 * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
 *
 * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
 * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
 * consistent across all functions that accept a `customType`.
 *
 * Note that as guide references are *case-insensitive* the casing given to this function does not matter, meaning
 * that invoking this function with `"deStROyeR"` will return the same result as if invoking it with `"destroyer"`
 * or any other casing variation of the same string.
 */
operator fun PackageGuide.contains(customType: String): Boolean = hasCustomType(customType)

// -- BINDINGS -- \\

// -- COLLECTION -- \\

// -- TOURS -- \\