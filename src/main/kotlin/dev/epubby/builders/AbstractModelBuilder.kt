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

import dev.epubby.Book

abstract class AbstractModelBuilder<Target : Any, Model : Any> internal constructor(
    target: Class<Model>
) : AbstractBuilder<Model>(target) {
    /**
     * Returns a new [Target] instance that will be tied to the given [book].
     *
     * This function will always return the actual proper instance of the builder rather than the model target, as
     * one can be properly constructed with the help of the [book] instance.
     */
    // TODO: the documentation for this is, uh, not very good
    abstract fun build(book: Book): Target
}