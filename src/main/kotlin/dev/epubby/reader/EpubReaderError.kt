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

package dev.epubby.reader

import dev.epubby.version.EpubVersionParseError

public sealed interface EpubReaderError {
    // TODO: better name
    public data class FailedToReadAsZip(public val cause: Throwable) : EpubReaderError
    public data class InvalidVersion(public val error: EpubVersionParseError) : EpubReaderError
    public object MissingMetaInf : EpubReaderError
    public object MissingMetaInfContainer : EpubReaderError
    public object MissingMimeType : EpubReaderError
    public object CorruptMimeType : EpubReaderError
    public object MimeTypeContentMismatch : EpubReaderError
}