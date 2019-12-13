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

package moe.kanon.epubby

// TODO: This
/**
 * TODO
 *
 * @property [mode] Determines how the system should act when encountering faulty/unknown elements when reading a
 * file into a [Book] instance.
 *
 * Note that this has no bearing on whether or not the system will throw a exception when doing the initial
 * validation that the given file *actually* is a EPUB.
 */
class BookReader private constructor(val mode: Mode) {

    /**
     * A builder for [BookReader].
     *
     * @property [mode] Determines how the system should act when encountering faulty/unknown elements when reading a
     * file into a [Book] instance.
     *
     * Note that this has no bearing on whether or not the system will throw a exception when doing the initial
     * validation that the given file *actually* is a EPUB.
     */
    class Builder internal constructor(
        @set:[JvmSynthetic JvmName("_setMode")]
        var mode: Mode = Mode.STRICT
    ) {
        /**
         * Determines how the system should act when encountering faulty/unknown elements when reading a
         * file into a [Book] instance.
         *
         * Note that this has no bearing on whether or not the system will throw a exception when doing the initial
         * validation that the given file *actually* is a EPUB.
         *
         * By default this is set to `STRICT`.
         */
        fun setMode(mode: Mode) = apply {
            this.mode = mode
        }

        fun build(): BookReader = BookReader(mode)
    }

    enum class Mode {
        /**
         * The system will throw exceptions when it encounters faulty/unknown elements.
         */
        STRICT,
        /**
         * The system will try it's best to recover from any faulty/unknown elements, and will log the errors rather
         * than throwing them as exceptions.
         *
         * Note that if this mode is used, then epubby can no longer make any guarantees that result of
         * [writing][BookWriter.writeToFile] the book to a file will produce a valid EPUB file. This also means that
         */
        LENIENT;
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()

        @JvmStatic
        @JvmName("newInstance")
        operator fun invoke(): BookReader = builder().build()

        @JvmSynthetic
        inline operator fun invoke(scope: Builder.() -> Unit): BookReader = builder().apply(scope).build()
    }
}