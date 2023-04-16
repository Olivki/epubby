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

package dev.epubby.prefix

import dev.epubby.Epub3Feature
import org.xbib.net.IRI

// TODO: better name
/**
 * Represents a [Prefix] that has a guaranteed `iri` mapping.
 *
 * @see [UnknownPrefix]
 */
@Epub3Feature
public sealed interface ResolvedPrefix : Prefix {
    override val iri: IRI
}