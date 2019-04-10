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

@file:JvmName("CssUtils")

package moe.kanon.epubby.utils

import arrow.core.Option
import com.helger.css.ECSSVersion
import com.helger.css.decl.CascadingStyleSheet
import com.helger.css.reader.CSSReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path

@JvmOverloads
fun Path.toStyleSheet(
    // EPUB 2 uses CSS 2, while EPUB 3 uses CSS 2.1
    version: ECSSVersion = ECSSVersion.CSS21,
    charset: Charset = StandardCharsets.UTF_8
): Option<CascadingStyleSheet> = Option.fromNullable(CSSReader.readFromFile(this.toFile(), charset, version))