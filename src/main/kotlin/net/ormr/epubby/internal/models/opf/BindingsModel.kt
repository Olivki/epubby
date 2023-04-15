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

import dev.epubby.Epub3DeprecatedFeature
import dev.epubby.Epub3Feature
import net.ormr.epubby.internal.models.SerializedName

// https://idpf.org/epub/301/spec/epub-publications-20140626.html#sec-bindings-elem
@Epub3Feature
@Epub3DeprecatedFeature
@SerializedName("bindings")
internal data class BindingsModel(
    val mediaTypes: List<MediaTypeModel>,
) {
    @SerializedName("mediaType")
    data class MediaTypeModel(
        @SerializedName("media-type")
        val mediaType: String,
        val handler: String,
    )
}