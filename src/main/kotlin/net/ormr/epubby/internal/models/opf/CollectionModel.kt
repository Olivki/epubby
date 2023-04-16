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
import net.ormr.epubby.internal.models.SerializedName
import org.jdom2.Element

// https://www.w3.org/publishing/epub3/epub-packages.html#elemdef-collection
// TODO: support 'collection' element
@Epub3Feature
@SerializedName("collection")
internal class CollectionModel(val element: Element)