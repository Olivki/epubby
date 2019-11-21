/*
 * Copyright 2019 Oliver Berg
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

package moe.kanon.epubby.resources

import moe.kanon.epubby.EpubbyException
import java.nio.file.Path

/**
 * Thrown to indicate that an error occurred somewhere when working with a [Resource] implementation.
 *
 * @property [resourceFile] The file that the [Resource] implementation is tied to.
 */
open class ResourceException(val resourceFile: Path, message: String?, cause: Throwable? = null) :
    EpubbyException(message, cause)