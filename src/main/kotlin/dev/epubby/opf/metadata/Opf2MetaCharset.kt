/*
 * Copyright 2023 Oliver Berg
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
@file:JvmName("Opf2Metas")

package dev.epubby.opf.metadata

import dev.epubby.Epub3LegacyFeature
import dev.epubby.xml.XmlAttribute
import net.ormr.epubby.internal.opf.metadata.Opf2MetaCharsetImpl

@Epub3LegacyFeature
public interface Opf2MetaCharset : Opf2Meta {
    public var scheme: String?
    public var charset: String
}

@Epub3LegacyFeature
@JvmName("newCharset")
public fun Opf2MetaCharset(
    charset: String,
    scheme: String? = null,
    extraAttributes: List<XmlAttribute> = emptyList(),
): Opf2MetaCharset = Opf2MetaCharsetImpl(
    scheme = scheme,
    charset = charset,
    extraAttributes = extraAttributes.toMutableList(),
)