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

package dev.epubby.metainf

import kotlinx.serialization.SerializationException

public sealed interface MetaInfReadError

public sealed interface MetaInfContainerReadError : MetaInfReadError {
    public data class InvalidModel(val cause: SerializationException) : MetaInfContainerReadError
    public data class MissingAttribute(val name: String, val path: String) : MetaInfContainerReadError
    public data class MissingElement(val name: String, val path: String) : MetaInfContainerReadError
    public object EmptyRootFiles : MetaInfContainerReadError
}