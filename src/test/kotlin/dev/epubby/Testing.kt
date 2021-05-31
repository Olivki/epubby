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

package dev.epubby

/*import moe.kanon.kommons.io.paths.pathOf
import java.nio.file.Path

const val FILE_NAME = "test_1.epub"
const val DIR = "!EPUB2"

private val input: Path = pathOf("H:", "Programming", "JVM", "Kotlin", "Data", "epubby", "reader")
    .resolve(DIR)
    .resolve(FILE_NAME)

private val output: Path = pathOf("H:", "Programming", "JVM", "Kotlin", "Data", "epubby", "writer")
    .resolve(DIR)
    .resolve(FILE_NAME)

private const val XML_EXAMPLE = """
<package version="2.0" unique-identifier="BookId" xmlns="http://www.idpf.org/2007/opf">
    <metadata xmlns:opf="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/">
        <dc:title>86 - Volume 02 - RUN THROUGH THE BATTLEFRONT &lt;UP&gt;</dc:title>
        <dc:language>en</dc:language>
        <dc:creator opf:role="aut">Asato Asato</dc:creator>
        <dc:creator opf:role="ilu">I-IV</dc:creator>
        <dc:creator opf:role="ilu">Shirabi</dc:creator>
        <dc:publisher>Dengeki Bunko</dc:publisher>
        <dc:description>Steve murphy</dc:description>
        <dc:coverage />
        <dc:source />
        <dc:date opf:event="publication">2017-08-10</dc:date>
        <dc:date opf:event="modification">2018-08-03</dc:date>
        <dc:rights />
        <dc:subject>Unknown</dc:subject>
        <meta content="0.9.10" name="Sigil version" />
        <dc:identifier id="BookId" opf:scheme="URI">SkZAhZlxQ</dc:identifier>
        <meta name="cover" content="x000_Cover.png" />
    </metadata>
    <manifest>
        <item id="navigation" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
        <item id="Illustration_01.xhtml" href="Text/Illustration_01.xhtml" media-type="application/xhtml+xml"/>
        <item id="Prologue.xhtml" href="Text/Prologue.xhtml" media-type="application/xhtml+xml"/>
        <item id="Chapter_01_03.xhtml" href="Text/Chapter_01_03.xhtml" media-type="application/xhtml+xml"/>
        <item id="Chapter_02.xhtml" href="Text/Chapter_02.xhtml" media-type="application/xhtml+xml"/>
        <item id="Chapter_03_02.xhtml" href="Text/Chapter_03_02.xhtml" media-type="application/xhtml+xml"/>
        <item id="Chapter_04_03.xhtml" href="Text/Chapter_04_03.xhtml" media-type="application/xhtml+xml"/>
        <item id="Chapter_05_02.xhtml" href="Text/Chapter_05_02.xhtml" media-type="application/xhtml+xml"/>
        <item id="Epilogue_Illustration_01.xhtml" href="Text/Epilogue_Illustration_01.xhtml" media-type="application/xhtml+xml"/>
    </manifest>
    <spine page-progression-direction="ltr">
        <itemref idref="intro"/>
        <itemref idref="c1"/>
        <itemref idref="c1-answerkey" linear="no"/>
        <itemref idref="c2"/>
        <itemref idref="c2-answerkey" linear="no"/>
        <itemref idref="c3"/>
        <itemref idref="c3-answerkey" linear="no"/>
        <itemref idref="notes" linear="no"/>
    </spine>
    <bindings>
        <mediaType handler="impl" media-type="application/x-demo-slideshow" />
    </bindings>
    <guide>
        <reference type="cover" title="Cover" href="Text/Cover.xhtml"/>
    </guide>
    <tours>
        <tour id="tour1" title="Chicken Recipes">
            <site title="Chicken Fingers" href="appetizers.html#r3" />
            <site title="Chicken a la King" href="entrees.html#r5" />
        </tour>
        <tour id="tour2" title="Vegan Recipes">
            <site title="Hummus" href ="appetizer.html#r6" />
            <site title="Lentil Casserole" href="lentils.html" />
        </tour>
    </tours>
</package>
"""

fun main() {
    val book = readBook(input)

    book.close()
}*/