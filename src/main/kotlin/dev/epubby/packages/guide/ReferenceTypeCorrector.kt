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

/**
 * A service interface that handles the correction of misspelling / errors that should be converted to a
 * [ReferenceType].
 */
interface ReferenceTypeCorrector {
    /**
     * Returns the equivalent [ReferenceType] for the given [type], or `null` if no equivalent exists.
     */
    fun getEquivalent(type: String): ReferenceType?
}