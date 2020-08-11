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

package dev.epubby

/**
 * Thrown to indicate that some part of a file that's being parsed into a [Book] is malformed.
 */
open class MalformedBookException(message: String?, cause: Throwable? = null) : Exception(message, cause) {
    internal companion object {
        internal fun forMissing(parent: String, missingElement: String): MalformedBookException =
            MalformedBookException("'$parent' is missing required '$missingElement' element(s).")
    }
}

class UnknownBookVersionException(version: String) : Exception("'$version' is not a known EPUB version")

class InvalidBookVersionException(
    val requiredMinimum: BookVersion,
    val currentVersion: BookVersion,
    val containerName: String,
    val featureName: String
) : MalformedBookException("$containerName feature $featureName requires at minimum version $requiredMinimum, but current version is $currentVersion.")

@Suppress("NOTHING_TO_INLINE")
internal inline fun invalidVersion(
    currentVersion: BookVersion,
    minVersion: BookVersion,
    name: String,
    feature: String
): Nothing = throw InvalidBookVersionException(currentVersion, minVersion, name, feature)