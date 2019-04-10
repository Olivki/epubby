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

@file:Suppress("NOTHING_TO_INLINE")

package moe.kanon.epubby.utils

import com.github.zafarkhaja.semver.Version
import moe.kanon.epubby.EpubFormat

/**
 * Type-alias for [Version] for easier use within the code base.
 */
typealias SemVer = Version

@DslMarker
annotation class FakeKeywordMarker

/**
 * Compares `this` version to the [version][EpubFormat.version] of the specified [format].
 */
operator fun SemVer.compareTo(format: EpubFormat): Int = this.compareTo(format.version)

/**
 * Returns whether or not `this` [SemVer] instance is inside of the specified [range], with the
 * [endInclusive][ClosedRange.endInclusive] not being inclusive.
 */
@FakeKeywordMarker
inline infix fun SemVer.inside(range: ClosedRange<SemVer>): Boolean = this >= range.start && this < range.endInclusive