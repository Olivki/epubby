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

@file:JvmName("___kt_type_alias_resource_filter")

package dev.epubby.resources

/**
 * A [ResourceVisitor] that determines whether a [ManifestResource] implementations [accept][ManifestResource.accept]
 * function should be invoked by another `ResourceVisitor`.
 *
 * When a `visitXXX` function of this visitor in invoked, if it returns `true` then the other visitor will visit that
 * resource, otherwise if it returns `false` then the other visitor will *not* visit that resource.
 */
// TODO: turn this into its own standalone interface
typealias ResourceFilter = ResourceVisitor<Boolean>

/**
 * Utility class that contains various default implementations of a [ResourceFilter].
 */
object ResourceFilters {
    /**
     * A [ResourceFilter] that lets all resource instances through.
     */
    @JvmField
    val ALLOW_ALL: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = true
    }

    /**
     * A [ResourceFilter] that lets no resource instances through.
     */
    @JvmField
    val DENY_ALL: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = false
    }

    /**
     * A [ResourceFilter] that only lets [ExternalResource] instances through.
     */
    @JvmField
    val ONLY_REMOTE: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is ExternalResource
    }

    /**
     * A [ResourceFilter] that only lets instances that are subclasses of [LocalResource] through.
     */
    @JvmField
    val ONLY_LOCAL: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is LocalResource
    }

    /**
     * A [ResourceFilter] that only lets [AudioResource] instances through.
     */
    @JvmField
    val ONLY_AUDIO: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is AudioResource
    }

    /**
     * A [ResourceFilter] that only lets instances that are subclasses of [CustomResource] through.
     */
    @JvmField
    val ONLY_CUSTOM: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is CustomResource
    }

    /**
     * A [ResourceFilter] that only lets [FontResource] instances through.
     */
    @JvmField
    val ONLY_FONTS: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is FontResource
    }

    /**
     * A [ResourceFilter] that only lets [ImageResource] instances through.
     */
    @JvmField
    val ONLY_IMAGES: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is ImageResource
    }

    /**
     * A [ResourceFilter] that only lets [MiscResource] instances through.
     */
    @JvmField
    val ONLY_MISC: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is MiscResource
    }

    /**
     * A [ResourceFilter] that only lets [NcxResource] instances through.
     */
    @JvmField
    val ONLY_NCX: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is NcxResource
    }

    /**
     * A [ResourceFilter] that only lets [PageResource] instances through.
     */
    @JvmField
    val ONLY_PAGES: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is PageResource
    }

    /**
     * A [ResourceFilter] that only lets [PageResource] that have a non `null` [page][PageResource.getPageOrNull]
     * instance through.
     */
    @JvmField
    val ONLY_SPINE_PAGES: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean =
            resource is PageResource && resource.getPageOrNull() != null
    }

    /**
     * A [ResourceFilter] that only lets [ScriptResource] instances through.
     */
    @JvmField
    val ONLY_SCRIPTS: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is ScriptResource
    }

    /**
     * A [ResourceFilter] that only lets [StyleSheetResource] instances through.
     */
    @JvmField
    val ONLY_STYLE_SHEETS: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is StyleSheetResource
    }

    /**
     * A [ResourceFilter] that only lets [VideoResource] instances through.
     */
    @JvmField
    val ONLY_VIDEOS: ResourceFilter = object : DefaultResourceVisitor<Boolean> {
        override fun getDefaultValue(resource: ManifestResource): Boolean = resource is VideoResource
    }
}