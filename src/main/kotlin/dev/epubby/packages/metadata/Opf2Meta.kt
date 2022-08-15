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

package dev.epubby.packages.metadata

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.resources.ManifestResource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

sealed class Opf2Meta : EpubElement {
    /**
     * A map of any global attributes defined on the `meta` element.
     *
     * The returned map is *not* modifiable, and the only way for it to be populated is if this `meta` element
     * originates from a parsed `meta` element that contained global attributes. User created `meta` elements should
     * never contain any global attributes.
     */
    abstract val globalAttributes: PersistentMap<String, String>

    abstract var scheme: String?

    final override val elementName: String
        get() = "PackageManifest.Opf2Meta"

    /**
     * Returns the result of invoking the appropriate `visitXXX` function of the given [visitor].
     */
    abstract fun <R> accept(visitor: Opf2MetaVisitor<R>): R

    /**
     * TODO: documentation
     */
    class HttpEquiv @JvmOverloads constructor(
        override val epub: Epub,
        var httpEquiv: String,
        var content: String,
        override var scheme: String? = null,
        override val globalAttributes: PersistentMap<String, String> = persistentMapOf()
    ) : Opf2Meta() {
        /**
         * Returns the result of invoking the [visitHttpEquiv][Opf2MetaVisitor.visitHttpEquiv] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: Opf2MetaVisitor<R>): R = visitor.visitHttpEquiv(this)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is HttpEquiv -> false
            httpEquiv != other.httpEquiv -> false
            content != other.content -> false
            scheme != other.scheme -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = httpEquiv.hashCode()
            result = 31 * result + content.hashCode()
            result = 31 * result + (scheme?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = "HttpEquiv(httpEquiv='$httpEquiv', content='$content', scheme=$scheme)"
    }

    /**
     * TODO: documentation
     *
     * @property [name] TODO
     * @property [content] TODO
     */
    // TODO: rename to something better than just 'Name'
    class Name @JvmOverloads constructor(
        override val epub: Epub,
        var name: String,
        var content: String,
        override var scheme: String? = null,
        override val globalAttributes: PersistentMap<String, String> = persistentMapOf()
    ) : Opf2Meta() {
        /**
         * Returns `true` if this `meta` element provides additional metadata for the given [resource], otherwise
         * `false`.
         */
        // TODO: name
        fun isFor(resource: ManifestResource): Boolean = content == resource.identifier

        /**
         * Returns the result of invoking the [visitName][Opf2MetaVisitor.visitName] function of the given [visitor].
         */
        override fun <R> accept(visitor: Opf2MetaVisitor<R>): R = visitor.visitName(this)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Name -> false
            name != other.name -> false
            content != other.content -> false
            scheme != other.scheme -> false
            globalAttributes != other.globalAttributes -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + content.hashCode()
            result = 31 * result + (scheme?.hashCode() ?: 0)
            result = 31 * result + globalAttributes.hashCode()
            return result
        }

        override fun toString(): String = "Name(name='$name', content='$content', scheme=$scheme)"
    }

    /**
     * TODO: documentation
     */
    class Charset @JvmOverloads constructor(
        override val epub: Epub,
        var charset: java.nio.charset.Charset,
        override var scheme: String? = null,
        override val globalAttributes: PersistentMap<String, String> = persistentMapOf()
    ) : Opf2Meta() {
        /**
         * Returns the result of invoking the [visitCharset][Opf2MetaVisitor.visitCharset] function of the given
         * [visitor].
         */
        override fun <R> accept(visitor: Opf2MetaVisitor<R>): R = visitor.visitCharset(this)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Charset -> false
            charset != other.charset -> false
            scheme != other.scheme -> false
            globalAttributes != other.globalAttributes -> false
            else -> true
        }

        override fun hashCode(): Int {
            var result = charset.hashCode()
            result = 31 * result + (scheme?.hashCode() ?: 0)
            result = 31 * result + globalAttributes.hashCode()
            return result
        }

        override fun toString(): String = "Charset(charset=$charset, scheme=$scheme)"
    }
}