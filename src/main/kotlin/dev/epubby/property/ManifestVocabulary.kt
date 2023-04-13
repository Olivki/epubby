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

package dev.epubby.property

import dev.epubby.Epub3Feature
import dev.epubby.prefix.VocabularyPrefix
import dev.epubby.resource.Resource

/**
 * Represents the [manifest properties vocabulary](https://www.w3.org/publishing/epub3/epub-packages.html#app-item-properties-vocab).
 */
@Epub3Feature
public enum class ManifestVocabulary(override val reference: String) : VocabularyProperty {
    /**
     * The `cover-image` property identifies the described [resource][Resource] as the cover image for the epub.
     */
    COVER_IMAGE("cover-image"),

    /**
     * The `mathml` property indicates that the described [resource][Resource] contains one or more instances of MathML
     * markup.
     */
    MATH_HTML("mahtml"),

    /**
     * The `nav` property indicates that the described [resource][Resource] constitutes the EPUB Navigation Document of
     * the given Rendition.
     */
    NAV("nav"),

    /**
     * The `remote-resources` property indicates that the described [resource][Resource] contains one or more internal
     * references to other `resources` that are located outside of the EPUB Container.
     *
     * Refer to [Publication Resource Locations](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-resource-locations)
     * for more information.
     */
    REMOTE_RESOURCES("remote-resources"),

    /**
     * The `scripted` property indicates that the described [resource][Resource] is a Scripted Content Document
     * *(i.e., contains scripted content and/or HTML form elements)*.
     */
    SCRIPTED("scripted"),

    /**
     * The `svg` property indicates that the described [resource][Resource] embeds one or more instances of SVG markup.
     *
     * This property *MUST* be set when SVG markup is included directly in the resource and *MAY* be set when the SVG
     * is referenced from the resource *(e.g., from an `HTML` img, `object` or `iframe` element)*.
     */
    SVG("svg");

    override val prefix: VocabularyPrefix
        get() = VocabularyPrefix.Manifest
}