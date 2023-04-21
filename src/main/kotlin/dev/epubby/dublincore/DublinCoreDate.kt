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
import net.ormr.epubby.internal.dublincore.DublinCoreDateImpl

/**
 * A point or period of time associated with an event in the lifecycle of the resource.
 *
 * `Date` may be used to express temporal information at any level of granularity.
 */
public interface DublinCoreDate : DublinCore, NonRequiredDublinCore {
    @Epub2Feature
    public var event: DateEvent?
}

@JvmName("newDate")
@OptIn(Epub2Feature::class)
public fun DublinCoreDate(
    identifier: String? = null,
    event: DateEvent? = null,
    content: String?,
): DublinCoreDate = DublinCoreDateImpl(identifier = identifier, event = event, content = content)