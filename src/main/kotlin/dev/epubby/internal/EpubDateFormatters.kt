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

package dev.epubby.internal

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle.EXCEEDS_PAD
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.HOUR_OF_DAY
import java.time.temporal.ChronoField.MINUTE_OF_HOUR
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.SECOND_OF_MINUTE
import java.time.temporal.ChronoField.YEAR_OF_ERA

object EpubDateFormatters {
    @JvmField
    val LOCAL_DATE: DateTimeFormatter = DateTimeFormatterBuilder() // yyyy-MM-dd
        .appendValue(YEAR_OF_ERA, 4, 19, EXCEEDS_PAD)
        .appendLiteral('-')
        .appendValue(MONTH_OF_YEAR, 2)
        .appendLiteral('-')
        .appendValue(DAY_OF_MONTH, 2)
        .toFormatter()

    @JvmField
    val LOCAL_DATE_TIME: DateTimeFormatter = DateTimeFormatterBuilder() // yyyy-MM-dd'T'HH:mm:ss'Z'
        .append(LOCAL_DATE)
        .appendLiteral('T')
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .appendLiteral('Z')
        .toFormatter()
}