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

package dev.epubby.resources

import com.github.michaelbull.logging.InlineLogger
import dev.epubby.Epub
import dev.epubby.files.DirectoryFile
import dev.epubby.files.RegularFile
import dev.epubby.resources.ResourceFileOrganizer.NameClashStrategy.INCREMENT
import dev.epubby.resources.ResourceFileOrganizer.NameClashStrategy.THROW_EXCEPTION
import java.nio.file.FileAlreadyExistsException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Handles the moving of all [LocalResource] instances to the directories defined in here.
 *
 * Once a directory has been registered to this organizer it can *not* be removed, it can however be changed.
 */
// TODO: 'Resolver' isn't really the correct term for what we're doing here, think of a better name
class ResourceFileOrganizer internal constructor(private val epub: Epub) {
    private val classToDirectory: MutableMap<Class<out LocalResource>, DirectoryFile> = hashMapOf(
        AudioResource::class.java to directoryOf("Audio/"),
        FontResource::class.java to directoryOf("Fonts/"),
        ImageResource::class.java to directoryOf("Images/"),
        MiscResource::class.java to directoryOf("Misc/"),
        NcxResource::class.java to epub.opfDirectory,
        PageResource::class.java to directoryOf("Text/"),
        ScriptResource::class.java to directoryOf("Scripts/"),
        StyleSheetResource::class.java to directoryOf("Styles/"),
        VideoResource::class.java to directoryOf("Video/"),
    )

    /**
     * Returns the directory desired by [AudioResource]s.
     */
    val audioDirectory: DirectoryFile
        get() = classToDirectory.getValue(AudioResource::class.java)

    /**
     * Returns the directory desired by [FontResource]s.
     */
    val fontDirectory: DirectoryFile
        get() = classToDirectory.getValue(FontResource::class.java)

    /**
     * Returns the directory desired by [ImageResource].
     */
    val imageDirectory: DirectoryFile
        get() = classToDirectory.getValue(ImageResource::class.java)

    /**
     * Returns the directory desired by [MiscResource]s.
     */
    val miscDirectory: DirectoryFile
        get() = classToDirectory.getValue(MiscResource::class.java)

    /**
     * Returns the directory desired by the [NcxResource].
     */
    val ncxDirectory: DirectoryFile
        get() = classToDirectory.getValue(NcxResource::class.java)

    /**
     * Returns the directory desired by [PageResource]s.
     */
    val pageDirectory: DirectoryFile
        get() = classToDirectory.getValue(PageResource::class.java)

    /**
     * Returns the directory desired by [ScriptResource]s.
     */
    val scriptDirectory: DirectoryFile
        get() = classToDirectory.getValue(ScriptResource::class.java)

    /**
     * Returns the directory desired by [StyleSheetResource]s.
     */
    val styleSheetDirectory: DirectoryFile
        get() = classToDirectory.getValue(StyleSheetResource::class.java)

    /**
     * Returns the directory desired by [VideoResource]s.
     */
    val videoDirectory: DirectoryFile
        get() = classToDirectory.getValue(VideoResource::class.java)

    // TODO: better name
    // TODO: documentation
    @JvmOverloads
    fun organizeFiles(nameClashStrategy: NameClashStrategy = THROW_EXCEPTION) {
        LOGGER.debug { "Starting the organizing of the resource files of $epub.." }
        for ((_, resource) in epub.manifest.localResources) {
            val directory = getDirectory(resource.javaClass)

            // it's already in the desired directory so we just skip it
            if (directory.exists) {
                if (resource.file.parent?.isSameAs(directory) == true) {
                    continue
                }
            }

            var target = directory.resolveFile(resource.file.name)

            // TODO: verify that this works
            if (target.exists) {
                if (target.isSameAs(resource.file)) {
                    continue
                }

                target = when (nameClashStrategy) {
                    THROW_EXCEPTION -> throw FileAlreadyExistsException(
                        resource.file.toString(),
                        target.toString(),
                        "Can't move resource file to new location as a file already exists there."
                    )
                    INCREMENT -> getNonClashingFile(target)
                }
            } else {
                target.createParents()
            }

            resource.file.moveTo(target)
        }
        LOGGER.debug { "Finished the organizing of the resource files.." }
    }

    // TODO: verify that 'name' doesn't try to go outside of the opfDirectory
    fun setDirectory(clz: Class<out LocalResource>, name: String) {
        val fixedName = when {
            name.isEmpty() -> ""
            else -> when {
                name.endsWith('/') -> name
                else -> "$name/"
            }
        }

        classToDirectory[clz] = directoryOf(fixedName)
    }

    @JvmSynthetic
    inline fun <reified T : LocalResource> setDirectory(name: String) {
        setDirectory(T::class.java, name)
    }

    /**
     * Returns the [DirectoryFile] that the given [clz] is mapped to, or [miscDirectory] if `clz` is not mapped
     * to a directory.
     */
    fun getDirectory(clz: Class<out LocalResource>): DirectoryFile =
        classToDirectory.getOrDefault(clz, classToDirectory.getValue(MiscResource::class.java))

    /**
     * Returns the [DirectoryFile] that the given [T] is mapped to, or [miscDirectory] if `clz` is not mapped
     * to a directory.
     */
    @JvmSynthetic
    inline fun <reified T : LocalResource> getDirectory(): DirectoryFile = getDirectory(T::class.java)

    /**
     * Returns `true` if the given [clz] has a directory mapped to it, otherwise `false`.
     */
    fun hasDirectory(clz: Class<out LocalResource>): Boolean = clz in classToDirectory

    /**
     * Returns `true` if the given [T] has a directory mapped to it, otherwise `false`.
     */
    @JvmSynthetic
    inline fun <reified T : LocalResource> hasDirectory(): Boolean = hasDirectory(T::class.java)

    private fun getNonClashingFile(target: RegularFile): RegularFile {
        val incrementer = AtomicInteger(0)
        var newTarget = target.resolveSiblingFile(target.createNewName(incrementer.getAndIncrement()))

        while (newTarget.exists) {
            newTarget = target.resolveSiblingFile(target.createNewName(incrementer.getAndIncrement()))
        }

        return newTarget
    }

    private fun RegularFile.createNewName(appendage: Any): String =
        "${simpleName}_$appendage${extension?.let { ".$it" } ?: ""}"

    private fun directoryOf(name: String): DirectoryFile = epub.root.resolveDirectory("OEBPS/$name")

    /**
     * The strategy to use when encountering a name clash when moving resources to their desired directories.
     */
    enum class NameClashStrategy {
        /**
         * A [FileAlreadyExistsException] will be thrown when a name clash is encountered.
         */
        THROW_EXCEPTION,

        /**
         * The name of the resource file that is currently being moved will have a number that will continue
         * incrementing until there no longer exists a file with that name appended onto its file name.
         */
        INCREMENT;
    }

    private companion object {
        private val LOGGER: InlineLogger = InlineLogger(ResourceFileOrganizer::class)
    }
}