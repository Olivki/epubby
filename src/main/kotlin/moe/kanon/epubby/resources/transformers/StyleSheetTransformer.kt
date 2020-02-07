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

package moe.kanon.epubby.resources.transformers

import com.helger.css.decl.CascadingStyleSheet
import moe.kanon.epubby.resources.StyleSheetResource

interface StyleSheetTransformer : Transformer {
    /**
     * Gets invoked when the [book] is saving the given [resource] to its file.
     *
     * This function allows `this` transformer to modify the contents of the given [styleSheet].
     */
    fun transformStyleSheet(styleSheet: CascadingStyleSheet, resource: StyleSheetResource)
}