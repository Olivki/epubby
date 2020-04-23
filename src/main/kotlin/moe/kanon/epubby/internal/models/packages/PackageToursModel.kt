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

package moe.kanon.epubby.internal.models.packages

import com.github.michaelbull.logging.InlineLogger
import moe.kanon.epubby.Book
import moe.kanon.epubby.MalformedBookException
import moe.kanon.epubby.ParseStrictness
import moe.kanon.epubby.internal.elementOf
import moe.kanon.epubby.internal.getAttributeValueOrThrow
import moe.kanon.epubby.internal.models.SerialName
import moe.kanon.epubby.mapToValues
import moe.kanon.epubby.packages.PackageTours
import moe.kanon.epubby.tryMap
import org.apache.logging.log4j.kotlin.loggerOf
import org.jdom2.Element
import moe.kanon.epubby.internal.Namespaces.OPF as NAMESPACE

@SerialName("tours")
internal data class PackageToursModel internal constructor(internal val entries: List<Tour>) {
    internal fun toElement(): Element = elementOf("tours", NAMESPACE) {
        entries.forEach { entry -> it.addContent(entry.toElement()) }
    }

    internal fun toPackageTours(book: Book): PackageTours {
        TODO("'toPackageTours' operation is not implemented yet.")
    }

    @SerialName("tour")
    data class Tour(
        @SerialName("id") internal val identifier: String,
        internal val title: String,
        internal val sites: List<Site>
    ) {
        internal fun toElement(): Element = elementOf("tour", NAMESPACE) {
            it.setAttribute("id", identifier)
            it.setAttribute("title", title)
            sites.forEach { site -> it.addContent(site.toElement()) }
        }

        internal fun toTour(book: Book): PackageTours.Tour {
            TODO("'toTour' operation is not implemented yet.")
        }

        @SerialName("site")
        data class Site(internal val href: String, internal val title: String) {
            internal fun toElement(): Element = elementOf("site", NAMESPACE) {
                it.setAttribute("href", href)
                it.setAttribute("title", title)
            }

            internal fun toSite(book: Book): PackageTours.Tour.Site {
                TODO("'toSite' operation is not implemented yet.")
            }

            internal companion object {
                internal fun fromElement(element: Element): Site {
                    val href = element.getAttributeValueOrThrow("href")
                    val title = element.getAttributeValueOrThrow("title")
                    return Site(href, title)
                }

                internal fun fromSite(origin: PackageTours.Tour.Site): Site {
                    TODO("'fromSite' operation is not implemented yet.")
                }
            }
        }

        internal companion object {
            internal fun fromElement(element: Element): Tour {
                val identifier = element.getAttributeValueOrThrow("id")
                val title = element.getAttributeValueOrThrow("title")
                val sites = element.getChildren("site", element.namespace)
                    .map { Site.fromElement(it) }
                    .ifEmpty { throw MalformedBookException.forMissing("tour", "site") }
                return Tour(identifier, title, sites)
            }

            internal fun fromTour(origin: PackageTours.Tour): Tour {
                TODO("'fromTour' operation is not implemented yet.")
            }
        }
    }

    internal companion object {
        private val logger = InlineLogger(PackageToursModel::class)

        internal fun fromElement(element: Element, strictness: ParseStrictness): PackageToursModel {
            val tours = element.getChildren("tour", element.namespace)
                .tryMap { Tour.fromElement(it) }
                .mapToValues(logger, strictness)
            return PackageToursModel(tours)
        }

        internal fun fromPackageTours(origin: PackageTours): PackageTours {
            TODO("'fromPackageTours' operation is not implemented yet.")
        }
    }
}