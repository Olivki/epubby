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

import net.lingala.zip4j.model.ExcludeFileFilter
import net.lingala.zip4j.model.ZipParameters.SymbolicLinkAction
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import net.lingala.zip4j.model.ZipParameters as Zip4jParameters

@JvmInline
public value class ZipParameters @PublishedApi internal constructor(
    @PublishedApi
    internal val parameters: Zip4jParameters,
) {
    public val compression: ZipCompression
        get() = ZipCompression(parameters)

    public val encryption: ZipEncryption
        get() = ZipEncryption(parameters)

    public val readHiddenFiles: Boolean
        get() = parameters.isReadHiddenFiles

    public val readHiddenFolders: Boolean
        get() = parameters.isReadHiddenFolders

    public val includeRootDirectory: Boolean
        get() = parameters.isIncludeRootFolder

    public val rootDirectoryName: String
        get() = parameters.rootFolderNameInZip

    public val entryCrc: Long
        get() = parameters.entryCRC

    public val entrySize: Long
        get() = parameters.entrySize

    // TODO: can this be null?
    public val defaultDirectoryPath: String
        get() = parameters.defaultFolderPath

    public val fileName: String?
        get() = parameters.fileNameInZip

    public val lastModifiedFileTime: FileTime
        get() = FileTime.fromMillis(parameters.lastModifiedFileTime)

    public val writeExtendedLocalFileHeader: Boolean
        get() = parameters.isWriteExtendedLocalFileHeader

    public val overrideExistingFiles: Boolean
        get() = parameters.isOverrideExistingFilesInZip

    public val fileComment: String?
        get() = parameters.fileComment?.ifEmpty { null }

    public val symbolicLinkAction: SymbolicLinkAction
        get() = parameters.symbolicLinkAction

    public val isUnixMode: Boolean
        get() = parameters.isUnixMode
}

@JvmInline
@ZipBuilderMarker
public value class ZipParametersBuilder @PublishedApi internal constructor(
    @PublishedApi
    internal val parameters: Zip4jParameters,
) {
    public var readHiddenFiles: Boolean
        get() = parameters.isReadHiddenFiles
        set(value) {
            parameters.isReadHiddenFiles = value
        }

    public var readHiddenFolders: Boolean
        get() = parameters.isReadHiddenFolders
        set(value) {
            parameters.isReadHiddenFolders = value
        }

    public var includeRootDirectory: Boolean
        get() = parameters.isIncludeRootFolder
        set(value) {
            parameters.isIncludeRootFolder = value
        }

    public var rootDirectoryName: String
        get() = parameters.rootFolderNameInZip
        set(value) {
            parameters.rootFolderNameInZip = value
        }

    public var entryCrc: Long
        get() = parameters.entryCRC
        set(value) {
            parameters.entryCRC = value
        }

    public var entrySize: Long
        get() = parameters.entrySize
        set(value) {
            parameters.entrySize = value
        }

    // TODO: can this be null?
    public var defaultDirectoryPath: String
        get() = parameters.defaultFolderPath
        set(value) {
            parameters.defaultFolderPath = value
        }

    public var fileName: String?
        get() = parameters.fileNameInZip
        set(value) {
            parameters.fileNameInZip = value
        }

    public var lastModifiedFileTime: FileTime
        get() = FileTime.fromMillis(parameters.lastModifiedFileTime)
        set(value) {
            parameters.entrySize = value.toMillis()
        }

    public var writeExtendedLocalFileHeader: Boolean
        get() = parameters.isWriteExtendedLocalFileHeader
        set(value) {
            parameters.isWriteExtendedLocalFileHeader = value
        }

    public var overrideExistingFiles: Boolean
        get() = parameters.isOverrideExistingFilesInZip
        set(value) {
            parameters.isOverrideExistingFilesInZip = value
        }

    public var fileComment: String?
        get() = parameters.fileComment?.ifEmpty { null }
        set(value) {
            parameters.fileComment = value ?: ""
        }

    public var symbolicLinkAction: SymbolicLinkAction
        get() = parameters.symbolicLinkAction
        set(value) {
            parameters.symbolicLinkAction = value
        }

    public var isUnixMode: Boolean
        get() = parameters.isUnixMode
        set(value) {
            parameters.isUnixMode = value
        }

    public inline fun excludeFileFilter(crossinline predicate: (Path) -> Boolean) {
        parameters.excludeFileFilter = ExcludeFileFilter { predicate(it.toPath()) }
    }

    public inline fun compression(builder: ZipCompressionBuilder.() -> Unit) {
        ZipCompressionBuilder(parameters).apply(builder)
    }

    public inline fun encryption(builder: ZipEncryptionBuilder.() -> Unit) {
        ZipEncryptionBuilder(parameters).apply(builder)
    }

    @PublishedApi
    internal fun build(): ZipParameters = ZipParameters(parameters)
}

internal fun ZipParameters.asZip4jParameters(): Zip4jParameters = parameters

public fun ZipParameters(): ZipParameters = ZipParameters(Zip4jParameters())

public inline fun ZipParameters(
    from: ZipParameters = ZipParameters(),
    builder: ZipParametersBuilder.() -> Unit = {},
): ZipParameters = ZipParametersBuilder(from.parameters).apply(builder).build()