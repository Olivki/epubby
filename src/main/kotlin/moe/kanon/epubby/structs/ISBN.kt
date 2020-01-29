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

package moe.kanon.epubby.structs

import java.util.regex.Pattern


class Isbn private constructor(originalIsbn: String) {
    val isbn: String
    private val normalizedIsbn // hyphens are removed
        : String
    var prefix: String? = null
    var group: String? = null
    var publisher: String? = null
    var bookName: String? = null
    var checkDigit: String? = null

    val linguisticArea: LinguisticArea
        get() = if ("" == prefix || "" == group) LinguisticArea.UNKNOWN else LinguisticArea.getValue(
            prefix!!.toInt(),
            group!!.toInt()
        )

    /**
     * @return original description of ISBN. It can include hyphens like 978-4-***-*****-*.
     */
    override fun toString(): String {
        return isbn
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj !is Isbn) return false
        return obj.normalizedIsbn == normalizedIsbn
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + normalizedIsbn.hashCode()
        return result
    }

    companion object {
        /** Number of digits in ISBN.  */
        const val LENGTH = 13
        /** Number of digits in old ISBN.  */
        const val OLD_LENGTH = 10

        /**
         * Check whether the number sequence is valid as ISBN.
         * Check method is: http://en.wikipedia.org/wiki/International_Standard_Book_Number#Check_digits
         * @param numberSequence the number sequence which you want to check. This sequence is allowed to include hyphens
         * @return true if the number sequence is valid as ISBN, otherwise false
         */
        fun isValid(numberSequence: String): Boolean {
            if (!Pattern.matches("^\\d+(-?\\d+)*$", numberSequence)) return false
            val normalizedSequence = removeHyphen(numberSequence)
            return when (normalizedSequence.length) {
                13 -> isValidAsIsbn13(normalizedSequence)
                10 -> isValidAsIsbn10(normalizedSequence)
                else -> false
            }
        }

        /**
         * Check whether the 13-digits number is valid as 13-digits ISBN.
         * @param number 13-digits number which you want to check. This must not include hyphens
         * @return true if the 13-digits number is valid as ISBN, otherwise false
         * @throws IllegalArgumentException number is not 13-digits
         */
        fun isValidAsIsbn13(number: String): Boolean {
            // Pattern.matches("^\\d{$LENGTH}$", number)
            require("^\\d{$LENGTH}$".toRegex().matches(number))
            val digits = number.toCharArray()
            val myDigit = computeIsbn13CheckDigit(digits)
            val checkDigit = digits[LENGTH - 1] - '0'
            return myDigit == 10 && checkDigit == 0 || myDigit == checkDigit
        }

        /**
         * Compute the check digits of 13-digits ISBN.
         * Both full 13-digits and check-digit-less 12-digits are allowed as the argument.
         *
         * @param digits the array of each digit in ISBN.
         *
         * @return check digit
         *
         * @throws IllegalArgumentException the length of the argument array is neither 12 nor 13 or the element of
         * digits is negative
         */
        fun computeIsbn13CheckDigit(digits: CharArray): Int {
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
        fun computeIsbn10CheckDigit(digits: CharArray): Int {
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
        fun toIsbn13(isbn10: String): String {
            val normalizedNumber = removeHyphen(isbn10)
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
         * Remove hyphens in the argument string.
         * @param s
         * @return string where hyphens are removed
         */
        fun removeHyphen(s: String): String {
            return s.replace("-", "")
        }

        /**
         * Static factory.
         * @param number ISBN which you want to instantiate.
         * @return ISBN Object
         * @throws IllegalArgumentException if the argument is invalid as ISBN
         */
        fun of(number: String): Isbn {
            require(isValid(number))
            return if (removeHyphen(number).length == OLD_LENGTH) {
                Isbn(toIsbn13(number))
            } else {
                Isbn(number)
            }
        }
    }

    init {
        assert(null != originalIsbn)
        isbn = originalIsbn
        normalizedIsbn = removeHyphen(isbn)
        val numbers = isbn.split("-").toTypedArray()
        if (numbers.size == 5) {
            prefix = numbers[0]
            group = numbers[1]
            publisher = numbers[2]
            bookName = numbers[3]
            checkDigit = numbers[4]
        } else {
            prefix = ""
            group = ""
            publisher = ""
            bookName = ""
            checkDigit = ""
        }
    }
}