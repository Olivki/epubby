/*
 * Copyright 2022-2023 Oliver Berg
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

package net.ormr.zip

import net.lingala.zip4j.model.UnzipParameters

@JvmInline
public value class ZipExtractParameters internal constructor(@PublishedApi internal val parameters: UnzipParameters) {
    public val extractSymbolicLinks: Boolean
        get() = parameters.isExtractSymbolicLinks
}

@JvmInline
public value class ZipExtractParametersBuilder @PublishedApi internal constructor(private val parameters: UnzipParameters) {
    public var extractSymbolicLinks: Boolean
        get() = parameters.isExtractSymbolicLinks
        set(value) {
            parameters.isExtractSymbolicLinks = value
        }

    @PublishedApi
    internal fun build(): ZipExtractParameters = ZipExtractParameters(parameters)
}

internal fun ZipExtractParameters.asUnzipParameters(): UnzipParameters = parameters

public fun ZipExtractParameters(): ZipExtractParameters = ZipExtractParameters(UnzipParameters())

public inline fun ZipExtractParameters(
    from: ZipExtractParameters = ZipExtractParameters(),
    builder: ZipExtractParametersBuilder.() -> Unit = {},
): ZipExtractParameters = ZipExtractParametersBuilder(from.parameters).apply(builder).build()