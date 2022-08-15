/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.packages.guide

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.EpubVersion.EPUB_3_0
import dev.epubby.internal.MarkedAsLegacy
import dev.epubby.packages.PackageDocument
import dev.epubby.packages.guide.CorrectorDuplicationStrategy.*
import dev.epubby.resources.PageResource
import krautils.collections.asUnmodifiableMap
import krautils.collections.emptyEnumMap
import krautils.collections.getOrThrow

// TODO: silently map any custom references that use actual proper reference types to a GuideReference instead of
//       throwing an exception when attempting to add them?

@MarkedAsLegacy(`in` = EPUB_3_0)
class PackageGuide(
    override val epub: Epub,
    private val _references: MutableMap<ReferenceType, GuideReference> = emptyEnumMap(),
    private val _customReferences: MutableMap<String, CustomGuideReference> = hashMapOf(),
) : EpubElement {
    val corrector: GuideReferenceCorrector by lazy { GuideReferenceCorrector() }

    /**
     * Returns a unmodifiable view of the `references` of this guide.
     */
    val references: Map<ReferenceType, GuideReference>
        get() = _references.asUnmodifiableMap()

    /**
     * Returns a unmodifiable view of the `customReferences` of this guide.
     */
    val customReferences: Map<String, CustomGuideReference>
        get() = _customReferences.asUnmodifiableMap()

    override val elementName: String
        get() = "PackageGuide"

    /**
     * Adds a new [reference][GuideReference] instance based on the given [type], [reference] and [title] to this guide.
     *
     * Note that if a `reference` already exists under the given [type], then it will be overridden.
     *
     * @param [type] the `type` to store the element under
     * @param [reference] the [PageResource] to inherit the [href][PageResource.href] of
     * @param [title] the *(optional)* title
     *
     * @return the newly created `reference` element
     */
    @JvmOverloads
    fun addReference(type: ReferenceType, reference: PageResource, title: String? = null): GuideReference {
        val ref = GuideReference(epub, type, reference, title)
        _references[type] = ref
        return ref
    }

    /**
     * Removes the [reference][GuideReference] element stored under the specified [type].
     *
     * @param [type] the `type` to remove
     */
    fun removeReference(type: ReferenceType) {
        _references -= type
    }

    /**
     * Returns the [reference][GuideReference] stored under the given [type], or throws a [NoSuchElementException] if
     * none is found.
     */
    fun getReference(type: ReferenceType): GuideReference =
        _references.getOrThrow(type) { "No reference found with type'$type'" }

    /**
     * Returns the [reference][GuideReference] stored under the given [type], or `null` if none is found.
     */
    fun getReferenceOrNull(type: ReferenceType): GuideReference? = _references[type]

    /**
     * Remaps any [CustomGuideReference] to [GuideReference] in this guide using the [corrector] of this guide.
     *
     * Any `CustomGuideReference` instances in this guide whose [type][CustomGuideReference.type] property is known by
     * the `corrector` will turned into a [GuideReference] instance.
     *
     * For example, say this guide has the `CustomGuideReference(type = "copyright")` instance, after invoking this
     * function that instance will be turned into a `GuideReference(type = ReferenceType.COPYRIGHT_PAGE)` instance.
     *
     * When correcting, if a corrected custom type happens to map to a [ReferenceType] that is already defined in this
     * guide, then the result of invoking the given [duplicationResolver] function will be used to determine what
     * course of action should be taken.
     *
     * @param [duplicationResolver] the function that determines what [CorrectorDuplicationStrategy] that should be
     * used when encountering a duplicate when correcting, [DO_NOTHING][CorrectorDuplicationStrategy.DO_NOTHING] by
     * default
     */
    // TODO: find a better more descriptive name of what this does?
    @JvmOverloads
    fun correctCustomTypes(duplicationResolver: CorrectorDuplicationResolver = DEFAULT_RESOLVER) {
        for ((customType, customReference) in _customReferences.toMap()) {
            if (corrector.hasCorrection(customType)) {
                val type = corrector.getCorrection(customType)

                if (type in _references) {
                    val knownReference = getReference(type)

                    when (duplicationResolver(customReference, knownReference)) {
                        REPLACE_EXISTING -> {
                            val reference = GuideReference(epub, type, customReference.reference, customReference.title)
                            LOGGER.debug { "Replacing $knownReference with $reference created from $customReference" }
                            _customReferences -= customType
                            _references[type] = reference
                        }

                        REMOVE_CUSTOM -> {
                            LOGGER.debug { "Removing $customReference in favour of $knownReference" }
                            _customReferences -= customType
                        }

                        DO_NOTHING -> {}
                    }
                } else {
                    val reference = GuideReference(epub, type, customReference.reference, customReference.title)
                    LOGGER.debug { "Remapping $customReference to $reference" }
                    _customReferences -= customReference.type
                    _references[type] = reference
                }
            }
        }
    }

    /**
     * Adds a new [reference][GuideReference] instance based on the given [type], [reference] and [title] to this guide.
     *
     * Note that if a `reference` already exists under the given [type], then it will be overridden.
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][ReferenceType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `(customType = "tn")` the system will *not* store the created
     * `reference` under the key `"tn"`, instead it will store the `reference` under the key `"other.tn"`. This
     * behaviour is consistent across all functions that accept a `customType`.
     *
     * @throws [IllegalArgumentException] if the given [type] matches an already known [type][ReferenceType]
     */
    @JvmOverloads
    fun addCustomReference(
        type: String,
        reference: PageResource,
        title: String? = null,
    ): CustomGuideReference {
        // TODO: remove this and instead just convert them at the serialization phase?
        require(!ReferenceType.isKnownType(type)) { "'type' matches an officially defined reference type ($type)" }
        val ref = CustomGuideReference(epub, type, reference, title)
        _customReferences[type] = ref
        return ref
    }

    /**
     * Removes the [reference][GuideReference] element stored under the specified [type].
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][ReferenceType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* remove a `reference` stored
     * under the key `"tn"`, instead it removes a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     */
    fun removeCustomReference(type: String) {
        _customReferences -= type
    }

    /**
     * Returns the [reference][GuideReference] stored under the given [type], or throws a [NoSuchElementException] if
     * none is found.
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][ReferenceType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     */
    fun getCustomReference(type: String): CustomGuideReference =
        _customReferences.getOrThrow(type) { "No custom reference found with type '$type'" }

    /**
     * Returns the [reference][GuideReference] stored under the given [type], or `null` if none is found.
     *
     * The [OPF][PackageDocument] specification states that;
     *
     * >".. Other types **may** be used when none of the [predefined types][ReferenceType] are applicable; their names
     * **must** begin with the string `'other.'`"
     *
     * To make sure that this rule is followed, `"other."` will be prepended to the given `customType`.
     *
     * This means that if this function is invoked with `("tn")` the system does *not* look for a `reference` stored
     * under the key `"tn"`, instead it looks for a `reference` stored under the key `"other.tn"`. This behaviour is
     * consistent across all functions that accept a `customType`.
     */
    fun getCustomReferenceOrNull(type: String): CustomGuideReference? = _customReferences[type]

    override fun toString(): String =
        "PackageGuide(references=$_references, customReferences=$_customReferences)"

    private companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageGuide::class)

        private val DEFAULT_RESOLVER: CorrectorDuplicationResolver = { _, _ -> DO_NOTHING }
    }
}