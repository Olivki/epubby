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
import net.ormr.epubby.internal.util.getChildObjects
import org.xbib.net.IRI

@Epub3Feature
public sealed class VocabularyPrefix(iri: String) : ResolvedPrefix {
    public object Manifest : VocabularyPrefix("http://idpf.org/epub/vocab/package/item/#")

    public object MetadataLink : VocabularyPrefix("http://idpf.org/epub/vocab/package/link/#")

    public object MetadataMeta : VocabularyPrefix("http://idpf.org/epub/vocab/package/meta/#")

    public object Spine : VocabularyPrefix("http://idpf.org/epub/vocab/package/itemref/#")

    /**
     * Returns an empty string, as vocabulary prefixes do not have a `name`.
     */
    override val name: String?
        get() = null

    override val iri: IRI = IRI.create(iri)

    public companion object {
        private val prefixes by lazy { getChildObjects<VocabularyPrefix>().toList() }

        /**
         * Returns `true` if `this` prefix has a `iri` that points to the same location as any of the
         * [VocabularyPrefix] instances, otherwise `false`.
         */
        public fun isVocabularyPrefix(prefix: Prefix): Boolean = when (prefix) {
            is VocabularyPrefix -> true
            else -> prefixes.any { it.iri == prefix.iri }
        }
    }
}