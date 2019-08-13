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

package moe.kanon.epubby.utils

import azadev.kotlin.css.Stylesheet
import com.helger.css.ECSSVersion
import com.helger.css.decl.CSSDeclaration
import com.helger.css.decl.CSSExpression
import com.helger.css.decl.CSSStyleRule
import com.helger.css.decl.CascadingStyleSheet
import com.helger.css.reader.CSSReader

@JvmOverloads
fun cssDeclarationOf(name: String, value: String, isImportant: Boolean = false): CSSDeclaration =
    CSSDeclaration(name, CSSExpression.createSimple(value), isImportant)

@JvmOverloads
fun CSSStyleRule.addDeclaration(name: String, value: String, isImportant: Boolean = false) {
    this.addDeclaration(cssDeclarationOf(name, value, isImportant))
}

fun CascadingStyleSheet.addRule(stylesheet: Stylesheet) {
    val sheet = CSSReader.readFromString(stylesheet.render(), ECSSVersion.CSS30)!!
    // TODO: add more?
    for (rule in sheet.allRules) {
        this.addRule(rule)
    }
}

fun CascadingStyleSheet.addSheet(callback: (Stylesheet.() -> Unit)) {
    val sheet = CSSReader.readFromString(Stylesheet(callback).render(), ECSSVersion.CSS30)!!
    // TODO: add more?
    for (rule in sheet.allRules) {
        this.addRule(rule)
    }
}