/*
 * Copyright 2019-2020 Oliver Berg
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

import dev.epubby.internal.utils.buildPersistentList
import kotlinx.collections.immutable.PersistentList

// TODO: find a better name? maybe something like 'Remapper'?
/**
 * Handles the storing of corrections of "misspelled" official reference types to their proper type.
 *
 * A common "misspelled" reference type is using `copyright` instead of the actual `copyright-page` value. All new
 * [GuideReferenceCorrector] instance will have a correction for that by default.
 *
 * Note that corrections can only be *added* and can never be *removed* from a corrector, likewise, one can not
 * override already existing corrections. This means that once a correction has been added to a corrector, it'll stay
 * there for its entire lifetime.
 */
class GuideReferenceCorrector internal constructor() {
    private val corrections: MutableMap<String, ReferenceType> = hashMapOf()

    init {
        addCorrection("copyright", ReferenceType.COPYRIGHT_PAGE)
    }

    /**
     * Adds a correction to convert any [customType] instances to the given [type] to this corrector.
     *
     * Once a correction has been added to a corrector, it can never be removed, and if a correction already exists
     * for `customType` then a [CorrectionAlreadyExistsException] will be thrown.
     *
     * @throws [CorrectionAlreadyExistsException] if there already exists a correction for the given [customType]
     *
     * @see [hasCorrection]
     */
    fun addCorrection(customType: String, type: ReferenceType) {
        if (customType !in corrections) {
            corrections[customType] = type
        } else {
            throw CorrectionAlreadyExistsException(customType)
        }
    }

    /**
     * Returns the correction for the given [customType], or throws a [NoSuchElementException] if none exists.
     */
    fun getCorrection(customType: String): ReferenceType =
        getCorrectionOrNull(customType) ?: throw NoSuchElementException("No correction exists for type '$customType'.")

    /**
     * Returns the correction for the given [customType], or `null` if none exists.
     */
    fun getCorrectionOrNull(customType: String): ReferenceType? = corrections[customType]

    /**
     * Returns a list of all the `customType`s that map to the given [type].
     *
     * If there are no known mappings for `type` then the returned list will be empty.
     */
    fun getCorrectionsFor(type: ReferenceType): PersistentList<String> = buildPersistentList {
        for ((key, value) in corrections) {
            if (value != type) {
                continue
            }

            add(key)
        }
    }

    /**
     * Returns `true` if this corrector has a correction for the given [customType], otherwise `false`.
     */
    fun hasCorrection(customType: String): Boolean = customType in corrections
}

class CorrectionAlreadyExistsException internal constructor(customType: String) :
    RuntimeException("A correction for the custom type '$customType' already exists.")