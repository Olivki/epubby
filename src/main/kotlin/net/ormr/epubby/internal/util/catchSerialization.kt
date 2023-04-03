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

package net.ormr.epubby.internal.util

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import net.ormr.epubby.internal.xml.Xml
import java.nio.file.Path

// TODO: better name
internal inline fun <V, E> catchSerialization(
    block: () -> V,
    errorMapper: (SerializationException) -> E,
): Result<V, E> = try {
    Ok(block())
} catch (e: SerializationException) {
    Err(errorMapper(e))
}

internal inline fun <V, E> Xml.safeDecodeFromFile(
    deserializer: DeserializationStrategy<V>,
    file: Path,
    errorMapper: (SerializationException) -> E,
): Result<V, E> = catchSerialization({ decodeFromFile(deserializer, file) }, errorMapper)