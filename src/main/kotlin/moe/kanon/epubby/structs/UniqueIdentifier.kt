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

package moe.kanon.epubby.structs

import java.util.UUID

sealed class UniqueIdentifier<out T> {
    abstract val value: T

    // TODO: make the names less verbose
    class DataObjectIdentifier internal constructor(override val value: DOI) : UniqueIdentifier<DOI>()
    class InternationalStandardBookNumber internal constructor(override val value: ISBN) : UniqueIdentifier<ISBN>()
    class UniversalUniqueIdentifier internal constructor(override val value: UUID) : UniqueIdentifier<UUID>()

    companion object {
        @JvmStatic
        fun from(doi: DOI): DataObjectIdentifier = DataObjectIdentifier(doi)

        @JvmStatic
        fun from(isbn: ISBN): InternationalStandardBookNumber = InternationalStandardBookNumber(isbn)

        @JvmStatic
        fun from(uuid: UUID): UniversalUniqueIdentifier = UniversalUniqueIdentifier(uuid)

        @JvmStatic
        fun parse(input: String): UniqueIdentifier<*> = when {
            else -> throw IllegalArgumentException("Given 'input' <$input> is not a valid DOI, ISBN or UUID")
        }
    }
}