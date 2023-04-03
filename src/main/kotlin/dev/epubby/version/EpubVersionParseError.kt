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

package dev.epubby.version

public sealed interface EpubVersionParseError {
    public object NoVersion : EpubVersionParseError
    public data class InvalidMajor(val major: String) : EpubVersionParseError
    public data class InvalidMinor(val minor: String) : EpubVersionParseError
    public object MissingSeparator : EpubVersionParseError
    public object TooManySeparators : EpubVersionParseError
    public data class UnknownVersion(val major: Int, val minor: Int) : EpubVersionParseError
}