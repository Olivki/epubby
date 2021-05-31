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

package dev.epubby.resources

import dev.epubby.packages.PackageManifest

/**
 * A visitor for [LocalResource] implementations.
 *
 * @see [PackageManifest.visitResources]
 * @see [PackageManifest.collectResources]
 */
interface ResourceVisitor<R> {
    /**
     * Gets invoked at the start of the visiting.
     */
    // TODO: name?
    fun begin(manifest: PackageManifest) {

    }

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitExternal(resource: ExternalResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitPage(resource: PageResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitStyleSheet(resource: StyleSheetResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitImage(resource: ImageResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitFont(resource: FontResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitAudio(resource: AudioResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitScript(resource: ScriptResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitVideo(resource: VideoResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitMisc(resource: MiscResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitNcx(resource: NcxResource): R

    /**
     * Returns a value that's appropriate for the given [resource].
     */
    fun visitCustom(resource: CustomResource): R

    /**
     * Gets invoked at the end of the visiting.
     */
    // TODO: name?
    fun end(manifest: PackageManifest) {

    }
}

/**
 * A [ResourceVisitor] implementation that by default returns the result of invoking [getDefaultValue] function on
 * every invocation of its `visitXXX` functions.
 */
interface DefaultResourceVisitor<R> : ResourceVisitor<R> {
    /**
     * Returns a value that may, or may not, be dependent on the given [resource].
     *
     * By default, all `visitXXX` functions of this interface invoke this function when they are invoked.
     */
    fun getDefaultValue(resource: ManifestResource): R

    override fun visitExternal(resource: ExternalResource): R = getDefaultValue(resource)

    override fun visitPage(resource: PageResource): R = getDefaultValue(resource)

    override fun visitStyleSheet(resource: StyleSheetResource): R = getDefaultValue(resource)

    override fun visitImage(resource: ImageResource): R = getDefaultValue(resource)

    override fun visitFont(resource: FontResource): R = getDefaultValue(resource)

    override fun visitAudio(resource: AudioResource): R = getDefaultValue(resource)

    override fun visitScript(resource: ScriptResource): R = getDefaultValue(resource)

    override fun visitVideo(resource: VideoResource): R = getDefaultValue(resource)

    override fun visitMisc(resource: MiscResource): R = getDefaultValue(resource)

    override fun visitNcx(resource: NcxResource): R = getDefaultValue(resource)

    override fun visitCustom(resource: CustomResource): R = getDefaultValue(resource)
}

/**
 * A [ResourceVisitor] implementation that returns `Unit` on every invocation of its `visitXXX` functions.
 *
 * Implementations of this interface should use the [visitLocalResources][PackageManifest.visitResources] over the
 * [collectLocalResources][PackageManifest.collectResources] function when mass-visiting all local resources in a
 * manifest.
 *
 * **NOTE:** Implementing this interface from the Java side is less than ideal due Java not being able to differentiate
 * between the functions and their bridge methods, it is therefore recommended to instead implement the base
 * `LocalResourceVisitor` interface with `R` set to `Void` and return `null` on every invocation.
 *
 * Example:
 * ```java
 *  public interface ExampleVisitor implements ResourceVisitor<Void> {
 *      @Override
 *      public Void visitPage(final PageResource page) {
 *          // do stuff
 *          return null;
 *      }
 *  }
 */
interface ResourceVisitorUnit : ResourceVisitor<Unit> {
    override fun visitExternal(resource: ExternalResource): Unit = Unit

    override fun visitPage(resource: PageResource): Unit = Unit

    override fun visitStyleSheet(resource: StyleSheetResource): Unit = Unit

    override fun visitImage(resource: ImageResource): Unit = Unit

    override fun visitFont(resource: FontResource): Unit = Unit

    override fun visitAudio(resource: AudioResource): Unit = Unit

    override fun visitScript(resource: ScriptResource): Unit = Unit

    override fun visitVideo(resource: VideoResource): Unit = Unit

    override fun visitMisc(resource: MiscResource): Unit = Unit

    override fun visitNcx(resource: NcxResource): Unit = Unit

    override fun visitCustom(resource: CustomResource): Unit = Unit
}