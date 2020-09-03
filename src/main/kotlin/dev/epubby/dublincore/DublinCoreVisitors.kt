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

package dev.epubby.dublincore

import dev.epubby.dublincore.DublinCore.*
import dev.epubby.dublincore.LocalizedDublinCore.*
import dev.epubby.packages.metadata.PackageMetadata

/**
 * A visitor for all the [DublinCore] implementations.
 *
 * @see [PackageMetadata.visitDublinCoreEntries]
 * @see [PackageMetadata.collectDublinCoreEntries]
 */
interface DublinCoreVisitor<R> {
    /**
     * Returns a value that's appropriate for the given [date] dublin core element.
     */
    fun visitDate(date: Date): R

    /**
     * Returns a value that's appropriate for the given [format] dublin core element.
     */
    fun visitFormat(format: Format): R

    /**
     * Returns a value that's appropriate for the given [identifier] dublin core element.
     */
    fun visitIdentifier(identifier: Identifier): R

    /**
     * Returns a value that's appropriate for the given [language] dublin core element.
     */
    fun visitLanguage(language: Language): R

    /**
     * Returns a value that's appropriate for the given [source] dublin core element.
     */
    fun visitSource(source: Source): R

    /**
     * Returns a value that's appropriate for the given [type] dublin core element.
     */
    fun visitType(type: Type): R

    /**
     * Returns a value that's appropriate for the given [contributor] dublin core element.
     */
    fun visitContributor(contributor: Contributor): R

    /**
     * Returns a value that's appropriate for the given [coverage] dublin core element.
     */
    fun visitCoverage(coverage: Coverage): R

    /**
     * Returns a value that's appropriate for the given [creator] dublin core element.
     */
    fun visitCreator(creator: Creator): R

    /**
     * Returns a value that's appropriate for the given [description] dublin core element.
     */
    fun visitDescription(description: Description): R

    /**
     * Returns a value that's appropriate for the given [publisher] dublin core element.
     */
    fun visitPublisher(publisher: Publisher): R

    /**
     * Returns a value that's appropriate for the given [relation] dublin core element.
     */
    fun visitRelation(relation: Relation): R

    /**
     * Returns a value that's appropriate for the given [rights] dublin core element.
     */
    fun visitRights(rights: Rights): R

    /**
     * Returns a value that's appropriate for the given [subject] dublin core element.
     */
    fun visitSubject(subject: Subject): R

    /**
     * Returns a value that's appropriate for the given [title] dublin core element.
     */
    fun visitTitle(title: Title): R
}

/**
 * A [DublinCoreVisitor] implementation that by default returns the result of invoking [getDefaultValue] function on
 * every invocation of its `visitXXX` functions.
 */
interface DefaultDublinCoreVisitor<R> : DublinCoreVisitor<R> {
    /**
     * Returns a value that may, or may not, be dependent on the given [dublinCore].
     *
     * By default, all `visitXXX` functions of this interface invoke this function when they are invoked.
     */
    fun getDefaultValue(dublinCore: DublinCore): R

    override fun visitDate(date: Date): R = getDefaultValue(date)

    override fun visitFormat(format: Format): R = getDefaultValue(format)

    override fun visitIdentifier(identifier: Identifier): R = getDefaultValue(identifier)

    override fun visitLanguage(language: Language): R = getDefaultValue(language)

    override fun visitSource(source: Source): R = getDefaultValue(source)

    override fun visitType(type: Type): R = getDefaultValue(type)

    override fun visitContributor(contributor: Contributor): R = getDefaultValue(contributor)

    override fun visitCoverage(coverage: Coverage): R = getDefaultValue(coverage)

    override fun visitCreator(creator: Creator): R = getDefaultValue(creator)

    override fun visitDescription(description: Description): R = getDefaultValue(description)

    override fun visitPublisher(publisher: Publisher): R = getDefaultValue(publisher)

    override fun visitRelation(relation: Relation): R = getDefaultValue(relation)

    override fun visitRights(rights: Rights): R = getDefaultValue(rights)

    override fun visitSubject(subject: Subject): R = getDefaultValue(subject)

    override fun visitTitle(title: Title): R = getDefaultValue(title)
}

/**
 * A [DublinCoreVisitorUnit] implementation that returns `Unit` on every invocation of its `visitXXX` functions.
 *
 * Implementations of this interface should use the [visitDublinCoreEntries][PackageMetadata.visitDublinCoreEntries]
 * over  the [collectDublinCoreEntries][PackageMetadata.collectDublinCoreEntries] function when mass-visiting all
 * dublin core entries in a metadata.
 *
 * **NOTE:** Implementing this interface from the Java side is less than ideal due Java not being able to differentiate
 * between the functions and their bridge methods, it is therefore recommended to instead implement the base
 * `DublinCoreVisitor` interface with `R` set to `Void` and return `null` on every invocation.
 *
 * Example:
 * ```java
 *  public interface ExampleVisitor implements DublinCoreVisitor<Void> {
 *      @Override
 *      public Void visitDate(final DublinCore.Date date) {
 *          // do stuff
 *          return null;
 *      }
 *  }
 * ```
 */
interface DublinCoreVisitorUnit : DublinCoreVisitor<Unit> {
    override fun visitDate(date: Date): Unit = Unit

    override fun visitFormat(format: Format): Unit = Unit

    override fun visitIdentifier(identifier: Identifier): Unit = Unit

    override fun visitLanguage(language: Language): Unit = Unit

    override fun visitSource(source: Source): Unit = Unit

    override fun visitType(type: Type): Unit = Unit

    override fun visitContributor(contributor: Contributor): Unit = Unit

    override fun visitCoverage(coverage: Coverage): Unit = Unit

    override fun visitCreator(creator: Creator): Unit = Unit

    override fun visitDescription(description: Description): Unit = Unit

    override fun visitPublisher(publisher: Publisher): Unit = Unit

    override fun visitRelation(relation: Relation): Unit = Unit

    override fun visitRights(rights: Rights): Unit = Unit

    override fun visitSubject(subject: Subject): Unit = Unit

    override fun visitTitle(title: Title): Unit = Unit
}