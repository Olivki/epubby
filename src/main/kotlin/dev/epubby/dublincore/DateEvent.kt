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

package dev.epubby.dublincore

import dev.epubby.Epub2Feature
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.models.dublincore.DateEventSerializer

@Epub2Feature
@Serializable(with = DateEventSerializer::class)
public class DateEvent private constructor(public val name: String) {
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is DateEvent -> false
        else -> name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = name

    public companion object {
        private val cache = hashMapOf<String, DateEvent>()

        /**
         * Represents the date when the epub was created.
         */
        public val CREATION: DateEvent = of("creation")

        /**
         * Represents the date when the epub was published.
         */
        public val PUBLICATION: DateEvent = of("publication")

        /**
         * Represents the date when the epub was last modified.
         */
        public val MODIFICATION: DateEvent = of("modification")

        public fun of(name: String): DateEvent = cache.getOrPut(name) { DateEvent(name) }
    }
}