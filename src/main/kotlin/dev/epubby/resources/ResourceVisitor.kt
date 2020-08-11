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

interface ResourceVisitor<R : Any> {
    fun visitPage(resource: PageResource): R

    fun visitStyleSheet(resource: StyleSheetResource): R

    fun visitImage(resource: ImageResource): R

    fun visitFont(resource: FontResource): R

    fun visitAudio(resource: AudioResource): R

    fun visitScript(resource: ScriptResource): R

    fun visitVideo(resource: VideoResource): R

    fun visitMisc(resource: MiscResource): R

    // this is the function that custom 'Resource' implementations should be invoking from the 'accept' function
    @JvmDefault
    fun visitOther(resource: Resource): R =
        throw UnsupportedOperationException("'visitOther' is not supported by default")
}