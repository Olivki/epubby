/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.properties

import dev.epubby.prefixes.Prefix
import dev.epubby.prefixes.VocabularyPrefix
import java.net.URI

object VocabularyPrefixes {
    @JvmField
    val MANIFEST: Prefix = VocabularyPrefix(URI.create("http://idpf.org/epub/vocab/package/item/#"))

    @JvmField
    val METADATA_LINK: Prefix = VocabularyPrefix(URI.create("http://idpf.org/epub/vocab/package/link/#"))

    @JvmField
    val METADATA_META: Prefix = VocabularyPrefix(URI.create("http://idpf.org/epub/vocab/package/meta/#"))

    @JvmField
    val SPINE: Prefix = VocabularyPrefix(URI.create("http://idpf.org/epub/vocab/package/itemref/#"))
}