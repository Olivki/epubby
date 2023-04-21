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

import dev.epubby.ReadingDirection
import net.ormr.epubby.internal.dublincore.DublinCoreSubjectImpl

/**
 * The topic of the resource.
 *
 * Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended
 * best practice is to use a controlled vocabulary.
 */
public interface DublinCoreSubject : LocalizedDublinCore, NonRequiredDublinCore

@JvmName("newSubject")
public fun DublinCoreSubject(
    identifier: String? = null,
    direction: ReadingDirection? = null,
    language: String? = null,
    content: String?,
): DublinCoreSubject = DublinCoreSubjectImpl(
    identifier = identifier,
    direction = direction,
    language = language,
    content = content,
)