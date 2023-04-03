/*
 * Copyright 2019-2023 Oliver Berg
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

package dev.epubby

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE_PREFIX
import net.ormr.epubby.internal.Namespaces.DUBLIN_CORE_URI
import net.ormr.epubby.internal.Namespaces.OPF_PREFIX
import net.ormr.epubby.internal.Namespaces.OPF_URI
import net.ormr.epubby.internal.models.dublincore.DublinCoreModel
import net.ormr.epubby.internal.util.encodeToString
import net.ormr.epubby.internal.xml.Xml
import net.ormr.epubby.internal.xml.XmlAdditionalNamespaces
import net.ormr.epubby.internal.xml.XmlNamespace
import kotlin.OptIn
import kotlin.String

@OptIn(ExperimentalSerializationApi::class)
public fun main() {
    //checkThing()
    checkMetadata()
}

private fun checkThing() {
    val thing = Thing(listOf(Child("gamer"), Child("steve")))
    val document = Xml.encodeToDocument(Thing.serializer(), thing)
    val xml = document.encodeToString()
    println(xml)
    println(Xml.decodeFromString(Thing.serializer(), xml))
    println("-".repeat(40))
}

private fun checkMetadata() {
    val data = """
    <metadata xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:opf="http://www.idpf.org/2007/opf">
        <dc:title>86 - Volume 02 - RUN THROUGH THE BATTLEFRONT &lt;UP&gt;</dc:title>
        <dc:language>en</dc:language>
        <dc:creator opf:role="aut">Asato Asato</dc:creator>
        <dc:creator opf:role="ilu">I-IV</dc:creator>
        <dc:creator opf:role="ilu">Shirabi</dc:creator>
        <dc:publisher>Dengeki Bunko</dc:publisher>
        <dc:description>The Republic of San Magnolia.</dc:description>
        <dc:coverage />
        <dc:source />
        <dc:date opf:event="publication">2017-08-10</dc:date>
        <dc:date opf:event="modification">2018-06-02</dc:date>
        <dc:rights />
        <dc:subject>Unknown</dc:subject>
        <meta name="Sigil version" content="0.9.9" />
        <dc:identifier id="BookId" opf:scheme="URI">SkZAhZlxQ</dc:identifier>
        <meta name="cover" content="x000_Cover.png" />
    </metadata>
    """.trimIndent()
    val metadata = Xml.decodeFromString(Metadata.serializer(), data)
    println(metadata)
    val document = Xml.encodeToDocument(Metadata.serializer(), metadata)
    val xml = document.encodeToString()
    println(xml)
}

@Serializable
@SerialName("thing")
private data class Thing(val things: List<Child>)

@Serializable
@SerialName("child")
private data class Child(val name: String)

@Serializable
@SerialName("metadata")
@XmlAdditionalNamespaces([XmlNamespace(DUBLIN_CORE_PREFIX, DUBLIN_CORE_URI), XmlNamespace(OPF_PREFIX, OPF_URI)])
private data class Metadata(val dcModels: List<DublinCoreModel>, val metaModels: List<Meta>)

@Serializable
@SerialName("meta")
private data class Meta(
    val name: String,
    val content: String,
)
