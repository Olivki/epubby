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

package dev.epubby.packages

import dev.epubby.Epub
import dev.epubby.EpubElement
import dev.epubby.EpubVersion
import dev.epubby.internal.IntroducedIn
import dev.epubby.internal.MarkedAsDeprecated

@IntroducedIn(version = EpubVersion.EPUB_3_0)
@MarkedAsDeprecated(`in` = EpubVersion.EPUB_3_2)
class PackageBindings(override val epub: Epub) : EpubElement {
    override val elementName: String
        get() = "PackageBindings"

    class MediaType
}