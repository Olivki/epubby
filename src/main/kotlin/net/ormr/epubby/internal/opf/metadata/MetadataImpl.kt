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

package net.ormr.epubby.internal.opf.metadata

import dev.epubby.Epub3Feature
import dev.epubby.Epub3LegacyFeature
import dev.epubby.dublincore.DublinCoreIdentifier
import dev.epubby.dublincore.DublinCoreLanguage
import dev.epubby.dublincore.DublinCoreTitle
import dev.epubby.dublincore.NonRequiredDublinCore
import dev.epubby.opf.metadata.Metadata
import dev.epubby.opf.metadata.MetadataLink
import dev.epubby.opf.metadata.Opf2Meta
import dev.epubby.opf.metadata.Opf3Meta
import net.ormr.epubby.internal.MutableOpfElementList
import net.ormr.epubby.internal.NonEmptyMutableOpfElementList

@OptIn(Epub3Feature::class, Epub3LegacyFeature::class)
internal class MetadataImpl(
    override val identifiers: NonEmptyMutableOpfElementList<DublinCoreIdentifier>,
    override val titles: NonEmptyMutableOpfElementList<DublinCoreTitle>,
    override val languages: NonEmptyMutableOpfElementList<DublinCoreLanguage>,
    override val dublinCoreElements: MutableOpfElementList<NonRequiredDublinCore>,
    override val opf2MetaEntries: MutableList<Opf2Meta>,
    override val opf3MetaEntries: MutableOpfElementList<Opf3Meta<*>>,
    override val links: MutableOpfElementList<MetadataLink>,
) : Metadata {
    override var primaryIdentifier: DublinCoreIdentifier
        get() = TODO("Not yet implemented")
        set(value) {}
    override var primaryTitle: DublinCoreTitle
        get() = TODO("Not yet implemented")
        set(value) {}
    override var primaryLanguage: DublinCoreLanguage
        get() = TODO("Not yet implemented")
        set(value) {}
}