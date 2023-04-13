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

package net.ormr.epubby.internal.models.content

import dev.epubby.Epub3Feature
import dev.epubby.ReadingDirection
import net.ormr.epubby.internal.models.SerializedName

// https://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm
// https://www.w3.org/publishing/epub3/epub-packages.html
// TODO: 'id' attributes must be unique within the document scope
//       this means that all elements in a 'Package' when an 'id' is defined
//       it needs to be fully unique
@SerializedName("package")
internal data class PackageModel(
    val version: String,
    @SerializedName("unique-identifier")
    val uniqueIdentifier: String,
    @SerializedName("dir")
    val readingDirection: ReadingDirection?,
    @SerializedName("id")
    val identifier: String?,
    @SerializedName("prefix")
    @property:Epub3Feature
    val prefixes: String?,
    @SerializedName("xml:lang")
    val language: String?,
    val metadata: MetadataModel,
    val manifest: ManifestModel,
    val spine: SpineModel,
    // TODO: guide, bindings, collection and tours
)