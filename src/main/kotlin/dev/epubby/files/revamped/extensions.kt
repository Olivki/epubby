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

@file:JvmName("Resources")

package dev.epubby.files.revamped

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.getOrHandle
import dev.epubby.errors.FileError
import dev.epubby.files.revamped.ExistingResourceVisitor.NilCaller
import dev.epubby.resources.LocalResource
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.util.*
import java.util.regex.PatternSyntaxException
import kotlin.io.path.*

@JvmSynthetic
operator fun EpubFileSystem.get(first: String, vararg more: String): Path = getPath(first, *more)

fun EpubFileSystem.getResource(first: String, vararg more: String): Resource = getPath(first, *more).resource

@JvmSynthetic
operator fun Path.div(other: String): Path = resolve(other)

@JvmSynthetic
operator fun Path.div(other: Path): Path = resolve(other)

/**
 * Returns the [absolute][Path.absolute] version of `this` path as a [String].
 */
fun Path.absolutePathString(): String = absolute().toString()

val Path.ancestors: Sequence<Path>
    get() = generateSequence(parent) { it.parent }

fun Path.resolve(other: Resource): Path = resolve(other.path)

fun Path.relativize(other: Resource): Path = relativize(other.path)

val Resource.name: String get() = path.name

val Resource.fileSystem: EpubFileSystem get() = path.fileSystem

@Suppress("RedundantNullableReturnType", "UnusedReceiverParameter")
val Resource.localResource: LocalResource? get() = TODO("return local resource")

val Resource.exists: Boolean get() = path.exists

val Resource.notExists: Boolean get() = path.notExists

fun Directory.resolve(other: String): Resource = path.resolve(other).resource

fun Directory.resolve(other: Path): Resource = path.resolve(other).resource

fun Directory.resolve(other: Resource): Resource = path.resolve(other.path).resource

fun Resource.resolveSibling(other: String): Resource = path.resolveSibling(other).resource

fun Resource.resolveSibling(other: Path): Resource = path.resolveSibling(other).resource

fun Resource.relativize(other: Resource): Resource = path.relativize(other.path).resource

val ExistingResource.ancestors: Sequence<Directory>
    get() = when (this) {
        is File -> generateSequence(directory) { it.parent }
        is Directory -> generateSequence(parent) { it.parent }
    }

// TODO: refactor the 'isXXX' properties when we migrate the types

val File.isMimeType: Boolean
    get() = isMimeTypePath(path)

val File.isOpf: Boolean
    get() = isOpfFilePath(path)

val File.isMetaInf: Boolean
    get() = isMetaInfFilePath(path)

val File.isLocalResource: Boolean
    get() = isLocalResourcePath(path)

inline fun File.forEachLine(charset: Charset = Charsets.UTF_8, action: (String) -> Unit): Either<FileError, Unit> =
    wrapIOException { delegatePath.forEachLine(charset, action) }

inline fun <T> File.useLines(
    charset: Charset = Charsets.UTF_8,
    action: (Sequence<String>) -> T,
): Either<FileError, T> = wrapIOException { delegatePath.useLines(charset, action) }

fun ModifiableFile.writeLines(
    lines: Sequence<CharSequence>,
    charset: Charset = Charsets.UTF_8,
): Either<FileError, ModifiableFile> = writeLines(lines.asIterable(), charset)

fun UnprotectedFile.writeLines(
    lines: Sequence<CharSequence>,
    charset: Charset = Charsets.UTF_8,
): Either<FileError, UnprotectedFile> = writeLines(lines.asIterable(), charset)

fun ModifiableFile.appendLines(
    lines: Sequence<CharSequence>,
    charset: Charset = Charsets.UTF_8,
): Either<FileError, ModifiableFile> = appendLines(lines.asIterable(), charset)

fun UnprotectedFile.appendLines(
    lines: Sequence<CharSequence>,
    charset: Charset = Charsets.UTF_8,
): Either<FileError, UnprotectedFile> = appendLines(lines.asIterable(), charset)

val Directory.isMetaInf: Boolean
    get() = isMetaInfDirectoryPath(path)

val Directory.isOpf: Boolean
    get() = isOpfDirectory(path)

fun ModifiableDirectory.createDirectory(
    name: String,
    vararg attributes: FileAttribute<*>,
): Either<FileError, Directory> = either.eager {
    when (val target = resolve(name)) {
        is Nil -> target.createDirectory(*attributes).bind()
        is File -> shift(FileError.NotDirectory(target.toString()))
        is Directory -> target
    }
}

fun ModifiableDirectory.createFile(
    name: String,
    vararg attributes: FileAttribute<*>,
): Either<FileError, File> = either.eager {
    when (val target = resolve(name)) {
        is Nil -> target.createFile(*attributes).bind()
        is File -> target
        is Directory -> shift(FileError.NotFile(target.toString()))
    }
}

