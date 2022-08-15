/*
 * Copyright 2019-2021 Oliver Berg
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

package dev.epubby.internal

import dev.epubby.internal.utils.classOf
import dev.epubby.internal.utils.isObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.security.AccessControlContext
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmName

// TODO: remove
internal class KServiceLoader<S : Any> private constructor(
    val serviceClass: KClass<S>,
    val loader: ClassLoader,
    val invoker: (KClass<S>) -> S
) : Iterable<S> {
    companion object {
        private const val PREFIX = "META-INF/services/"

        @JvmStatic
        @JvmName("loadServices")
        operator fun <S : Any> invoke(
            service: KClass<S>,
            loader: ClassLoader = ClassLoader.getSystemClassLoader(),
            invoker: (KClass<S>) -> S = { it.createInstance() }
        ): KServiceLoader<S> = KServiceLoader(service, loader, invoker)

        @JvmSynthetic
        inline operator fun <reified S : Any> invoke(
            loader: ClassLoader = ClassLoader.getSystemClassLoader(),
            noinline invoker: (KClass<S>) -> S = { it.createInstance() }
        ): KServiceLoader<S> = invoke(S::class, loader, invoker)

        private fun fail(service: KClass<*>, msg: String, cause: Throwable? = null): Nothing =
            throw ServiceConfigurationError("${service.jvmName}: $msg", cause)

        private fun fail(service: KClass<*>, url: URL, line: Int, msg: String): Nothing =
            fail(service, "$url: $line: $msg")
    }

    private val accessContext: AccessControlContext? =
        System.getSecurityManager()?.let { AccessController.getContext() }
    private val providers: MutableMap<String, S> = LinkedHashMap()
    private lateinit var lookupIterator: Iterator<S>

    init {
        reload()
    }

    fun reload() {
        providers.clear()
        lookupIterator = LazyIterator(serviceClass, loader)
    }

    private fun parseLine(
        service: KClass<S>,
        url: URL,
        reader: BufferedReader,
        lineIndex: Int,
        names: MutableList<String>
    ): Int {
        val line = reader.readLine()?.let { if ('#' in it) it.substringBefore('#') else it }?.trim() ?: return -1
        val length = line.length

        if (length != 0) {
            if (' ' in line || '\t' in line) fail(service, url, lineIndex, "Illegal configuration-file syntax")
            var codePoint = line.codePointAt(0)
            if (!Character.isJavaIdentifierStart(codePoint))
                fail(service, url, lineIndex, "Illegal provider-class name: $line")
            var i = Character.charCount(codePoint)
            while (i < length) {
                codePoint = line.codePointAt(i)
                if (!Character.isJavaIdentifierPart(codePoint) && codePoint != '.'.code)
                    fail(service, url, lineIndex, "Illegal provider-class name: $line")
                i += Character.charCount(codePoint)
            }
            if (!providers.containsKey(line) && line !in names) names += line
        }

        return lineIndex + 1
    }

    private fun parse(service: KClass<S>, url: URL): Iterator<String> {
        val names: MutableList<String> = ArrayList()

        try {
            url.openStream().use { input ->
                BufferedReader(InputStreamReader(input, "UTF-8")).use { reader ->
                    do {
                        var lineCount = 1
                        lineCount = parseLine(service, url, reader, lineCount, names)
                    } while (lineCount >= 0)
                }
            }
        } catch (e: IOException) {
            fail(service, "Error reading configuration file", e)
        }

        return names.iterator()
    }

    override fun iterator(): Iterator<S> = object : Iterator<S> {
        private val knownProviders = providers.entries.iterator()

        override fun hasNext(): Boolean = if (knownProviders.hasNext()) true else lookupIterator.hasNext()

        override fun next(): S = if (knownProviders.hasNext()) knownProviders.next().value else lookupIterator.next()
    }

    override fun toString(): String = "KServiceLoader[$serviceClass]"

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is KServiceLoader<*> -> false
        serviceClass != other.serviceClass -> false
        loader != other.loader -> false
        invoker != other.invoker -> false
        accessContext != other.accessContext -> false
        providers != other.providers -> false
        lookupIterator != other.lookupIterator -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = serviceClass.hashCode()
        result = 31 * result + loader.hashCode()
        result = 31 * result + invoker.hashCode()
        result = 31 * result + (accessContext?.hashCode() ?: 0)
        result = 31 * result + providers.hashCode()
        result = 31 * result + lookupIterator.hashCode()
        return result
    }

    private inner class LazyIterator(private val service: KClass<S>, private val loader: ClassLoader) : Iterator<S> {
        private var configs: Enumeration<URL>? = null
        private var pending: Iterator<String>? = null
        private var nextName: String? = null

        override fun hasNext(): Boolean = when (accessContext) {
            null -> hasNextService()
            else -> AccessController.doPrivileged(PrivilegedAction { hasNextService() }, accessContext)
        }

        private fun hasNextService(): Boolean {
            if (nextName != null) return true

            if (configs == null) {
                configs = try {
                    loader.getResources("$PREFIX${service.jvmName}")
                } catch (e: IOException) {
                    fail(service, "Error locating configuration files", e)
                }
            }

            while ((pending == null) || !(pending!!.hasNext())) {
                if (!(configs!!.hasMoreElements())) return false
                pending = parse(service, configs!!.nextElement())
            }

            nextName = pending!!.next()

            return true
        }

        override fun next(): S = when (accessContext) {
            null -> nextService()
            else -> AccessController.doPrivileged(PrivilegedAction { nextService() }, accessContext)
        }

        @Suppress("UNCHECKED_CAST")
        private fun nextService(): S {
            if (!hasNextService()) throw NoSuchElementException()

            val clzName = nextName!!
            nextName = null

            val clz: KClass<*> = try {
                classOf(clzName, loader = loader)
            } catch (x: ClassNotFoundException) {
                fail(service, "Provider $clzName not found")
            }

            if (!service.isSuperclassOf(clz)) fail(service, "Provider $clzName not a subtype of $service")

            return try {
                when {
                    clz.isObject -> clz.objectInstance as S
                    else -> invoker(clz as KClass<S>).also { providers[clzName] = it }
                }
            } catch (x: Throwable) {
                fail(service, "Provider $clzName could not be instantiated", x)
            }
        }
    }
}

internal inline fun <reified S : Any> loadServices(
    loader: ClassLoader = ClassLoader.getSystemClassLoader(),
    noinline invoker: (KClass<S>) -> S = { it.createInstance() }
): KServiceLoader<S> = KServiceLoader(loader, invoker)