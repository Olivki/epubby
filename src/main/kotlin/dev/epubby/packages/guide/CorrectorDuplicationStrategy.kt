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
 * Determines how the [PackageGuide.correctCustomTypes] function should behave when encountering a duplicate.
 */
// TODO: name
enum class CorrectorDuplicationStrategy {
    /**
     * The already existing [GuideReference] will be replaced with the `GuideReference` instance created from
     * the [CustomGuideReference].
     */
    REPLACE_EXISTING,

    /**
     * The [CustomGuideReference] that is being corrected will be removed from the guide, and the already existing
     * [GuideReference] will be left as is.
     */
    REMOVE_CUSTOM,

    /**
     * Nothing will be done.
     *
     * This means that both the [GuideReference] and [CustomGuideReference] instances will still exist afterwards.
     */
    DO_NOTHING;
}