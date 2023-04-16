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

package dev.epubby.dublincore

/**
 * Represents a custom creative role that helped in some manner with the creation of some part of an epub.
 *
 * @property [customCode] The custom [marc-relator code](https://www.loc.gov/marc/relators/) representing the role.
 *
 * This code can only be max `3` characters long.
 */
public class CustomCreativeRole internal constructor(
    public val customCode: String,
    override val name: String?,
) : CreativeRole {
    /**
     * The [marc-relator code](https://www.loc.gov/marc/relators/) representing the role.
     *
     * The code of a custom role can only be max `7` characters long.
     *
     * All custom roles have a `oth.` prefix before their [customCode], hence the extra 4 character length compared
     * to [DefaultCreativeRole.code].
     *
     * @see [customCode]
     */
    override val code: String = "oth.$customCode"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is CustomCreativeRole -> false
        customCode != other.customCode -> false
        name != other.name -> false
        else -> code == other.code
    }

    override fun hashCode(): Int {
        var result = customCode.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + code.hashCode()
        return result
    }

    override fun toString(): String = "CustomCreativeRole(customCode='$customCode', code='$code', name=$name)"
}