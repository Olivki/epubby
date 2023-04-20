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

import cc.ekblad.konbini.ParserResult
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun <A> ParserResult<A>.shouldBeOk() {
    contract {
        returns() implies (this@shouldBeOk is ParserResult.Ok<A>)
    }
    this should beParserOk()
}

inline infix fun <A> ParserResult<A>.shouldBeOk(fn: (ParserResult.Ok<A>) -> Unit) {
    this.shouldBeOk()
    fn(this)
}

fun <A> beParserOk() = object : Matcher<ParserResult<A>> {
    override fun test(value: ParserResult<A>): MatcherResult = MatcherResult(
        value is ParserResult.Ok<A>,
        { "Expected Ok, got Error: <$value>" },
        { "Expected Error, got Ok: <$value>" },
    )
}

@OptIn(ExperimentalContracts::class)
fun ParserResult<*>.shouldBeError() {
    contract {
        returns() implies (this@shouldBeError is ParserResult.Error)
    }
    this should beParserError()
}

inline infix fun ParserResult<*>.shouldBeError(fn: (ParserResult.Error) -> Unit) {
    this.shouldBeError()
    fn(this)
}

fun beParserError() = object : Matcher<ParserResult<*>> {
    override fun test(value: ParserResult<*>): MatcherResult = MatcherResult(
        value is ParserResult.Error,
        { "Expected Error, got Ok: <$value>" },
        { "Expected Ok, got Error: <$value>" },
    )
}