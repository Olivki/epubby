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

@file:JvmMultifileClass
@file:JvmName("DublinCores")

package dev.epubby.dublincore

import dev.epubby.Epub
import dev.epubby.Epub2Feature
import dev.epubby.ReadingDirection
import dev.epubby.marc.CreativeRole
import net.ormr.epubby.internal.dublincore.DublinCoreCreatorImpl

/**
 * The entity primarily responsible for making the [Epub].
 *
 * Do note that by "primarily responsible" it means the one who *originally* wrote the *contents* of the `Epub`,
 * not the person who made the epub.
 *
 * Examples of a `Creator` include a person, an organization, or a service. Typically, the name of a `Creator`
 * should be used to indicate the entity.
 */
public interface DublinCoreCreator : LocalizedDublinCore, NonRequiredDublinCore {
    @Epub2Feature
    public var role: CreativeRole?

    @Epub2Feature
    public var fileAs: String?
}

@JvmName("newCreator")
public fun DublinCoreCreator(
    identifier: String? = null,
    direction: ReadingDirection? = null,
    language: String? = null,
    role: CreativeRole? = null,
    fileAs: String? = null,
    content: String?,
): DublinCoreCreator = DublinCoreCreatorImpl(
    identifier = identifier,
    direction = direction,
    language = language,
    role = role,
    fileAs = fileAs,
    content = content,
)