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

package moe.kanon.epubby.internal.models.metainf

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

// as of writing this, the latest of the epub specification (that being epub 3.2) does not specify a specific format
// for how the 'rights.xml' document should look, but they state a newer version might. This means there is nothing for
// us to model, and therefore this is just an object.
// This does also mean that as long as as the epub file *contains* a /META-INF/rights.xml file signifies that the book
// is governed by some sort of digital rights (DRM).
@Serializable
@XmlSerialName("rights", "", "")
object MetaInfRightsModel