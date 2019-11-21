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

package moe.kanon.epubby.structs.props.vocabs

import moe.kanon.epubby.structs.props.BasicPropertyPrefix
import moe.kanon.epubby.structs.props.Property
import moe.kanon.epubby.structs.props.PropertyPrefix
import moe.kanon.epubby.utils.internal.findProperty

/**
 * Represents the [manifest properties vocabulary](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-packages.html#app-item-properties-vocab).
 *
 * Used for resolving any [item properties][Manifest.Item.properties] that are missing prefixes.
 */
enum class ManifestVocabulary(override val reference: String) : Property {
    /**
     * The `cover-image` property identifies the described Publication Resource as the cover image for the Publication.
     */
    COVER_IMAGE("cover-image"),
    /**
     * The `mathml` property indicates that the described Publication Resource contains one or more instances of MathML
     * markup.
     */
    MATH_HTML("mahtml"),
    /**
     * The `nav` property indicates that the described Publication Resource constitutes the EPUB Navigation Document of
     * the given Rendition.
     */
    NAV("nav"),
    /**
     * The `remote-resources` property indicates that the described Publication Resource contains one or more internal
     * references to other Publication Resources that are located outside of the EPUB Container.
     *
     * Refer to [Publication Resource Locations](https://w3c.github.io/publ-epub-revision/epub32/spec/epub-spec.html#sec-resource-locations)
     * for more information.
     */
    REMOTE_RESOURCES("remote-resources"),
    /**
     * The `scripted` property indicates that the described Publication Resource is a Scripted Content Document
     * *(i.e., contains scripted content and/or HTML form elements)*.
     */
    SCRIPTED("scripted"),
    /**
     * The `svg` property indicates that the described Publication Resource embeds one or more instances of SVG markup.
     *
     * This property *MUST* be set when SVG markup is included directly in the resource and *MAY* be set when the SVG
     * is referenced from the resource *(e.g., from an `HTML` img, `object` or `iframe` element)*.
     */
    SVG("svg");

    // TODO: Update documentation with references to the actual implementations of the concepts they speak of
    //       And references to what each property actually applies to

    override val prefix: PropertyPrefix =
        BasicPropertyPrefix(null, "http://idpf.org/epub/vocab/package/item/#")

    companion object {
        @JvmStatic fun fromReference(reference: String): ManifestVocabulary = findProperty(reference)
    }
}