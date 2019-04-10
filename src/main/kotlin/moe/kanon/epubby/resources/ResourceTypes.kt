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

import moe.kanon.kommons.io.extension
import java.nio.file.Path

/**
 * Represents a 'type' of file, categorized by the mime-types of the file.
 *
 * This is roughly based on the
 * [Core Media Types](http://www.idpf.org/epub/301/spec/epub-publications.html#sec-publication-resources) paragraph of
 * the epub specification.
 *
 * @property [location] The name of the directory where any resources that are of `this` type should be stored.
 * @property [mimeTypes] A array containing the mime types that a resource of `this` type can be.
 */
enum class ResourceType(val location: String, vararg val mimeTypes: String) {
    /**
     * @see PageResource
     */
    PAGE("Text/", "application/xhtml+xml"),
    /**
     * @see StyleSheetResource
     */
    STYLE_SHEET("Styles/", "text/css"),
    /**
     * @see ImageResource
     */
    IMAGE("Images/", "image/gif", "image/jpeg", "image/png", "image/svg+xml"),
    /**
     * TODO: Make a specific [Resource] for this one
     * @see MiscResource
     */
    FONT("Fonts/", "application/vnd.ms-opentype", "application/font-woff"),
    /**
     * TODO: Maybe make a specific resource for this one?
     * @see MiscResource
     */
    AUDIO("Audio/", "audio/mpeg"),
    /**
     * TODO: Maybe make a specific resource for this one?
     * @see MiscResource
     */
    SCRIPTS("Scripts/", "text/javascript"),
    /**
     * TODO: Maybe make a specific resource for this one?
     * @see MiscResource
     */
    VIDEO("Video/", "audio/mp4"),
    /**
     * @see OpfResource
     */
    OPF("", "OPF"),
    /**
     * @see NcxResource
     */
    NCX("", "NCX"),
    /**
     * @see MiscResource
     */
    MISC("Misc/");
    
    companion object {
        /**
         * Returns the [ResourceType] that has an [extension][ResourceType.mimeTypes] that matches with the specified
         * [extension], or [MISC] if none is found.
         */
        @JvmStatic
        fun from(extension: String): ResourceType = values().find { extension.toUpperCase() in it.mimeTypes } ?: MISC
        
        /**
         * Returns the [ResourceType] that has an [extension][ResourceType.mimeTypes] that matches that of the given [file],
         * or [MISC] if none is found.
         */
        @JvmStatic
        fun from(file: Path): ResourceType = values().find { file.extension.toUpperCase() in it.mimeTypes } ?: MISC
    }
}

/**
 * Represents a known image file type that is supported by the epub specification.
 */
enum class ImageType {
    PNG, JPG, GIF, SVG, UNKNOWN;
    
    companion object {
        /**
         * Returns the [ImageType] that has an [extension][Path.extension] that matches with the specified
         * [extension], if none is found then [UNKNOWN] will be returned.
         */
        @JvmStatic
        fun from(extension: String): ImageType =
            values().find { it.name.equals(extension, true) } ?: UNKNOWN
        
        /**
         * Returns the [ImageType] that matches the [extension][Path.extension] of the given [file], if none is found
         * then [UNKNOWN] will be returned.
         */
        @JvmStatic
        fun from(file: Path): ImageType =
            values().find { it.name.equals(file.extension, true) } ?: UNKNOWN
    }
}