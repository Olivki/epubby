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

package dev.epubby.opf.guide

import dev.epubby.Epub3LegacyFeature

@Epub3LegacyFeature
public sealed interface GuideReference<T : Any> {
    public val type: T
    public var reference: ReferenceReference // TODO: PageResource
    public var title: String?

    public companion object {
        public fun of(type: GuideReferenceType, reference: ReferenceReference): DefaultGuideReference = TODO()

        public fun of(type: String, reference: ReferenceReference): GuideReference<*> = TODO()
    }
}