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

package dev.epubby.dublincore

import dev.epubby.Epub

/**
 * The spatial or temporal topic of the resource, the spatial applicability of the resource, or the jurisdiction
 * under which the [Epub] is relevant.
 *
 * Spatial topic and spatial applicability may be a named place or a location specified by its geographic
 * coordinates. Temporal topic may be a named period, date, or date range. A jurisdiction may be a named
 * administrative entity or a geographic place to which the resource applies. Recommended best practice is to use a
 * controlled vocabulary such as the
 * [Thesaurus of Geographic Names](http://www.getty.edu/research/tools/vocabulary/tgn/index.html). Where
 * appropriate, named places or time periods can be used in preference to numeric identifiers such as sets of
 * coordinates or date ranges.
 */
public interface DublinCoreCoverage : LocalizedDublinCore, NonRequiredDublinCore