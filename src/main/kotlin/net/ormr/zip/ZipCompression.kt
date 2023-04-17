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

import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.ZipParameters as Zip4jParameters

@JvmInline
public value class ZipCompression internal constructor(private val parameters: Zip4jParameters) {
    public val method: CompressionMethod
        get() = parameters.compressionMethod

    public val level: CompressionLevel
        get() = parameters.compressionLevel
}

@JvmInline
@ZipBuilderMarker
public value class ZipCompressionBuilder @PublishedApi internal constructor(private val parameters: ZipParameters) {
    public var method: CompressionMethod
        get() = parameters.compressionMethod
        set(value) {
            parameters.compressionMethod = value
        }

    public var level: CompressionLevel
        get() = parameters.compressionLevel
        set(value) {
            parameters.compressionLevel = value
        }
}