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

import dev.epubby.Epub2Feature
import net.ormr.epubby.internal.dublincore.DublinCoreIdentifierImpl

/**
 * An unambiguous reference to the resource within a given context.
 *
 * Recommended best practice is to identify the resource by means of a string conforming to a formal identification
 * system.
 */
public interface DublinCoreIdentifier : DublinCore {
    @Epub2Feature
    public var scheme: String?
}

@JvmName("newIdentifier")
public fun DublinCoreIdentifier(
    identifier: String? = null,
    scheme: String? = null,
    content: String?,
): DublinCoreIdentifier = DublinCoreIdentifierImpl(identifier = identifier, scheme = scheme, content = content)