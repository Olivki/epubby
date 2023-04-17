/*
 * Copyright 2022-2023 Oliver Berg
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

package net.ormr.zip

import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.AesVersion
import net.lingala.zip4j.model.enums.EncryptionMethod
import net.lingala.zip4j.model.ZipParameters as Zip4jParameters

@JvmInline
public value class ZipEncryption internal constructor(private val parameters: Zip4jParameters) {
    public val method: EncryptionMethod
        get() = parameters.encryptionMethod

    public val aesKeyStrength: AesKeyStrength
        get() = parameters.aesKeyStrength

    public val aesVersion: AesVersion
        get() = parameters.aesVersion
}

@JvmInline
public value class ZipEncryptionBuilder @PublishedApi internal constructor(private val parameters: Zip4jParameters) {
    public var method: EncryptionMethod
        get() = parameters.encryptionMethod
        set(value) {
            parameters.isEncryptFiles = value != EncryptionMethod.NONE
            parameters.encryptionMethod = value
        }

    public var aesKeyStrength: AesKeyStrength
        get() = parameters.aesKeyStrength
        set(value) {
            parameters.aesKeyStrength = value
        }

    public var aesVersion: AesVersion
        get() = parameters.aesVersion
        set(value) {
            parameters.aesVersion = value
        }
}