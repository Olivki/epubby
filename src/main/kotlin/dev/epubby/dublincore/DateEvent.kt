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

package dev.epubby.dublincore

class DateEvent private constructor(val name: String) {
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is DateEvent -> false
        name != other.name -> false
        else -> true
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "DateEvent(name='$name')"

    companion object {
        private val CACHE: MutableMap<String, DateEvent> = hashMapOf()

        private fun getOrCreate(name: String): DateEvent = CACHE.getOrPut(name) { DateEvent(name) }

        /**
         * Represents the date when the e-epub was created.
         */
        @JvmField
        val CREATION: DateEvent = getOrCreate("creation")

        /**
         * Represents the date when the e-epub was published.
         */
        @JvmField
        val PUBLICATION: DateEvent = getOrCreate("publication")

        /**
         * Represents the date when the e-epub was last modified.
         */
        @JvmField
        val MODIFICATION: DateEvent = getOrCreate("modification")

        @JvmStatic
        fun of(name: String): DateEvent = getOrCreate(name)
    }
}