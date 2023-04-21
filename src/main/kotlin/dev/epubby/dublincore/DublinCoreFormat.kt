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

package dev.epubby.dublincore

/**
 * The file format, physical medium, or dimensions of the resource.
 *
 * Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary
 * such as the list of [Internet Media Types](http://www.iana.org/assignments/media-types/).
 */
public data class DublinCoreFormat(
    override var identifier: String? = null,
    override var content: String?,
) : DublinCore, NonRequiredDublinCore