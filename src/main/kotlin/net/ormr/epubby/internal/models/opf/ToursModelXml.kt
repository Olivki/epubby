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

package net.ormr.epubby.internal.models.opf

import dev.epubby.Epub2DeprecatedFeature
import dev.epubby.Epub2Feature
import dev.epubby.opf.OpfReadError
import dev.epubby.opf.OpfReadError.MissingAttribute
import dev.epubby.opf.OpfReadError.MissingElement
import dev.epubby.opf.ToursReadError.NoTourSiteElements
import net.ormr.epubby.internal.models.ModelXmlSerializer
import net.ormr.epubby.internal.models.opf.ToursModel.TourModel
import net.ormr.epubby.internal.models.opf.ToursModel.TourSiteModel
import net.ormr.epubby.internal.util.buildElement
import net.ormr.epubby.internal.util.effect
import org.jdom2.Element
import net.ormr.epubby.internal.Namespaces.OPF_NO_PREFIX as NAMESPACE

@OptIn(Epub2Feature::class, Epub2DeprecatedFeature::class)
internal object ToursModelXml : ModelXmlSerializer<OpfReadError>() {
    fun read(tours: Element) = effect {
        val entries = tours
            .children("tour", NAMESPACE)
            .map { readTour(it).bind() }
        ToursModel(
            entries = entries,
        )
    }

    private fun readTour(tour: Element) = effect {
        val sites = tour
            .children("site", NAMESPACE)
            .map { readTourSite(it).bind() }
        ensure(sites.isNotEmpty()) { NoTourSiteElements }
        TourModel(
            identifier = tour.attr("id").bind(),
            title = tour.attr("title").bind(),
            sites = sites,
        )
    }

    private fun readTourSite(site: Element) = effect {
        TourSiteModel(
            href = site.attr("href").bind(),
            title = site.attr("title").bind(),
        )
    }

    fun write(tours: ToursModel): Element = buildElement("tours", NAMESPACE) {
        addChildren(tours.entries, ::writeTour)
    }

    private fun writeTour(tour: TourModel): Element = buildElement("tour", NAMESPACE) {
        this["id"] = tour.identifier
        this["title"] = tour.title
        addChildren(tour.sites, ::writeTourSite)
    }

    private fun writeTourSite(site: TourSiteModel): Element = buildElement("site", NAMESPACE) {
        this["href"] = site.href
        this["title"] = site.title
    }

    override fun missingAttribute(name: String, path: String): OpfReadError = MissingAttribute(name, path)

    override fun missingElement(name: String, path: String): OpfReadError = MissingElement(name, path)

    override fun missingText(path: String): OpfReadError = error("'missingText' should never be used")
}