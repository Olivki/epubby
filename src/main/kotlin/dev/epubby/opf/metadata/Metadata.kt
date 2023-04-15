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

import com.google.common.net.MediaType
import dev.epubby.Epub3Feature
import dev.epubby.Epub3LegacyFeature
import dev.epubby.NonEmptyMutableList
import dev.epubby.dublincore.DublinCore
import dev.epubby.dublincore.LocalizedDublinCore
import dev.epubby.dublincore.NonRequiredDublinCore
import dev.epubby.property.PropertySet
import dev.epubby.property.Relationship
import org.xbib.net.IRI

public interface Metadata {
    public val identifiers: NonEmptyMutableList<DublinCore.Identifier>

    public val titles: NonEmptyMutableList<LocalizedDublinCore.Title>

    public val languages: NonEmptyMutableList<DublinCore.Language>

    public val dublinCoreElements: MutableList<NonRequiredDublinCore>

    @Epub3LegacyFeature
    public val opf2MetaEntries: MutableList<Opf2Meta>

    @Epub3Feature
    public val opf3MetaEntries: MutableList<Opf3Meta>

    @Epub3Feature
    public val links: MutableList<Link>

    public var primaryIdentifier: DublinCore.Identifier
    public var primaryTitle: LocalizedDublinCore.Title
    public var primaryLanguage: DublinCore.Language

    @Epub3Feature
    public interface Link {
        public var href: IRI

        @Epub3Feature
        public var relation: Relationship?

        public var mediaType: MediaType?

        public var identifier: String?

        @Epub3Feature
        public val properties: PropertySet

        @Epub3Feature
        public var refines: String?
    }
}