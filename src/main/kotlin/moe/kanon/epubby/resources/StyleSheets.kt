/*
 * Copyright 2019 Oliver Berg
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

package moe.kanon.epubby.resources

import com.helger.css.decl.CascadingStyleSheet
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import moe.kanon.epubby.Book
import moe.kanon.kommons.collections.asUnmodifiable
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.func.getValueOrNone

/**
 * A repository containing all the [StyleSheet] instances used by the given [book].
 */
class StyleSheetRepository(val book: Book) : Iterable<StyleSheet> {
    private val styleSheets: MutableMap<String, StyleSheet> = LinkedHashMap()

    /**
     * Returns the [StyleSheet] stored under the given [key], or throws a [NoSuchElementException] if none is found.
     */
    fun getStyleSheet(key: String): StyleSheet =
        styleSheets[key] ?: throw NoSuchElementException("No style-sheet found under key <$key>")

    /**
     * Returns the [StyleSheet] stored under the given [key], or [None] if none is found.
     */
    fun getStyleSheetOrNone(key: String): Option<StyleSheet> = styleSheets.getValueOrNone(key)

    override fun iterator(): Iterator<StyleSheet> = styleSheets.values.iterator().asUnmodifiable()
}

/**
 * Returns the [StyleSheet] stored under the given [key], or throws a [NoSuchElementException] if none is found.
 */
operator fun StyleSheetRepository.get(key: String): StyleSheet = this.getStyleSheet(key)

/**
 * A simple wrapper around a [CascadingStyleSheet] to make it work nicer within our system.
 *
 * @property [book] The [Book] instance that `this` stylesheet is tied to.
 * @property [resource] The [Resource] instance created for the file that [delegate] is referencing.
 * @property [delegate] The underlying [CascadingStyleSheet] that `this` class is wrapped around.
 */
data class StyleSheet(val book: Book, val resource: StyleSheetResource, val delegate: CascadingStyleSheet) {
    @get:JvmSynthetic internal val _pageReferences: MutableList<Page> = ArrayList()

    /**
     * Returns a list of all [Page] instances that use `this` stylesheet.
     */
    val pageReferences: ImmutableList<Page> get() = _pageReferences.toImmutableList()
}