inline fun Directory.forEachEntry(
    glob: String = "*",
    action: (ExistingResource) -> Unit,
): Either<FileError, Unit> = try {
    wrapIOException {
        delegatePath.forEachDirectoryEntry(glob) {
            val resource = Path(it, fileSystem).resource
            if (resource is ExistingResource) action(resource)
        }
    }
} catch (e: PatternSyntaxException) {
    Either.Left(e.toFileError())
}

inline fun <T> Directory.useEntries(
    glob: String = "*",
    action: (entries: Sequence<ExistingResource>) -> T,
): Either<FileError, T> = try {
    wrapIOException {
        delegatePath.useDirectoryEntries(glob) { sequence ->
            val newSequence = sequence
                .map { Path(it, fileSystem).resource }
                .filterIsInstance<ExistingResource>()
            action(newSequence)
        }
    }
} catch (e: PatternSyntaxException) {
    Either.Left(e.toFileError())
}

/**
 * Removes *all* the files from `this` directory, even the subdirectories, but leaves all directories intact.
 */
fun <T : ModifiableDirectory> T.deleteExistingFilesRecursively(): Either<FileError, T> = walkFileTree(FileDeleter)

private object FileDeleter : ExistingResourceVisitor {
    override fun visitFile(
        file: File,
        attributes: BasicFileAttributes,
    ): Either<FileError, FileVisitResult> = either.eager {
        if (file is DeletableFile) {
            file.delete().bind()
        } else {
            shift(FileError.NotDeletable(file.toString()))
        }

        FileVisitResult.CONTINUE
    }
}

// the only option available in 'FileVisitOption' is 'FOLLOW_LINKS', and we don't even support symbolic links
// so there's no real reason to include it as a parameter
/**
 * Walks the file tree of `this` directory, using the given [visitor], going to [maxDepth].
 *
 * @see [Files.walkFileTree]
 */
fun <T : Directory> T.walkFileTree(
    visitor: ExistingResourceVisitor,
    maxDepth: Int = Int.MAX_VALUE,
): Either<FileError, T> = try {
    Files.walkFileTree(delegatePath, emptySet(), maxDepth, FileVisitorImpl(visitor, fileSystem))
    Either.Right(this)
} catch (e: WrappedFileError) {
    Either.Left(e.error)
}

@JvmSynthetic
inline fun <T : Directory> T.walkFileTree(
    maxDepth: Int = Int.MAX_VALUE,
    visitor: ExistingResourceVisitorDsl.() -> Unit,
): Either<FileError, T> = walkFileTree(ExistingResourceVisitorDslBuilder().apply(visitor).build(), maxDepth)

// TODO: replace with context receiver when they're stable enough

@Suppress("UnusedReceiverParameter")
inline fun ExistingResourceVisitor.catching(block: () -> FileVisitResult): Either<FileError, FileVisitResult> =
    wrapIOException(block)

@ResourceVisitorDslMarker
inline fun existingResourceVisitor(builder: ExistingResourceVisitorDsl.() -> Unit): ExistingResourceVisitor =
    ExistingResourceVisitorDslBuilder().apply(builder).build()

@ResourceVisitorDslMarker
@Suppress("UnusedReceiverParameter", "NOTHING_TO_INLINE")
inline fun ExistingResourceVisitorDsl.error(error: FileError): Either.Left<FileError> = Either.Left(error)

/**
 * Continue.
 *
 * @see [FileVisitResult.CONTINUE]
 */
@ResourceVisitorDslMarker
@Suppress("UnusedReceiverParameter")
inline val ExistingResourceVisitorDsl.CONTINUE: Either.Right<FileVisitResult>
    get() = Either.Right(FileVisitResult.CONTINUE)

/**
 * Terminate.
 *
 * @see [FileVisitResult.TERMINATE]
 */
@ResourceVisitorDslMarker
@Suppress("UnusedReceiverParameter")
inline val ExistingResourceVisitorDsl.TERMINATE: Either.Right<FileVisitResult>
    get() = Either.Right(FileVisitResult.CONTINUE)

/**
 * Continue without visiting the entries of the directory.
 *
 * @see [FileVisitResult.SKIP_SUBTREE]
 */
@ResourceVisitorDslMarker
@Suppress("UnusedReceiverParameter")
inline val ExistingResourceVisitorDsl.SKIP_SUBTREE: Either.Right<FileVisitResult>
    get() = Either.Right(FileVisitResult.CONTINUE)

/**
 * Continue without visiting the siblings of the file or directory.
 *
 * @see [FileVisitResult.SKIP_SIBLINGS]
 */
@ResourceVisitorDslMarker
@Suppress("UnusedReceiverParameter")
inline val ExistingResourceVisitorDsl.SKIP_SIBLINGS: Either.Right<FileVisitResult>
    get() = Either.Right(FileVisitResult.CONTINUE)

