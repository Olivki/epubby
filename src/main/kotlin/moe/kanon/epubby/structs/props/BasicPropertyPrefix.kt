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

package moe.kanon.epubby.structs.props

/**
 * A prefix for a [BasicProperty].
 *
 * @property [name] The shorthand name used to refer to the underlying [url] when [processing][BasicProperty.process] a
 * property.
 *
 * May be `null`.
 * @property [url] The URL that this prefix points towards.
 */
internal data class BasicPropertyPrefix(override val prefix: String?, override val url: String) : PropertyPrefix {
    override fun toString(): String = when (prefix) {
        null -> "{$url}"
        else -> "{$prefix:$url}"
    }
}