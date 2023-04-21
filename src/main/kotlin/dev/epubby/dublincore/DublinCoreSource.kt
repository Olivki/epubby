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

import net.ormr.epubby.internal.dublincore.DublinCoreSourceImpl

/**
 * A related resource from which the described resource is derived.
 *
 * The described resource may be derived from the related resource in whole or in part. Recommended best practice
 * is to identify the related resource by means of a string conforming to a formal identification system.
 */
public interface DublinCoreSource : DublinCore, NonRequiredDublinCore

@JvmName("newSource")
public fun DublinCoreSource(
    identifier: String? = null,
    content: String?,
): DublinCoreSource = DublinCoreSourceImpl(identifier = identifier, content = content)