private class WrappedFileError(val error: FileError) : RuntimeException()

private class FileVisitorImpl(
    private val visitor: ExistingResourceVisitor,
    private val fileSystem: EpubFileSystem,
) : FileVisitor<JPath> {
    private fun directory(resource: ExistingResource): Directory {
        check(resource is Directory) { "File at '$resource' should be directory, but it was not." }
        return resource
    }

    private fun file(resource: ExistingResource): File {
        check(resource is File) { "File at '$resource' should be regular file, but it was not." }
        return resource
    }

    private inline fun <T : ExistingResource> visit(
        path: JPath,
        caller: NilCaller,
        converter: (ExistingResource) -> T,
        handler: (T) -> Either<FileError, FileVisitResult>,
    ): FileVisitResult = when (val resource = Path(path, fileSystem).resource) {
        is Nil -> visitor.visitNil(resource, caller).getOrHandle(this::wrappedError)
        is ExistingResource -> handler(converter(resource)).getOrHandle(this::wrappedError)
    }

    private fun wrappedError(error: FileError): Nothing = throw WrappedFileError(error)

    override fun preVisitDirectory(
        dir: JPath,
        attrs: BasicFileAttributes,
    ): FileVisitResult =
        visit(dir, NilCaller.PreVisitDirectory(attrs), this::directory) { visitor.preVisitDirectory(it, attrs) }

    override fun visitFile(
        file: JPath,
        attrs: BasicFileAttributes,
    ): FileVisitResult = visit(file, NilCaller.VisitFile(attrs), this::file) { visitor.visitFile(it, attrs) }

    override fun visitFileFailed(
        file: JPath,
        exc: IOException,
    ): FileVisitResult = visit(file, NilCaller.VisitFileFailed(exc), this::file) { visitor.visitFileFailed(it, exc) }

    override fun postVisitDirectory(
        dir: JPath,
        exc: IOException?,
    ): FileVisitResult =
        visit(dir, NilCaller.PostVisitDirectory(exc), this::directory) { visitor.postVisitDirectory(it, exc) }
}

@PublishedApi
internal class ExistingResourceVisitorDslBuilder : ExistingResourceVisitorDsl {
    private var onPreDirectory: ((directory: Directory, attributes: BasicFileAttributes) -> Either<FileError, FileVisitResult>)? =
        null
    private var onFile: ((file: File, attributes: BasicFileAttributes) -> Either<FileError, FileVisitResult>)? = null
    private var onFileVisitFailed: ((file: File, exception: IOException) -> Either<FileError, FileVisitResult>)? = null
    private var onPostDirectory: ((directory: Directory, exception: IOException?) -> Either<FileError, FileVisitResult>)? =
        null
    private var onNil: ((nil: Nil, caller: NilCaller) -> Either<FileError, FileVisitResult>)? = null

    override fun onPreDirectory(
        block: (directory: Directory, attributes: BasicFileAttributes) -> Either<FileError, FileVisitResult>,
    ) {
        onPreDirectory = block
    }

    override fun onFile(
        block: (file: File, attributes: BasicFileAttributes) -> Either<FileError, FileVisitResult>,
    ) {
        onFile = block
    }

    override fun onFileVisitFailed(
        block: (file: File, exception: IOException) -> Either<FileError, FileVisitResult>,
    ) {
        onFileVisitFailed = block
    }

    override fun onPostDirectory(
        block: (directory: Directory, exception: IOException?) -> Either<FileError, FileVisitResult>,
    ) {
        onPostDirectory = block
    }

    override fun onNil(block: (nil: Nil, caller: NilCaller) -> Either<FileError, FileVisitResult>) {
        onNil = block
    }

    @PublishedApi
    internal fun build(): ExistingResourceVisitor = object : ExistingResourceVisitor {
        override fun preVisitDirectory(
            directory: Directory,
            attributes: BasicFileAttributes,
        ): Either<FileError, FileVisitResult> =
            onPreDirectory?.invoke(directory, attributes) ?: super.preVisitDirectory(directory, attributes)

        override fun visitFile(file: File, attributes: BasicFileAttributes): Either<FileError, FileVisitResult> =
            onFile?.invoke(file, attributes) ?: super.visitFile(file, attributes)

        override fun visitFileFailed(file: File, exception: IOException): Either<FileError, FileVisitResult> =
            onFileVisitFailed?.invoke(file, exception) ?: super.visitFileFailed(file, exception)

        override fun postVisitDirectory(
            directory: Directory,
            exception: IOException?,
        ): Either<FileError, FileVisitResult> =
            onPostDirectory?.invoke(directory, exception) ?: super.postVisitDirectory(directory, exception)

        override fun visitNil(nil: Nil, caller: NilCaller): Either<FileError, FileVisitResult> =
            onNil?.invoke(nil, caller) ?: super.visitNil(nil, caller)
    }
}