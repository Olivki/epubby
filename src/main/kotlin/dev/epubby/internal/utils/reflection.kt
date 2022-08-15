/*
 * Copyright 2019-2022 Oliver Berg
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

package dev.epubby.internal.utils

import kotlin.reflect.KClass

internal val Class<*>.isKotlinClass: Boolean
    get() = this.declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }

internal val KClass<*>.isKotlinClass: Boolean get() = this.java.isKotlinClass

internal val Class<*>.isKotlinObject: Boolean
    get() = this.isKotlinClass && this.declaredFields.any { it.type == this && it.name == "INSTANCE" }

internal val KClass<*>.isObject: Boolean get() = this.java.isKotlinObject

internal fun classOf(
    fqName: String,
    initialize: Boolean = false,
    loader: ClassLoader = ClassLoader.getSystemClassLoader()
): KClass<*> = Class.forName(fqName, initialize, loader).kotlin