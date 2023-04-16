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

package dev.epubby

import dev.epubby.opf.metadata.Opf3MetaCreativeRoleFactory
import dev.epubby.opf.metadata.Opf3MetaFactory
import dev.epubby.property.Property
import net.ormr.epubby.internal.Properties.MARC_RELATORS

@OptIn(Epub3Feature::class)
public sealed class EpubConfig(
    public val opf3MetaFactories: Map<Property, Opf3MetaFactory<*, *>>,
) {
    public companion object Default : EpubConfig(
        opf3MetaFactories = mapOf(
            MARC_RELATORS to Opf3MetaCreativeRoleFactory,
        ),
    )
}