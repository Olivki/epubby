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

/*import azadev.kotlin.css.Stylesheet
import azadev.kotlin.css.display
import azadev.kotlin.css.fontFamily
import azadev.kotlin.css.fontSize
import azadev.kotlin.css.textIndent
import com.google.auto.service.AutoService
import com.helger.css.decl.CSSStyleRule
import com.helger.css.decl.visit.CSSVisitor
import com.helger.css.decl.visit.DefaultCSSVisitor
import kotlinx.collections.immutable.persistentHashSetOf
import dev.epubby.internal.logger
import dev.epubby.resources.pages.Page
import dev.epubby.resources.transformers.PageTransformer
import dev.epubby.resources.transformers.StyleSheetTransformer
import dev.epubby.utils.hasChildren
import dev.epubby.utils.matches
import dev.epubby.utils.removeClass
import moe.kanon.kommons.io.paths.copyTo
import moe.kanon.kommons.io.paths.createTmpDirectory
import moe.kanon.kommons.io.paths.delete
import moe.kanon.kommons.io.paths.entries
import moe.kanon.kommons.io.paths.isRegularFile
import moe.kanon.kommons.io.paths.pathOf
import moe.kanon.kommons.reflection.loadServices
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.nio.file.Path
import java.util.WeakHashMap

private val TRANSFORM_IN = pathOf("I:", "Light Novels", "!TRANSFORM", "!IN")
private val TRANSFORM_OUT = pathOf("I:", "Light Novels", "!TRANSFORM", "!OUT")
private val writer = BookWriter()

fun main() {
    for (file in TRANSFORM_OUT.entries) file.delete()
    for (epubFile in TRANSFORM_IN.entries.filter { it.isRegularFile }) {
        val epub = readBook(createBackup(epubFile)).use { epub ->
            epub.loadServices()
            epub.resources.moveToDesiredDirectories()
            epub
        }
        val title = epub.title.sanitize()
        writer.writeToFile(epub, TRANSFORM_OUT.resolve("$title.epub"))

        epubFile.delete()
    }
}

private fun Epub.loadServices() {
    transformers.apply {
        registerTransformer(DivToParagraph(this@loadServices))
        registerTransformer(RemoveGalleyRw(this@loadServices))
        registerTransformer(RemoveInlineSpan(this@loadServices))
        registerTransformer(ModifyParagraphStyle(this@loadServices))
    }
}

private fun createBackup(original: Path): Path =
    original.copyTo(createTmpDirectory("epubby"), keepName = true).apply { toFile().deleteOnExit() }

private fun String.sanitize(): String = this.replace(":", "-").replace("?", "")

class DivToParagraph(override val epub: Epub) : PageTransformer {
    override fun transformPage(page: Page, document: Document, body: Element) {
        body.select("div")
            .asSequence()
            .filter { it.children().isNotEmpty() }
            .filter { it.hasChildren("div", "span", "a", "br") }
            .forEach { it.tagName("p") }
    }
}

class ModifyParagraphStyle(override val epub: Epub) : PageTransformer {
    private val customSheet = Stylesheet {
        body {
            fontFamily = "Georgia, serif"
        }

        p {
            display = "block"
            textIndent = "16pt"
            fontSize = "1.2em"
        }
    }

    private val allParagraphStyles: Sequence<String> by lazy {
        epub.pages
            .asSequence()
            .map { it.body.getElementsByAttribute("class") }
            .flatten()
            .filter { it matches "p" }
            .map { it.attr("class") }
    }

    private val mostFrequentStyle: String by lazy {
        allParagraphStyles
            .distinct()
            .map { it to allParagraphStyles.count { attr -> it == attr } }
            .sortedByDescending { it.second }
            .map { it.first }
            .firstOrNull() ?: ""
    }

    private val hasMostFrequentStyle: Boolean by lazy {
        val result = mostFrequentStyle.isNotBlank()

        if (!result) {
            logger.error { "There is no most frequent style for $epub" }
        }

        result
    }

    private val pageVisitor = object : DefaultCSSVisitor() {
        override fun onBeginStyleRule(rule: CSSStyleRule) {
            val hasRuleForStyle = rule.allSelectors.any { selector ->
                selector.allMembers.any { member -> member.asCSSString == ".$mostFrequentStyle" }
            }

            if (hasRuleForStyle) {
                rule.removeAllDeclarations()
                rule.addDeclaration("display", "block")
                rule.addDeclaration("text-indent", "16pt")
                rule.addDeclaration("font-size", "1.2em")
            }
        }
    }

    init {
        val tags = persistentHashSetOf("body", "p")

        val visitor = object : DefaultCSSVisitor() {
            override fun onBeginStyleRule(rule: CSSStyleRule) {
                val hasStylesForTags = rule.allSelectors.any { selector ->
                    selector.allMembers.any { member -> member.asCSSString in tags }
                }

                if (hasStylesForTags) rule.removeAllDeclarations()
            }
        }

        for (css in epub.resources.styleSheets.values.map { it.styleSheet }) {
            CSSVisitor.visitCSS(css, visitor)
            css.addRule(customSheet)
        }
    }

    override fun transformPage(page: Page, document: Document, body: Element) {
        if (!hasMostFrequentStyle) return

        for (css in page.styleSheets.map { it.styleSheet }) CSSVisitor.visitCSS(css, pageVisitor)

        body.removeClass(true)
    }
}

class RemoveGalleyRw(override val epub: Epub) : PageTransformer {
    override fun transformPage(page: Page, document: Document, body: Element) {
        document.getElementsByClass("galley-rw").forEach { it.removeClass(true) }
    }
}

class RemoveInlineSpan(override val epub: Epub) : PageTransformer {
    override fun transformPage(page: Page, document: Document, body: Element) {
        document.select("p")
            .select("span[style]")
            .filter { it.attr("style") == " font-size: 1.00em;" }
            .forEach { it.unwrap() }
    }
}*/