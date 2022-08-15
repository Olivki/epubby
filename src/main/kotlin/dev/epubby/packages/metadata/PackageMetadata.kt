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
import dev.epubby.dublincore.*
import dev.epubby.dublincore.DublinCore.Identifier
import dev.epubby.dublincore.DublinCore.Language
import dev.epubby.dublincore.LocalizedDublinCore.Creator
import dev.epubby.dublincore.LocalizedDublinCore.Title
import dev.epubby.internal.utils.buildPersistentList
import dev.epubby.utils.NonEmptyList
import kotlinx.collections.immutable.PersistentList
import krautils.collections.asUnmodifiableList

class PackageMetadata(
    override val epub: Epub,
    val identifiers: NonEmptyList<Identifier>,
    val titles: NonEmptyList<Title>,
    val languages: NonEmptyList<Language>,
    private val _dublinCoreEntries: MutableList<DublinCore> = arrayListOf(),
    val opf2MetaEntries: MutableList<Opf2Meta> = arrayListOf(),
    val opf3MetaEntries: MutableList<Opf3Meta> = arrayListOf(),
    val links: MutableList<MetadataLink> = arrayListOf(),
) : EpubElement {
    override val elementName: String
        get() = "PackageMetadata"

    var primaryIdentifier: Identifier
        get() = identifiers.head
        set(value) {
            identifiers[0] = value
        }

    var primaryTitle: Title
        get() = titles.head
        set(value) {
            titles[0] = value
        }

    var primaryLanguage: Language
        get() = languages.head
        set(value) {
            languages[0] = value
        }

    /**
     * The primary author of the epub, or `null` if no primary author is defined.
     *
     * Note that this looks for the first instance of [LocalizedDublinCore.Creator] that has the
     * [author][CreativeRole.AUTHOR] [role][LocalizedDublinCore.Creator.role].
     */
    var primaryAuthor: Creator?
        get() = _dublinCoreEntries
            .asSequence()
            .filterIsInstance<Creator>()
            .firstOrNull { it.isAuthor }
        set(value) {
            val index = primaryAuthor?.let(_dublinCoreEntries::indexOf) ?: -1

            if (index > 0) {
                if (value != null) {
                    _dublinCoreEntries[index] = value
                } else {
                    _dublinCoreEntries.removeAt(index)
                }
            } else {
                if (value != null) {
                    _dublinCoreEntries += value
                } else {
                    throw NoSuchElementException("Can't remove non-existent creator.")
                }
            }
        }

    val dublinCoreEntries: List<DublinCore>
        get() = _dublinCoreEntries.asUnmodifiableList()

    val allDublinCoreEntries: PersistentList<DublinCore>
        get() = buildPersistentList {
            addAll(identifiers)
            addAll(titles)
            addAll(languages)
            addAll(_dublinCoreEntries)
        }

    fun addDublinCore(entry: DublinCore) {
        when (entry) {
            is Identifier -> identifiers.add(entry)
            is Title -> titles.add(entry)
            is Language -> languages.add(entry)
            else -> _dublinCoreEntries.add(entry)
        }
    }

    fun removeDublinCore(entry: DublinCore): Boolean = when (entry) {
        is Identifier -> identifiers.remove(entry)
        is Title -> titles.remove(entry)
        is Language -> languages.remove(entry)
        else -> _dublinCoreEntries.remove(entry)
    }

    /**
     * Invokes the [accept][DublinCore.accept] function of all the dublin core entries in this `metadata` with the
     * given [visitor].
     *
     * The `accept` function of each resource will only be invoked if the given [filter] returns `true` for that
     * resource.
     *
     * @param [visitor] the visitor to collect the results from
     * @param [filter] the filter to check before visiting the resource with [visitor],
     * [ALLOW_ALL][DublinCoreFilters.ALLOW_ALL] by default
     *
     * @see [collectDublinCoreEntries]
     */
    @JvmOverloads
    fun visitDublinCoreEntries(visitor: DublinCoreVisitor<*>, filter: DublinCoreFilter = DublinCoreFilters.ALLOW_ALL) {
        for (identifier in identifiers) {
            if (!(identifier.accept(filter))) {
                continue
            }

            identifier.accept(visitor)
        }

        for (title in titles) {
            if (!(title.accept(filter))) {
                continue
            }

            title.accept(visitor)
        }

        for (language in languages) {
            if (!(language.accept(filter))) {
                continue
            }

            language.accept(visitor)
        }

        for (dublinCore in _dublinCoreEntries) {
            if (!(dublinCore.accept(filter))) {
                continue
            }

            dublinCore.accept(visitor)
        }
    }

    /**
     * Returns a list of the results of invoking the [accept][DublinCore.accept] function of all the dublin core
     * entries in this `metadata` with the given [visitor].
     *
     * The `accept` function of each resource will only be invoked if the given [filter] returns `true` for that
     * resource.
     *
     * @param [visitor] the visitor to collect the results from
     * @param [filter] the filter to check before visiting the resource with [visitor],
     * [ALLOW_ALL][DublinCoreFilters.ALLOW_ALL] by default
     *
     * @see [visitDublinCoreEntries]
     */
    @JvmOverloads
    fun <R> collectDublinCoreEntries(
        visitor: DublinCoreVisitor<R>,
        filter: DublinCoreFilter = DublinCoreFilters.ALLOW_ALL
    ): PersistentList<R> = buildPersistentList {
        for (identifier in identifiers) {
            if (!(identifier.accept(filter))) {
                continue
            }

            val result = identifier.accept(visitor)

            if (result != null && result != Unit) {
                add(identifier.accept(visitor))
            }
        }

        for (title in titles) {
            if (!(title.accept(filter))) {
                continue
            }

            val result = title.accept(visitor)

            if (result != null && result != Unit) {
                add(title.accept(visitor))
            }
        }

        for (language in languages) {
            if (!(language.accept(filter))) {
                continue
            }

            val result = language.accept(visitor)

            if (result != null && result != Unit) {
                add(language.accept(visitor))
            }
        }

        for (dublinCore in _dublinCoreEntries) {
            if (!(dublinCore.accept(filter))) {
                continue
            }

            val result = dublinCore.accept(visitor)

            if (result != null && result != Unit) {
                add(dublinCore.accept(visitor))
            }
        }
    }

    override fun toString(): String =
        "PackageMetadata(identifiers=$identifiers, titles=$titles, languages=$languages, dublinCoreEntries=$_dublinCoreEntries, opf2MetaEntries=$opf2MetaEntries, opf3MetaEntries=$opf3MetaEntries, links=$links)"
}