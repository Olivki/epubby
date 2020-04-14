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
import moe.kanon.epubby.internal.ElementNamespaces.META_INF_METADATA
import nl.adaptivity.xmlutil.serialization.XmlSerialName

// This version of the OCF specification does not define metadata for use in the metadata.xml file. Container-level
// metadata MAY be defined in future versions of this specification and in EPUB extension specifications.
@Serializable
@XmlSerialName("container", META_INF_METADATA, "")
internal object MetaInfMetadataModel