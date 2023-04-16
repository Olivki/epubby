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

package dev.epubby.opf

import dev.epubby.dublincore.DublinCoreReadError
import dev.epubby.version.EpubVersionParseError
import org.xbib.net.IRISyntaxException

public sealed interface OpfReadError {
    public data class MissingAttribute(val name: String, val path: String) : OpfReadError
    public data class MissingElement(val name: String, val path: String) : OpfReadError
    public data class MissingText(val path: String) : OpfReadError
    public data class UnknownReadingDirection(val direction: String) : OpfReadError
    public data class InvalidIri(val cause: IRISyntaxException) : OpfReadError
    public data class InvalidMediaType(val mediaType: String) : OpfReadError
}

public sealed interface PackageDocumentReadError : OpfReadError {
    public data class InvalidVersion(val error: EpubVersionParseError) : PackageDocumentReadError
}

public sealed interface MetadataReadError : OpfReadError {
    public object MissingIdentifier : MetadataReadError
    public object MissingTitle : MetadataReadError
    public object MissingLanguage : MetadataReadError
    public data class DublinCoreError(val error: DublinCoreReadError) : MetadataReadError
}

public sealed interface ManifestReadError : OpfReadError {
    public object NoItemElements : ManifestReadError
}

public sealed interface SpineReadError : OpfReadError {
    public object NoItemRefElements : SpineReadError
    public data class InvalidLinearValue(val value: String) : SpineReadError
}

public sealed interface BindingsReadError : OpfReadError {
    public object NoMediaTypeElements : BindingsReadError
}

public sealed interface ToursReadError : OpfReadError {
    public object NoTourSiteElements : ToursReadError
}