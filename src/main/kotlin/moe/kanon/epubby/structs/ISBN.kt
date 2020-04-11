/*
* Copyright 2015 Kohei Yamamoto
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package moe.kanon.epubby.structs

import moe.kanon.kommons.requireThat
import java.util.Arrays
import java.util.Objects


class ISBN private constructor(val isbn: String) {

    // hyphens are removed
    private val normalizedIsbn: String = normalizeSequence(isbn)

    private val numbers = isbn.split("-")
    val prefix: String = getNumberOrBlank(0)
    val group: String = getNumberOrBlank(1)
    val publisher: String = getNumberOrBlank(2)
    val bookName: String = getNumberOrBlank(3)
    val checkDigit: String = getNumberOrBlank(4)

    val linguisticArea: LinguisticArea = when {
        prefix.isBlank() || group.isBlank() -> LinguisticArea.UNKNOWN
        else -> LinguisticArea.getValue(prefix.toInt(), group.toInt())
    }

    private fun getNumberOrBlank(index: Int): String = if (numbers.size == 5) numbers[index] else ""

    /**
     * @return original description of ISBN. It can include hyphens like 978-4-***-*****-*.
     */
    override fun toString(): String = isbn

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ISBN -> false
        else -> other.normalizedIsbn == normalizedIsbn
    }

    override fun hashCode(): Int = Objects.hashCode(normalizedIsbn)

    enum class LinguisticArea {
        ENGLISH,
        FRENCH,
        GERMAN,
        JAPANESE,
        RUSSIAN,
        CHINESE,
        CZECHOSLOVAKIAN,
        INDIAN,
        NORWEGIAN,
        POLISH,
        SPANISH,
        BRAZILIAN,
        SERBIAN,
        DANISH,
        ITALIAN,
        KOREAN,
        NETHERLANDISH,
        SWEDISH,
        NGO_EU,
        OTHERS,
        UNKNOWN;

        companion object {
            @JvmStatic
            fun getValue(prefix: Int, group: Int): LinguisticArea = when (prefix) {
                978 -> when (group) {
                    in 600..621, in 950..99972 -> OTHERS
                    0, 1 -> ENGLISH
                    2 -> FRENCH
                    3 -> GERMAN
                    4 -> JAPANESE
                    5 -> RUSSIAN
                    7 -> CHINESE
                    80 -> CZECHOSLOVAKIAN
                    81, 93 -> INDIAN
                    82 -> NORWEGIAN
                    83 -> POLISH
                    84 -> SPANISH
                    85 -> BRAZILIAN
                    86 -> SERBIAN
                    87 -> DANISH
                    88 -> ITALIAN
                    89 -> KOREAN
                    90, 94 -> NETHERLANDISH
                    91 -> SWEDISH
                    92 -> NGO_EU
                    else -> UNKNOWN
                }
                979 -> {
                    when (group) {
                        10 -> FRENCH
                        11 -> KOREAN
                        else -> UNKNOWN
                    }
                }
                else -> UNKNOWN
            }
        }
    }

    companion object {
        /**
         * Number of digits in ISBN.
         */
        private const val LENGTH = 13

        /**
         * Number of digits in old ISBN.
         */
        private const val OLD_LENGTH = 10

        /**
         * Returns `true` if the given [numberSequence] is a valid ISBN, otherwise `false`.
         *
         * Check method is: http://en.wikipedia.org/wiki/International_Standard_Book_Number#Check_digits
         *
         * @param [numberSequence] the number sequence which you want to check, may include hyphens
         */
        @JvmStatic
        fun isValid(numberSequence: String): Boolean {
            // !Pattern.matches("^\\d+(-?\\d+)*$", numberSequence)
            if (!("^\\d+(-?\\d+)*$".toRegex().matches(numberSequence))) return false
            val normalizedSequence = normalizeSequence(numberSequence)
            return when (normalizedSequence.length) {
                13 -> isValidAsIsbn13(normalizedSequence)
                10 -> isValidAsIsbn10(normalizedSequence)
                else -> false
            }
        }

        /**
         * Returns `true` if the given [numberSequence] is a valid 13-digit ISBN, otherwise `false`.
         *
         * @param [numberSequence] the 13 digit number sequence to validate, may *not* include hyphens
         *
         * @throws [IllegalArgumentException] if [numberSequence] is not 13-digits
         */
        @JvmStatic
        fun isValidAsIsbn13(numberSequence: String): Boolean {
            // Pattern.matches("^\\d{$LENGTH}$", number)
            require("^\\d{$LENGTH}$".toRegex().matches(numberSequence))
            val digits = numberSequence.toCharArray()
            val myDigit = computeIsbn13CheckDigit(digits)
            val checkDigit = digits[LENGTH - 1] - '0'
            return myDigit == 10 && checkDigit == 0 || myDigit == checkDigit
        }

        // TODO: update documentation

        /**
         * Compute the check digits of 13-digits ISBN.
         *
         * Both full 13-digits and check-digit-less 12-digits are allowed as the argument.
         *
         * @param [digits] the array of each digit in ISBN.
         *
         * @return check digit
         *
         * @throws [IllegalArgumentException] the length of the argument array is neither 12 nor 13 or the element of
         * digits is negative
         */
        private fun computeIsbn13CheckDigit(digits: CharArray): Int {
            require(!(digits.size != LENGTH && digits.size != LENGTH - 1))
            for (c in digits) {
                require(!(c < '0' || '9' < c))
            }
            val weights = intArrayOf(1, 3)
            var sum = 0
            for (i in 0 until LENGTH - 1) {
                sum += (digits[i] - '0') * weights[i % 2]
            }
            return 10 - sum % 10
        }

        /**
         * Check whether the 10-digits number is valid as 10-digits ISBN.
         * @param number 10-digits number which you want to check. This must not include hyphens
         * @return true if the 10-digits number is valid as ISBN, otherwise false
         * @throws IllegalArgumentException number is not 10-digits
         */
        @JvmStatic
        fun isValidAsIsbn10(number: String): Boolean {
            // Pattern.matches("^\\d{$OLD_LENGTH}$", number)
            require("^\\d{$OLD_LENGTH}$".toRegex().matches(number))
            val digits = number.toCharArray()
            val myDigit = computeIsbn10CheckDigit(digits)
            if (myDigit == 10) return digits[9] == 'X'
            val checkDigit = digits[9] - '0'
            return myDigit == 11 && checkDigit == 0 || myDigit == checkDigit
        }

        /**
         * Compute the check digits of 10-digits ISBN.
         * Both full 10-digits and check-digit-less 9-digits are allowed as the argument.
         * @param digits the array of each digit in ISBN.
         * @return check digit
         * @throws IllegalArgumentException the length of the argument array is neither 9 nor 10 / the element in digits is negative
         */
        private fun computeIsbn10CheckDigit(digits: CharArray): Int {
            require(!(digits.size != OLD_LENGTH && digits.size != OLD_LENGTH - 1))
            for (c in digits) {
                require(!(c < '0' || '9' < c))
            }
            var sum = 0
            var i = 0
            var weight = 10
            while (i < 9) {
                sum += (digits[i] - '0') * weight
                ++i
                --weight
            }
            return 11 - sum % 11
        }

        /**
         * Convert 10-digits ISBN to 13-digits ISBN. Check digit is re-computed.
         * @param isbn10 10-digits ISBN. It can include hyphens
         * @return 13-digits ISBN
         * @throws IllegalArgumentException the number of digits of the argument is not 10
         */
        @JvmStatic
        fun toIsbn13(isbn10: String): String {
            val normalizedNumber = normalizeSequence(isbn10)
            require(normalizedNumber.length == OLD_LENGTH)
            // Compute check digit
            val isbn13 = "978" + normalizedNumber.substring(0, OLD_LENGTH - 1)
            val checkDigit = computeIsbn13CheckDigit(isbn13.toCharArray())
            // Compose 13-digits ISBN from 10-digits ISBN
            return if (isbn10.contains("-")) {
                "978-" + isbn10.substring(0, isbn10.length - 2) + "-" + checkDigit.toString()
            } else {
                "978" + isbn10.substring(0, isbn10.length - 1) + checkDigit.toString()
            }
        }

        /**
         * Remove hyphens and spaces in the argument string, as a ISBN can use either.
         */
        private fun normalizeSequence(numberSequence: String): String = numberSequence.replace("-", "").replace(" ", "")

        // TODO: Fix all of this up, just use the apache validation service for ISBN instead of these homebaked ones,
        //       as we're already using apache validator in the library anyways, and fix up how the actual class looks
        /**
         * Parses the given [isbn] into a [ISBN] instance and returns it.
         *
         * @param [isbn] ISBN which you want to instantiate.
         *
         * @throws [IllegalArgumentException] if the given [isbn] is not a valid ISBN, as determined by the [isValid]
         * function
         */
        fun parse(isbn: String): ISBN {
            require(isValid(isbn))
            val result = isbn.replace(" ", "-").let { if ('-' !in it) appendHyphenToISBN(it) else it }
            return if (normalizeSequence(isbn).length == OLD_LENGTH) {
                ISBN(toIsbn13(result))
            } else {
                ISBN(result)
            }
        }

        /**
         * Appends hyphens to an ISBN-10 or ISBN-13 without hyphens.
         *
         * In an ISBN-10 with hyphens, these hyphens separate the number of the
         * group (similar but no equal to country), the number of the editor, the
         * number of the title and the "checksum" number. The ISBN-13 adds a 3 digit
         * code before.
         *
         * @param isbn ISBN to which hyphens are to be added
         * @return the ISBN with the added hyphens
         * @throws NullPointerException if the ISBN-10 provided is `null`
         * @throws IllegalArgumentException if the length of the ISBN provided is
         * not 10 or 13
         * @throws UnsupportedOperationException if the ISBN provided is from a ISBN
         * group not implemented
         */
        private fun appendHyphenToISBN(isbn: String): String {
            // checks if the length of the ISBN is 10
            val isbn13: String = when (isbn.length) {
                10 -> "978$isbn"
                else -> when (isbn.length) {
                    13 -> isbn
                    else -> throw IllegalArgumentException("given isbn '$isbn' is not a valid ISBN")
                }
            }

            // gets the group for the ISBN
            requireThat(ISBNGroup.getGroup(isbn13) != null) { "given isbn '$isbn' is from an unimplemented group" }
            val group = ISBNGroup.getGroup(isbn13)!!

            // checks if the group of the ISBN is implemented
            // gets the group number
            var groupNumber = group.number.toString()
            val groupNumberLength: Int
            if (isbn.length == 10) {
                groupNumber = groupNumber.substring(3)
                // gets the length of the group number
                groupNumberLength = groupNumber.length
            } else {
                // gets the length of the group number
                groupNumberLength = groupNumber.length
                groupNumber = ("${groupNumber.substring(0, 3)}-${groupNumber.substring(3)}")
            }

            val maximumPublisherNumberLength = group.maximumPublisherNumberLength
            // gets the publisher part
            val publisherPart = isbn.substring(groupNumberLength).substring(0, maximumPublisherNumberLength)
            // gets the valid publisher numbers of the group
            val validPublisherNumbers = group.validPublisherNumbers
            // tries to find the number of the publisher in one of the valid number ranges for the group
            var i = 0
            var found = false
            while (!found && i < validPublisherNumbers.size) { //Add "0 padding" to the maximum length of the number of publisher
                val minValue = validPublisherNumbers[i][0].rightPad('0', maximumPublisherNumberLength)
                //Adds "9 padding" to the maxiumum publisher number length
                val maxValue = validPublisherNumbers[i][1].rightPad('9', maximumPublisherNumberLength)
                found = (publisherPart in minValue..maxValue)
                i++
            }

            return if (found) {
                // gets the mid part
                // the mid part is the ISBN part without the group number and the check digit
                val midPart = isbn.substring(groupNumberLength, isbn.length - 1)
                val midHyphenPosition = validPublisherNumbers[i - 1][0].length
                // builds the result with hyphens
                buildString {
                    // appends the group number
                    append(groupNumber)
                    // appends the first hyphen
                    append('-')
                    // appends the number of the publisher
                    append(midPart.substring(0, midHyphenPosition))
                    // appends the mid hyphen
                    append('-')
                    // appends the number of the title
                    append(midPart.substring(midHyphenPosition))
                    // appends the last hyphen
                    append('-')
                    // appends the check number
                    append(isbn.substring(isbn.length - 1))
                }
            } else {
                throw IllegalArgumentException("$isbn is a invalid ISBN for this group.")
            }
        }

        /**
         * Right pad a `String` with the specified `character`. The
         * string is padded to the size of `maxLength`.
         *
         * @param the String to pad
         * @param character the character to pad with
         * @param maxLength the size to pad to
         * @return right padded `String` or original `String` if no
         * padding is necessary
         * @throws NumberFormatException if the parameter `string` is null
         */
        private fun String.rightPad(character: Char, maxLength: Int): String {
            var _string = this
            val padLength = maxLength - _string.length
            if (padLength > 0) {
                val charPad = CharArray(padLength)
                Arrays.fill(charPad, character)
                _string = StringBuilder(_string).append(String(charPad)).toString()
            }
            return _string
        }
    }
}