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

import net.ormr.epubby.internal.dublincore.DublinCoreLanguageImpl

/**
 * A language of the resource.
 *
 * Recommended best practice is to use a controlled vocabulary such as
 * [RFC 4646](http://www.ietf.org/rfc/rfc4646.txt).
 */
public interface DublinCoreLanguage : DublinCore

@JvmName("newLanguage")
public fun DublinCoreLanguage(
    identifier: String? = null,
    content: String?,
): DublinCoreLanguage = DublinCoreLanguageImpl(identifier = identifier, content = content)