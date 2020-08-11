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

package dev.epubby.builders

sealed class BuilderException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Thrown to indicate that the required value with the given [valueName] has not been set, but the `build` function was
 * invoked anyways.
 */
class MissingRequiredValueException(
    val valueName: String,
    message: String
) : BuilderException(message)

/**
 * Thrown to indicate that the value with the given [valueName] is malformed in some manner, and can therefore not be
 * used as a value in the builder.
 */
class MalformedValueException(
    val valueName: String,
    message: String,
    cause: Throwable? = null
) : BuilderException(message, cause)