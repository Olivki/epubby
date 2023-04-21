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

package net.ormr.epubby.internal.dublincore

import dev.epubby.dublincore.DublinCoreSource
import net.ormr.epubby.internal.identifierDelegate
import net.ormr.epubby.internal.opf.OpfImpl

internal class DublinCoreSourceImpl(
    identifier: String? = null,
    override var content: String?,
) : DublinCoreSource, DublinCoreImpl {
    override var identifier: String? by identifierDelegate(identifier)

    override var opf: OpfImpl? = null
}