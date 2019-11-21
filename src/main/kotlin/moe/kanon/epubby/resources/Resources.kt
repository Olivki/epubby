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

package moe.kanon.epubby.resources

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentHashMap
import moe.kanon.epubby.Book
import moe.kanon.epubby.structs.Identifier
import moe.kanon.kommons.collections.asUnmodifiable

class Resources(val book: Book) : Iterable<Resource> {
    private val resources: MutableMap<Identifier, Resource> = hashMapOf()

    /**
     * Returns a map of all the resources in the [book], mapped like `identifier::resource`.
     */
    val entries: ImmutableMap<Identifier, Resource> get() = resources.toPersistentHashMap()

    fun visitResources(visitor: ResourceVisitor) {
        for ((_, resource) in resources) {
            when (resource) {
                is TableOfContentsResource -> visitor.onTableOfContents(resource)
                is PageResource -> visitor.onPage(resource)
                is StyleSheetResource -> visitor.onStyleSheet(resource)
                is ImageResource -> visitor.onImage(resource)
                is FontResource -> visitor.onFont(resource)
                is AudioResource -> visitor.onAudio(resource)
                is ScriptResource -> visitor.onScript(resource)
                is VideoResource -> visitor.onVideo(resource)
                is MiscResource -> visitor.onMisc(resource)
            }
        }
    }

    /**
     * Returns `true` if there exists a resource with the given [identifier], `false` otherwise.
     */
    fun hasResource(identifier: Identifier): Boolean = identifier in resources

    /**
     * Returns `true` if the given [resource] is known, `false` otherwise.
     */
    fun hasResource(resource: Resource): Boolean = resources.containsValue(resource)

    override fun iterator(): Iterator<Resource> = resources.values.iterator().asUnmodifiable()
}