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

package dev.epubby.builders

import kotlin.reflect.KMutableProperty0

abstract class AbstractBuilder<T : Any> internal constructor(private val target: Class<T>) {
    protected val targetName: String
        get() = target.name

    // TODO: does this return the class for 'AbstractBuilder' and not the child class like we want?
    protected val builderName: String
        get() = javaClass.name

    /**
     * Returns a new [T] instance containing the values set in `this` builder.
     *
     * @throws [MissingRequiredValueException] if a required value has not yet been set in `this` builder
     * @throws [MalformedValueException] if a value in `this` builder is malformed in some manner
     */
    abstract fun build(): T

    protected fun <T : Any> verify(property: KMutableProperty0<T?>): T = when (val value = property.get()) {
        null -> {
            val name = property.name
            throw MissingRequiredValueException(
                name,
                "Can't build a '${target.name}' instance, as required value '$name' has not been set yet."
            )
        }
        else -> value
    }

    protected fun malformedValue(name: String, description: String, cause: Throwable? = null): Nothing =
        throw MalformedValueException(name, "Value '$name' in builder '$builderName' is malformed; $description", cause)

    protected inline fun <reified E : Exception> rethrowWrapped(
        exception: Exception,
        valueName: () -> String
    ): Nothing {
        if (exception is E) {
            malformedValue(valueName(), exception.message!!, exception)
        } else {
            throw exception
        }
    }
}