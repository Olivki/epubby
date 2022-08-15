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

package dev.epubby.resources

import dev.epubby.Epub
import dev.epubby.files.RegularFile

// TODO: remove 'CustomResource'
abstract class CustomResource(epub: Epub, identifier: String, file: RegularFile) :
    LocalResource(epub, identifier, file) {
    /**
     * Returns the result of invoking the [visitCustom][ResourceVisitor.visitCustom] function of the given
     * [visitor].
     */
    final override fun <R> accept(visitor: ResourceVisitor<R>): R = visitor.visitCustom(this)

    override fun toString(): String = "CustomResource(identifier='$identifier', mediaType=$mediaType, file='$file')"
}