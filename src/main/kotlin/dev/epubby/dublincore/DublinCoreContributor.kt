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
import net.ormr.epubby.internal.dublincore.DublinCoreContributorImpl

/**
 * A contributor is an entity that is responsible for making contributions to the [Epub].
 *
 * Examples of a `Contributor` include a person, an organization, or a service. Typically, the name of a
 * `Contributor` should be used to indicate the entity.
 */
public interface DublinCoreContributor : LocalizedDublinCore, NonRequiredDublinCore {
    @Epub2Feature
    public var role: CreativeRole?

    @Epub2Feature
    public var fileAs: String?
}

@JvmName("newContributor")
public fun DublinCoreContributor(
    identifier: String? = null,
    direction: ReadingDirection? = null,
    language: String? = null,
    role: CreativeRole? = null,
    fileAs: String? = null,
    content: String?,
): DublinCoreContributor = DublinCoreContributorImpl(
    identifier = identifier,
    direction = direction,
    language = language,
    role = role,
    fileAs = fileAs,
    content = content,
)