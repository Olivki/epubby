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

package dev.epubby.content

import dev.epubby.dublincore.DublinCoreReadError
import org.xbib.net.IRISyntaxException

public sealed interface ContentReadError {
    public data class MissingAttribute(val name: String, val path: String) : ContentReadError
    public data class MissingElement(val name: String, val path: String) : ContentReadError
    public data class MissingText(val path: String) : ContentReadError
    public data class UnknownReadingDirection(val direction: String) : ContentReadError
    public data class InvalidIri(val cause: IRISyntaxException) : ContentReadError
}

public sealed interface PackageDocumentReadError : ContentReadError

public sealed interface MetadataReadError : ContentReadError {
    public object MissingIdentifier : MetadataReadError
    public object MissingTitle : MetadataReadError
    public object MissingLanguage : MetadataReadError
    public data class DublinCoreError(val error: DublinCoreReadError) : MetadataReadError
}

public sealed interface ManifestReadError : ContentReadError {
    public object NoItemElements : ManifestReadError
}

public sealed interface SpineReadError : ContentReadError {
    public object NoItemRefElements : SpineReadError
    public data class InvalidLinearValue(val value: String) : SpineReadError
}

public sealed interface BindingsReadError : ContentReadError {
    public object NoMediaTypeElements : BindingsReadError
}

public sealed interface ToursReadError : ContentReadError {
    public object NoTourSiteElements : ToursReadError
}