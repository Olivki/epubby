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

package moe.kanon.epubby.structs

/**
 * Stands for International Standard Book Number.
 *
 * TODO: Documentation
 */
// TODO: Remove?
class ISBN private constructor(
    val prefix: String?,
    val group: String,
    val registrant: String,
    val publication: String,
    val checksum: String
) {
    /**
     * Returns the length of `this` ISBN number, which is either `10` if the ISBN number was assigned before 2007, or
     * `13` if it was assigned afterwards.
     */
    val length: Int = if (prefix == null) 10 else 13

    /**
     * Returns `true` if the [length] of `this` ISBN number is `10`, otherwise `false`.
     */
    val isOld: Boolean = length == 10

    fun toISBN13(): ISBN = ISBN("978", group, registrant, publication, checksum) // TODO: Recalculate the checksum

    override fun toString(): String =
        if (isOld) "$group-$registrant-$publication-$checksum" else "$prefix-$group-$registrant-$publication-$checksum"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ISBN -> false
        prefix != other.prefix -> false
        group != other.group -> false
        registrant != other.registrant -> false
        publication != other.publication -> false
        checksum != other.checksum -> false
        length != other.length -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = prefix?.hashCode() ?: 0
        result = 31 * result + group.hashCode()
        result = 31 * result + registrant.hashCode()
        result = 31 * result + publication.hashCode()
        result = 31 * result + checksum.hashCode()
        result = 31 * result + length
        result = 31 * result + isOld.hashCode()
        return result
    }

    companion object {

    }
}