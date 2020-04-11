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

enum class BookReadMode {
    /**
     * The system will throw exceptions when it encounters faulty/unknown elements.
     */
    STRICT,
    /**
     * The system will try it's best to recover from any faulty/unknown elements, and will log the errors rather
     * than throwing them as exceptions.
     *
     * Note that if this mode is used, then epubby can no longer make any guarantees that the result of writing the
     * book to a file will produce a valid EPUB file.
     */
    LENIENT;
}