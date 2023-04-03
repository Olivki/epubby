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

@file:Suppress("NAME_SHADOWING")

package dev.epubby.dublincore

import dev.epubby.Epub2Feature
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.models.dublincore.CreativeRoleSerializer
import net.ormr.epubby.internal.util.isLowerCase

// http://idpf.org/epub/20/spec/OPF_2.0.1_draft.htm#Section2.2.6

/**
 * Represents a creative role that helped in some manner with the creation of some part of an epub.
 *
 * @see [DefaultCreativeRole]
 * @see [CustomCreativeRole]
 */
@Epub2Feature
@Serializable(with = CreativeRoleSerializer::class)
public sealed interface CreativeRole {
    /**
     * The [marc-relator code](https://www.loc.gov/marc/relators/) representing the role.
     *
     * The length of the code is implementation dependant, see [DefaultCreativeRole.code] or [CustomCreativeRole.code]
     * for more info.
     *
     * @see [DefaultCreativeRole.code]
     * @see [CustomCreativeRole.code]
     */
    public val code: String

    /**
     * A more human-readable version of [code].
     *
     * Note that the `name` of a `CreativeRole` is never actually serialized in any manner, so this is merely here for
     * metadata and debugging purposes.
     */
    public val name: String?

    public companion object {
        private val defaultRoles: MutableMap<String, CreativeRole> by lazy {
            DefaultCreativeRole::class
                .sealedSubclasses
                .asSequence()
                .map { it.objectInstance ?: error("Non object default role $it") }
                .associateByTo(hashMapOf(), { it.code }, { it })
        }

        /**
         * Returns a [CreativeRole] instance based on the given [code] and [name].
         *
         * The returned instance will be a [DefaultCreativeRole] if `code` is a known value, otherwise it will be a
         * [CustomCreativeRole] instance.
         *
         * Any non-recognized `code` values will be prefixed with `'oth.'` before being created, meaning that if you,
         * for example, invoke this function with `code` set as `'öad'` the [code][CreativeRole.code] of the returned
         * instance will be `'oth.öad'` and not `'öad'`. This will not happen if `code` is already prefixed
         * with `'oth.'`.
         *
         * In cases where `code` resolves to a `CustomCreativeRole` the custom code is preserved in the
         * [customCode][CustomCreativeRole.customCode] property.
         *
         * Note that `name` is only used in case `code` does *not* match a `DefaultCreativeRole`. For example, say you
         * invoke this with `of("art", "The Artist")` the returned instance will *not* be
         * `CustomCreativeRole("art", "The Artist")` but rather `DefaultCreativeRole.Artist` as the code `art` is
         * associated with the default role [Artist][DefaultCreativeRole.Artist].
         *
         * @param [code] the [marc-relator code](https://www.loc.gov/marc/relators/) of the role, must be all lowercase
         * @param [name] a human-readable variant of [code], not used for resolution
         *
         * @throws [IllegalArgumentException] if the given [code] is malformed in some manner
         */
        public fun of(code: String, name: String? = null): CreativeRole {
            require(code.isLowerCase()) { "'code' must be all lowercase, was '${code}'." }
            require(code.length == 3 && !code.startsWith("oth.")) { "'code' must be exactly 3 characters long, was ${code.length} characters long." }
            require(code.length == 7 && code.startsWith("oth.")) { "custom codes must be exactly 7 characters long, was ${code.length} characters long." }
            return create(code, name)
        }

        // used internally for lenient parsing when deserializing from XML
        @JvmSynthetic
        internal fun create(code: String, name: String? = null): CreativeRole {
            val code = code.lowercase()
            return defaultRoles[code] ?: CustomCreativeRole(code.toCustomCode(), name)
        }

        private fun String.toCustomCode(): String = if (startsWith("oth.")) this else "oth.$this"
    }
}