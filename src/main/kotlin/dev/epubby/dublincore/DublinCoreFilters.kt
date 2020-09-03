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

@file:JvmName("___kt_type_alias_dc_filter")

package dev.epubby.dublincore

/**
 * A [DublinCoreVisitor] that determines whether a [DublinCore] implementations [accept][DublinCore.accept]
 * function should be invoked by another `ResourceVisitor`.
 *
 * When a `visitXXX` function of this visitor in invoked, if it returns `true` then the other visitor will visit that
 * resource, otherwise if it returns `false` then the other visitor will *not* visit that resource.
 */
typealias DublinCoreFilter = DublinCoreVisitor<Boolean>

/**
 * Utility class that contains various default implementations of a [DublinCoreFilter].
 */
object DublinCoreFilters {
    /**
     * A [DublinCoreFilter] that lets all dublin core elements through.
     */
    @JvmField
    val ALLOW_ALL: DublinCoreFilter = object : DefaultDublinCoreVisitor<Boolean> {
        override fun getDefaultValue(dublinCore: DublinCore): Boolean = true
    }

    /**
     * A [DublinCoreFilter] that lets no dublin core elements through.
     */
    @JvmField
    val DENY_ALL: DublinCoreFilter = object : DefaultDublinCoreVisitor<Boolean> {
        override fun getDefaultValue(dublinCore: DublinCore): Boolean = false
    }

    /**
     * A [DublinCoreFilter] that only lets through dublin core elements that are *not* subclasses of
     * [LocalizedDublinCore].
     */
    @JvmField
    val ONLY_NON_LOCALIZED: DublinCoreFilter = object : DefaultDublinCoreVisitor<Boolean> {
        override fun getDefaultValue(dublinCore: DublinCore): Boolean = dublinCore !is LocalizedDublinCore
    }

    /**
     * A [DublinCoreFilter] that only lets through dublin core elements that are subclasses of
     * [LocalizedDublinCore].
     */
    @JvmField
    val ONLY_LOCALIZED: DublinCoreFilter = object : DefaultDublinCoreVisitor<Boolean> {
        override fun getDefaultValue(dublinCore: DublinCore): Boolean = dublinCore is LocalizedDublinCore
    }
}