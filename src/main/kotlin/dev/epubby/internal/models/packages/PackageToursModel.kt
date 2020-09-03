/*
 * Copyright 2019-2020 Oliver Berg
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

package dev.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.*
import dev.epubby.internal.elementOf
import dev.epubby.internal.getAttributeValueOrThrow
import dev.epubby.internal.models.SerializedName
import dev.epubby.packages.PackageTours
import dev.epubby.utils.toNonEmptyList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jdom2.Element
import dev.epubby.internal.Namespaces.OPF as NAMESPACE

@SerializedName("tours")
data class PackageToursModel internal constructor(val entries: PersistentList<TourModel>) {
    @JvmSynthetic
    internal fun toElement(): Element = elementOf("tours", NAMESPACE) {
        entries.forEach { entry -> it.addContent(entry.toElement()) }
    }

    @JvmSynthetic
    internal fun toPackageTours(book: Book): PackageTours {
        val tours = entries.map { it.toTour(book) }.toMutableList()
        return PackageTours(book, tours)
    }

    @SerializedName("tour")
    data class TourModel internal constructor(
        @SerializedName("id")
        val identifier: String,
        val title: String,
        val sites: PersistentList<SiteModel>,
    ) {
        @JvmSynthetic
        internal fun toElement(): Element = elementOf("tour", NAMESPACE) {
            it.setAttribute("id", identifier)
            it.setAttribute("title", title)
            sites.forEach { site -> it.addContent(site.toElement()) }
        }

        @JvmSynthetic
        internal fun toTour(book: Book): PackageTours.Tour {
            val sites = sites.map { it.toSite(book) }.toNonEmptyList()
            return PackageTours.Tour(book, identifier, title, sites)
        }

        @SerializedName("site")
        data class SiteModel internal constructor(val href: String, val title: String) {
            @JvmSynthetic
            internal fun toElement(): Element = elementOf("site", NAMESPACE) {
                it.setAttribute("href", href)
                it.setAttribute("title", title)
            }

            @JvmSynthetic
            internal fun toSite(book: Book): PackageTours.Tour.Site = PackageTours.Tour.Site(book, href, title)

            internal companion object {
                @JvmSynthetic
                internal fun fromElement(element: Element): SiteModel {
                    val href = element.getAttributeValueOrThrow("href")
                    val title = element.getAttributeValueOrThrow("title")
                    return SiteModel(href, title)
                }

                @JvmSynthetic
                internal fun fromSite(origin: PackageTours.Tour.Site): SiteModel = SiteModel(origin.href, origin.title)
            }
        }

        internal companion object {
            @JvmSynthetic
            internal fun fromElement(element: Element): TourModel {
                val identifier = element.getAttributeValueOrThrow("id")
                val title = element.getAttributeValueOrThrow("title")
                val sites = element.getChildren("site", element.namespace)
                    .map { SiteModel.fromElement(it) }
                    .ifEmpty { throw MalformedBookException.forMissing("tour", "site") }
                    .toPersistentList()
                return TourModel(identifier, title, sites)
            }

            @JvmSynthetic
            internal fun fromTour(origin: PackageTours.Tour): TourModel {
                val sites = origin.sites.map { SiteModel.fromSite(it) }.toPersistentList()
                return TourModel(origin.identifier, origin.title, sites)
            }
        }
    }

    internal companion object {
        private val LOGGER: InlineLogger = InlineLogger(PackageToursModel::class)

        @JvmSynthetic
        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageToursModel {
            val tours = element.getChildren("tour", element.namespace)
                .tryMap { TourModel.fromElement(it) }
                .mapToValues(LOGGER, strictness)
                .toPersistentList()
            return PackageToursModel(tours)
        }

        @JvmSynthetic
        internal fun fromPackageTours(origin: PackageTours): PackageToursModel {
            val entries = origin.entries.map { TourModel.fromTour(it) }.toPersistentList()
            return PackageToursModel(entries)
        }
    }
}