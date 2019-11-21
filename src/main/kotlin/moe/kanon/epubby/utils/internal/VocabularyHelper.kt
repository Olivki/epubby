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

package moe.kanon.epubby.utils.internal

import moe.kanon.epubby.structs.props.Property

internal inline fun <reified E> findProperty(ref: String): E
    where E : Enum<E>, E : Property = enumValues<E>().firstOrNull { it.reference == ref }
    ?: throw NoSuchElementException("No vocabulary entry found with the given reference '$ref'")