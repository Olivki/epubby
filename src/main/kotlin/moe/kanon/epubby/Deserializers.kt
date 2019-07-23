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

import org.jdom2.Document
import org.jdom2.Element

/**
 * Represents a class that can create a new instance of [T] from a given [Document] instance.
 */
interface DocumentDeserializer<out T> {
    /**
     * Returns a new instance of [T] using the given [document], or throws an [Exception] if something went wrong.
     */
    @Throws(EpubbyException::class)
    fun fromDocument(document: Document): T
}

/**
 * Represents a class that can create a new instance of [T] from a given [Element] instance.
 */
interface ElementDeserializer<out T> {
    /**
     * Returns a new instance of [T] using the given [element], or throws an [Exception] if something went wrong.
     */
    @Throws(EpubbyException::class)
    fun fromElement(element: Element): T
}