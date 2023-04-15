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

package net.ormr.epubby.internal.models.opf

import dev.epubby.Epub3Feature
import dev.epubby.Epub3LegacyFeature
import dev.epubby.ReadingDirection
import net.ormr.epubby.internal.models.SerializedName

// https://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.4
// https://www.w3.org/publishing/epub3/epub-packages.html#elemdef-opf-spine
@SerializedName("spine")
internal data class SpineModel(
    @SerializedName("id")
    val identifier: String?,
    @SerializedName("page-progression-direction")
    val pageProgressionDirection: ReadingDirection?,
    @SerializedName("toc")
    @property:Epub3LegacyFeature
    val toc: String?,
    val references: List<ItemRefModel>,
) {
    // TODO: support fallback chain?
    // https://www.w3.org/publishing/epub3/epub-packages.html#elemdef-spine-itemref
    @SerializedName("itemref")
    data class ItemRefModel(
        @SerializedName("idref")
        val idRef: String,
        @SerializedName("id")
        val identifier: String?,
        @SerializedName("linear")
        val isLinear: Boolean, // yes/no
        @property:Epub3Feature
        val properties: String?,
    )
}