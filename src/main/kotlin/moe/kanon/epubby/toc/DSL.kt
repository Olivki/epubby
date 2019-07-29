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

package moe.kanon.epubby.toc

import moe.kanon.epubby.BuilderMarker
import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.kommons.func.None
import moe.kanon.kommons.func.Option
import moe.kanon.kommons.requireThat

// TODO: Documentation?

@BuilderMarker class TOCContainer internal constructor() {
    @JvmSynthetic internal val entries: MutableList<TOCEntryContainer> = ArrayList()

    @BuilderMarker fun entry(
        title: String,
        resource: PageResource,
        fragment: String? = null,
        scope: TOCEntryContainer.() -> Unit
    ): TOCContainer = apply {
        entries += TOCEntryContainer(None, title, resource, Option(fragment)).apply(scope)
    }

    @BuilderMarker fun entry(
        title: String,
        resource: Resource,
        fragment: String? = null,
        scope: TOCEntryContainer.() -> Unit
    ): TOCContainer = apply {
        requireThat(resource is PageResource) { "Expected 'resource' to be a 'PageResource', got <${resource::class}>" }
        entries += TOCEntryContainer(None, title, resource, Option(fragment)).apply(scope)
    }
}

@BuilderMarker class TOCEntryContainer internal constructor(
    @BuilderMarker val parent: Option<TOCEntryContainer>,
    @BuilderMarker val title: String,
    @BuilderMarker val resource: PageResource,
    @BuilderMarker val fragmentIdentifier: Option<String>
) {
    private val children: MutableList<TOCEntryContainer> = ArrayList()

    @BuilderMarker fun child(
        title: String,
        resource: PageResource,
        fragment: String? = null,
        scope: TOCEntryContainer.() -> Unit
    ): TOCEntryContainer = apply {
        children += TOCEntryContainer(None, title, resource, Option(fragment)).apply(scope)
    }

    @BuilderMarker fun child(
        title: String,
        resource: Resource,
        fragment: String? = null,
        scope: TOCEntryContainer.() -> Unit
    ): TOCEntryContainer = apply {
        requireThat(resource is PageResource) { "Expected 'resource' to be a 'PageResource', got <${resource::class}>" }
        children += TOCEntryContainer(None, title, resource, Option(fragment)).apply(scope)
    }

    @JvmSynthetic internal fun toEntry(): TableOfContents.Entry = TableOfContents.Entry(
        parent.map { it.toEntry() },
        title,
        resource,
        fragmentIdentifier,
        children.asSequence().map { it.toEntry() }.toMutableList()
    )
}