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

package dev.epubby.opf.metadata

import dev.epubby.Epub3Feature
import dev.epubby.Epub3LegacyFeature
import dev.epubby.NonEmptyMutableList
import dev.epubby.dublincore.DublinCoreIdentifier
import dev.epubby.dublincore.DublinCoreLanguage
import dev.epubby.dublincore.DublinCoreTitle
import dev.epubby.dublincore.NonRequiredDublinCore

public interface Metadata {
    public val identifiers: NonEmptyMutableList<DublinCoreIdentifier>

    public val titles: NonEmptyMutableList<DublinCoreTitle>

    public val languages: NonEmptyMutableList<DublinCoreLanguage>

    public val dublinCoreElements: MutableList<NonRequiredDublinCore>

    @Epub3LegacyFeature
    public val opf2MetaEntries: MutableList<Opf2Meta>

    @Epub3Feature
    public val opf3MetaEntries: MutableList<Opf3Meta<*>>

    @Epub3Feature
    public val links: MutableList<MetadataLink>

    public var primaryIdentifier: DublinCoreIdentifier
    public var primaryTitle: DublinCoreTitle
    public var primaryLanguage: DublinCoreLanguage
}