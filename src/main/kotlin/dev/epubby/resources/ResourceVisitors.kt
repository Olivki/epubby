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
     * Returns a value that's appropriate for the given [remote] resource.
     */
    fun visitRemote(remote: RemoteResource): R

    /**
     * Returns a value that's appropriate for the given [page] resource.
     */
    fun visitPage(page: PageResource): R

    /**
     * Returns a value that's appropriate for the given [styleSheet] resource.
     */
    fun visitStyleSheet(styleSheet: StyleSheetResource): R

    /**
     * Returns a value that's appropriate for the given [image] resource.
     */
    fun visitImage(image: ImageResource): R

    /**
     * Returns a value that's appropriate for the given [font] resource.
     */
    fun visitFont(font: FontResource): R

    /**
     * Returns a value that's appropriate for the given [audio] resource.
     */
    fun visitAudio(audio: AudioResource): R

    /**
     * Returns a value that's appropriate for the given [script] resource.
     */
    fun visitScript(script: ScriptResource): R

    /**
     * Returns a value that's appropriate for the given [video] resource.
     */
    fun visitVideo(video: VideoResource): R

    /**
     * Returns a value that's appropriate for the given [misc] resource.
     */
    fun visitMisc(misc: MiscResource): R

    /**
     * Returns a value that's appropriate for the given [custom] resource.
     */
    fun visitCustom(custom: CustomResource): R
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

    override fun visitRemote(remote: RemoteResource): R = getDefaultValue(remote)

    override fun visitPage(page: PageResource): R = getDefaultValue(page)

    override fun visitStyleSheet(styleSheet: StyleSheetResource): R = getDefaultValue(styleSheet)

    override fun visitImage(image: ImageResource): R = getDefaultValue(image)

    override fun visitFont(font: FontResource): R = getDefaultValue(font)

    override fun visitAudio(audio: AudioResource): R = getDefaultValue(audio)

    override fun visitScript(script: ScriptResource): R = getDefaultValue(script)

    override fun visitVideo(video: VideoResource): R = getDefaultValue(video)

    override fun visitMisc(misc: MiscResource): R = getDefaultValue(misc)

    override fun visitCustom(custom: CustomResource): R = getDefaultValue(custom)
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
    override fun visitRemote(remote: RemoteResource): Unit = Unit

    override fun visitPage(page: PageResource): Unit = Unit

    override fun visitStyleSheet(styleSheet: StyleSheetResource): Unit = Unit

    override fun visitImage(image: ImageResource): Unit = Unit

    override fun visitFont(font: FontResource): Unit = Unit

    override fun visitAudio(audio: AudioResource): Unit = Unit

    override fun visitScript(script: ScriptResource): Unit = Unit

    override fun visitVideo(video: VideoResource): Unit = Unit

    override fun visitMisc(misc: MiscResource): Unit = Unit

    override fun visitCustom(custom: CustomResource): Unit = Unit
}