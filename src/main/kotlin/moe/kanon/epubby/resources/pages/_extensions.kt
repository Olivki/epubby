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

package moe.kanon.epubby.resources.pages

import moe.kanon.epubby.resources.PageResource
import moe.kanon.epubby.resources.Resource
import moe.kanon.epubby.structs.Identifier

operator fun Pages.get(index: Int): Page = getPageAt(index)

/**
 * Returns the first page that has a [resource][Page.resource] that matches the given [resource], or throws a
 * [NoSuchElementException] if none is found.
 */
operator fun Pages.get(resource: PageResource): Page = getPageByResource(resource)

/**
 * Returns the first page that has a [resource][Page.resource] with an [identifier][Resource.identifier] that
 * matches the given [identifier], or throws a [NoSuchElementException] if none is found.
 */
operator fun Pages.get(identifier: Identifier): Page = getPageByIdentifier(identifier)

operator fun Pages.contains(resource: PageResource): Boolean = hasPage(resource)