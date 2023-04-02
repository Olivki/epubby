/*
 * Copyright 2019-2022 Oliver Berg
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

@file:Suppress("ClassName", "DANGEROUS_CHARACTERS")

package dev.epubby.files.revamped

// really can't come up with good names for these, so they're just going to be named after what they are: budget union types

sealed interface `ModifiableFile | Nil` : Resource

sealed interface `ModifiableDirectory | Nil` : Resource