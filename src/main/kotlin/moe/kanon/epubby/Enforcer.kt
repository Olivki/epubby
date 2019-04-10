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

/**
 * Utility class containing helper functions for enforcing that the folder structure of the epub is correct.
 */
@file:JvmName("BookStructureEnforcer")

package moe.kanon.epubby

import java.io.IOException

/**
 * Attempts to correct any wrong parts of the structure of the specified [book].
 *
 * @param [book] the [Book] instance to traverse
 */
@Throws(IOException::class)
fun validateBookStructure(book: Book) {

}